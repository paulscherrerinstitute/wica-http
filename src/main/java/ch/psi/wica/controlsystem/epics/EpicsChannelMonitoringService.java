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
import org.epics.ca.impl.LibraryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;
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

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitoringService.class );
   private final EpicsChannelMonitoringServiceStatistics statisticsCollector;

   private final Map<EpicsChannelMonitoringRequest,Channel<?>> channels;
   private final Map<EpicsChannelMonitoringRequest,Monitor<?>> monitors;

   private final Context caContext;

   private final boolean epicsGetChannelValueOnMonitorConnect;
   private final EpicsChannelMetadataGetter epicsChannelMetadataGetter;
   private final EpicsChannelValueGetter epicsChannelValueGetter;
   private final EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber;
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
    * @param epicsGetChannelValueOnMonitorConnect whether an explicit get will be performed to read a channel's value
    *        when it first comes online.
    * @param epicsChannelMetadataGetter an object which can be used to get the channel metadata.
    * @param epicsChannelValueGetter an object which can be used to get the channel value.
    * @param epicsChannelConnectionChangeSubscriber an object which can be used to subscribe to connection state changes.
    * @param epicsChannelValueChangeSubscriber an object which can be used to subscribe to value changes.
    * @param epicsEventPublisher an object which publishes events of interest to consumers within the application.
    * @param statisticsCollectionService an object which will collect the statistics associated with this class instance.
    */
   public EpicsChannelMonitoringService( @Value( "${wica.epics-ca-library-monitor-notifier-impl}") String  epicsCaLibraryMonitorNotifierImpl,
                                         @Value( "${wica.epics-ca-library-debug-level}") int epicsCaLibraryDebugLevel,
                                         @Value( "${wica.epics-get-channel-value-on-monitor-connect}") boolean epicsGetChannelValueOnMonitorConnect,
                                         @Autowired EpicsChannelMetadataGetter epicsChannelMetadataGetter,
                                         @Autowired EpicsChannelValueGetter epicsChannelValueGetter,
                                         @Autowired EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber,
                                         @Autowired EpicsChannelValueChangeSubscriber epicsChannelValueChangeSubscriber,
                                         @Autowired EpicsEventPublisher epicsEventPublisher,
                                         @Autowired StatisticsCollectionService statisticsCollectionService )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );

      this.epicsGetChannelValueOnMonitorConnect = epicsGetChannelValueOnMonitorConnect;
      this.epicsChannelMetadataGetter = Validate.notNull( epicsChannelMetadataGetter );
      this.epicsChannelValueGetter = Validate.notNull( epicsChannelValueGetter );
      this.epicsChannelConnectionChangeSubscriber = Validate.notNull( epicsChannelConnectionChangeSubscriber );
      this.epicsChannelValueChangeSubscriber = Validate.notNull( epicsChannelValueChangeSubscriber );
      this.epicsEventPublisher = Validate.notNull( epicsEventPublisher );

      channels = new ConcurrentHashMap<>();
      monitors = new ConcurrentHashMap<>();

      this.statisticsCollector = new EpicsChannelMonitoringServiceStatistics( channels, monitors );
      statisticsCollectionService.addCollectable( statisticsCollector );

      // Setup a context that uses the monitor notification policy and debug
      // message log level defined in the configuration file.
      final Properties properties = new Properties();
      properties.setProperty( LibraryConfiguration.PropertyNames.CA_MONITOR_NOTIFIER_IMPL.toString(), epicsCaLibraryMonitorNotifierImpl );
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
    * Starts monitoring the EPICS control system channel according to the
    * parameters in the supplied request object.
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
    * @param requestObject the request specification object.
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this monitoring service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was already active.
    */
   void startMonitoring( EpicsChannelMonitoringRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The monitoring service was previously closed." );
      Validate.validState( ! channels.containsKey( requestObject ), "The request object is already active." );

      statisticsCollector.incrementStartRequests();

      final EpicsChannelName epicsChannelName = requestObject.getEpicsChannelName();

      logger.info("'{}' - starting to monitor... ", requestObject );

      try
      {
         logger.trace("'{}' - creating channel of type '{}'...", requestObject, "generic");
         final Channel<Object> epicsChannel = caContext.createChannel( epicsChannelName.asString(), Object.class);
         channels.put( requestObject, epicsChannel );
         logger.trace("'{}' - channel created ok.", requestObject );

         // Synchronously add a connection listener before making any attempt to connect the channel.
         logger.trace("'{}' - adding connection listener... ", requestObject );
         epicsChannelConnectionChangeSubscriber.subscribe( epicsChannel, (conn) -> {
            if ( conn )
            {
               logger.info("'{}' - connection state changed to CONNECTED.", requestObject );

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
            }
            epicsEventPublisher.publishConnectionStateChanged( requestObject.getPublicationChannel(), conn );
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
               .exceptionally(( ex ) -> {
                  logger.warn("'{}' - exception on channel, details were as follows: {}", this, ex.toString());
                  return null;
               });
      }
      catch ( Exception ex )
      {
         logger.error("'{}' - exception on channel, details were as follows: {}", requestObject, ex.toString() );
      }
      logger.info("'{}' - monitor created ok.", requestObject );
   }

   /**
    * Stops monitoring the EPICS control system channel specified by the supplied request
    * object.
    *
    * @param requestObject the request specification object.
    *
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this polling service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was not recognised.
    */
   void stopMonitoring( EpicsChannelMonitoringRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The monitoring service was previously closed." );
      Validate.validState( channels.containsKey( requestObject ), "The request object was not recognised." );

      statisticsCollector.incrementStopRequests();
      final EpicsChannelName epicsChannelName= requestObject.getEpicsChannelName();
      logger.info("'{}' - stopping monitoring on.", epicsChannelName);

      // Close the channel and remove the request object.
      channels.get( requestObject ).close();
      channels.remove( requestObject );

      // Remove the monitor.
      // Note: there is no need here to explicitly close the monitor since this is
      // handled by the CA library automatically.
      monitors.remove( requestObject );
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

      // Note: closing the context automatically disposes of any open channels and monitors.
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

   private void handleChannelComesOnline( EpicsChannelMonitoringRequest requestObject,  Channel<Object> epicsChannel )
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
      if ( epicsGetChannelValueOnMonitorConnect )
      {
         logger.trace("'{}' - getting channel value...", requestObject );
         final var wicaChannelValue = epicsChannelValueGetter.get( epicsChannel );
         logger.trace("'{}' - channel value obtained ok.", requestObject );
         logger.trace( "'{}' - publishing channel value...", requestObject );
         epicsEventPublisher.publishMonitoredValueUpdated( publicationChannel, wicaChannelValue );
         logger.trace("'{}' - channel value published ok.", requestObject);
      }

      // -----------------------------------------------------------
      // STEP 3: Establish or re-establish polling on the channel.
      // -----------------------------------------------------------

      // 3a) Create a handler for value change notifications
      final Consumer<WicaChannelValue> valueChangedHandler = v -> {
         epicsEventPublisher.publishMonitoredValueUpdated( publicationChannel, v );
         statisticsCollector.incrementMonitorUpdateCount();
      };

      // 3b) Create a monitor which will notify future value changes.
      logger.trace("'{}' - subscribing for monitor updates.", requestObject);
      final Monitor<Timestamped<Object>> monitor = epicsChannelValueChangeSubscriber.subscribe( epicsChannel, valueChangedHandler );
      logger.trace("'{}' - subscribed ok.", requestObject );

      // 3c) Update the cache of monitors (so we know where to send future stop monitoring requests).
      monitors.put( requestObject, monitor);
   }

/*- Nested Classes -----------------------------------------------------------*/

}

