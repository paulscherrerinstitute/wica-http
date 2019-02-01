/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;

import java.util.Objects;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaStreamId
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static int nextAllocationId;
   private final String id;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaStreamId( String id )
   {
      this.id = Validate.notBlank( id );
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaStreamId createNext()
   {
      return new WicaStreamId(String.valueOf( nextAllocationId++ ) );
   }

   public static WicaStreamId of( String string )
   {
      return new WicaStreamId(string );
   }

   public static void resetAllocationSequencer()
   {
      nextAllocationId = 0;
   }

   public static int getCreationCount() { return nextAllocationId; }

   /*- Public methods -----------------------------------------------------------*/

   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( o == null || getClass() != o.getClass() ) return false;
      WicaStreamId wicaStreamId = (WicaStreamId) o;
      return Objects.equals(id, wicaStreamId.id);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(id);
   }

   public String asString()
   {
      return id;
   }

   @Override
   public String toString()
   {
      return "WicaStreamId<" + id + '>';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
