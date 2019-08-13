/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelPolledValueUpdateEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannel wicaChannel;
   private final WicaChannelValue wicaChannelValue;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelPolledValueUpdateEvent( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelPolledValueUpdateEvent.class);

      Validate.notNull( wicaChannel );
      Validate.notNull( wicaChannelValue );
      Validate.isTrue( wicaChannel.getProperties().getDataAcquisitionMode().doesPolling(), "The data acquisition mode of this channel does not support polling." );

      this.wicaChannel = wicaChannel;
      this.wicaChannelValue = wicaChannelValue;
      logger.trace("Event created: '{}'.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannel getWicaChannel()
   {
      return wicaChannel;
   }

   public WicaChannelValue getWicaChannelValue()
   {
      return wicaChannelValue;
   }

   @Override
   public String toString()
   {
      return "WicaChannelPolledValueUpdateEvent{" +
         "wicaChannel=" + wicaChannel +
         ", wicaChannelValue=" + wicaChannelValue +
      '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}