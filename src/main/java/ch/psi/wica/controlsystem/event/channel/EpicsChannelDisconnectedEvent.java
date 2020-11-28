/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class EpicsChannelDisconnectedEvent
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String scope;
   private final Channel<Object> caChannel;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelDisconnectedEvent( String scope, Channel<Object> caChannel )
   {
      this.scope = Validate.notNull( scope );
      this.caChannel = Validate.notNull( caChannel );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String getScope()
   {
      return scope;
   }

   public Channel<Object> getCaChannel()
   {
      return caChannel;
   }

   public EpicsChannelName getEpicsChannelName()
   {
      return EpicsChannelName.of( getCaChannel().getName() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}