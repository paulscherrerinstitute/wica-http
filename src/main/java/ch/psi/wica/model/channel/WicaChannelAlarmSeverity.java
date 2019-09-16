/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the <i>alarm severity</i> of a wica channel, an abstraction
 * which specifies whether a wica channel is operating normally or is in
 * an alarm or warning state.
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
