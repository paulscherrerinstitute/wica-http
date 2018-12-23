/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelProperties extends WicaProperties
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaChannelProperties( Map<String,String> map )
   {
      super( map );
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelProperties of( Map<String,String> map )
   {
      Validate.notNull( map );
      return new WicaChannelProperties( map );
   }

   public static WicaChannelProperties ofEmpty()
   {
      return new WicaChannelProperties( Map.of() );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
