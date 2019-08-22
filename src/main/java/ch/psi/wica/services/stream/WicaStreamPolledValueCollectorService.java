/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.infrastructure.stream.WicaStreamPolledValueDataBuffer;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.services.channel.WicaChannelValueFilteringService;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Service
@ThreadSafe
public class WicaStreamPolledValueCollectorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaStreamPolledValueDataBuffer wicaStreamPolledValueDataBuffer;
   private final WicaChannelValueFilteringService wicaChannelValueFilteringService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamPolledValueCollectorService( @Value( "${wica.channel-polled-value-buffer-size}") int bufferSize,
                                                 @Autowired WicaChannelValueFilteringService wicaChannelValueFilteringService )
   {
      this.wicaStreamPolledValueDataBuffer = new WicaStreamPolledValueDataBuffer( bufferSize );
      this.wicaChannelValueFilteringService = wicaChannelValueFilteringService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel,List<WicaChannelValue>> get( WicaStream wicaStream, LocalDateTime since )
   {
      final var inputMap = wicaStreamPolledValueDataBuffer.getLaterThan( wicaStream.getWicaChannels(), since );
      return inputMap.entrySet()
                     .stream()
                     .filter( e -> e.getKey().getProperties().getDataAcquisitionMode().doesPolling() )
                     .filter( e -> e.getValue().size() > 0 )
                     .collect( Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue ) );
   }

   Map<WicaChannel,List<WicaChannelValue>> getLatest( WicaStream wicaStream )
   {
      final var inputMap = wicaStreamPolledValueDataBuffer.getLaterThan( wicaStream.getWicaChannels(), LocalDateTime.MIN );
      return inputMap.entrySet()
            .stream()
            .filter( e -> e.getKey().getProperties().getDataAcquisitionMode().doesPolling() )
            .map( e -> new AbstractMap.SimpleEntry<>(e.getKey(), wicaChannelValueFilteringService.filterLastValues( e.getValue() ) ) )
            .filter( e -> e.getValue().size() > 0 )
            .collect( Collectors.toUnmodifiableMap( Map.Entry::getKey, Map.Entry::getValue ) );

   }

/*- Private methods ----------------------------------------------------------*/

   @EventListener
   public void handleUpdateEvent( WicaChannelPolledValueUpdateEvent event)
   {
      Validate.notNull( event );
      final WicaChannel wicaChannel = event.getWicaChannel();
      final WicaDataBufferStorageKey wicaDataBufferStorageKey = WicaDataBufferStorageKey.getPolledValueStorageKey(wicaChannel );
      final WicaChannelValue wicaChannelValue = event.getWicaChannelValue();
      wicaStreamPolledValueDataBuffer.saveDataPoint( wicaDataBufferStorageKey, wicaChannelValue );
   }

/*- Nested Classes -----------------------------------------------------------*/


}
