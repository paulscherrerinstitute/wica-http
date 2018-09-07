/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class EpicsChannelValueStream
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
   public EpicsChannelValueStream( Set<EpicsChannelName> channels )
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
