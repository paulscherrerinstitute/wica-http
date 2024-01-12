/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import java.time.LocalDateTime;
import java.util.Arrays;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the value of a connected channel whose underlying type is REAL_ARRAY.
 */
public class WicaChannelValueConnectedRealArray extends WicaChannelValueConnected
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final double[] value;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueConnectedRealArray( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, double[] value )
   {
      super( WicaChannelType.REAL_ARRAY, alarmSeverity, alarmStatus, dataSourceTimestamp );
      this.value = value;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public double[] getValue()
   {
      return value;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueConnectedRealArray{" +
              "value=" + Arrays.toString( value ) +
              '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
