/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelPollMonitorEvent;
import ch.psi.wica.controlsystem.event.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.infrastructure.channel.WicaChannelValueTimestampRewriter;
import ch.psi.wica.infrastructure.stream.WicaStreamMonitoredValueDataBuffer;
import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

   public WicaStreamMonitoredValueCollectorService( @Value( "${wica.channel-value-stash-buffer-size}") int bufferSize,
                                                    @Autowired ApplicationEventPublisher applicationEventPublisher,
                                                    @Autowired WicaChannelValueTimestampRewriter wicaChannelValueTimestampRewriter,
                                                    @Autowired WicaChannelValueFilteringService wicaChannelValueFilteringService)
   {
      this.wicaStreamMonitoredValueDataBuffer = new WicaStreamMonitoredValueDataBuffer( bufferSize );
      this.applicationEventPublisher = applicationEventPublisher;
      this.wicaChannelValueTimestampRewriter = wicaChannelValueTimestampRewriter;
      this.wicaChannelValueFilteringService = wicaChannelValueFilteringService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel,List<WicaChannelValue>> get( WicaStream wicaStream, LocalDateTime since)
   {
      final var inputMap = wicaStreamMonitoredValueDataBuffer.getLaterThan( wicaStream.getWicaChannels(), since );
      final Map<WicaChannel,List<WicaChannelValue>> outputMap = new ConcurrentHashMap<>();
      inputMap.forEach( (ch,lst ) -> {
         if ( ch.getProperties().getDataAcquisitionMode().doesMonitorPublication() )
         {
            var outputList = wicaChannelValueFilteringService.filterValues( ch, lst );
            if ( outputList.size() != 0 )
            {
               outputMap.put(ch, outputList);
            }
         }
      } );
      return Collections.unmodifiableMap( outputMap );
   }

/*- Private methods ----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelMonitoredValueUpdateEvent( WicaChannelMonitoredValueUpdateEvent event)
   {
      Validate.notNull( event );
      final ControlSystemName controlSystemName = event.getControlSystemName();
      final WicaChannelValue wicaChannelValue = event.getWicaChannelValue();
      wicaStreamMonitoredValueDataBuffer.saveDataPoint(controlSystemName, wicaChannelValue );
   }

   @EventListener
   public void handleWicaChannelPollMonitorEvent( WicaChannelPollMonitorEvent event)
   {
      Validate.notNull( event );
      final WicaChannelName wicaChannelName = event.getWicaChannelName();
      final ControlSystemName controlSystemName = wicaChannelName.getControlSystemName();
      final WicaChannelValue wicaChannelValue = wicaStreamMonitoredValueDataBuffer.getLastDataPoint( controlSystemName );
      final WicaChannelValue rewrittenChannelValue = wicaChannelValueTimestampRewriter.rewrite( wicaChannelValue, LocalDateTime.now() );
      applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannelName, rewrittenChannelValue ) );
   }

/*- Nested Classes -----------------------------------------------------------*/


}
