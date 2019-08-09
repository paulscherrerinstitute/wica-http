/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelName;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelStopPollingEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelName wicaChannelName;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelStopPollingEvent( WicaChannelName wicaChannelName )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelStopPollingEvent.class);
      this.wicaChannelName = Validate.notNull( wicaChannelName );
      logger.trace("Event created: '{}'.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannelName get()
   {
      return wicaChannelName;
   }

   @Override
   public String toString()
   {
      return "WicaChannelStopPollingEvent{" +
         "wicaChannelName=" + wicaChannelName +
      '}';
}

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}