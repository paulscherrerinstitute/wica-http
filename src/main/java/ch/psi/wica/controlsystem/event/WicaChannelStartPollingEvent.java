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

public class WicaChannelStartPollingEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannel wicaChannel;
   private final WicaDataAcquisitionMode wicaDataAcquisitionMode;
   private final int pollingIntervalInMillis;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelStartPollingEvent( WicaChannel wicaChannel )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelStartPollingEvent.class);

      Validate.notNull( wicaChannel );
      Validate.isTrue( wicaChannel.getProperties().getDataAcquisitionMode().doesPolling() );
      Validate.isTrue(  wicaChannel.getProperties().getPollingIntervalInMillis() > 0 );

      this.wicaChannel = wicaChannel;
      this.wicaDataAcquisitionMode = wicaChannel.getProperties().getDataAcquisitionMode();
      this.pollingIntervalInMillis = wicaChannel.getProperties().getPollingIntervalInMillis();

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

   public int getPollingIntervalInMillis()
   {
      return pollingIntervalInMillis;
   }

   @Override
   public String toString()
   {
      return "WicaChannelStartPollingEvent{" +
         "wicaChannel=" + wicaChannel +
         ", wicaDataAcquisitionMode=" + wicaDataAcquisitionMode +
         ", pollingIntervalInMillis=" + pollingIntervalInMillis +
      '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}