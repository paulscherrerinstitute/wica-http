/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelDataBuffer;
import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.stream.WicaStream;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
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
public class WicaStreamMetadataCollectorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelDataBuffer<WicaChannelMetadata> wicaChannelDataBuffer;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    */
   public WicaStreamMetadataCollectorService()
   {
      this.wicaChannelDataBuffer = new WicaChannelDataBuffer<>(1 );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel,WicaChannelMetadata> get( WicaStream wicaStream)
   {
      final Map<WicaChannel, Optional<WicaChannelMetadata>> map = wicaChannelDataBuffer.getLatest(wicaStream.getWicaChannels() );

      //noinspection OptionalGetWithoutIsPresent
      return map.keySet().stream()
              .filter( c -> map.get( c ).isPresent() )
              .collect( Collectors.toUnmodifiableMap( c -> c, c-> map.get( c ).get() ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   @Component
   public class ChannelValueUpdateListener
   {
      @EventListener
      public void handleWicaChannelValueUpdateEvent( WicaChannelMetadataUpdateEvent wicaChannelMetadataUpdateEvent )
      {
         Validate.notNull( wicaChannelMetadataUpdateEvent );
         final ControlSystemName controlSystemName = wicaChannelMetadataUpdateEvent.getControlSystemName();
         final WicaChannelMetadata wicaChannelValue = wicaChannelMetadataUpdateEvent.getWicaChannelData();
         wicaChannelDataBuffer.saveControlSystemDataPoint( controlSystemName, wicaChannelValue );
      }
   }

}
