/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;


/*- Interface Declaration ----------------------------------------------------*/
/*- Record Declaration -------------------------------------------------------*/

/**
 * Models an event that is fired when an EPICS channel is disconnected.
 *
 * @param scope the scope.
 * @param caChannel the channel.
 */
public record EpicsChannelDisconnectedEvent( String scope, Channel<Object> caChannel )
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Models an event that is fired when an EPICS channel is disconnected.
    *
    * @param scope the scope.
    * @param caChannel the channel.
    */
   public EpicsChannelDisconnectedEvent( String scope, Channel<Object> caChannel )
   {
      this.scope = Validate.notNull( scope, "The 'scope' argument is null." );
      this.caChannel = Validate.notNull( caChannel, "The 'caChannel' argument is null." );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the name of the channel.
    *
    * @return the name.
    */
   public EpicsChannelName getEpicsChannelName()
   {
      //noinspection resource
      return EpicsChannelName.of( caChannel().getName() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}