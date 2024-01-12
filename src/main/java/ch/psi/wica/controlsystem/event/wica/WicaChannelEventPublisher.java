/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.wica;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.metadata.WicaChannelMetadata;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
public class WicaChannelEventPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelEventPublisher.class );
   private final ApplicationEventPublisher applicationEventPublisher;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelEventPublisher( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher, "The 'applicationEventPublisher' argument is null." );
   }

/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles a metadata change published by the EPICS channel monitor or EPICS channel poller.
    *
    * @param wicaChannel the name of the channel whose metadata has changed.
    * @param wicaChannelMetadata the metadata
    */
   public void publishMetadataUpdated( WicaChannel wicaChannel, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelMetadata, "The 'wicaChannelMetadata' argument was null");

      logger.trace("'{}' - metadata changed.", wicaChannel );
      applicationEventPublisher.publishEvent( new WicaChannelMetadataUpdateEvent( wicaChannel, wicaChannelMetadata ) );
      logger.trace("'{}' - metadata  published ok", wicaChannel );
   }

   /**
    * Handles a value update published by the EPICS channel monitor.
    *
    * @param wicaChannel the name of the channel whose value has changed.
    * @param wicaChannelValue the new value.
    */
   public void publishMonitoredValueUpdated( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
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
   public void publishPolledValueUpdated( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument was null");

      logger.trace("'{}' - value changed to: '{}'", wicaChannel, wicaChannelValue );
      applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannel, wicaChannelValue ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
