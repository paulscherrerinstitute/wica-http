/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Map;

@Immutable
public class WicaStreamPropertiesOld extends WicaProperties
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaStreamPropertiesOld( Map<String,String> map )
   {
      super( map );
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaStreamPropertiesOld of( Map<String,String> map )
   {
      Validate.notNull( map );
      return new WicaStreamPropertiesOld(map );
   }

   public static WicaStreamPropertiesOld ofEmpty()
   {
      return new WicaStreamPropertiesOld(Map.of() );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
