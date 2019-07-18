/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStream
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @JsonIgnore
   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   @JsonIgnore
   private LocalDateTime lastPublicationTime = LONG_AGO;

   @JsonIgnore
   private final WicaStreamId wicaStreamId;

   @JsonProperty( "props")
   private final WicaStreamProperties wicaStreamProperties;

   @JsonProperty( "channels")
   private final Set<WicaChannel> wicaChannels;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new stream for the specified channels with the specified
    * stream properties.
    *
    * @param wicaStreamId the stream's id.
    * @param wicaStreamProperties the stream's properties.
    * @param wicaChannels the stream's channels.
    */
   private WicaStream( WicaStreamId wicaStreamId,
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

   public static Builder createBuilder()
   {
      return new Builder();
   }


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
    * @return optionally empty result.
    */
   @SuppressWarnings( "WeakerAccess" )
   public Optional<WicaChannel> getWicaChannel( String wicaChannelName )
   {
      return wicaChannels.stream().filter( c -> c.getNameAsString().equals( wicaChannelName ) ).findFirst();
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

   public String toJsonString()
   {
      try
      {
         return new ObjectMapper().writeValueAsString( this);
      }
      catch ( JsonProcessingException ex )
      {
         throw new RuntimeException( "oh dear !" + ex.getMessage(), ex );
      }
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
      private WicaStreamId wicaStreamId;
      private WicaStreamProperties wicaStreamProperties = WicaStreamProperties.createDefaultInstance();
      private List<WicaChannel> wicaChannels = new ArrayList<>();

      // Private to force use of the createBuilder factory method.
      private Builder() {}

      public Builder withId( String wicaStreamId )
      {
         this.wicaStreamId = WicaStreamId.of( wicaStreamId );
         return this;
      }

      public Builder withId( WicaStreamId wicaStreamId )
      {
         this.wicaStreamId = wicaStreamId;
         return this;
      }

      public Builder withChannels( Set<WicaChannel> wicaChannels )
      {
         this.wicaChannels.addAll( wicaChannels );
         return this;
      }

      public Builder withStreamProperties( WicaStreamProperties wicaStreamProperties )
      {
         this.wicaStreamProperties = wicaStreamProperties;
         return this;
      }

      public Builder withChannel( WicaChannel wicaChannel )
      {
         wicaChannels.add( wicaChannel );
         return this;
      }

      public Builder withChannelName( WicaChannelName wicaChannelName )
      {
         wicaChannels.add( WicaChannel.createFromNameAndProperties( wicaChannelName, wicaStreamProperties.getDefaultWicaChannelProperties() ) );
         return this;
      }

      public Builder withChannelName( String wicaChannelName )
      {
         wicaChannels.add( WicaChannel.createFromNameAndProperties( wicaChannelName, wicaStreamProperties.getDefaultWicaChannelProperties() ) );
         return this;
      }

      public Builder withChannelNameAndProperties( WicaChannelName wicaChannelName, WicaChannelProperties wicaChannelProperties  )
      {
         wicaChannels.add( WicaChannel.createFromNameAndProperties( wicaChannelName, wicaChannelProperties ) );
         return this;
      }

      public Builder withChannelNameAndProperties( String wicaChannelName, WicaChannelProperties wicaChannelProperties  )
      {
         wicaChannels.add( WicaChannel.createFromNameAndProperties( wicaChannelName, wicaChannelProperties ) );
         return this;
      }

      public WicaStream build()
      {
         final WicaStreamId wicaStreamId = this.wicaStreamId == null ? WicaStreamId.createNext() : this.wicaStreamId;
         return new WicaStream( wicaStreamId, wicaStreamProperties, new HashSet<>( wicaChannels ) );
      }

   }
}
