/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.*;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStream
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private final Logger logger = LoggerFactory.getLogger( WicaStream.class );

   private final int defaultHeartBeatFluxInterval;
   private final int defaultChannelValueUpdateFluxInterval;

   private final WicaStreamId wicaStreamId;

   private final WicaStreamProperties wicaStreamProperties;
   private final Set<WicaChannel> wicaChannels;
   private final WicaChannelMetadataMapSerializer wicaChannelMetadataMapSerializer;
   private final WicaChannelValueMapSerializer wicaChannelValueMapSerializer;
   private final WicaChannelValueMapTransformer wicaChannelValueMapTransformer;



   private Flux<ServerSentEvent<String>> combinedFlux;

   private LocalDateTime lastPublicationTime = LONG_AGO;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new stream with no special stream properties.
    *
    * @param wicaStreamId the stream's id.
    * @param wicaChannels the stream's channels.
    * @param defaultHeartBeatFluxInterval the interval between successive
    * @param defaultChannelValueUpdateFluxInterval
    */
   public WicaStream( WicaStreamId wicaStreamId,
                      Set<WicaChannel> wicaChannels,
                      int defaultHeartBeatFluxInterval,
                      int defaultChannelValueUpdateFluxInterval )
   {
      this( wicaStreamId, WicaStreamProperties.ofEmpty(), wicaChannels, defaultHeartBeatFluxInterval, defaultChannelValueUpdateFluxInterval );
   }

   public WicaStream( WicaStreamId wicaStreamId,
                      WicaStreamProperties wicaStreamProperties,
                      Set<WicaChannel> wicaChannels,
                      int defaultHeartBeatFluxInterval,
                      int defaultChannelValueUpdateFluxInterval )
   {
      // Capture and Validate all parameters
      this.wicaStreamId = wicaStreamId;
      this.wicaStreamProperties = Validate.notNull( wicaStreamProperties );
      this.wicaChannels = Validate.notNull( wicaChannels );
      this.defaultHeartBeatFluxInterval =  defaultHeartBeatFluxInterval;
      this.defaultChannelValueUpdateFluxInterval = defaultChannelValueUpdateFluxInterval;
      Validate.isTrue(defaultHeartBeatFluxInterval > 50, "The 'wica.heartbeat_flux_interval_default_in_ms' setting cannot be less than 50ms." );
      Validate.isTrue(defaultChannelValueUpdateFluxInterval > 50, "The 'wica.channel_value_flux_update_flux_interval_default_in_ms' setting cannot be less than 50ms." );

      // Build an object that can transform/filter the input data received from the channel's
      // underlying data source to something that can be sent down the wire as part of the
      // Server Sent Event (SSE) stream.
      this.wicaChannelValueMapTransformer = new WicaChannelValueMapTransformer( this );

      // Build an object that can return information about the required numeric precision
      // For serializing channel floating point values.
      final var wicaChannelValueNumericScaleSupplier = new WicaChannelDataNumericScaleSupplier( this );

      // Build an object that can return information about the required data fields for
      // serializing channels values.
      final var wicaChannelValueFieldsOfInterestSupplier = new WicaChannelDataFieldsOfInterestSupplier( this );

      // Build an object that can serialize channel metadata to a JSON String.
      this.wicaChannelValueMapSerializer = new WicaChannelValueMapSerializer( wicaChannelValueNumericScaleSupplier, false, false, wicaChannelValueFieldsOfInterestSupplier);

      // Build an object that can serialize channel values to a JSON String.
      logger.info( "Fields selected for metadata serialization are '{}'", "ALL" );
      this.wicaChannelMetadataMapSerializer = new WicaChannelMetadataMapSerializer(2, false, false, c -> Set.of() );

      // Output some diagnostic information to the log.
      logger.info( "Created new WicaStream with properties as follows: '{}'", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /*-------------------------------------------------------------------------*/
   /* 1.0 Getter Methods                                                      */
   /*-------------------------------------------------------------------------*/

   /**
    * Returns the stream's id object.
    * @return the object.
    */
   public WicaStreamId getWicaStreamId()
   {
      return wicaStreamId;
   }

   /**
    * Returns the stream's properties object.
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
    * Returns the last time the stream was published.
    *
    * @return the timestamp
    */
   public LocalDateTime getLastPublicationTime()
   {
      return lastPublicationTime;
   }

   /**
    * Returns the stream's combined Flux.
    *
    * @return the combinedFlux.
    */
   public Flux<ServerSentEvent<String>> getCombinedFluxReference()
   {
      return combinedFlux;
   }

   /**
    * Returns an object that can be used to filter the stream's values and
    * to generate the information required for serialization
    */
   public WicaChannelValueMapTransformer getWicaChannelValueMapTransformer()
   {
      return this.wicaChannelValueMapTransformer;
   }

   /**
    * Returns an object that can be used to serialize a map of channel
    * names and their associated metadata.
    *
    * @return the object.
    */
   public WicaChannelMetadataMapSerializer getWicaChannelMetadataMapSerializer()
   {
      return this.wicaChannelMetadataMapSerializer;
   }

   /**
    * Returns an object that can be used to serialize a map of channel
    * names and their associated values.
    *
    * @return the object.
    */
   public WicaChannelValueMapSerializer getWicaChannelValueMapSerializer()
   {
      return this.wicaChannelValueMapSerializer;
   }

   /**
    * Returns the stream's heartbeat interval, based on either the default value or
    * the value that was specified in the client's create stream request.
    *
    * @return the interval between successive heartbeat messages in milliseconds.
    */
   public int getHeartbeatFluxInterval()
   {
      final WicaStreamProperties streamProperties = getWicaStreamProperties();
      return streamProperties.hasProperty( "heartbeatInterval" ) ?
            Integer.parseUnsignedInt( streamProperties.getPropertyValue("heartbeatInterval" ) ) :
            defaultHeartBeatFluxInterval;
   }

   /**
    * Returns the stream's channel value update interval, based on either the
    * default value or the value that was specified in the client's create
    * stream request.
    *
    * @return the interval between successive channel value update messages
    *     in milliseconds.
    */
   public int getChannelValueUpdateFluxInterval()
   {
      final WicaStreamProperties streamProperties = getWicaStreamProperties();
      return streamProperties.hasProperty( "channelValueUpdateInterval" ) ?
            Integer.parseUnsignedInt( streamProperties.getPropertyValue("channelValueUpdateInterval" ) ) :
            defaultChannelValueUpdateFluxInterval;
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
            "  defaultHeartBeatFluxInterval=" + defaultHeartBeatFluxInterval +
            ", defaultChannelValueUpdateFluxInterval=" + defaultChannelValueUpdateFluxInterval +
            ", lastPublicationTime=" + lastPublicationTime +
            '}';
   }

   /*-------------------------------------------------------------------------*/
   /* 2.0 Mutator Methods                                                     */
   /*-------------------------------------------------------------------------*/

   /**
    * Saves the reference to the stream's combined combined Flux.
    *
    * @param flux the combinedFlux.
    */
   public void saveCombinedFluxReference( Flux<ServerSentEvent<String>> flux )
   {
      this.combinedFlux = flux;
   }

   /**
    * Sets with the current time the timestamp which indicates the last time
    * the stream was published.
    */
   public void updateLastPublicationTime()
   {
      this.lastPublicationTime = LocalDateTime.now();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
