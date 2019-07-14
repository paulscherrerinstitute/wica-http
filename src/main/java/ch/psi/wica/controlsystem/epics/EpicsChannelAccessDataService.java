/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStartMonitoringEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopMonitoringEvent;
import ch.psi.wica.controlsystem.event.WicaChannelValueUpdateEvent;
import ch.psi.wica.model.app.*;
import ch.psi.wica.model.channel.*;
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
 */
@Service
@ThreadSafe
public class EpicsChannelAccessDataService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelAccessDataService.class );

   private final ApplicationEventPublisher applicationEventPublisher;
   private final EpicsChannelMonitorService epicsChannelMonitorService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelAccessDataService( ApplicationEventPublisher applicationEventPublisher,
                                  EpicsChannelMonitorService epicsChannelMonitorService )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
      this.epicsChannelMonitorService = Validate.notNull ( epicsChannelMonitorService );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartMonitoringEvent( WicaChannelStartMonitoringEvent wicaChannelStartMonitoringEvent )
   {
      Validate.notNull(wicaChannelStartMonitoringEvent);
      final WicaChannelName wicaChannelName = wicaChannelStartMonitoringEvent.get();

      // This service will start monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA ) )
      {
         logger.trace( "Starting to monitor wica channel named: '{}'", wicaChannelName );
         startMonitoring( wicaChannelName.getControlSystemName());
      }
      else
      {
         logger.trace( "Ignoring start monitoring request for wica channel named: '{}'", wicaChannelName );
      }
   }

   @EventListener
   public void handleWicaChannelStopMonitoringEvent( WicaChannelStopMonitoringEvent wicaChannelStopMonitoringEvent )
   {
      Validate.notNull(wicaChannelStopMonitoringEvent);
      final WicaChannelName wicaChannelName = wicaChannelStopMonitoringEvent.get();

      // This service will stop monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA ) )
      {
         logger.trace( "Stopping monitoring wica channel named: '{}'", wicaChannelName );
         stopMonitoring( wicaChannelName.getControlSystemName() );
      }
      else
      {
         logger.trace( "Ignoring stop monitor request for wica channel named: '{}'", wicaChannelName );
      }
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Starts monitoring the control system channel with the specified name.
    *
    * @param controlSystemName the name of the channel to monitor.
    */
   private void startMonitoring( ControlSystemName controlSystemName )
   {
      Validate.notNull( controlSystemName );
      logger.trace("Subscribing to new control system channel named: '{}'", controlSystemName.asString() );

      // Define the handlers to be informed of interesting changes to the channel
      final Consumer<Boolean> stateChangedHandler = b -> handleConnectionStateChanged( controlSystemName, b );
      final Consumer<WicaChannelValue> valueChangedHandler = v -> handleValueChanged( controlSystemName, v );
      final Consumer<WicaChannelMetadata> metadataChangedHandler = v -> handleMetadataChanged( controlSystemName, v );

      // Now start monitoring
      epicsChannelMonitorService.startMonitoring( EpicsChannelName.of( controlSystemName ), stateChangedHandler, metadataChangedHandler, valueChangedHandler );
   }

   /**
    * Stops monitoring the control system channel with the specified name.
    *
    * @param controlSystemName the name of the channel which is no longer of interest.
    */
   private void stopMonitoring( ControlSystemName controlSystemName  )
   {
      Validate.notNull( controlSystemName );
      logger.trace( "Unsubscribing to control system channel named: '{}'", controlSystemName.asString() );

      epicsChannelMonitorService.stopMonitoring( EpicsChannelName.of( controlSystemName ) );
   }

   /**
    * Handles a connection state change published by the EPICS channel monitor.
    *
    * @param controlSystemName the name of the channel whose connection state has changed.
    * @param isConnected the new connection state.
    */
   private void handleConnectionStateChanged( ControlSystemName controlSystemName, Boolean isConnected )
   {
      Validate.notNull( controlSystemName, "The 'controlSystemName' argument was null" );
      Validate.notNull( isConnected, "The 'isConnected' argument was null"  );

      logger.trace("'{}' - connection state changed to '{}'.", controlSystemName, isConnected);

      if ( ! isConnected )
      {
         logger.trace("'{}' - value changed to DISCONNECTED to indicate the connection was lost.", controlSystemName);
         final WicaChannelValue disconnectedValue = WicaChannelValue.createChannelValueDisconnected();

         applicationEventPublisher.publishEvent( new WicaChannelValueUpdateEvent(controlSystemName, disconnectedValue ) );
      }
   }

   /**
    * Handles a value change published by the EPICS channel monitor.
    *
    * @param controlSystemName the name of the channel whose value has changed.
    * @param wicaChannelValue the new value.
    */
   private void handleValueChanged( ControlSystemName controlSystemName, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( controlSystemName, "The 'controlSystemName' argument was null");
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument was null");

      logger.trace("'{}' - value changed to: '{}'", controlSystemName, wicaChannelValue );

      applicationEventPublisher.publishEvent( new WicaChannelValueUpdateEvent( controlSystemName, wicaChannelValue ) );
   }

   /**
    * Handles a metadata change published by the EPICS channel monitor.
    *
    * @param controlSystemName the name of the channel whose metadata has changed.
    * @param wicaChannelMetadata the metadata
    */
   private void handleMetadataChanged( ControlSystemName controlSystemName, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull( controlSystemName, "The 'controlSystemName' argument was null");
      Validate.notNull( wicaChannelMetadata, "The 'wicaChannelMetadata' argument was null");

      logger.trace("'{}' - metadata changed to: '{}'", controlSystemName, wicaChannelMetadata);

      applicationEventPublisher.publishEvent( new WicaChannelMetadataUpdateEvent(controlSystemName, wicaChannelMetadata ) );
   }


/*- Nested Classes -----------------------------------------------------------*/

}
