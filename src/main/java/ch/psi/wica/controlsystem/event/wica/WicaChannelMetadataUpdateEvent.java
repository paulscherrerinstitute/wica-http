/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.wica;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.metadata.WicaChannelMetadata;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelMetadataUpdateEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannel wicaChannel;
   private final WicaChannelMetadata wicaChannelMetadata;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelMetadataUpdateEvent( WicaChannel wicaChannel, WicaChannelMetadata wicaChannelMetadata )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelMetadataUpdateEvent.class);
      this.wicaChannel = Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      this.wicaChannelMetadata = Validate.notNull( wicaChannelMetadata, "The 'wicaChannelMetadata' argument is null." );
      logger.trace("Event created: '{}'.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannel getWicaChannel()
   {
      return wicaChannel;
   }

   public WicaChannelMetadata getWicaChannelMetadata()
   {
      return wicaChannelMetadata;
   }

   @Override
   public String toString()
   {
      return "WicaChannelMetadataUpdateEvent{" +
         "wicaChannel=" + wicaChannel +
         ", wicaChannelMetadata=" + wicaChannelMetadata +
      '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}