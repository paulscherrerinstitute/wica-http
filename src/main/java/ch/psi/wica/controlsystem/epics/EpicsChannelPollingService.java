/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.channel.WicaChannel;
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

   private final Map<EpicsChannelPollingRequest,Channel<?>> channels;
   private final Map<EpicsChannelPollingRequest,ScheduledPoller> pollers;

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
    * Starts polling the EPICS control system channel according to the parameters
    * in the supplied request object.
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
    * @param requestObject the request specification object.
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this polling service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was already active.
    */
   void startPolling( EpicsChannelPollingRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The polling service was previously closed." );
      Validate.validState( ! channels.containsKey( requestObject ), "The request object is already active." );

      statisticsCollector.incrementStartRequests();

      final EpicsChannelName epicsChannelName = requestObject.getEpicsChannelName();

      logger.info("'{}' - starting to poll... ", requestObject );
      try
      {
         logger.trace("'{}' - creating channel of type '{}'...", requestObject, "generic");
         final Channel<Object> epicsChannel = caContext.createChannel( epicsChannelName.asString(), Object.class );
         channels.put( requestObject, epicsChannel );
         logger.trace("'{}' - channel created ok.", requestObject );

         logger.trace("'{}' - creating poller...", requestObject );
         final ScheduledPoller scheduledPoller = new ScheduledPoller( requestObject, epicsChannelValueGetter );
         pollers.put( requestObject, scheduledPoller );
         logger.trace("'{}' - poller created ok.", requestObject ) ;

         // Synchronously add a connection listener before making any attempt to connect the channel.
         logger.trace("'{}' - adding connection listener... ", requestObject );
         epicsChannelConnectionChangeSubscriber.subscribe( epicsChannel, (conn) -> {
            if ( conn )
            {
               logger.info("'{}' - connection state changed to CONNECTED.", requestObject );

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
                  handleChannelComesOnline( requestObject, epicsChannel );
               }
               catch( RuntimeException ex)
               {
                  logger.error("'{}' - exception when reestablishing channel connection, details were as follows: {}", this, ex.toString() );
               }
            }
            else
            {
               logger.info("'{}' - connection state changed to DISCONNECTED.", requestObject );
               handleChannelGoesOffline( requestObject );
            }
            epicsEventPublisher.publishPollerConnectionStateChanged( requestObject.getPublicationChannel(), conn );
         } );
         logger.trace("'{}' - connection listener added ok.", requestObject );

         logger.trace("'{}' - connecting asynchronously... ", requestObject );
         epicsChannel.connectAsync()
               .thenRunAsync( () -> {

                  // Note the CA current (1.2.2) implementation of the CA library calls back
                  // this code block using MULTIPLE threads taken from the so-called LeaderFollowersThreadPool.
                  // By default this pool is configured for FIVE threads but where necessary this can be
                  // increased by setting the system property shown below:
                  // System.setProperty( "LeaderFollowersThreadPool.thread_pool_size", "50" );

                  logger.trace("'{}' - asynchronous connect completed. Waiting for channel to come online.", requestObject );
               })
               .exceptionally( (ex) -> {
                  logger.warn("'{}' - exception on channel, details were as follows: {}", this, ex.toString());
                  return null;
               });
      }
      catch ( Exception ex )
      {
         logger.error("'{}' - exception on channel, details were as follows: {}", requestObject, ex.toString() );
      }
      logger.info("'{}' - poller created ok.", requestObject);
   }

   /**
    * Stops polling the EPICS control system channel specified by the supplied request
    * object.
    *
    * @param requestObject the request specification object.
    *
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this polling service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was not recognised.
    */
   void stopPolling( EpicsChannelPollingRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The polling service was previously closed." );
      Validate.validState( channels.containsKey( requestObject ), "The request object was not recognised." );

      statisticsCollector.incrementStopRequests();
      final EpicsChannelName epicsChannelName = requestObject.getEpicsChannelName();
      logger.info("'{}' - stopping polling on.", epicsChannelName);

      // Close the channel and remove the request object.
      channels.get( requestObject ).close();
      channels.remove( requestObject );

      // Cancel the scheduled polling execution task and remove the request object.
      pollers.get( requestObject ).cancel();
      pollers.remove( requestObject );
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

   private void handleChannelComesOnline( EpicsChannelPollingRequest requestObject, Channel<Object> epicsChannel )
   {
      final WicaChannel publicationChannel = requestObject.getPublicationChannel();

      // ----------------------------------------------------------
      // STEP 1: Obtain and publish the channel's metadata.
      // ----------------------------------------------------------

      logger.trace( "'{}' - getting channel metadata...", requestObject );
      final var wicaChannelMetadata = epicsChannelMetadataGetter.get( epicsChannel );
      logger.trace( "'{}' - channel metadata obtained ok.", requestObject );
      logger.trace( "'{}' - publishing channel metadata...", requestObject );
      epicsEventPublisher.publishMetadataChanged( publicationChannel, wicaChannelMetadata );
      logger.trace( "'{}' - channel metadata published ok.", requestObject );

      // -----------------------------------------------------------
      // STEP 2: Obtain and publish the channel's initial value.
      // -----------------------------------------------------------

      // If feature enabled in application.properties file...
      if ( epicsGetChannelValueOnPollerConnect )
      {
         logger.trace("'{}' - getting channel value...", requestObject );
         final var wicaChannelValue = epicsChannelValueGetter.get( epicsChannel );
         logger.trace("'{}' - channel value obtained ok.", requestObject );
         logger.trace( "'{}' - publishing channel value...", requestObject );
         epicsEventPublisher.publishPolledValueUpdated( publicationChannel, wicaChannelValue );
         logger.trace("'{}' - channel value published ok.", requestObject);
      }

      // -----------------------------------------------------------
      // STEP 3: Establish or re-establish polling on the channel.
      // -----------------------------------------------------------

      // 3a) Create a handler for value change notifications
      final Consumer<WicaChannelValue> valueUpdateHandler = v -> {
         epicsEventPublisher.publishPolledValueUpdated( publicationChannel, v );
         statisticsCollector.incrementPollCycleCount();
         statisticsCollector.updatePollingResult(  v.isConnected() );
      };

      // 3b) Start the poller which will notify future value updates.
      logger.trace("'{}' - subscribing for polling value updates.", requestObject );
      final ScheduledPoller scheduledPoller = pollers.get( requestObject );
      scheduledPoller.start( epicsChannel, valueUpdateHandler );
      logger.trace("'{}' - subscribed ok.", requestObject );
   }

   private void handleChannelGoesOffline( EpicsChannelPollingRequest epicsChannelPollingRequest )
   {
      pollers.get( epicsChannelPollingRequest ).cancel();
   }

/*- Nested Classes -----------------------------------------------------------*/

   private static class ScheduledPoller
   {
      private final Logger logger = LoggerFactory.getLogger( ScheduledPoller.class );

      private final EpicsChannelPollingRequest requestObject;
      private final EpicsChannelValueGetter epicsChannelValueGetter;
      private final ScheduledExecutorService executor;
      private ScheduledFuture<?> scheduledFuture;

      public ScheduledPoller( EpicsChannelPollingRequest requestObject, EpicsChannelValueGetter epicsChannelValueGetter )
      {
         this.requestObject = requestObject;
         this.epicsChannelValueGetter = epicsChannelValueGetter;
         this.executor = Executors.newSingleThreadScheduledExecutor();
      }

      public void start( Channel<Object> epicsChannel, Consumer<WicaChannelValue> valueUpdateHandler )
      {
         logger.trace("'{}' - starting to poll...", requestObject);
         this.scheduledFuture = executor.scheduleAtFixedRate( () -> {

            // Get and publish the current value of the channel
            try
            {
               // Now construct and return a wica value object using the timestamped object information.
               logger.trace("'{}' - polling now...", requestObject );
               final WicaChannelValue channelValue = epicsChannelValueGetter.get( epicsChannel );
               logger.trace("'{}' - value obtained OK.", requestObject );
               valueUpdateHandler.accept( channelValue );
            }
            catch ( Exception ex )
            {
               logger.error( "'{}' - generated exception '{}' during poll operation.", requestObject, ex.toString() );
            }
         }, requestObject.getPollingInterval(), requestObject.getPollingInterval(), TimeUnit.MILLISECONDS );
      }

      public void cancel()
      {
         logger.trace( "'{}' - cancelling polling...", requestObject );
         this.scheduledFuture.cancel( false );
         logger.trace("'{}' - cancelled.", requestObject );
      }
   }

}

