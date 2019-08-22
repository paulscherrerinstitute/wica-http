/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelPollMonitorEvent;
import ch.psi.wica.controlsystem.event.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.infrastructure.channel.WicaChannelValueTimestampRewriter;
import ch.psi.wica.infrastructure.stream.WicaStreamMonitoredValueDataBuffer;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.services.channel.WicaChannelValueFilteringService;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
public class WicaStreamMonitoredValueCollectorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaStreamMonitoredValueDataBuffer wicaStreamMonitoredValueDataBuffer;
   private final ApplicationEventPublisher applicationEventPublisher;
   private final WicaChannelValueTimestampRewriter wicaChannelValueTimestampRewriter;
   private final WicaChannelValueFilteringService wicaChannelValueFilteringService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamMonitoredValueCollectorService( @Value( "${wica.channel-monitored-value-buffer-size}") int bufferSize,
                                                    @Autowired ApplicationEventPublisher applicationEventPublisher,
                                                    @Autowired WicaChannelValueTimestampRewriter wicaChannelValueTimestampRewriter,
                                                    @Autowired WicaChannelValueFilteringService wicaChannelValueFilteringService )
   {
      this.wicaStreamMonitoredValueDataBuffer = new WicaStreamMonitoredValueDataBuffer( bufferSize );
      this.applicationEventPublisher = applicationEventPublisher;
      this.wicaChannelValueTimestampRewriter = wicaChannelValueTimestampRewriter;
      this.wicaChannelValueFilteringService = wicaChannelValueFilteringService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel,List<WicaChannelValue>> get( WicaStream wicaStream, LocalDateTime since )
   {
      final var inputMap = wicaStreamMonitoredValueDataBuffer.getLaterThan( wicaStream.getWicaChannels(), since );
      return inputMap.entrySet()
            .stream()
            .filter( e -> e.getKey().getProperties().getDataAcquisitionMode().doesMonitorPublication() )
            .map( e -> new AbstractMap.SimpleEntry<>( e.getKey(), wicaChannelValueFilteringService.filterValues( e.getKey(), e.getValue() ) ) )
            .filter( e -> e.getValue().size() > 0 )
            .collect( Collectors.toUnmodifiableMap( Map.Entry::getKey, Map.Entry::getValue ) );
   }

   Map<WicaChannel,List<WicaChannelValue>> getLatest( WicaStream wicaStream )
   {
      final var inputMap = wicaStreamMonitoredValueDataBuffer.getLaterThan( wicaStream.getWicaChannels(), LocalDateTime.MIN );
      return inputMap.entrySet()
                     .stream()
                     .filter( e -> e.getKey().getProperties().getDataAcquisitionMode().doesMonitorPublication() )
                     .map( e -> new AbstractMap.SimpleEntry<>( e.getKey(), wicaChannelValueFilteringService.filterLastValues( e.getValue() ) ) )
                     .filter( e -> e.getValue().size() > 0 )
                     .collect( Collectors.toUnmodifiableMap( Map.Entry::getKey, Map.Entry::getValue ) );
   }

   @EventListener
   public void handleWicaChannelMonitoredValueUpdateEvent( WicaChannelMonitoredValueUpdateEvent event)
   {
      Validate.notNull( event );
      final WicaChannel wicaChannel = event.getWicaChannel();
      final WicaDataBufferStorageKey wicaDataBufferStorageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey(wicaChannel );
      final WicaChannelValue wicaChannelValue = event.getWicaChannelValue();
      wicaStreamMonitoredValueDataBuffer.saveDataPoint( wicaDataBufferStorageKey, wicaChannelValue );
   }

   @EventListener
   public void handleWicaChannelPollMonitorEvent( WicaChannelPollMonitorEvent event)
   {
      Validate.notNull( event );
      final WicaChannel wicaChannel = event.getWicaChannel();
      final WicaDataBufferStorageKey wicaDataBufferStorageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey(wicaChannel );
      final WicaChannelValue wicaChannelValue = wicaStreamMonitoredValueDataBuffer.getLatest( wicaDataBufferStorageKey );
      final WicaChannelValue rewrittenChannelValue = wicaChannelValueTimestampRewriter.rewrite( wicaChannelValue, LocalDateTime.now() );
      applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannel, rewrittenChannelValue ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
