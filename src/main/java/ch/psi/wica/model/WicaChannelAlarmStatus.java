/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.epics.ca.data.AlarmStatus;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelAlarmStatus
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private int wicaAlarmStatusCode;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaChannelAlarmStatus( int wicaAlarmStatusCode )
   {
      this.wicaAlarmStatusCode = wicaAlarmStatusCode;
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelAlarmStatus of( int statusCode )
   {
      return new WicaChannelAlarmStatus( statusCode );
   }

   public static WicaChannelAlarmStatus ofNoError()
   {
      return new WicaChannelAlarmStatus( 0 );
   }

/*- Public methods -----------------------------------------------------------*/

   public int getStatusCode()
   {
      return wicaAlarmStatusCode;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
