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
import org.epics.ca.Monitor;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service which establishes EPICS CA monitors on user-specified channels of
 * interest, subsequently publishing changes in the channel's value or connection
 * state to interested consumers within the application.
 *
 * @implNote.
 * The current implementation uses PSI's CA EPICS client library to create a
 * single shared EPICS CA Context per class instance. The EPICS CA context and
 * all associated resources are disposed of when the service instance is closed.
 */
@Service
@ThreadSafe
public class EpicsChannelMonitoringService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelMonitoringService.class );
   private final EpicsChannelMonitoringServiceStatistics statisticsCollector;

   private final Map<EpicsChannelName,Channel<?>> channels;
   private final Map<EpicsChannelName,Monitor<?>> monitors;

   private final Context caContext;
   private final EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber;
   private final EpicsChannelMetadataGetter epicsChannelMetadataGetter;
   private final EpicsChannelValueGetter epicsChannelValueGetter;
   private final EpicsChannelValueChangeSubscriber epicsChannelValueChangeSubscriber;
   private final EpicsEventPublisher epicsEventPublisher;

   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance.
    *
    * @param epicsCaLibraryMonitorNotifierImpl the CA library monitor notifier configuration.
    * @param epicsCaLibraryDebugLevel the CA library debug level.
    * @param epicsChannelMetadataGetter an object which can be used to get the channel metadata.
    * @param epicsChannelValueGetter an object which can be used to get the channel value.
    * @param epicsChannelConnectionChangeSubscriber an object which can be used to subscribe to connection state changes.
    * @param epicsChannelValueChangeSubscriber an object which can be used to subscribe to value changes.
    * @param epicsEventPublisher an object which publishes events of interest to consumers within the application.
    * @param statisticsCollectionService an object which will collect the statistics associated with this class instance.
    */
   public EpicsChannelMonitoringService( @Value( "${wica.epics-ca-library-monitor-notifier-impl}") String  epicsCaLibraryMonitorNotifierImpl,
                                         @Value( "${wica.epics-ca-library-debug-level}") int epicsCaLibraryDebugLevel,
                                         @Autowired EpicsChannelMetadataGetter epicsChannelMetadataGetter,
                                         @Autowired EpicsChannelValueGetter epicsChannelValueGetter,
                                         @Autowired EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber,
                                         @Autowired EpicsChannelValueChangeSubscriber epicsChannelValueChangeSubscriber,
                                         @Autowired EpicsEventPublisher epicsEventPublisher,
                                         @Autowired StatisticsCollectionService statisticsCollectionService
                                         )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );

      this.epicsChannelConnectionChangeSubscriber = Validate.notNull( epicsChannelConnectionChangeSubscriber );
      this.epicsChannelMetadataGetter = Validate.notNull( epicsChannelMetadataGetter );
      this.epicsChannelValueGetter = Validate.notNull( epicsChannelValueGetter );
      this.epicsChannelValueChangeSubscriber = Validate.notNull( epicsChannelValueChangeSubscriber );
      this.epicsEventPublisher = Validate.notNull( epicsEventPublisher );

      channels = new ConcurrentHashMap<>();
      monitors = new ConcurrentHashMap<>();

      this.statisticsCollector = new EpicsChannelMonitoringServiceStatistics( channels, monitors );
      statisticsCollectionService.addCollectable( statisticsCollector );

      // Setup a context that uses the monitor notification policy and debug
      // message log level defined in the configuration file.
      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", epicsCaLibraryMonitorNotifierImpl );
      System.setProperty( "CA_DEBUG", String.valueOf( epicsCaLibraryDebugLevel ) );

      //System.setProperty( "EPICS_CA_ADDR_LIST", "192.168.0.46:5064" );
      //System.setProperty( "EPICS_CA_ADDR_LIST", "129.129.145.206:5064" );
      //System.setProperty( "EPICS_CA_ADDR_LIST", "proscan-cagw:5062" );

      caContext = new Context();
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Starts monitoring the EPICS control system channel associated with the
    * specified Wica Channel. Sets up a mechanism whereby future changes to the
    * channel's connection state or value are published using the configured
    * EPICS Event Publisher.
    *
    * The creation of the underlying EPICS monitor is performed asynchronously
    * so the invocation of this method does NOT incur the cost of a network
    * round trip.
    *
    * The EPICS channel may or may not be online when this method is invoked.
    * The connection-state-change event will be published when the connection to
    * the remote IOC is eventually established. Subsequently, the value-change
    * event will be published to provide the latest value of the channel.
    *
    * The current implementation is based on the assumption that higher layers
    * within the application will ensure that the information obtained from a
    * single EPICS channel monitor is shared across the entire application.
    * It is, therefore, the responsibility of the caller to ensure that monitoring
    * should not already have been established on the targeted EPICS channel.
    * This precondition is actively enforced and violations will result in an
    * IllegalStateException being thrown.
    *
    * @param wicaChannel the channel to be monitored.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    * @throws IllegalStateException if this monitor service was previously closed.
    * @throws IllegalStateException if the EPICS channel to be monitored is already being monitored.
    */
   void startMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      Validate.validState( ! closed, "The monitor service was previously closed and can no longer be used." );

      final EpicsChannelName epicsChannelName = EpicsChannelName.of( wicaChannel.getName().getControlSystemName() );

      Validate.validState( ! channels.containsKey( epicsChannelName ), "The channel name: '" + epicsChannelName.asString() + "' is already being monitored."  );
      statisticsCollector.incrementStartRequests();

      logger.info("'{}' - starting to monitor... ", epicsChannelName);

      try
      {
         logger.info("'{}' - creating channel of type '{}'...", epicsChannelName, "generic");
         final Channel<Object> channel = caContext.createChannel( epicsChannelName.asString(), Object.class);
         channels.put( epicsChannelName, channel);
         logger.info("'{}' - channel created ok.", epicsChannelName);

         // Synchronously add a connection listener before making any attempt to connect the channel.
         logger.info("'{}' - adding connection listener... ", epicsChannelName );
         epicsChannelConnectionChangeSubscriber.subscribe( channel, (conn) -> {
            if ( conn )
            {
               logger.info("'{}' - connection state changed to CONNECTED.", epicsChannelName );

               // The processing below needs to be scheduled every time a channel comes online.
               // This could be for any of the following reasons:
               //
               // a) this EPICS monitoring service has been requested to start monitoring a new
               //    EPICS channel and the channel has just connected for the very first time.
               // b) the IOC hosting the channel has just come online following a loss of network
               //    connectivity. In this case monitors that were already established on the
               //    IOC will be intact.
               // c) the IOC hosting the channel has just come online following a reboot. In
               //    this case monitors that were already established on the IOC will be lost.
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
            }
            epicsEventPublisher.publishConnectionStateChanged( wicaChannel, conn );
         } );
         logger.info("'{}' - connection listener added ok.", epicsChannelName);

         logger.info("'{}' - connecting asynchronously to... ", epicsChannelName);
         channel.connectAsync()
            .thenRunAsync( () -> {

               // Note the CA current (1.2.2) implementation of the CA library calls back
               // this code block using MULTIPLE threads taken from the so-called LeaderFollowersThreadPool.
               // By default this pool is configured for FIVE threads but where necessary this can be
               // increased by setting the system property shown below:
               // System.setProperty( "LeaderFollowersThreadPool.thread_pool_size", "50" );

               logger.info("'{}' - asynchronous connect completed. Waiting for channel to come online.", epicsChannelName );
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
      logger.info("'{}' - monitoring set up completed ok.", epicsChannelName);
   }

   /**
    * Stops monitoring the EPICS control system channel associated with the
    * specified Wica Channel.
    *
    * It is the responsibility of the caller to ensure that monitoring should
    * already have been established on the targeted EPICS channel. Should
    * this precondition be violated an IllegalStateException will be thrown.
    *
    * @param wicaChannel the channel which is no longer of interest.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    * @throws IllegalStateException if this monitor service was previously closed.
    * @throws IllegalStateException if the channel whose monitoring to be stopped
    *     was not already being monitored.
    */
   void stopMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      Validate.validState( ! closed, "The monitor service was previously closed and can no longer be used." );
      statisticsCollector.incrementStopRequests();

      final EpicsChannelName epicsChannelName= EpicsChannelName.of( wicaChannel.getName().getControlSystemName() );
      Validate.validState( channels.containsKey( epicsChannelName ), "The channel name: '" + epicsChannelName.asString() + "' was not recognised."  );

      logger.trace("'{}' - stopping monitoring on.", epicsChannelName);
      channels.get( epicsChannelName ).close();
      channels.remove( epicsChannelName );

      final boolean channelCloseAlsoDisposesMonitorCache = true;
      //noinspection ConstantConditions,PointlessBooleanExpression
      if ( !channelCloseAlsoDisposesMonitorCache )
      {
         monitors.get(epicsChannelName).close();
      }
      monitors.remove( epicsChannelName );
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

      // Note: closing the context disposes of any open channels and monitors
      caContext.close();
      monitors.clear();
      channels.clear();

      logger.debug( "'{}' - resources disposed ok.", this );
   }

   public EpicsChannelMonitoringServiceStatistics getStatistics()
   {
      return statisticsCollector;
   }

/*- Private methods ----------------------------------------------------------*/

   public void handleChannelComesOnline( WicaChannel wicaChannel, Channel<Object> epicsChannel )
   {
      final EpicsChannelName epicsChannelName = EpicsChannelName.of(wicaChannel.getName().getControlSystemName());

      // ----------------------------------------------------------
      // STEP 1: Obtain and publish the channel's metadata.
      // ----------------------------------------------------------

      final var wicaChannelMetadata = epicsChannelMetadataGetter.get(epicsChannel);
      logger.info("'{}' - channel metadata obtained ok.", epicsChannelName);
      epicsEventPublisher.publishMetadataChanged(wicaChannel, wicaChannelMetadata);
      logger.info("'{}' - channel metadata published ok.", epicsChannelName);

      // -----------------------------------------------------------
      // STEP 2: Obtain and publish the channel's initial value.
      // -----------------------------------------------------------

//      final var wicaChannelValue = epicsChannelValueGetter.get(epicsChannel);
//      logger.info("'{}' - channel value obtained ok.", epicsChannelName);
//      epicsEventPublisher.publishMonitoredValueChanged(wicaChannel, wicaChannelValue);
//      logger.info("'{}' - channel value published ok.", epicsChannelName);

      // -----------------------------------------------------------
      // STEP 3: Establish or re-establish a monitor on the channel.
      // -----------------------------------------------------------

      // 3a) Create a handler for value change notifications
      final Consumer<WicaChannelValue> valueChangedHandler = v -> {
         epicsEventPublisher.publishMonitoredValueChanged(wicaChannel, v);
         statisticsCollector.incrementMonitorUpdateCount();
      };

      // 3b) Create a monitor which will notify future value changes.
      logger.info("'{}' - subscribing for monitor updates.", epicsChannelName);
      final Monitor<Timestamped<Object>> monitor = epicsChannelValueChangeSubscriber.subscribe(epicsChannel, valueChangedHandler);
      logger.info("'{}' - subscribed ok.", epicsChannelName);

      // 3c) Update the cache of monitors (so we know where to send future stop monitoring requests).
      monitors.put(epicsChannelName, monitor);
   }

/*- Nested Classes -----------------------------------------------------------*/

}

