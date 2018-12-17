/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannel;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelProperties;
import ch.psi.wica.model.WicaStream;
import net.jcip.annotations.Immutable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
@Service
public class WicaStreamMapperBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaChannelValueMapperBuilder wicaChannelValueMapperBuilder;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamMapperBuilder( @Autowired WicaChannelValueMapperBuilder wicaChannelValueMapperBuilder )
   {
      this.wicaChannelValueMapperBuilder = wicaChannelValueMapperBuilder;
   }

/*- Class methods ------------------------------------------------------------*/

   public WicaStreamMapper create( WicaStream wicaStream )
   {
      final Set<WicaChannel> wicaChannels = wicaStream.getWicaChannels();
      final ConcurrentMap<WicaChannelName, WicaChannelValueMapper> map = wicaChannels.stream()
                                                                                         .collect( Collectors.toMap( c -> c., n -> {
                                                                                             final WicaChannelProperties wicaChannelProperties = wicaStream.getWicaChannelProperties( n );
                                                                                             wicaChannelValueMapperBuilder.createFromChannelProperties( wicaChannelProperties );
                                                                                         } ) );
      return WicaStreamMapper.of( map );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
