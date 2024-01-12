/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.value.WicaChannelValue;
import net.jcip.annotations.Immutable;

import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public interface WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Transforms the values in the input apply to the output apply using
    * some configurable filtering algorithm.
    *
    * @param inputList the list of values to process.
    * @return the list of values that were produced from the processing step.
    */
   List<WicaChannelValue> apply( List<WicaChannelValue> inputList );


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/




}
