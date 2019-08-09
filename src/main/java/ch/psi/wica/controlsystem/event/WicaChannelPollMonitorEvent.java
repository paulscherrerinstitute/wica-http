/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelPollMonitorEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelName wicaChannelName;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelPollMonitorEvent( WicaChannelName wicaChannelName )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelPollMonitorEvent.class);
      this.wicaChannelName = wicaChannelName;
      logger.trace("Event created: '{}'.", this );

   }

   /*- Class methods ------------------------------------------------------------*/
   /*- Public methods -----------------------------------------------------------*/

   public WicaChannelName getWicaChannelName()
   {
      return wicaChannelName;
   }

   @Override
   public String toString()
   {
      return "WicaChannelPollMonitorEvent{" +
         "wicaChannelName=" + wicaChannelName +
         '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}