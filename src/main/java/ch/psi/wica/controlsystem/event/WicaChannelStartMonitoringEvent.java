/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelName;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelStartMonitoringEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelName wicaChannelName;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelStartMonitoringEvent( WicaChannelName wicaChannelName )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelStartMonitoringEvent.class);
      Validate.notNull( wicaChannelName );
      this.wicaChannelName = wicaChannelName;
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
      return "WicaChannelStartMonitoringEvent{" +
         "wicaChannelName=" + wicaChannelName +
      '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}