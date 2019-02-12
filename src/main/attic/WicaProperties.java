/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaProperties
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Map<String,String> map;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaProperties( Map<String,String> map )
   {
      Validate.notNull( map );
      this.map = Collections.unmodifiableMap( map );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Set<String> getPropertyNames()
   {
      return map.keySet();
   }

   public int getNumberOfProperties()
   {
      return map.keySet().size();
   }


   public boolean hasProperty( String propertyName  )
   {
      Validate.notEmpty( propertyName );
      return map.containsKey( propertyName );
   }

   public String getPropertyValue( String propertyName )
   {
      Validate.notEmpty( propertyName );

      if ( ! map.containsKey( propertyName ) )
      {
         throw new IllegalArgumentException( "The property apply does not contain a property named: '" + propertyName + "'" );
      }
      return map.get( propertyName );
   }

   @Override
   public String toString()
   {
      return "WicaProperties{" +
            "apply=" + map +
            '}';
   }
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
