/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelName;
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

   private final WicaChannelName wicaChannelName;
   private final WicaChannelValue wicaChannelValue;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelPolledValueUpdateEvent( WicaChannelName WicaChannelName, WicaChannelValue wicaChannelValue )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelPolledValueUpdateEvent.class);
      this.wicaChannelName = Validate.notNull( WicaChannelName );
      this.wicaChannelValue = Validate.notNull( wicaChannelValue );
      logger.trace("Event created: '{}'.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannelName getWicaChannelName()
   {
      return wicaChannelName;
   }

   public WicaChannelValue getWicaChannelValue()
   {
      return wicaChannelValue;
   }

   @Override
   public String toString()
   {
      return "WicaChannelPolledValueUpdateEvent{" +
         "wicaChannelName=" + wicaChannelName +
         ", wicaChannelValue=" + wicaChannelValue +
      '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}