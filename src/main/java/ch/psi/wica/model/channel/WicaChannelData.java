/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;

/**
 * Represents the abstract root of a typed hierarchy of objects whose
 * information is obtained from the underlying control system and which
 * can be serialized as a JSON string for including as part of a <i>wica
 * stream</i>.
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
