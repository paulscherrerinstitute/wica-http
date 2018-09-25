/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaStream
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaStreamId wicaStreamId;
   private final Set<WicaChannelName> channels;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new stash instance.
    *
    * @param channels the channels of interest
    */
   public WicaStream( Set<WicaChannelName> channels )
   {
      this.channels = Validate.notNull ( channels );
      this.wicaStreamId = WicaStreamId.createNext();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaStreamId getWicaStreamId()
   {
      return wicaStreamId;
   }
   public Set<WicaChannelName> getChannels()
   {
      return channels;
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
