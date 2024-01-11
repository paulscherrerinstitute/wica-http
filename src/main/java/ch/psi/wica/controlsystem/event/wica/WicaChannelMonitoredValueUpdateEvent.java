/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.wica;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelMonitoredValueUpdateEvent
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   private final WicaChannel wicaChannel;
   private final WicaChannelValue wicaChannelValue;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelMonitoredValueUpdateEvent( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelMonitoredValueUpdateEvent.class);

      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument is null." );
      Validate.isTrue( wicaChannel.getProperties().getDataAcquisitionMode().doesMonitoring(), "The data acquisition mode of this channel does not support monitoring." );

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
      return "WicaChannelMonitoredValueUpdateEvent{" +
            "wicaChannel=" + wicaChannel +
            ", wicaChannelValue=" + wicaChannelValue +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}