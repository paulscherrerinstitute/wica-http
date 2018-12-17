/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;

import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public interface WicaChannelValueMapper
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
   List<WicaChannelValue> map( List<WicaChannelValue> inputList );

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
