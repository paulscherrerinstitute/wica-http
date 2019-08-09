/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelName;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelStartPollingEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelName wicaChannelName;
   private final int pollingIntervalInMillis;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelStartPollingEvent( WicaChannelName wicaChannelName, int pollingIntervalInMillis )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelStartPollingEvent.class);
      Validate.notNull( wicaChannelName );
      Validate.isTrue( pollingIntervalInMillis > 0 );

      this.wicaChannelName = wicaChannelName;
      this.pollingIntervalInMillis = pollingIntervalInMillis;
      logger.trace("Event created: '{}'.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannelName get()
   {
      return wicaChannelName;
   }

   public int getPollingIntervalInMillis()
   {
      return pollingIntervalInMillis;
   }

   @Override
   public String toString()
   {
      return "WicaChannelStartPollingEvent{" +
         "wicaChannelName=" + wicaChannelName +
         ", pollingIntervalInMillis=" + pollingIntervalInMillis +
      '}';
   }

   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}