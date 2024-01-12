/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the value of a channel in the disconnected state.
 */
public class WicaChannelValueDisconnected extends WicaChannelValue
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

    private final String val;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueDisconnected()
   {
      super( WicaChannelType.UNKNOWN, LocalDateTime.now( ), false );
      this.val = null;
   }

   public String getValue()
   {
      return val;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueDisconnected{" +
              "val='" + val + '\'' +
              '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
