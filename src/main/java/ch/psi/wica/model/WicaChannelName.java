/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;
import java.util.Objects;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelName
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String channelName;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelName( String channelName )
   {
      this.channelName = Validate.notBlank( channelName );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( o == null || getClass() != o.getClass() ) return false;
      WicaChannelName that = (WicaChannelName) o;
      return Objects.equals(channelName, that.channelName);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(channelName);
   }

   @Override
   public String toString()
   {
      return channelName;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
