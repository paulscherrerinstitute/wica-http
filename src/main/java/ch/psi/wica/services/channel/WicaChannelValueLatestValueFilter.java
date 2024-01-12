/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.value.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A filter that returns an output list with the most recent N values taken
 * from the input list.
 */
@Immutable
class WicaChannelValueLatestValueFilter implements WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int maxNumberOfSamples;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which returns up to the specified maximum
    * number of samples.
    *
    * @param maxNumberOfSamples - the maximum number of values to include
    *     in the output list.
    */
   WicaChannelValueLatestValueFilter( int maxNumberOfSamples )
   {
      Validate.isTrue( maxNumberOfSamples >= 0 );
      this.maxNumberOfSamples = maxNumberOfSamples;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> apply( List<WicaChannelValue> inputList )
   {
      final int startInclusive = Math.max( 0, inputList.size() - maxNumberOfSamples );
      final int endExclusive = inputList.size();

      return IntStream.range( startInclusive, endExclusive ).mapToObj( inputList::get ).collect( Collectors.toList() );
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueLatestValueFilter{" +
            "maxNumberOfSamples=" + maxNumberOfSamples +
            '}';
   }
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
