/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;

/**
 * Represents the abstract root of a hierarchy of objects which provide
 * fields to describe a control point's general characteristics (that is
 * its <i>metadata</i>) and/or fields to describe its evolving state
 * that is its <i>instantaneous value</i>).
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

   /**
    * Constructs a new instance of unknown type based on the current server timestamp.
    */
   public WicaChannelData()
   {
      this.wicaChannelType = WicaChannelType.UNKNOWN;
      this.wicaServerTimestamp = LocalDateTime.now();
   }

   /**
    * Constructs a new instance of the specified type and with the specified timestamp.
    *
    * @param wicaChannelType the channel type.
    * @param wicaServerTimestamp the server timestamp.
    */
   public WicaChannelData( WicaChannelType wicaChannelType, LocalDateTime wicaServerTimestamp )
   {
      this.wicaChannelType = wicaChannelType;
      this.wicaServerTimestamp = Validate.notNull( wicaServerTimestamp, "wicaServerTimestamp cannot be null" );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the channel type.
    *
    * @return the channel type.
    */
   public WicaChannelType getType()
   {
      return wicaChannelType;
   }

   /**
    * Returns the server timestamp.
    *
    * @return the server timestamp.
    */
   public LocalDateTime getWicaServerTimestamp()
   {
      return this.wicaServerTimestamp;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
