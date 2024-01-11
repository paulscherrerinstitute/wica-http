/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.wica;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelStartMonitoringEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannel wicaChannel;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelStartMonitoringEvent( WicaChannel wicaChannel )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelStartMonitoringEvent.class);
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      Validate.isTrue( wicaChannel.getProperties().getDataAcquisitionMode().doesMonitoring() );

      this.wicaChannel = wicaChannel;
      logger.trace("Event created: '{}'.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannel get()
   {
      return wicaChannel;
   }

   @Override
   public String toString()
   {
      return "WicaChannelStartMonitoringEvent{" +
         "wicaChannel=" + wicaChannel +
      '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}