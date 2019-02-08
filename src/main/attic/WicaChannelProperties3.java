/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Map;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelProperties3 extends WicaProperties
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaChannelProperties3( Map<String,String> map )
   {
      super( map );
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelProperties3 of( Map<String,String> map )
   {
      Validate.notNull( map );
      return new WicaChannelProperties3( map );
   }

   public static WicaChannelProperties3 ofEmpty()
   {
      return new WicaChannelProperties3( Map.of() );
   }


/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
