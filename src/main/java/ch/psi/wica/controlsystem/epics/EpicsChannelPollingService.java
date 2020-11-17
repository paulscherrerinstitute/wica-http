/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.impl.LibraryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service which schedules the polling of user-specified EPICS channels of
 * interest, subsequently publishing the results of each poll operation to
 * interested consumers within the application.
 *
 * @implNote.
 * The current implementation uses PSI's CA EPICS client library to create a
 * single shared EPICS CA Context per class instance. The EPICS CA context and
 * all associated resources are disposed of when the service instance is closed.
 */
@Service
@ThreadSafe
public class EpicsChannelPollingService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelPollingService.class );
   private final EpicsChannelPollingServiceStatistics statisticsCollector;

   private final Map<EpicsChannelName,Channel<?>> channels;
   private final Map<WicaChannel, ScheduledPoller> pollers;

   private final Context caContext;

   private final boolean epicsGetChannelValueOnPollerConnect;
   private final EpicsChannelMetadataGetter epicsChannelMetadataGetter;
   private final EpicsChannelValueGetter epicsChannelValueGetter;
   private final EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber;
   private final EpicsEventPublisher epicsEventPublisher;

   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance.
    *
    * @param epicsCaLibraryDebugLevel the CA library debug level.
    * @param epicsGetChannelValueOnPollerConnect whether an explicit get will be performed to read a channel's value
    *        when it first comes online.
    * @param epicsChannelMetadataGetter an object which can be used to get the channel metadata.
    * @param epicsChannelValueGetter an object which can be used to get the channel value.
    * @param epicsChannelConnectionChangeSubscriber an object which can be used to subscribe to connection state changes.
    * @param epicsEventPublisher an object which publishes events of interest to consumers within the application.
    * @param statisticsCollectionService an object which will collect the statistics associated with this class instance.
    */
   public EpicsChannelPollingService( @Value( "${wica.epics-ca-library-debug-level}") int epicsCaLibraryDebugLevel,
                                      @Value( "${wica.epics-get-channel-value-on-poller-connect}") boolean epicsGetChannelValueOnPollerConnect,
                                      @Autowired EpicsChannelMetadataGetter epicsChannelMetadataGetter,
                                      @Autowired EpicsChannelValueGetter epicsChannelValueGetter,
                                      @Autowired EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber,
                                      @Autowired EpicsEventPublisher epicsEventPublisher,
                                      @Autowired StatisticsCollectionService statisticsCollectionService )
   {
      logger.debug( "'{}' - constructing new EpicsChannelPollingService instance...", this );

      this.epicsGetChannelValueOnPollerConnect = epicsGetChannelValueOnPollerConnect;
      this.epicsChannelMetadataGetter = Validate.notNull( epicsChannelMetadataGetter );
      this.epicsChannelValueGetter = Validate.notNull( epicsChannelValueGetter );
      this.epicsChannelConnectionChangeSubscriber = Validate.notNull( epicsChannelConnectionChangeSubscriber );
      this.epicsEventPublisher = Validate.notNull( epicsEventPublisher );

      channels = new ConcurrentHashMap<>();
      pollers = new ConcurrentHashMap<>();

      this.statisticsCollector = new EpicsChannelPollingServiceStatistics( channels );
      statisticsCollectionService.addCollectable( statisticsCollector );

      // Setup a context that uses the debug message log level defined in the
      // configuration file.
      final Properties properties = new Properties();
      properties.setProperty( LibraryConfiguration.PropertyNames.CA_LIBRARY_LOG_LEVEL.toString(), String.valueOf( epicsCaLibraryDebugLevel ) );

      //System.setProperty( "EPICS_CA_ADDR_LIST", "192.168.0.46:5064" );
      //System.setProperty( "EPICS_CA_ADDR_LIST", "129.129.145.206:5064" );
      //System.setProperty( "EPICS_CA_ADDR_LIST", "proscan-cagw:5062" );

      caContext = new Context( properties );
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Starts polling the EPICS control system channel associated with the
    * specified Wica Channel. Sets up a mechanism whereby future changes to the
    * channel's connection state or value are published using the configured
    * EPICS Event Publisher.
    *
    * The creation of the underlying EPICS poller is performed asynchronously
    * so the invocation of this method does NOT incur the cost of a network
    * round trip.
    *
    * The EPICS channel may or may not be online when this method is invoked.
    * The connection-state-change event will be published when the connection to
    * the remote IOC is eventually established. Subsequently, the value-change
    * event updates will be published on each periodic polling cycle to provide
    * the latest value of the channel.
    *
    * The current implementation is based on the assumption that higher layers
    * within the application will ensure that the information obtained from a
    * single EPICS channel poller operating at a particular polling rate is
    * shared across the entire application.  It is, therefore, the responsibility
    * of the caller to ensure that polling should not already have been established
    * on the targeted EPICS channel. This precondition is actively enforced and
    * violations will result in an IllegalStateException being thrown.
    *
    * @param wicaChannel the channel to be polled.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    * @throws IllegalStateException if this polling service was previously closed.
    * @throws IllegalStateException if the EPICS channel to be polled is already being polled.
    */
   void startPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      Validate.validState( ! closed, "The poller service was previously closed and can no longer be used." );

      final EpicsChannelName epicsChannelName = EpicsChannelName.of( wicaChannel.getName().getControlSystemName() );

      Validate.validState( validateRequest( wicaChannel ), "The channel name: '" + epicsChannelName.asString() + "' is already being polled with an identical polling rate."  );
      statisticsCollector.incrementStartRequests();

      logger.info("'{}' - starting to poll... ", epicsChannelName);

      try
      {
         logger.info("'{}' - creating channel of type '{}'...", epicsChannelName, "generic");
         final Channel<Object> channel = caContext.createChannel( epicsChannelName.asString(), Object.class );
         channels.put( epicsChannelName, channel);
         logger.info("'{}' - channel created ok.", epicsChannelName);

         logger.info("'{}' - creating poller...", epicsChannelName );
         final ScheduledPoller scheduledPoller = new ScheduledPoller( wicaChannel, epicsChannelValueGetter );
         pollers.put( wicaChannel, scheduledPoller );
         logger.info("'{}' - poller created ok.", epicsChannelName);

         // Synchronously add a connection listener before making any attempt to connect the channel.
         logger.info("'{}' - adding connection listener... ", epicsChannelName );
         epicsChannelConnectionChangeSubscriber.subscribe( channel, (conn) -> {
            if ( conn )
            {
               logger.info("'{}' - connection state changed to CONNECTED.", epicsChannelName );

               // The processing below needs to be scheduled every time a channel comes online.
               // This could be for any of the following reasons:
               //
               // a) this EPICS polling service has been requested to start polling a new
               //    EPICS channel and the channel has just connected for the very first time.
               // b) the IOC hosting the channel has just come online following a loss of network
               //    connectivity.
               // c) the IOC hosting the channel has just come online following a reboot.
               try
               {
                  handleChannelComesOnline( wicaChannel, channel );
               }
               catch( RuntimeException ex)
               {
                  logger.error("'{}' - exception when reestablishing channel connection, details were as follows: {}", this, ex.toString() );
               }
            }
            else
            {
               logger.info("'{}' - connection state changed to DISCONNECTED.", epicsChannelName );
               handleChannelGoesOffline( wicaChannel );
            }
            epicsEventPublisher.publishConnectionStateChanged( wicaChannel, conn );
         } );
         logger.info("'{}' - connection listener added ok.", epicsChannelName);

         logger.info("'{}' - connecting asynchronously... ", epicsChannelName);
         channel.connectAsync()
               .thenRunAsync( () -> {

                  // Note the CA current (1.2.2) implementation of the CA library calls back
                  // this code block using MULTIPLE threads taken from the so-called LeaderFollowersThreadPool.
                  // By default this pool is configured for FIVE threads but where necessary this can be
                  // increased by setting the system property shown below:
                  // System.setProperty( "LeaderFollowersThreadPool.thread_pool_size", "50" );

                  logger.info("'{}' - asyncronous connect completed. Waiting for channel to come online.", epicsChannelName );
               })
               .exceptionally(( ex ) -> {
                  logger.warn("'{}' - exception on channel, details were as follows: {}", this, ex.toString());
                  return null;
               });
      }
      catch ( Exception ex )
      {
         logger.error("'{}' - exception on channel, details were as follows: {}", epicsChannelName, ex.toString() );
      }
      logger.info("'{}' - polling set up completed ok.", epicsChannelName);
   }

   /**
    * Stops polling the EPICS control system channel associated with the
    * specified Wica Channel.
    *
    * It is the responsibility of the caller to ensure that polling should
    * already have been established on the targeted EPICS channel. Should
    * this precondition be violated an IllegalStateException will be thrown.
    *
    * @param wicaChannel the channel which is no longer of interest.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    * @throws IllegalStateException if this polling service was previously closed.
    * @throws IllegalStateException if the channel whose polling is to be stopped
    *     was not already being polled.
    */
   void stopPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      Validate.validState( ! closed, "The polling service was previously closed and can no longer be used." );
      statisticsCollector.incrementStopRequests();

      final EpicsChannelName epicsChannelName= EpicsChannelName.of( wicaChannel.getName().getControlSystemName() );
      Validate.validState( channels.containsKey( epicsChannelName ), "The channel name: '" + epicsChannelName.asString() + "' was not recognised."  );

      logger.info("'{}' - stopping polling on.", epicsChannelName);
      channels.get( epicsChannelName ).close();
      channels.remove( epicsChannelName );

      final boolean channelCloseAlsoDisposesMonitorCache = true;
      //noinspection ConstantConditions,PointlessBooleanExpression
      if ( !channelCloseAlsoDisposesMonitorCache )
      {
         pollers.get(wicaChannel).cancel();
      }
      pollers.remove( wicaChannel );
   }

   /**
    * Disposes of all resources associated with this class instance.
    */
   @Override
   public void close()
   {
      // Set a flag to prevent further usage
      closed = true;

      // Dispose of any references that are no longer required
      logger.debug( "'{}' - disposing resources...", this );

      // Note: closing the context automatically disposes of any open channels.
      caContext.close();

      // Explicitly cancel the scheduled executors.
      pollers.keySet().forEach( (p) -> pollers.get( p ).cancel() );

      // Clear data structures.
      pollers.clear();
      channels.clear();

      logger.debug( "'{}' - resources disposed ok.", this );
   }

   public EpicsChannelPollingServiceStatistics getStatistics()
   {
      return statisticsCollector;
   }

