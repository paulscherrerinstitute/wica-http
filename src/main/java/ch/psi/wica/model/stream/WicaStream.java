/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStream
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );
   private LocalDateTime lastPublicationTime = LONG_AGO;

   private final WicaStreamId wicaStreamId;
   private final WicaStreamProperties wicaStreamProperties;
   private final Set<WicaChannel> wicaChannels;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new stream for the specified channels with the default
    * stream properties.
    *
    * @param wicaStreamId the stream's id.
    * @param wicaChannels the stream's channels.
    */
   public WicaStream( WicaStreamId wicaStreamId, Set<WicaChannel> wicaChannels )
   {
      this( wicaStreamId, WicaStreamProperties.createDefaultInstance(), wicaChannels );
   }

   /**
    * Constructs a new stream for the specified channels with the specified
    * stream properties.
    *
    * @param wicaStreamId the stream's id.
    * @param wicaStreamProperties the stream's properties.
    * @param wicaChannels the stream's channels.
    */
   public WicaStream( WicaStreamId wicaStreamId,
                      WicaStreamProperties wicaStreamProperties,
                      Set<WicaChannel> wicaChannels )
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
    * Returns the stream's string representation.
    *
    * @return the representation
    */
   @Override
   public String toString()
   {
      return "WicaStream{" +
             "wicaStreamId=" + wicaStreamId +
             ", wicaStreamProperties=" + getWicaStreamProperties() +
             ", wicaChannels=" + getWicaChannels() +
      "}'";
   }

   /**
    * Returns the timestamp for the last time the values from the stream were
    * published.
    *
    * @return the timestamp.
    */
   public LocalDateTime getLastPublicationTime()
   {
      return lastPublicationTime;
   }

   /**
    * Updates the the timestamp which indicates the last time this stream
    * instance was published.
    */
   public void updateLastPublicationTime()
   {
      lastPublicationTime = LocalDateTime.now();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   public static class Builder
   {
      private WicaStreamId wicaStreamId = WicaStreamId.createNext();
      private WicaStreamProperties wicaStreamProperties = WicaStreamProperties.createDefaultInstance();
      private List<WicaChannel> wicaChannels = new ArrayList<>();

      public Builder withId( String wicaStreamId )
      {
         this.wicaStreamId = WicaStreamId.of( wicaStreamId );
         return this;
      }

      public Builder withStreamProperties( WicaStreamProperties wicaStreamProperties )
      {
         this.wicaStreamProperties = wicaStreamProperties;
         return this;
      }

      public Builder withChannelNamed( String wicaChannelName )
      {
         wicaChannels.add( WicaChannel.createFromName( wicaChannelName ) );
         return this;
      }

      public Builder andChannelProperties( WicaChannelProperties wicaChannelProperties )
      {
         WicaChannel wicaChannel = wicaChannels.remove( wicaChannels.size() - 1 );
         wicaChannels.add( WicaChannel.createFromNameAndProperties( wicaChannel.getName(), wicaChannelProperties ) );
         return this;
      }

      public WicaStream build()
      {
         return new WicaStream( wicaStreamId, wicaStreamProperties, new HashSet<>( wicaChannels ) );
      }

   }
}
