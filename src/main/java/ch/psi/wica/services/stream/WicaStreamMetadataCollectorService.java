/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.infrastructure.stream.WicaStreamMetadataDataBuffer;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.stream.WicaStream;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

   private final Logger logger = LoggerFactory.getLogger( WicaStreamMetadataCollectorService.class );
   private final WicaStreamMetadataDataBuffer wicaStreamMetadataDataBuffer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    */
   public WicaStreamMetadataCollectorService()
   {
      this.wicaStreamMetadataDataBuffer = new WicaStreamMetadataDataBuffer();
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a map of any channels in the specified wica stream whose metadata
    * has changed since the specified date.
    *
    * @param wicaStream the stream
    * @param since the time and date used for comparison.
    */
   public Map<WicaChannel,WicaChannelMetadata> get( WicaStream wicaStream, LocalDateTime since )
   {
      final Map<WicaChannel, List<WicaChannelMetadata>> inputMap = wicaStreamMetadataDataBuffer.getLaterThan( wicaStream.getWicaChannels(), since );
      final var outputMap = inputMap.keySet().stream()
        .filter( c -> ! inputMap.get( c ).isEmpty() )
        .collect( Collectors.toUnmodifiableMap( c -> c, c-> inputMap.get( c ).get( 0 ) ) );

      logger.trace( "INPUT MAP IS: {} ", inputMap );
      logger.trace( "OUTPUT MAP IS: {} ", outputMap );
      return outputMap;
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

         logger.info( "Received METADATA update event: {} ", wicaChannelMetadataUpdateEvent);

         final WicaChannel wicaChannel = wicaChannelMetadataUpdateEvent.getWicaChannel();
         final WicaDataBufferStorageKey wicaDataBufferStorageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey(wicaChannel );
         final WicaChannelMetadata wicaChannelMetadata = wicaChannelMetadataUpdateEvent.getWicaChannelData();
         wicaStreamMetadataDataBuffer.saveDataPoint( wicaDataBufferStorageKey, wicaChannelMetadata );

      }
   }

}
