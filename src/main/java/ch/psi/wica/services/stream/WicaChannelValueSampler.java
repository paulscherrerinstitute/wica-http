/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public abstract class WicaChannelValueSampler
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Transforms the values in the input map to the output map using
    * some configurable algorithm.
    *
    * @param inputList the list of values to process.
    * @return the list of values that were produced from the processing step.
    */
   abstract List<WicaChannelValue> sample( List<WicaChannelValue> inputList );

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   public class WicaAllValueSampler extends WicaChannelValueSampler
   {
      @Override
      List<WicaChannelValue> sample(List<WicaChannelValue> inputList)
      {
         return inputList;
      }
   }

   public class WicaLastValueSampler extends WicaChannelValueSampler
   {
      @Override
      List<WicaChannelValue> sample( List<WicaChannelValue> inputList )
      {
         return inputList.size() == 0 ? List.of() : List.of( inputList.get( inputList.size() - 1 ) );
      }
   }

   public class WicaPeriodicValueSampler extends WicaChannelValueSampler
   {


      @Override
      List<WicaChannelValue> sample( List<WicaChannelValue> inputList )
      {

         return inputList.stream().

                 IntStream.range( 0, inputList.size() )
                  .map( e -> { return inputList.get( e ); } )
              //    .filter( getSampleCount() % 1000 == 0 )
                  .collect( Collectors.toList() );
      }


     int getSampleCount()
     {

     }

   }


}
