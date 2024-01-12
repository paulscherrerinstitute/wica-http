/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelData;
import ch.psi.wica.model.channel.WicaChannelType;
import net.jcip.annotations.Immutable;

import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the abstract root of a hierarchy of objects which provide
 * fields to describe a control point's instantaneous state.
 * <p>
 * The value information typically changes often and is obtained by
 * continuous communication with the control system hosting the
 * control point. Subsequently the information is captured and
 * made available for JSON string serialisation and for inclusion
 * as part of a <i>wica stream</i>.
 * <p>
 * The set of fields that are concretely provided for each control
 * point depend on the underlying control system.  Typical fields
 * include: whether the channel is <i>online</i> or <i>offline</i>,
 * the <i>raw value</i> and <i>timestamp</i> obtained when the channel
 * was last read out, and whether an <i>alarm</i> or <i>warning</i>
 * condition exists.
 */
@Immutable
public abstract class WicaChannelValue extends WicaChannelData
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final boolean connected;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValue( WicaChannelType wicaChannelType, LocalDateTime wicaServerTimestamp, boolean connected )
   {
      super( wicaChannelType, wicaServerTimestamp);
      this.connected = connected;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public boolean isConnected()
   {
      return connected;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValue{" +
            "connected=" + connected +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}