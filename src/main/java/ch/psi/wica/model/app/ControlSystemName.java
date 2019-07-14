/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class ControlSystemName
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String name;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   protected ControlSystemName( String name )
   {
      this.name = Validate.notBlank( name );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String asString()
   {
      return name;
   }

   public static ControlSystemName of( String name )
   {
      return new ControlSystemName( name );
   }

   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof ControlSystemName) ) return false;
      ControlSystemName that = (ControlSystemName) o;
      return Objects.equals(name, that.name);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(name);
   }

   @Override
   public String toString()
   {
      return name;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
