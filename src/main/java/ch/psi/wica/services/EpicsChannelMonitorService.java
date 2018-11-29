/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelAlarmSeverity;
import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Context;
import org.epics.ca.Monitor;
import org.epics.ca.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
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
      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", "BlockingQueueMultipleWorkerMonitorNotificationServiceImpl,16,10" );
      caContext = new Context();
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/


//   public void get( WicaChannelName wicaChannelName, Consumer<Boolean> connectionStateChangeHandler,
//                                Consumer<WicaChannelMetadata> metadataChangeHandler, Consumer<WicaChannelValue> valueChangeHandler  )
//   {
//      Validate.notNull(wicaChannelName);
//      Validate.notNull( connectionStateChangeHandler );
//      Validate.notNull( metadataChangeHandler );
//      Validate.notNull( valueChangeHandler );
//
//      logger.debug("'{}' - starting to monitor... ", wicaChannelName);
//
//      try
//      {
//         logger.debug("'{}' - creating channel of type '{}'...", wicaChannelName, "generic" );
//
//         final Channel<Object> channel = caContext.createChannel( wicaChannelName.toString(), Object.class ) ;
//         channels.add( channel );
//
//         logger.debug("'{}' - channel created ok.", wicaChannelName);
//         logger.debug("'{}' - connecting synchronously to... ", wicaChannelName);
//         channel.connect();
//         logger.debug("'{}' - asynchronous connect completed ok.", wicaChannelName);
//
//
//         logger.debug("'{}' - channel connected ok.", wicaChannelName);
//         logger.debug("'{}' - getting first value...", wicaChannelName);
//
//         final Object firstGet = channel.get();
//         logger.debug("'{}' - first value received.", wicaChannelName);
//
//            if ( ( firstGet instanceof String ) || ( firstGet instanceof String[] ) )
//            {
//               logger.debug("'{}' - first value was STRING.", wicaChannelName);
//               publishStringMetadata( metadataChangeHandler );
//            }
//            else if ( ( firstGet instanceof Integer ) || ( firstGet instanceof int[] ) )
//            {
//               logger.debug("'{}' - first value was INTEGER.", wicaChannelName);
//               publishIntegerMetadata( metadataChangeHandler );
//            }
//            else if ( ( firstGet instanceof Double ) || ( firstGet instanceof double[] ) )
//            {
//               logger.debug( "'{}' - first value was DOUBLE.", wicaChannelName);
//               // Request the initial value of the channel with the widest possible
//               // set of metadata. In EPICS this is the DBR_CTRL_xxx type which is
//               // supported in PSI's CA library via the Metadata<Control> class.
//               logger.debug( "'{}' - getting epics CTRL metadata...", wicaChannelName);
//               final Object metadataObject = channel.get( Control.class );
//               logger.debug( "'{}' - EPICS CTRL metadata received.", wicaChannelName);
//
//               // Publish the metadata obtained from the first value.
//               logger.trace( "'{}' - publishing metadata...", wicaChannelName);
//               publishNumericMetadata( (Control<?,?>) metadataObject, metadataChangeHandler );
//            }
//            else
//            {
//               logger.warn( "'{}' - first value was of unsupported type", wicaChannelName);
//               return;
//            }
//            logger.debug( "'{}' - metadata published.", wicaChannelName);
//
//            // Establish a monitor on the most dynamic properties of the channel.
//            // These include the timestamp, the alarm information and value itself.
//            // In EPICS this is the DBR_TIME_xxx type which is supported in PSI's
//            // CA library via the Metadata<Timestamped> class.
//            logger.debug("'{}' - adding monitor...", wicaChannelName);
//            final Monitor<Timestamped<Object>> monitor = channel.addMonitor( Timestamped.class, valueObj -> {
//               logger.trace("'{}' - publishing new value...", wicaChannelName);
//               publishValue( valueObj, valueChangeHandler);
//               logger.trace("'{}' - new value published.", wicaChannelName);
//            } );
//            logger.debug("'{}' - monitor added.", wicaChannelName);
//            monitors.add( monitor );
//         } );
//      }
//      catch ( Exception ex )
//      {
//         logger.debug("'{}' - exception on channel, details were as follows: ", wicaChannelName, ex.toString() );
//      }
//   }

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
    * @param wicaChannelName the name of the channel to be monitored.
    * @param connectionStateChangeHandler the handler to be informed of changes
    *        to the channel's connection state.
    * @param metadataChangeHandler the handler to be informed of metadata changes.
    * @param valueChangeHandler the handler to be informed of value changes.
    *
    */
   public void startMonitoring( WicaChannelName wicaChannelName, Consumer<Boolean> connectionStateChangeHandler,
                                Consumer<WicaChannelMetadata> metadataChangeHandler, Consumer<WicaChannelValue> valueChangeHandler  )
   {
      Validate.notNull(wicaChannelName);
      Validate.notNull( connectionStateChangeHandler );
      Validate.notNull( metadataChangeHandler );
      Validate.notNull( valueChangeHandler );

      logger.debug("'{}' - starting to monitor... ", wicaChannelName);

      try
      {
         logger.debug("'{}' - creating channel of type '{}'...", wicaChannelName, "generic" );

         final Channel<Object> channel = caContext.createChannel( wicaChannelName.toString(), Object.class ) ;
         channels.add( channel );

         logger.debug("'{}' - channel created ok.", wicaChannelName);

         logger.debug("'{}' - adding connection listener... ", wicaChannelName);
         channel.addConnectionListener( ( chan, isConnected ) -> connectionStateChangeHandler.accept( isConnected ) );
         logger.debug("'{}' - connection listener added ok.", wicaChannelName);

         logger.debug("'{}' - connecting asynchronously to... ", wicaChannelName);
         final CompletableFuture<Channel<Object>> completableFuture = channel.connectAsync();
         logger.debug("'{}' - asynchronous connect completed ok.", wicaChannelName);

         completableFuture.thenRunAsync( () -> {
            logger.debug("'{}' - channel connected ok.", wicaChannelName);
            logger.debug("'{}' - getting first value...", wicaChannelName);

            final Object firstGet = channel.get();
            logger.debug("'{}' - first value received.", wicaChannelName);

            logger.debug("'{}' - publishing metadata...", wicaChannelName);
            if ( firstGet instanceof String )
            {
               logger.debug("'{}' - first value was STRING.", wicaChannelName);
               publishStringMetadata( metadataChangeHandler );
            }
            else if ( firstGet instanceof String[] )
            {
               logger.debug("'{}' - first value was STRING ARRAY.", wicaChannelName);
               publishStringArrayMetadata( metadataChangeHandler );
            }
            else if ( firstGet instanceof Integer )
            {
               logger.debug("'{}' - first value was INTEGER.", wicaChannelName);
               // Request the initial value of the channel with the widest possible
               // set of metadata. In EPICS this is the DBR_CTRL_xxx type which is
               // supported in PSI's CA library via the Metadata<Control> class.
               logger.debug( "'{}' - getting epics CTRL metadata...", wicaChannelName);
               final Object metadataObject = channel.get( Control.class );
               logger.debug( "'{}' - EPICS CTRL metadata received.", wicaChannelName);
               publishIntegerMetadata( (Control<?,?>) metadataObject, metadataChangeHandler );
            }
            else if ( firstGet instanceof int[] )
            {
               logger.debug("'{}' - first value was INTEGER ARRAY.", wicaChannelName);
               // Request the initial value of the channel with the widest possible
               // set of metadata. In EPICS this is the DBR_CTRL_xxx type which is
               // supported in PSI's CA library via the Metadata<Control> class.
               logger.debug( "'{}' - getting epics CTRL metadata...", wicaChannelName);
               final Object metadataObject = channel.get( Control.class );
               logger.debug( "'{}' - EPICS CTRL metadata received.", wicaChannelName);
               publishIntegerArrayMetadata( (Control<?,?>) metadataObject, metadataChangeHandler );
            }
            else if ( firstGet instanceof Double )
            {
               logger.debug( "'{}' - first value was DOUBLE.", wicaChannelName);
               // Request the initial value of the channel with the widest possible
               // set of metadata. In EPICS this is the DBR_CTRL_xxx type which is
               // supported in PSI's CA library via the Metadata<Control> class.
               logger.debug( "'{}' - getting epics CTRL metadata...", wicaChannelName);
               final Object metadataObject = channel.get( Control.class );
               logger.debug( "'{}' - EPICS CTRL metadata received.", wicaChannelName);

               // Publish the metadata obtained from the first value.
               logger.trace( "'{}' - publishing metadata...", wicaChannelName);
               publishRealMetadata( (Control<?,?>) metadataObject, metadataChangeHandler );
            }
            else if ( firstGet instanceof double[] )
            {
               logger.debug( "'{}' - first value was DOUBLE ARRAY.", wicaChannelName);
               // Request the initial value of the channel with the widest possible
               // set of metadata. In EPICS this is the DBR_CTRL_xxx type which is
               // supported in PSI's CA library via the Metadata<Control> class.
               logger.debug( "'{}' - getting epics CTRL metadata...", wicaChannelName);
               final Object metadataObject = channel.get( Control.class );
               logger.debug( "'{}' - EPICS CTRL metadata received.", wicaChannelName);

               // Publish the metadata obtained from the first value.
               logger.trace( "'{}' - publishing metadata...", wicaChannelName);
               publishRealArrayMetadata( (Control<?,?>) metadataObject, metadataChangeHandler );
            }
            else
            {
               logger.warn( "'{}' - first value was of unsupported type", wicaChannelName);
               return;
            }
            logger.debug( "'{}' - metadata published.", wicaChannelName);

            // Establish a monitor on the most dynamic properties of the channel.
            // These include the timestamp, the alarm information and value itself.
            // In EPICS this is the DBR_TIME_xxx type which is supported in PSI's
            // CA library via the Metadata<Timestamped> class.
            logger.debug("'{}' - adding monitor...", wicaChannelName);
            final Monitor<Timestamped<Object>> monitor = channel.addMonitor( Timestamped.class, valueObj -> {
               logger.trace("'{}' - publishing new value...", wicaChannelName);
               publishValue( valueObj, valueChangeHandler);
               logger.trace("'{}' - new value published.", wicaChannelName);
            } );
            logger.debug("'{}' - monitor added.", wicaChannelName);
            monitors.add( monitor );
         } );
      }
      catch ( Exception ex )
      {
         logger.debug("'{}' - exception on channel, details were as follows: ", wicaChannelName, ex.toString() );
      }
   }

   /**
    * Stop monitoring the specified channel.
    *
    * @param wicaChannelName the channel which is no longer of interest.
    */
   public void stopMonitoring( WicaChannelName wicaChannelName )
   {
      Validate.notNull(wicaChannelName);

      final boolean channelNameIsRecognised = channels.stream()
                                                      .anyMatch( c -> c.getName().equals(wicaChannelName.toString() ) );

      Validate.isTrue( channelNameIsRecognised, "channel name not recognised" );

      logger.debug("'{}' - stopping monitor... ", wicaChannelName);

      channels.stream()
              .filter( c -> c.getName().equals(wicaChannelName.toString() ) )
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

   private void publishStringMetadata( Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createStringInstance();
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private void publishStringArrayMetadata( Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createStringArrayInstance();
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishIntegerMetadata( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int upperDisplay = (int) metadataObject.getUpperDisplay();
      final int lowerDisplay = (int) metadataObject.getLowerDisplay();
      final int upperControl = (int) metadataObject.getUpperControl();
      final int lowerControl = (int) metadataObject.getLowerControl();
      final int upperAlarm   = (int) metadataObject.getUpperAlarm();
      final int lowerAlarm   = (int) metadataObject.getLowerAlarm();
      final int upperWarning = (int) metadataObject.getUpperWarning();
      final int lowerWarning = (int) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createIntegerInstance( units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishIntegerArrayMetadata( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int upperDisplay = (int) metadataObject.getUpperDisplay();
      final int lowerDisplay = (int) metadataObject.getLowerDisplay();
      final int upperControl = (int) metadataObject.getUpperControl();
      final int lowerControl = (int) metadataObject.getLowerControl();
      final int upperAlarm   = (int) metadataObject.getUpperAlarm();
      final int lowerAlarm   = (int) metadataObject.getLowerAlarm();
      final int upperWarning = (int) metadataObject.getUpperWarning();
      final int lowerWarning = (int) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createIntegerArrayInstance( units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishRealMetadata( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int precision   = metadataObject.getPrecision();
      final double upperDisplay = (double) metadataObject.getUpperDisplay();
      final double lowerDisplay = (double) metadataObject.getLowerDisplay();
      final double upperControl = (double) metadataObject.getUpperControl();
      final double lowerControl = (double) metadataObject.getLowerControl();
      final double upperAlarm   = (double) metadataObject.getUpperAlarm();
      final double lowerAlarm   = (double) metadataObject.getLowerAlarm();
      final double upperWarning = (double) metadataObject.getUpperWarning();
      final double lowerWarning = (double) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createRealInstance( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishRealArrayMetadata( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int precision   = metadataObject.getPrecision();
      final double upperDisplay = (double) metadataObject.getUpperDisplay();
      final double lowerDisplay = (double) metadataObject.getLowerDisplay();
      final double upperControl = (double) metadataObject.getUpperControl();
      final double lowerControl = (double) metadataObject.getLowerControl();
      final double upperAlarm   = (double) metadataObject.getUpperAlarm();
      final double lowerAlarm   = (double) metadataObject.getLowerAlarm();
      final double upperWarning = (double) metadataObject.getUpperWarning();
      final double lowerWarning = (double) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createRealArrayInstance(units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T> void publishValue( Timestamped<T> valueObj, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final T value =  valueObj.getValue();
      final int alarmStatus = valueObj.getAlarmStatus().ordinal();
      final WicaChannelAlarmSeverity epicsAlarmSeverity = WicaChannelAlarmSeverity.from(valueObj.getAlarmSeverity() );
      final LocalDateTime wicaServerTimestamp = LocalDateTime.now();
      final long secondsPastEpicsEpoch = valueObj.getSeconds();
      final int nanoseconds = valueObj.getNanos();
      final LocalDateTime epicsIocTimestamp  = getEpicsTimestamp( secondsPastEpicsEpoch, nanoseconds );
      final WicaChannelValue<T> wicaChannelValue = WicaChannelValue.createChannelConnectedValue( value, epicsAlarmSeverity, alarmStatus, wicaServerTimestamp, epicsIocTimestamp );
      valueChangeHandler.accept( wicaChannelValue );
   }

   // TODO: this ties the current location to PSI's site. Should be made configurable.
   private static LocalDateTime getEpicsTimestamp( long secondsPastEpicsEpoch, int nanoseconds )
   {
      final Instant instant = Instant.ofEpochSecond( secondsPastEpicsEpoch, nanoseconds );
      final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( instant, ZoneId.of( "Europe/Zurich") );
      return zonedDateTime.toLocalDateTime();
   }


/*- Nested Classes -----------------------------------------------------------*/

}

