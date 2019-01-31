/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import ch.psi.wica.model.WicaStream;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A WicaChannelValueMapper that returns an output list with all input values
 * passed through unchanged.
 */
@Immutable
class WicaChannelValueMapperFieldSerializationChooser implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> map( List<WicaChannelValue> inputList )
   {
      Validate.notNull( inputList );
      return inputList;
   }

/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaChannelValueMapperFieldSerializationChooser( WicaStream  wicaStream )
   {

   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
