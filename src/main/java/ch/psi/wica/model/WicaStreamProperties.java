/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Map;

@Immutable
public class WicaStreamProperties extends WicaProperties
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaStreamProperties(  Map<String,String> map )
   {
      super( map );
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaStreamProperties of( Map<String,String> map )
   {
      Validate.notNull( map );
      return new WicaStreamProperties( map );
   }

   public static WicaStreamProperties ofEmpty()
   {
      return new WicaStreamProperties( Map.of() );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
