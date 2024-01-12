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
    private final WicaChannelAlarmSeverity wicaChannelAlarmSeverity;
    private final WicaChannelAlarmStatus wicaChannelAlarmStatus;
    private final LocalDateTime dataSourceTimestamp;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueConnected( WicaChannelType wicaChannelType, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp )
   {
      super( wicaChannelType, LocalDateTime.now( ), true );
      this.wicaChannelType = Validate.notNull( wicaChannelType, "wicaChannelType cannot be null " );
      this.wicaChannelAlarmSeverity = Validate.notNull( wicaChannelAlarmSeverity, "wicaAlarmSeverity cannot be null " );
      this.wicaChannelAlarmStatus = Validate.notNull( wicaChannelAlarmStatus, "wicaAlarmStatus cannot be null " );
      this.dataSourceTimestamp = Validate.notNull( dataSourceTimestamp, "dataSourceTimestamp cannot be null " );
   }

   public WicaChannelType getWicaChannelType()
   {
      return wicaChannelType;
   }

   public WicaChannelAlarmSeverity getWicaAlarmSeverity()
   {
      return wicaChannelAlarmSeverity;
   }

   public WicaChannelAlarmStatus getWicaChannelAlarmStatus()
   {
      return wicaChannelAlarmStatus;
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
               ", wicaChannelAlarmSeverity=" + wicaChannelAlarmSeverity +
               ", wicaChannelAlarmStatus=" + wicaChannelAlarmStatus +
               ", dataSourceTimestamp=" + dataSourceTimestamp +
               '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
