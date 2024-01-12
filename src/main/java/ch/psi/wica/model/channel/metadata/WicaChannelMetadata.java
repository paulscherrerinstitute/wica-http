/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelData;
import ch.psi.wica.model.channel.WicaChannelType;
import net.jcip.annotations.Immutable;

import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the abstract root of a hierarchy of objects which provide
 * fields to describe a control point's general characteristics or basic
 * nature.
 * <p>
 * The metadata information is obtained by communication with the control
 * system hosting the control point. Subsequently, the information is captured
 * and made available for JSON string serialisation and for inclusion as part
 * of a <i>wica stream</i>.
 * <p>
 * The metadata information typically does not change, or changes only
 * very rarely, for example after a control point has been brought back
 * online following a software update.
 * <p>
 * The set of fields that are provided for each metadata object
 * depend on the underlying control system.  Typical fields include:
 * <i>units</i> - a string representing the physical quantity that the
 * control point represents; the <i>numeric precision</i>; the allowed
 * <i>operating range</i> and/or the thresholds which correspond to
 * <i>error</i> or <i>warning</i> conditions.
 */
@Immutable
public abstract class WicaChannelMetadata extends WicaChannelData
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance based on the supplied channel type.
    *
    * @param wicaChannelType the channel type.
    */
   public WicaChannelMetadata( WicaChannelType wicaChannelType )
   {
      super( wicaChannelType, LocalDateTime.now() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
