/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonFilter;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;

/**
 * Root of hierarchy of objects which can be serialized and sent down the
 * wire as a component of the Server Sent Event (SSE) stream.
 */

@Immutable
public abstract class WicaChannelData
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelType wicaChannelType;
   private final LocalDateTime wicaServerTimestamp;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelData()
   {
      this.wicaChannelType = WicaChannelType.UNKNOWN;
      this.wicaServerTimestamp = LocalDateTime.now();
   }

   public WicaChannelData( WicaChannelType wicaChannelType, LocalDateTime wicaServerTimestamp )
   {
      this.wicaChannelType = wicaChannelType;
      this.wicaServerTimestamp = Validate.notNull( wicaServerTimestamp, "wicaServerTimestamp cannot be null" );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannelType getType()
   {
      return wicaChannelType;
   }
   public LocalDateTime getWicaServerTimestamp()
{
   return this.wicaServerTimestamp;
}

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
