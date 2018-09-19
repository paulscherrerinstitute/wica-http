/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;
import org.epics.ca.data.AlarmSeverity;

import java.util.Objects;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public enum EpicsChannelAlarmSeverity
{

/*- Public attributes --------------------------------------------------------*/

   NO_ALARM,      // 0
   MINOR_ALARM,   // 1
   MAJOR_ALARM,   // 2
   INVALID_ALARM; // 3

/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/

   public static EpicsChannelAlarmSeverity from( AlarmSeverity caAlarmSeverity )
   {
      return EpicsChannelAlarmSeverity.valueOf( caAlarmSeverity.toString() );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
