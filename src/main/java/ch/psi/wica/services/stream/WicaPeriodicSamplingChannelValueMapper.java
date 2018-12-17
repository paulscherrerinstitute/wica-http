/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
class WicaPeriodicSamplingChannelValueMapper implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int samplingInterval;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaPeriodicSamplingChannelValueMapper( int samplingInterval )
   {
      Validate.isTrue( samplingInterval > 1 );
      this.samplingInterval = Validate.notNull( samplingInterval );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> map( List<WicaChannelValue> inputList )
   {
      final List<WicaChannelValue> outputList = new LinkedList<>();

      return IntStream.range(0, inputList.size())
            .filter(n -> n % samplingInterval == 0)
            .mapToObj( inputList::get)
            .collect( Collectors.toList() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
