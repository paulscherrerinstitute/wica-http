/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.Validate;

import java.util.LinkedList;
import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A filter that returns an output list which includes one-in-every-N values
 * taken from the input list over successive invocations.
 */
@NotThreadSafe
class WicaChannelValueFixedSamplingCycleFilter implements WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int samplingCycleLength;
   private int samplingCycleIndex = 0;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance with the specified sampling cycle length.
    *
    * @param samplingCycleLength - the sampling cycle length. (ie the value
    *     of N in this 1-in-every-N sampler).
    */
   WicaChannelValueFixedSamplingCycleFilter( int samplingCycleLength )
   {
      Validate.isTrue( samplingCycleLength > 0 );
      this.samplingCycleLength = samplingCycleLength;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> apply( List<WicaChannelValue> inputList )
   {
      final List<WicaChannelValue> outputList = new LinkedList<>();
      for ( WicaChannelValue inputValue : inputList )
      {
         if ( samplingCycleIndex % samplingCycleLength == 0 )
         {
            outputList.add( inputValue );
         }
         samplingCycleIndex++;
      }
      return outputList;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueFixedSamplingCycleFilter{" +
            "samplingCycleLength=" + samplingCycleLength +
            ", samplingCycleIndex=" + samplingCycleIndex +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
