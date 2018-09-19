/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.EpicsChannelAlarmSeverity;
import ch.psi.wica.model.EpicsChannelMetadata;
import ch.psi.wica.model.EpicsChannelName;
import ch.psi.wica.model.EpicsChannelValue;
import io.netty.channel.ChannelMetadata;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Context;
import org.epics.ca.Monitor;
import org.epics.ca.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service to facilitate the monitoring of multiple EPICS
 * channels, and to subsequently inform interested third-parties of changes
 * of interest.
 *
 * @implNote
 * The current implementation creates a single Context per class instance.
 * The context is disposed of when the service instance is closed.
 */
@Service
public class EpicsChannelMonitorService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime javaEpoch;
   private static final LocalDateTime epicsEpoch;
   private static final long secondsBetweenEpochs;

   // Note: The EPICS epoch is currently January 1, 1990. The Java LocalDateTimeEpoch is 1970-01-01T00:00:00Z.
   static
   {
      javaEpoch  = LocalDateTime.of( 1970, 1, 1, 1, 0, 0, 0 );
      epicsEpoch = LocalDateTime.of( 1990, 1, 1, 1, 0, 0, 0 );
      secondsBetweenEpochs = Duration.between( javaEpoch, epicsEpoch ).toSeconds();
   }

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitorService.class );
   private final Context caContext;

   private static final List<Channel> channels = new ArrayList<>();
   private static final List<Monitor> monitors = new ArrayList<>();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitorService()
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );

      // Setup a context that does no buffering. This is good enough for most
      // status display purposes to humans.
      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", "BlockingQueueMultipleWorkerMonitorNotificationServiceImpl,16,1" );
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
    * @param epicsChannelName the name of the channel to be monitored.
    * @param connectionStateChangeHandler the handler to be informed of changes
    *        to the channel's connection state.
    * @param metadataChangeHandler the handler to be informed of metadata changes.
    * @param valueChangeHandler the handler to be informed of value changes.
    *
    */
   public void startMonitoring( EpicsChannelName epicsChannelName, Consumer<Boolean> connectionStateChangeHandler,
                                Consumer<EpicsChannelMetadata> metadataChangeHandler, Consumer<EpicsChannelValue> valueChangeHandler  )
   {
      Validate.notNull( epicsChannelName );
      Validate.notNull( connectionStateChangeHandler );
      Validate.notNull( metadataChangeHandler );
      Validate.notNull( valueChangeHandler );

      logger.debug("'{}' - starting to monitor... ", epicsChannelName);

      try
      {
         logger.debug("'{}' - creating channel of type '{}'...", epicsChannelName, "generic" );

         final Channel<Object> channel = caContext.createChannel( epicsChannelName.toString(), Object.class ) ;
         channels.add( channel );

         logger.debug("'{}' - channel created ok.", epicsChannelName);

         logger.debug("'{}' - adding connection listener... ", epicsChannelName);
         channel.addConnectionListener( ( chan, isConnected ) -> connectionStateChangeHandler.accept( isConnected ) );
         logger.debug("'{}' - connection listener added ok.", epicsChannelName);

         logger.debug("'{}' - connecting asynchronously to... ", epicsChannelName);
         final CompletableFuture<Channel<Object>> completableFuture = channel.connectAsync();
         logger.debug("'{}' - asynchronous connect completed ok.", epicsChannelName);

         completableFuture.thenRunAsync( () -> {
            logger.debug("'{}' - channel connected ok.", epicsChannelName);
            logger.debug("'{}' - getting first value...", epicsChannelName);

            final Object firstGet = channel.get();
            logger.debug("'{}' - first value received.", epicsChannelName );

            logger.debug( "'{}' - publishing metadata...", epicsChannelName );
            if ( ( firstGet instanceof String ) || ( firstGet instanceof String[] ) )
            {
               logger.debug("'{}' - first value was STRING.", epicsChannelName );
               publishStringMetadata( metadataChangeHandler );
            }
            else if ( ( firstGet instanceof Integer ) || ( firstGet instanceof int[] ) )
            {
               logger.debug("'{}' - first value was INTEGER.", epicsChannelName );
               publishIntegerMetadata( metadataChangeHandler );
            }
            else if ( ( firstGet instanceof Double ) || ( firstGet instanceof double[] ) )
            {
               logger.debug( "'{}' - first value was DOUBLE.", epicsChannelName );
               // Request the initial value of the channel with the widest possible
               // set of metadata. In EPICS this is the DBR_CTRL_xxx type which is
               // supported in PSI's CA library via the Metadata<Control> class.
               logger.debug("'{}' - getting epics CTRL metadata...", epicsChannelName );
               final Control metadataObject = channel.get( Control.class );
               logger.debug("'{}' - EPICS CTRL metadata received.", epicsChannelName );

               // Publish the metadata obtained from the first value.
               logger.trace("'{}' - publishing metadata...", epicsChannelName );
               publishNumericMetadata( metadataObject, metadataChangeHandler );
            }
            else
            {
               logger.warn( "'{}' - first value was of unsupported type", epicsChannelName );
               return;
            }
            logger.debug( "'{}' - metadata published.", epicsChannelName );

            // Establish a monitor on the most dynamic properties of the channel.
            // These include the timestamp, the alarm information and value itself.
            // In EPICS this is the DBR_TIME_xxx type which is supported in PSI's
            // CA library via the Metadata<Timestamped> class.
            logger.debug("'{}' - adding monitor...", epicsChannelName );
            final Monitor<Timestamped<Object>> monitor = channel.addMonitor( Timestamped.class, valueObj -> {
               logger.trace("'{}' - publishing new value...", epicsChannelName );
               publishValue( valueObj, valueChangeHandler);
               logger.trace("'{}' - new value published.", epicsChannelName );
            } );
            logger.debug("'{}' - monitor added.", epicsChannelName );
            monitors.add( monitor );
         } );
      }
      catch ( Exception ex )
      {
         logger.debug("'{}' - exception on channel, details were as follows: ", epicsChannelName, ex.toString() );
      }
   }

   /**
    * Stop monitoring the specified channel.
    *
    * @param epicsChannelName the channel which is no longer of interest.
    */
   public void stopMonitoring( EpicsChannelName epicsChannelName )
   {
      Validate.notNull( epicsChannelName );

      final boolean channelNameIsRecognised = channels.stream()
                                                      .anyMatch( c -> c.getName().equals(epicsChannelName.toString() ) );

      Validate.isTrue( channelNameIsRecognised, "channel name not recognised" );

      logger.debug("'{}' - stopping monitor... ", epicsChannelName );

      channels.stream()
              .filter( c -> c.getName().equals( epicsChannelName.toString() ) )
              .forEach( Channel::close );
   }


   @Override
   public void close()
   {
      logger.debug( "'{}' - disposing resources...", this );
      caContext.close();

      // Dispose of any references that are no longer required
      monitors.clear();
      channels.clear();

      logger.debug( "'{}' - resources disposed ok.", this );
   }


   /**
    * Returns the count of channels established by this class.
    *
    * @return the count
    */
   public static long getChannelCreationCount()
   {
      return channels.size();
   }

   /**
    * Returns the count of channels established by this class.
    *
    * @return the count
    */
   public static long getChannelConnectionCount()
   {
      return channels.stream()
                     .filter( c -> c.getConnectionState() == ConnectionState.CONNECTED )
                     .count();
   }

   /**
    * Returns the count of monitors established by this class.
    *
    * @return the count
    */
   public static long getMonitorCreationCount()
   {
      return monitors.size();
   }


