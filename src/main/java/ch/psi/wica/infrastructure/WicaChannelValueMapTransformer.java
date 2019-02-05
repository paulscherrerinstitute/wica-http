/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannel;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import ch.psi.wica.model.WicaStream;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueMapTransformer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Map<WicaChannelName,WicaChannel> channelMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueMapTransformer( WicaStream wicaStream )
   {
      var wicaChannels = wicaStream.getWicaChannels();
      this.channelMap = Collections.unmodifiableMap( wicaChannels.stream().collect(Collectors.toConcurrentMap(WicaChannel::getName, c -> c ) ) );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    *
    * @param inputMap
    * @return
    */
   public Map<WicaChannelName,List<WicaChannelValue>> map( Map<WicaChannelName,List<WicaChannelValue>> inputMap )
   {
      Validate.isTrue(inputMap.keySet().stream().allMatch( channelMap::containsKey ), "One or more channels in the inputMap were unknown" );

      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
      inputMap.keySet().forEach( c -> {
         final List<WicaChannelValue> inputList = inputMap.get( c );
         final WicaChannel wicaChannel = this.channelMap.get(c );
         final List<WicaChannelValue> outputList = wicaChannel.map( inputList );
         if ( outputList.size() > 0 ) {
            outputMap.put( c, outputList );
         }
      } );

      return outputMap;
   }

   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
