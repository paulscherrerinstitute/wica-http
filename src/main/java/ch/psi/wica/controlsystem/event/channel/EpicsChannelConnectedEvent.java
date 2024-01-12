/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Models an event that is fired when an EPICS channel is connected.
 *
 * @param scope the scope.
*  @param caChannel the channel.
 */
public record EpicsChannelConnectedEvent( String scope, Channel<Object> caChannel )
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
   
   /**
    * Models an event that is fired when an EPICS channel is connected.
    *
    * @param scope the scope.
    * @param caChannel the channel.
    */
   public EpicsChannelConnectedEvent
   {
      Validate.notNull( scope, "The 'scope' argument is null." );
      Validate.notNull( caChannel, "The 'caChannel' argument is null." );
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
      return EpicsChannelName.of( caChannel().getName());
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}