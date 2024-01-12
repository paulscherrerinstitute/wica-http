/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event.channel;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the functionality to publish EPICS channel events.
 */
@Component
public class EpicsChannelEventPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ApplicationEventPublisher applicationEventPublisher;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param applicationEventPublisher the application event publisher.
    */
   public EpicsChannelEventPublisher( ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher, "The 'applicationEventPublisher' argument is null." );
   }

/*- Public methods -----------------------------------------------------------*/

   /**
    * Publishes an event to indicate that the specified channel has been connected for the first time.
    *
    * @param scope the scope.
    * @param caChannel the channel.
    */
   public void publishFirstConnected( String scope, Channel<Object> caChannel )
   {
      applicationEventPublisher.publishEvent( new EpicsChannelFirstConnectedEvent( scope, caChannel ) );
   }

   /**
    * Publishes an event to indicate that the specified channel has been connected.
    *
    * @param scope the scope.
    * @param caChannel the channel.
    */
   public void publishChannelConnected( String scope, Channel<Object> caChannel)
   {
      applicationEventPublisher.publishEvent( new EpicsChannelConnectedEvent( scope, caChannel ) );
   }

   /**
    * Publishes an event to indicate that the specified channel has been disconnected.
    *
    * @param scope the scope.
    * @param caChannel the channel.
    */
   public void publishChannelDisconnected( String scope, Channel<Object> caChannel)
   {
      applicationEventPublisher.publishEvent( new EpicsChannelDisconnectedEvent( scope, caChannel ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
