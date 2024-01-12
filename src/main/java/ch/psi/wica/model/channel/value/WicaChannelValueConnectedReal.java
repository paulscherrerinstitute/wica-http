/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the value of a connected channel whose underlying type is REAL.
 */
public class WicaChannelValueConnectedReal extends WicaChannelValueConnected
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final double value;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance of a value for a channel whose type is REAL.
    *
    * @param alarmSeverity the alarm severity.
    * @param alarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    */
   public WicaChannelValueConnectedReal( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, double value )
   {
      super( WicaChannelType.REAL, alarmSeverity, alarmStatus, dataSourceTimestamp );
      this.value = value;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the value of the channel.
    *
    * @return the value.
    */
   public double getValue()
   {
      return value;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueConnectedReal{" +
              "value=" + value +
              '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
