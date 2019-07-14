/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelName;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service which listens and buffers metadata update events
 * received via the Spring event listening service making them available
 * as a service to the rest of the application.
 */
@Service
@ThreadSafe
public class WicaChannelMetadataBufferingService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * Stash of names in the control system and their most recently
    * received metadata.
    */
   private final Map<ControlSystemName, WicaChannelMetadata> stash;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    */
   public WicaChannelMetadataBufferingService()
   {
      stash = Collections.synchronizedMap( new HashMap<>() );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Buffers incoming metadata update events.
    *
    * @param wicaChannelMetadataUpdateEvent the event.
    */
   @EventListener
   public void handleMetadataUpdate( WicaChannelMetadataUpdateEvent wicaChannelMetadataUpdateEvent )
   {
      final var controlSystemName = Validate.notNull( wicaChannelMetadataUpdateEvent.getControlSystemName() );
      final var wicaChannelMetadata = Validate.notNull( wicaChannelMetadataUpdateEvent.getWicaChannelMetadata() );

      stash.put( controlSystemName, wicaChannelMetadata );
   }

   /**
    * Gets the metadata for the specified wica channel's (whose metadata must already
    * exist in the stash).
    *
    * @param wicaChannels the channels of interest.
    * @return the stream's metadata.
    *
    * @throws IllegalStateException if the stash has no metadata for one or more channel's in the stream.
    */
   public Map<WicaChannel,WicaChannelMetadata> get( Set<WicaChannel> wicaChannels )
   {
      Validate.notNull( wicaChannels );
      Validate.validState( wicaChannels.stream().anyMatch( c -> stash.containsKey( c.getName().getControlSystemName() ) ),"no metadata for one or more channels");

      return wicaChannels.stream().collect( Collectors.toMap( c -> c, c -> stash.get( c.getName().getControlSystemName())) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
