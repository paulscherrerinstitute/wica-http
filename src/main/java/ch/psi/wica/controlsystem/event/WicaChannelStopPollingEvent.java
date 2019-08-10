/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelStopPollingEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannel wicaChannel;
   private final WicaDataAcquisitionMode wicaDataAcquisitionMode;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelStopPollingEvent( WicaChannel wicaChannel, WicaDataAcquisitionMode wicaDataAcquisitionMode )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelStopPollingEvent.class);
      this.wicaChannel = Validate.notNull( wicaChannel );
      this.wicaDataAcquisitionMode = Validate.notNull( wicaDataAcquisitionMode );
      logger.trace("Event created: '{}'.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannel get()
   {
      return wicaChannel;
   }

   public WicaDataAcquisitionMode getWicaDataAcquisitionMode()
   {
      return wicaDataAcquisitionMode;
   }

   @Override
   public String toString()
   {
      return "WicaChannelStopPollingEvent{" +
         "wicaChannel=" + wicaChannel +
         ", wicaDataAcquisitionMode=" + wicaDataAcquisitionMode +
      '}';
}

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}