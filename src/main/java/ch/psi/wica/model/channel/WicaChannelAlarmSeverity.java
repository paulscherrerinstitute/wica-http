/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the *alarm severity* of a wica channel, an enumeration which
 * may have different meanings depending on the nature of the underlying
 * control system from which it's value is derived.
 */
public enum WicaChannelAlarmSeverity
{

/*- Public attributes --------------------------------------------------------*/

   NO_ALARM,      // 0
   MINOR_ALARM,   // 1
   MAJOR_ALARM,   // 2
   INVALID_ALARM;  // 3

/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public String toString()
   {
      return String.valueOf( this.ordinal() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
