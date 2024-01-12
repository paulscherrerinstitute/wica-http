/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import org.apache.commons.lang3.Validate;
import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the value of a channel in the connected state.
 */
public abstract class WicaChannelValueConnected extends WicaChannelValue
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

    private final WicaChannelType wicaChannelType;
    private final WicaChannelAlarmSeverity alarmSeverity;
    private final WicaChannelAlarmStatus alarmStatus;
    private final LocalDateTime dataSourceTimestamp;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance of a value for a channel in the connected state.
    *
    * @param wicaChannelType the channel type.
    * @param alarmSeverity the alarm severity.
    * @param alarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    */
   public WicaChannelValueConnected( WicaChannelType wicaChannelType, WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp )
   {
      super( wicaChannelType, LocalDateTime.now( ), true );
      this.wicaChannelType = Validate.notNull( wicaChannelType, "wicaChannelType cannot be null " );
      this.alarmSeverity = Validate.notNull( alarmSeverity, "alarmSeverity cannot be null " );
      this.alarmStatus = Validate.notNull( alarmStatus, "alarmStatus cannot be null " );
      this.dataSourceTimestamp = Validate.notNull( dataSourceTimestamp, "dataSourceTimestamp cannot be null " );
   }

   public WicaChannelType getWicaChannelType()
   {
      return wicaChannelType;
   }

   public WicaChannelAlarmSeverity getWicaAlarmSeverity()
   {
      return alarmSeverity;
   }

   public WicaChannelAlarmStatus getWicaChannelAlarmStatus()
   {
      return alarmStatus;
   }

   public LocalDateTime getDataSourceTimestamp()
   {
      return dataSourceTimestamp;
   }

   @Override
   public String toString()
   {
       return "WicaChannelValueConnected{" +
               "wicaChannelType=" + wicaChannelType +
               ", wicaChannelAlarmSeverity=" + alarmSeverity +
               ", wicaChannelAlarmStatus=" + alarmStatus +
               ", dataSourceTimestamp=" + dataSourceTimestamp +
               '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
