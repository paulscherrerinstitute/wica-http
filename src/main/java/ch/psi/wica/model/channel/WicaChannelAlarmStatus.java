/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelAlarmStatus
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int wicaAlarmStatusCode;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaChannelAlarmStatus( int wicaAlarmStatusCode )
   {
      this.wicaAlarmStatusCode = wicaAlarmStatusCode;
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelAlarmStatus of( int statusCode )
   {
      return new WicaChannelAlarmStatus( statusCode );
   }

/*- Public methods -----------------------------------------------------------*/

   public int getStatusCode()
   {
      return wicaAlarmStatusCode;
   }

   @Override
   public String toString()
   {
      return String.valueOf( getStatusCode() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
