/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the value of a connected channel whose underlying type is STRING.
 */
public class WicaChannelValueConnectedString extends WicaChannelValueConnected
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String value;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance of a value for a channel whose type is STRING.
    *
    * @param alarmSeverity the alarm severity.
    * @param alarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    */
   public WicaChannelValueConnectedString( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, String value )
   {
      super( WicaChannelType.STRING, alarmSeverity, alarmStatus, dataSourceTimestamp );
      this.value = value;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the value of the channel.
    *
    * @return the value.
    */
   public String getValue()
   {
      return value;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueConnectedString{" +
              "value='" + value + '\'' +
              '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