/*- Private methods ----------------------------------------------------------*/

   private boolean validateRequest( WicaChannel wicaChannel )
   {
      final long identicalPollerCount = pollers.keySet()
            .stream()
            .filter( (ch) -> ch.getName().equals( wicaChannel.getName() ) )
            .filter( (ch) -> ch.getProperties().getPollingIntervalInMillis() == wicaChannel.getProperties().getPollingIntervalInMillis() )
            .count();

      return identicalPollerCount == 0;
   }


   private void handleChannelComesOnline( WicaChannel wicaChannel, Channel<Object> epicsChannel )
   {
      final EpicsChannelName epicsChannelName = EpicsChannelName.of( wicaChannel.getName().getControlSystemName());

      // ----------------------------------------------------------
      // STEP 1: Obtain and publish the channel's metadata.
      // ----------------------------------------------------------

      logger.info( "'{}' - getting channel metadata...", epicsChannelName );
      final var wicaChannelMetadata = epicsChannelMetadataGetter.get( epicsChannel );
      logger.info( "'{}' - channel metadata obtained ok.", epicsChannelName );
      logger.info( "'{}' - publishing channel metadata...", epicsChannelName );
      epicsEventPublisher.publishMetadataChanged( wicaChannel, wicaChannelMetadata );
      logger.info( "'{}' - channel metadata published ok.", epicsChannelName );

      // -----------------------------------------------------------
      // STEP 2: Obtain and publish the channel's initial value.
      // -----------------------------------------------------------

      // If feature enabled in application.properties file...
      if ( epicsGetChannelValueOnPollerConnect )
      {
         logger.info("'{}' - getting channel value...", epicsChannelName );
         final var wicaChannelValue = epicsChannelValueGetter.get( epicsChannel );
         logger.info("'{}' - channel value obtained ok.", epicsChannelName );
         logger.info( "'{}' - publishing channel value...", epicsChannelName );
         epicsEventPublisher.publishPolledValueUpdated( wicaChannel, wicaChannelValue );
         logger.info("'{}' - channel value published ok.", epicsChannelName);
      }

      // -----------------------------------------------------------
      // STEP 3: Establish or re-establish polling on the channel.
      // -----------------------------------------------------------

      // 3a) Create a handler for value change notifications
      final Consumer<WicaChannelValue> valueUpdateHandler = v -> {
         epicsEventPublisher.publishPolledValueUpdated( wicaChannel, v );
         statisticsCollector.incrementPollCycleCount();
         statisticsCollector.updatePollingResult(  v.isConnected() );
      };

      // 3b) Start the poller which will notify future value updates.
      logger.info("'{}' - subscribing for polling value updates.", epicsChannelName);
      final ScheduledPoller scheduledPoller = pollers.get( wicaChannel );
      scheduledPoller.start( epicsChannel, valueUpdateHandler );
      logger.info("'{}' - subscribed ok.", epicsChannelName);
   }

   private void handleChannelGoesOffline( WicaChannel wicaChannel )
   {
      pollers.get( wicaChannel ).cancel();
   }

