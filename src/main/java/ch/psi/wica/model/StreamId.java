/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;
import java.util.Objects;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class StreamId
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static int nextAllocationId;
   private final String id;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private StreamId( String id )
   {
      this.id = Validate.notBlank( id );
   }

/*- Class methods ------------------------------------------------------------*/

   public static StreamId createNext()
   {
      return new StreamId( String.valueOf( nextAllocationId++ ) );
   }

   public static StreamId of( String string )
   {
      return new StreamId( string );
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
      StreamId streamId = (StreamId) o;
      return Objects.equals(id, streamId.id);
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
      return "StreamId<" + id + '>';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
