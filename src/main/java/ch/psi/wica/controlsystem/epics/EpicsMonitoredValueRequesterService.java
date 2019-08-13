/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStartMonitoringEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopMonitoringEvent;
import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for listening and responding to application event
 * requests which require starting or stopping the monitoring of EPICS
 * Process Variables (PV's) using Channel Access protocol.
 *
 * Additionally supports the handling of requests for polling EPICS
 * channels at a fixed rate.
 */
@Service
@ThreadSafe
public class EpicsMonitoredValueRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger(EpicsMonitoredValueRequesterService.class );

   private final ApplicationEventPublisher applicationEventPublisher;
   private final EpicsChannelMonitoringService epicsChannelMonitoringService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsMonitoredValueRequesterService( ApplicationEventPublisher applicationEventPublisher,
                                        EpicsChannelMonitoringService epicsChannelMonitoringService )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
      this.epicsChannelMonitoringService = Validate.notNull (epicsChannelMonitoringService);
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartMonitoringEvent( WicaChannelStartMonitoringEvent wicaChannelStartMonitoringEvent )
   {
      Validate.notNull( wicaChannelStartMonitoringEvent);
      final WicaChannel wicaChannel = wicaChannelStartMonitoringEvent.get();

      // This service will start monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( ( isMonitorable( wicaChannel.getName() )) )
      {
         logger.trace( "Starting to monitor wica channel: '{}'", wicaChannel );
         startMonitoring( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring start monitoring request for wica channel: '{}'", wicaChannel );
      }
   }

   @EventListener
   public void handleWicaChannelStopMonitoringEvent( WicaChannelStopMonitoringEvent wicaChannelStopMonitoringEvent )
   {
      Validate.notNull( wicaChannelStopMonitoringEvent);
      final WicaChannel wicaChannel = wicaChannelStopMonitoringEvent.get();

      // This service will stop monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( isMonitorable( wicaChannel.getName() ) )
      {
         logger.trace( "Stopping monitoring wica channel named: '{}'", wicaChannel );
         stopMonitoring( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring stop monitor request for wica channel: '{}'", wicaChannel );
      }
   }

/*- Private methods ----------------------------------------------------------*/

   private boolean isMonitorable( WicaChannelName wicaChannelName)
   {
      return ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA );
   }

   /**
    * Starts monitoring the control system channel with the specified name.
    *
    * @param wicaChannel the name of the channel to monitor.
    */
   private void startMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      final ControlSystemName controlSystemName = wicaChannel.getName().getControlSystemName();

      appLogger.info( "EPICS channel subscribe: '{}'", controlSystemName.asString() );
      logger.trace( "Subscribing to new control system channel named: '{}'", controlSystemName.asString() );

      // Define the handlers to be informed of interesting changes to the channel
      final Consumer<Boolean> stateChangedHandler = b -> handleConnectionStateChanged( wicaChannel, b );
      final Consumer<WicaChannelValue> valueChangedHandler = v -> handleMonitoredValueChanged( wicaChannel, v );
      final Consumer<WicaChannelMetadata> metadataChangedHandler = v -> handleMetadataChanged( wicaChannel, v );

      // Now start monitoring
      epicsChannelMonitoringService.startMonitoring( EpicsChannelName.of( controlSystemName ), stateChangedHandler, metadataChangedHandler, valueChangedHandler );
   }

   /**
    * Stops monitoring the control system channel with the specified name.
    *
    * @param wicaChannel the name of the channel which is no longer of interest.
    */
   private void stopMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      final ControlSystemName controlSystemName = wicaChannel.getName().getControlSystemName();

      appLogger.info( "EPICS channel unsubscribe: '{}'", controlSystemName.asString() );
      logger.trace( "Unsubscribing from control system channel named: '{}'", controlSystemName.asString() );

      epicsChannelMonitoringService.stopMonitoring( EpicsChannelName.of( controlSystemName ) );
   }

   /**
    * Handles a connection state change published by the EPICS channel monitor.
    *
    * @param wicaChannel the name of the channel whose connection state has changed.
    * @param isConnected the new connection state.
    */
   private void handleConnectionStateChanged( WicaChannel wicaChannel, Boolean isConnected )
   {
      Validate.notNull( wicaChannel, "The 'controlSystemName' argument was null" );
      Validate.notNull( isConnected, "The 'isConnected' argument was null"  );

      logger.trace("'{}' - connection state changed to '{}'.", wicaChannel, isConnected);

      if ( ! isConnected )
      {
         logger.trace("'{}' - value changed to DISCONNECTED to indicate the connection was lost.", wicaChannel);
         final WicaChannelValue disconnectedValue = WicaChannelValue.createChannelValueDisconnected();

         applicationEventPublisher.publishEvent( new WicaChannelMonitoredValueUpdateEvent( wicaChannel, disconnectedValue ) );
      }
   }

   /**
    * Handles a metadata change published by the EPICS channel monitor.
    *
    * @param wicaChannel the name of the channel whose metadata has changed.
    * @param wicaChannelMetadata the metadata
    */
   private void handleMetadataChanged( WicaChannel wicaChannel, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelMetadata, "The 'wicaChannelMetadata' argument was null");

      logger.trace("'{}' - metadata changed to: '{}'", wicaChannel, wicaChannelMetadata);
      applicationEventPublisher.publishEvent( new WicaChannelMetadataUpdateEvent( wicaChannel, wicaChannelMetadata ) );
   }

   /**
    * Handles a value change published by the EPICS channel monitor.
    *
    * @param wicaChannel the name of the channel whose value has changed.
    * @param wicaChannelValue the new value.
    */
   private void handleMonitoredValueChanged( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument was null");

      logger.trace("'{}' - value changed to: '{}'", wicaChannel, wicaChannelValue );
      applicationEventPublisher.publishEvent( new WicaChannelMonitoredValueUpdateEvent( wicaChannel, wicaChannelValue ) );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
