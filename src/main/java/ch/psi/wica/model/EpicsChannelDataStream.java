/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class EpicsChannelDataStream
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final StreamId streamId;
   private final Set<EpicsChannelName> channels;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new stash instance.
    *
    * @param channels the channels of interest
    */
   public EpicsChannelDataStream( Set<EpicsChannelName> channels )
   {
      this.channels = Validate.notNull ( channels );
      this.streamId = StreamId.createNext();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public StreamId getStreamId()
   {
      return streamId;
   }
   public Set<EpicsChannelName> getChannels()
   {
      return channels;
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
