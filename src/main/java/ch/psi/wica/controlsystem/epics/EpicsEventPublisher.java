/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
public class EpicsEventPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsEventPublisher.class );
   private final ApplicationEventPublisher applicationEventPublisher;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsEventPublisher( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
   }

/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles a connection state change published by the EPICS channel monitor.
    *
    * @param wicaChannel the name of the channel whose connection state has changed.
    * @param isConnected the new connection state.
    */
   void publishMonitorConnectionStateChanged( WicaChannel wicaChannel, Boolean isConnected )
   {
      Validate.notNull( wicaChannel, "The 'controlSystemName' argument was null" );
      Validate.notNull( isConnected, "The 'isConnected' argument was null"  );

      logger.trace("'{}' - connection state changed to '{}'.", wicaChannel, isConnected);

      // The implementation here simply uses the disconnect event to publish a
      // new value indicating disconnection.
      if ( ! isConnected )
      {
         logger.trace("'{}' - value changed to DISCONNECTED to indicate the connection was lost.", wicaChannel);
         final WicaChannelValue disconnectedValue = WicaChannelValue.createChannelValueDisconnected();
         applicationEventPublisher.publishEvent( new WicaChannelMonitoredValueUpdateEvent( wicaChannel, disconnectedValue ) );
      }
   }

   /**
    * Handles a connection state change published by the EPICS channel poller.
    *
    * @param wicaChannel the name of the channel whose connection state has changed.
    * @param isConnected the new connection state.
    */
   void publishPollerConnectionStateChanged( WicaChannel wicaChannel, Boolean isConnected )
   {
      Validate.notNull( wicaChannel, "The 'controlSystemName' argument was null" );
      Validate.notNull( isConnected, "The 'isConnected' argument was null"  );

      logger.trace("'{}' - connection state changed to '{}'.", wicaChannel, isConnected);

      // The implementation here simply uses the disconnect event to publish a
      // new value indicating disconnection.
      if ( ! isConnected )
      {
         logger.trace("'{}' - value changed to DISCONNECTED to indicate the connection was lost.", wicaChannel);
         final WicaChannelValue disconnectedValue = WicaChannelValue.createChannelValueDisconnected();
         applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannel, disconnectedValue ) );
      }
   }

   /**
    * Handles a metadata change published by the EPICS channel monitor or EPICS channel poller.
    *
    * @param wicaChannel the name of the channel whose metadata has changed.
    * @param wicaChannelMetadata the metadata
    */
   void publishMetadataChanged ( WicaChannel wicaChannel, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelMetadata, "The 'wicaChannelMetadata' argument was null");

      logger.trace("'{}' - metadata changed.", wicaChannel );
      applicationEventPublisher.publishEvent( new WicaChannelMetadataUpdateEvent(wicaChannel, wicaChannelMetadata ) );
      logger.trace("'{}' - metadata  published ok", wicaChannel );
   }

   /**
    * Handles a monitor update published by the EPICS channel monitor.
    *
    * @param wicaChannel the name of the channel whose value has changed.
    * @param wicaChannelValue the new value.
    */
   void publishMonitoredValueUpdated( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument was null");

      logger.trace("'{}' - value changed.", wicaChannel );
      applicationEventPublisher.publishEvent( new WicaChannelMonitoredValueUpdateEvent( wicaChannel, wicaChannelValue ) );
      logger.trace("'{}' - value  published ok", wicaChannel );
   }

   /**
    * Handles a value update published by the EPICS channel poller.
    *
    * @param wicaChannel the name of the channel whose value has changed.
    * @param wicaChannelValue the new value.
    */
   void publishPolledValueUpdated( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument was null");

      logger.trace("'{}' - value changed to: '{}'", wicaChannel, wicaChannelValue );
      applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannel, wicaChannelValue ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
