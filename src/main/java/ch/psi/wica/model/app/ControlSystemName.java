/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Models the name of a point of interest in a control system.
 */
@Immutable
public class ControlSystemName
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String name;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new instance.
    *
    * @param name the name.
    */
   protected ControlSystemName( String name )
   {
      this.name = Validate.notBlank( name );
   }

/*- Class methods ------------------------------------------------------------*/

   public static ControlSystemName of( String name )
   {
      return new ControlSystemName( name );
   }

/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a string representation of the name.
    *
    * @return the name.
    */
   public String asString()
   {
      return name;
   }

   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !( o instanceof ControlSystemName that ) ) return false;
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
