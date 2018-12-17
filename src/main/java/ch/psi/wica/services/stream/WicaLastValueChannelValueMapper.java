/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
class WicaLastValueChannelValueMapper implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> map( List<WicaChannelValue> inputList )
   {
      Validate.notNull( inputList );

      return (inputList.size() == 0 ) ? List.of() : List.of( inputList.get( inputList.size() - 1 ) );
   }

/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
