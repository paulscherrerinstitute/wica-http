/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Context;
import org.epics.ca.Monitor;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service for establishing a channel access monitor on a single EPICS
 * channel and for notifying service consumers of any received changes
 * of value or changes to the state of the underlying connection.
 *
 * @implNote.
 * The current implementation creates a single Context per class instance.
 * The context is disposed of when the service instance is closed.
 */
@Service
@ThreadSafe
public class EpicsChannelMonitoringService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private boolean closed = false;
   private int channelsCreatedCount;
   private int channelsDeletedCount;

   private final Map<EpicsChannelName,Channel> channels = new HashMap<>();
   private final Map<EpicsChannelName,Monitor> monitors = new HashMap<>();

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelMonitoringService.class );
   private final Context caContext;
   private final EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber;
   private final EpicsChannelMetadataGetter epicsChannelMetadataGetter;
   private final EpicsChannelValueGetter epicsChannelValueGetter;
   private final EpicsChannelValueChangeSubscriber epicsChannelValueChangeSubscriber;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance that will extract information from the EPICS channels
    * of interest using the supplied metadata and value getters.
    *
    * @param epicsCaLibraryMonitorNotifierImpl the CA library monitor notifier configuration.
    * @param epicsCaLibraryDebugLevel the CA library debug level.
    * @param epicsChannelMetadataGetter the metadata publisher.
    * @param epicsChannelValueGetter the metadata publisher.
    * @param epicsChannelValueChangeSubscriber the value publisher.
    */
   public EpicsChannelMonitoringService( @Value( "${wica.epics-ca-library-monitor-notifier-impl}") String  epicsCaLibraryMonitorNotifierImpl,
                                         @Value( "${wica.epics-ca-library-debug-level}") int epicsCaLibraryDebugLevel,
                                         @Autowired EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber,
                                         @Autowired EpicsChannelMetadataGetter epicsChannelMetadataGetter,
                                         @Autowired EpicsChannelValueGetter epicsChannelValueGetter,
                                         @Autowired EpicsChannelValueChangeSubscriber epicsChannelValueChangeSubscriber )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );

      this.epicsChannelConnectionChangeSubscriber = Validate.notNull( epicsChannelConnectionChangeSubscriber );
      this.epicsChannelMetadataGetter = Validate.notNull( epicsChannelMetadataGetter );
      this.epicsChannelValueGetter = Validate.notNull( epicsChannelValueGetter );
      this.epicsChannelValueChangeSubscriber = Validate.notNull( epicsChannelValueChangeSubscriber );


      // Setup a context that uses the monitor notification policy and debug
      // message log level defined in the configuration file.
      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", epicsCaLibraryMonitorNotifierImpl );
      System.setProperty( "CA_DEBUG", String.valueOf( epicsCaLibraryDebugLevel ) );

      caContext = new Context();
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Starts monitoring the specified EPICS channel and sets up a plan whereby
    * future changes to the state of the underlying channel will be informed to
    * the supplied connection-state-change and value-change handlers.
    *
    * The creation of the underlying EPICS monitor is performed asynchronously
    * so the invocation of this method does NOT incur the cost of a network
    * round trip.
    *
    * The supplied channel may or may not be available when this method is invoked.
    * The connection-state-change handler will receive it's first message when the
    * connection to the remote IOC is eventually established. Subsequently, the
    * value-change handler will receive notification of the initial value of the
    * channel.
    *
    * The supplied handlers will be potentially called back on multiple threads.
    *
    * It is the responsibility of the caller to ensure that monitoring should
    * not already have been established on the targeted EPICS channel. Should
    * this precondition be violated an IllegalStateException will be thrown.
    *
    * @param epicsChannelName the name of the channel to be monitored.
    * @param connectionStateChangeHandler the handler to be informed of changes
    *        to the channel's connection state.
    * @param metadataChangeHandler the handler to be informed of metadata changes.
    * @param valueChangeHandler the handler to be informed of value changes.
    * @throws NullPointerException if any of the input arguments were null.
    * @throws IllegalStateException if this monitor service was previously closed.
    * @throws IllegalStateException if the channel to be monitored is already
    *     being monitored.
    */
    void startMonitoring( EpicsChannelName epicsChannelName,
                          Consumer<Boolean> connectionStateChangeHandler,
                          Consumer<WicaChannelMetadata> metadataChangeHandler,
                          Consumer<WicaChannelValue> valueChangeHandler )
   {
      Validate.notNull( epicsChannelName);
      Validate.notNull( connectionStateChangeHandler );
      Validate.notNull( metadataChangeHandler );
      Validate.notNull( valueChangeHandler );
      Validate.validState( ! closed, "The monitor service was previously closed and can no longer be used." );
      Validate.validState( ! channels.containsKey( epicsChannelName ), "The channel name: '" + epicsChannelName.asString() + "' is already being monitored."  );

      logger.trace("'{}' - starting to monitor... ", epicsChannelName);
      channelsCreatedCount++;

      try
      {
         logger.trace("'{}' - creating channel of type '{}'...", epicsChannelName, "generic");
         final Channel<Object> channel = caContext.createChannel(epicsChannelName.asString(), Object.class);
         channels.put(epicsChannelName, channel);
         logger.trace("'{}' - channel created ok.", epicsChannelName);

         // Synchronously add a connection listener before making any attempt to connect the channel.
         logger.trace("'{}' - adding connection listener... ", epicsChannelName);
         epicsChannelConnectionChangeSubscriber.subscribe( channel, (conn) -> {
            if ( conn )
            {
               logger.trace("'{}' - connection state changed to CONNECTED.", epicsChannelName);

               // Synchronously obtain the channel's metadata.
               final var wicaChannelMetadata = epicsChannelMetadataGetter.get( channel );
               logger.trace("'{}' - channel metadata obtained ok.", epicsChannelName);
               metadataChangeHandler.accept( wicaChannelMetadata );

               // Synchronously obtain the channel's initial value.
               final var wicaChannelValue = epicsChannelValueGetter.get(channel);
               logger.trace("'{}' - channel value obtained ok.", epicsChannelName);
               valueChangeHandler.accept(wicaChannelValue);

               // Synchronously create a monitor which will notify all future value changes.
               final Monitor<Timestamped<Object>> monitor = epicsChannelValueChangeSubscriber.subscribe(channel, valueChangeHandler);
               monitors.put(epicsChannelName, monitor);
            }
            connectionStateChangeHandler.accept( conn );
         } );
         logger.trace("'{}' - connection listener added ok.", epicsChannelName);

         logger.trace("'{}' - connecting asynchronously to... ", epicsChannelName);
         channel.connectAsync()
            .thenRunAsync(() -> logger.trace("'{}' - asynchronous connect completed ok.", epicsChannelName))
            .exceptionally(( ex ) -> {
               logger.warn("'{}' - exception on channel, details were as follows: {}", this, ex.toString());
               return null;
            });
      }
      catch ( Exception ex )
      {
         logger.error("'{}' - exception on channel, details were as follows: {}", epicsChannelName, ex.toString() );
      }
   }

   /**
    * Stop monitoring the specified channel.
    *
    * It is the responsibility of the caller to ensure that monitoring should
    * already have been established on the targeted EPICS channel. Should
    * this precondition be violated an IllegalStateException will be thrown.
    *
    * @param epicsChannelName the channel which is no longer of interest.
    * @throws IllegalStateException if this monitor service was previously closed.
    * @throws IllegalStateException if the channel whose monitoring to be stopped
    *     was not already being monitored.
    */
   void stopMonitoring( EpicsChannelName epicsChannelName )
   {
      Validate.notNull( epicsChannelName );
      Validate.validState( ! closed, "The monitor service was previously closed and can no longer be used." );
      Validate.validState( channels.containsKey( epicsChannelName ), "The channel name: '" + epicsChannelName.asString() + "' was not recognised."  );
      channelsDeletedCount++;

      logger.trace("'{}' - stopping monitoring on... ", epicsChannelName);
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

   /**
    * Returns the count of active channels.
    *
    * @return the count
    */
   public long getChannelsActiveCount()
   {
      return channels.size();
   }

   /**
    * Returns the count of channels known to this class instance which are
    * connected to the underlying data source.
    *
    * A channel is "connected" asynchronously, every time a previous call to the
    * startMonitoring method has subsequently resulted in a successful transaction
    * with the underlying CA data source. If a channel is available online this
    * will occur (typically) within a few milliseconds. If not it may not take
    * seconds/minutes/hours/weeks/days.
    *
    * @return the count
    */
   public long getChannelsConnectedCount()
   {
      return channels.values()
                     .stream()
                     .filter( c -> c.getConnectionState() == ConnectionState.CONNECTED )
                     .count();
   }

   public int getChannelsCreatedCount()
   {
      return channelsCreatedCount;
   }

   public int getChannelsDeletedCount()
   {
      return channelsDeletedCount;
   }

   /**
    * Returns the count of monitors established by this class.
    *
    * @return the count
    */
   public long getMonitorsConnectedCount()
   {
      return monitors.size();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

