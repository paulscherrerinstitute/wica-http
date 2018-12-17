/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;

import java.util.List;
import java.util.Map;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public interface WicaStreamMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannelName,List<WicaChannelValue>> map( Map<WicaChannelName,List<WicaChannelValue>> inputMap );


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