/*- Private methods ----------------------------------------------------------*/

   private void publishStringMetadata( Consumer<EpicsChannelMetadata> metadataChangeHandler )
   {
      final EpicsChannelMetadata epicsChannelMetadata = EpicsChannelMetadata.createStringInstance();
      metadataChangeHandler.accept( epicsChannelMetadata );
   }

   private void publishIntegerMetadata( Consumer<EpicsChannelMetadata> metadataChangeHandler )
   {
      final EpicsChannelMetadata epicsChannelMetadata = EpicsChannelMetadata.createIntegerInstance();
      metadataChangeHandler.accept( epicsChannelMetadata );
   }

   private <T,ST> void publishNumericMetadata( Control<T,ST> metadataObject, Consumer<EpicsChannelMetadata> metadataChangeHandler )
   {
      final String units    = metadataObject.getUnits();
      final int precision   = metadataObject.getPrecision();
      final double upperDisplay = (double) metadataObject.getUpperDisplay();
      final double lowerDisplay = (double) metadataObject.getLowerDisplay();
      final double upperControl = (double) metadataObject.getUpperControl();
      final double lowerControl = (double) metadataObject.getLowerControl();
      final double upperAlarm   = (double) metadataObject.getUpperAlarm();
      final double lowerAlarm   = (double) metadataObject.getLowerAlarm();
      final double upperWarning = (double) metadataObject.getUpperWarning();
      final double lowerWarning = (double) metadataObject.getLowerWarning();

      final EpicsChannelMetadata epicsChannelMetadata = EpicsChannelMetadata.createRealInstance( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept( epicsChannelMetadata );
   }

   private <T> void publishValue( Timestamped<T> valueObj, Consumer<EpicsChannelValue> valueChangeHandler )
   {
      final T value =  valueObj.getValue();
      final int alarmStatus = valueObj.getAlarmStatus().ordinal();
      final EpicsChannelAlarmSeverity epicsAlarmSeverity = EpicsChannelAlarmSeverity.from( valueObj.getAlarmSeverity() );
      final LocalDateTime wicaServerTimestamp = LocalDateTime.now();
      final long secondsPastEpicsEpoch = valueObj.getSeconds();
      final int nanoseconds = valueObj.getNanos();
      final LocalDateTime epicsIocTimestamp  = getEpicsTimestamp( secondsPastEpicsEpoch, nanoseconds );

      final EpicsChannelValue<T> epicsChannelValue = new EpicsChannelValue<>( value, epicsAlarmSeverity, alarmStatus, wicaServerTimestamp, epicsIocTimestamp );
      valueChangeHandler.accept( epicsChannelValue );
   }

   private static LocalDateTime getEpicsTimestamp( long secondsPastEpicsEpoch, int nanoseconds )
   {
      return LocalDateTime.ofEpochSecond( secondsPastEpicsEpoch + secondsBetweenEpochs, nanoseconds, ZoneOffset.UTC );
   }


/*- Nested Classes -----------------------------------------------------------*/

}

