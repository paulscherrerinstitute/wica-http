/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.epics.ca.data.AlarmSeverity;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public enum WicaChannelAlarmSeverity
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

   public static WicaChannelAlarmSeverity from( AlarmSeverity caAlarmSeverity )
   {
      return WicaChannelAlarmSeverity.valueOf(caAlarmSeverity.toString() );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
