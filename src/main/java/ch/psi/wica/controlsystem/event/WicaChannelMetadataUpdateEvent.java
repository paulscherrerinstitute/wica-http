/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelMetadataUpdateEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ControlSystemName controlSystemName;
   private final WicaChannelMetadata wicaChannelMetadata;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelMetadataUpdateEvent( ControlSystemName controlSystemName, WicaChannelMetadata wicaChannelMetadata )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelMetadataUpdateEvent.class);
      logger.trace("Creating event: WicaChannelMetadataUpdateEvent");

      this.controlSystemName = Validate.notNull( controlSystemName );
      this.wicaChannelMetadata = Validate.notNull( wicaChannelMetadata );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public ControlSystemName getControlSystemName()
   {
      return controlSystemName;
   }

   public WicaChannelMetadata getWicaChannelMetadata()
   {
      return wicaChannelMetadata;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}