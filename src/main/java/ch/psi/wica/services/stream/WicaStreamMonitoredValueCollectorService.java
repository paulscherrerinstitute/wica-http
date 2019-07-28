/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelDataBuffer;
import ch.psi.wica.controlsystem.event.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.model.app.ControlSystemName;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Service
@ThreadSafe
public class WicaStreamMonitoredValueCollectorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelDataBuffer<WicaChannelValue> wicaChannelDataBuffer;
   private final WicaChannelValueFilteringService wicaChannelValueFilteringService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamMonitoredValueCollectorService( @Value( "${wica.channel-value-stash-buffer-size}") int bufferSize,
                                                    @Autowired WicaChannelValueFilteringService wicaChannelValueFilteringService)
   {
      this.wicaChannelDataBuffer = new WicaChannelDataBuffer<>( bufferSize );
      this.wicaChannelValueFilteringService = wicaChannelValueFilteringService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel,List<WicaChannelValue>> get( WicaStream wicaStream, LocalDateTime since)
   {
      final var inputMap = wicaChannelDataBuffer.getLaterThan( wicaStream.getWicaChannels(), since );

      final Map<WicaChannel,List<WicaChannelValue>> outputMap = new ConcurrentHashMap<>();
      inputMap.forEach( (ch,lst ) -> {
         var outputList = wicaChannelValueFilteringService.filterValues(ch, lst );
         outputMap.put( ch, outputList );
      } );

      return Collections.unmodifiableMap( outputMap );
   }

   Map<WicaChannel,Optional<WicaChannelValue>> getLatest( WicaStream wicaStream )
   {
      return Collections.unmodifiableMap( wicaChannelDataBuffer.getLatest( wicaStream.getWicaChannels() ) );
   }

/*- Private methods ----------------------------------------------------------*/

   @EventListener
   public void handleUpdateEvent( WicaChannelMonitoredValueUpdateEvent event)
   {
      Validate.notNull( event );
      final ControlSystemName controlSystemName = event.getControlSystemName();
      final WicaChannelValue wicaChannelValue = event.getWicaChannelValue();
      wicaChannelDataBuffer.saveControlSystemDataPoint( controlSystemName, wicaChannelValue );
   }

/*- Nested Classes -----------------------------------------------------------*/


}
