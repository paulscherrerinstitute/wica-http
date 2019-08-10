/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.stream.WicaStreamPolledValueDataBuffer;
import ch.psi.wica.controlsystem.event.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
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
public class WicaStreamPolledValueCollectorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaStreamPolledValueDataBuffer wicaStreamPolledValueDataBuffer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamPolledValueCollectorService( @Value( "${wica.channel-value-stash-buffer-size}") int bufferSize )
   {
      this.wicaStreamPolledValueDataBuffer = new WicaStreamPolledValueDataBuffer( bufferSize );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel,List<WicaChannelValue>> get( WicaStream wicaStream, LocalDateTime since)
   {
      final var inputMap = wicaStreamPolledValueDataBuffer.getLaterThan( wicaStream.getWicaChannels(), since );
      final Map<WicaChannel,List<WicaChannelValue>> outputMap = new ConcurrentHashMap<>();
      inputMap.forEach( (ch,lst ) -> {
         if ( ch.getProperties().getDataAcquisitionMode().doesPolling() )
         {
            if ( lst.size() != 0 )
            {
               outputMap.put(ch, lst );
            }
         }
      } );

      return Collections.unmodifiableMap( outputMap );
   }

/*- Private methods ----------------------------------------------------------*/

   @EventListener
   public void handleUpdateEvent( WicaChannelPolledValueUpdateEvent event)
   {
      Validate.notNull( event );
      final WicaChannel wicaChannel = event.getWicaChannel();
      final WicaChannelValue wicaChannelValue = event.getWicaChannelValue();
      wicaStreamPolledValueDataBuffer.saveDataPoint( wicaChannel, wicaChannelValue );
   }

/*- Nested Classes -----------------------------------------------------------*/


}
