/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents a collection of unique wica channels that are grouped together
 * for the purpose of real-time monitoring.
 */
@Immutable
public class WicaStream
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaStreamId wicaStreamId;
   private final WicaStreamProperties wicaStreamProperties;
   private final Set<WicaChannel> wicaChannels;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStream()
   {
      this.wicaStreamId = null;
      this.wicaStreamProperties = null;
      this.wicaChannels = null;
   }

   public WicaStream( WicaStreamProperties wicaStreamProperties, Set<WicaChannel> wicaChannels )
   {
      this.wicaStreamId = WicaStreamId.createNext();
      this.wicaStreamProperties = wicaStreamProperties;
      this.wicaChannels = wicaChannels;
   }

   /**
    * Constructs a new stream for the specified channels with the specified
    * stream properties.
    *
    * @param wicaStreamId the stream's id.
    * @param wicaStreamProperties the stream's properties.
    * @param wicaChannels the stream's channels.
    */
   // WARNING: Signature here must match EXACTLY with that in WicaStreamDeserializationMixin.
   public WicaStream( WicaStreamId wicaStreamId, WicaStreamProperties wicaStreamProperties, Set<WicaChannel> wicaChannels )
   {
      // Capture and Validate all parameters
      this.wicaStreamId = Validate.notNull( wicaStreamId );
      this.wicaStreamProperties = Validate.notNull( wicaStreamProperties );
      this.wicaChannels = Validate.notNull( wicaChannels );

      // Output some diagnostic information to the log.
      final Logger logger = LoggerFactory.getLogger(WicaStream.class);
      logger.trace("Created new WicaStream with properties as follows: '{}'", this );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the stream's id.
    * @return the object.
    */
   public WicaStreamId getWicaStreamId()
   {
      return wicaStreamId;
   }

   /**
    * Returns the stream's properties.
    * @return the object.
    */
   public WicaStreamProperties getWicaStreamProperties()
   {
      return wicaStreamProperties;
   }

   /**
    * Returns the stream's channels.
    * @return the object.
    */
   public Set<WicaChannel> getWicaChannels()
   {
      return wicaChannels;
   }

   /**
    * Returns the channel with the specified name (if present).
    *
    * @param wicaChannelName the channel name.
    * @return optionally empty result.
    */
   public Optional<WicaChannel> getWicaChannel( String wicaChannelName )
   {
      return wicaChannels.stream().filter( c -> c.getNameAsString().equals( wicaChannelName ) ).findFirst();
   }

   @Override
   public String toString()
   {
      return "WicaStream{" +
            "wicaStreamId=" + wicaStreamId +
            ", wicaStreamProperties=" + wicaStreamProperties +
            ", wicaChannels=" + wicaChannels +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
