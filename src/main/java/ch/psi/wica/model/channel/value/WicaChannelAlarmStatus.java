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

   /**
    * Constructs a new instance of a WicaChannelAlarmStatus.
    *
    * @param wicaAlarmStatusCode the status code.
    */
   public WicaChannelAlarmStatus( int wicaAlarmStatusCode )
   {
      this.wicaAlarmStatusCode = wicaAlarmStatusCode;
   }

/*- Class methods ------------------------------------------------------------*/

   /**
    * Returns a new instance of a WicaChannelAlarmStatus.
    *
    * @param statusCode the status code.
    * @return the new instance.
    */
   public static WicaChannelAlarmStatus of( int statusCode )
   {
      return new WicaChannelAlarmStatus( statusCode );
   }

   /**
    * Returns a new instance of a WicaChannelAlarmStatus with a status code of zero.
    *
    * @return the new instance.
    */
   public static WicaChannelAlarmStatus ofNoError()
   {
      return new WicaChannelAlarmStatus( 0 );
   }

/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the status code.
    *
    * @return the status code.
    */
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