/*- Nested Classes -----------------------------------------------------------*/

   private static class ScheduledPoller
   {
      private final Logger logger = LoggerFactory.getLogger( ScheduledPoller.class );

      private final WicaChannelName wicaChannelName;
      private final int pollingIntervalInMillis;
      private final EpicsChannelValueGetter epicsChannelValueGetter;
      private final ScheduledExecutorService executor;

      public ScheduledPoller( WicaChannel wicaChannel, EpicsChannelValueGetter epicsChannelValueGetter )
      {
         this.wicaChannelName = wicaChannel.getName();
         this.pollingIntervalInMillis = wicaChannel.getProperties().getPollingIntervalInMillis();
         this.epicsChannelValueGetter = epicsChannelValueGetter;
         this.executor = Executors.newSingleThreadScheduledExecutor();
      }

      public void start( Channel<Object> epicsChannel, Consumer<WicaChannelValue> valueUpdateHandler )
      {
         logger.info("'{}' - starting to poll with periodicity of {} milliseconds.", wicaChannelName, pollingIntervalInMillis );

         executor.scheduleAtFixedRate( () -> {

            // Get and publish the current value of the channel
            try
            {
               // Now construct and return a wica value object using the timestamped object information.
               logger.info("'{}' - polling now...", wicaChannelName );
               final WicaChannelValue channelValue = epicsChannelValueGetter.get( epicsChannel );
               logger.info("'{}' - value obtained OK.", wicaChannelName );
               valueUpdateHandler.accept( channelValue );
            }
            catch ( Exception ex )
            {
               logger.error( "'{}' - generated exception '{}' during poll operation.", wicaChannelName, ex.toString() );
            }

         }, pollingIntervalInMillis, pollingIntervalInMillis, TimeUnit.MILLISECONDS );
      }

      public void cancel()
      {
         logger.info( "'{}' - cancelling polling...", wicaChannelName );
         executor.shutdown();
         logger.info("'{}' - scheduler has been shutdown.", wicaChannelName );
      }

   }

}

