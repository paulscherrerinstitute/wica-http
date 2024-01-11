/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.channel;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
public class EpicsChannelEventPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ApplicationEventPublisher applicationEventPublisher;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelEventPublisher( ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher, "The 'applicationEventPublisher' argument is null." );
   }

/*- Public methods -----------------------------------------------------------*/

   public void publishFirstConnected( String scope, Channel<Object> caChannel )
   {
      applicationEventPublisher.publishEvent( new EpicsChannelFirstConnectedEvent( scope, caChannel ) );
   }

   public void publishChannelConnected( String scope, Channel<Object> caChannel)
   {
      applicationEventPublisher.publishEvent( new EpicsChannelConnectedEvent( scope, caChannel ) );
   }

   public void publishChannelDisconnected( String scope, Channel<Object> caChannel)
   {
      applicationEventPublisher.publishEvent( new EpicsChannelDisconnectedEvent( scope, caChannel ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
