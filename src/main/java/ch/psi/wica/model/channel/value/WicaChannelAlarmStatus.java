/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the <i>alarm status</i> of a wica channel, an abstraction whose
 * value and meaning depends on the underlying control system which hosts it.
 */
public class WicaChannelAlarmStatus
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int wicaAlarmStatusCode;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelAlarmStatus( int wicaAlarmStatusCode )
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

   @Override
   public String toString()
   {
      return String.valueOf( getStatusCode() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
