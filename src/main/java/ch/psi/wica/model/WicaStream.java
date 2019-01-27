/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.WicaObjectToJsonSerializer;
import ch.psi.wica.services.stream.WicaStreamMapper;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStream implements WicaStreamMapper
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
   private final WicaObjectToJsonSerializer serializer;
   private final Map<WicaChannelName,WicaChannel> channelMap;

   private Flux<ServerSentEvent<String>> flux;

   private LocalDateTime lastPublicationTime = LONG_AGO;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

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
      this.wicaStreamId = wicaStreamId;
      this.wicaStreamProperties = Validate.notNull( wicaStreamProperties );
      this.wicaChannels = Validate.notNull( wicaChannels );
      this.channelMap = Collections.unmodifiableMap( wicaChannels.stream().collect(Collectors.toConcurrentMap( WicaChannel::getName, c -> c ) ) );

      Validate.isTrue(defaultHeartBeatFluxInterval > 50, "The 'wica.heartbeat_flux_interval_default_in_ms' setting cannot be less than 50ms." );
      Validate.isTrue(defaultChannelValueUpdateFluxInterval > 50, "The 'wica.channel_value_flux_update_flux_interval_default_in_ms' setting cannot be less than 50ms." );

      this.defaultHeartBeatFluxInterval = defaultHeartBeatFluxInterval;
      this.defaultChannelValueUpdateFluxInterval = defaultChannelValueUpdateFluxInterval;

      boolean isPlotStream = wicaStreamProperties.hasProperty( "plotStream" ) && wicaStreamProperties.getPropertyValue( "plotStream" ).equals( "true" );
      logger.info( "Is this stream a plot stream: '{}'", isPlotStream );
      final Set<String> fieldSelectors = isPlotStream ? Set.of( "val", "sevr", "ts" ) : Set.of( "val", "sevr" );
      logger.info( "Fields selected for value serialization are '{}'", fieldSelectors );
      this.serializer = new WicaObjectToJsonSerializer( fieldSelectors, 2 );

      logger.info( "Created new WicaStream with properties as follows: '{}'", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaStreamId getWicaStreamId()
   {
      return wicaStreamId;
   }

   public WicaStreamProperties getWicaStreamProperties()
   {
      return wicaStreamProperties;
   }

   public Set<WicaChannel> getWicaChannels()
   {
      return wicaChannels;
   }

   public LocalDateTime getLastPublicationTime()
   {
      return lastPublicationTime;
   }

   public void setLastPublicationTime()
   {
      this.lastPublicationTime = LocalDateTime.now();
   }

   public Flux<ServerSentEvent<String>> getFlux()
   {
      return flux;
   }

   public void setFlux( Flux<ServerSentEvent<String>> flux )
   {
      this.flux = flux;
   }

   public WicaObjectToJsonSerializer getSerializer()
   {
      return this.serializer;
   }

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

   public int getHeartbeatFluxInterval()
   {
      final WicaStreamProperties streamProperties = getWicaStreamProperties();
      return streamProperties.hasProperty( "heartbeatInterval" ) ?
            Integer.parseUnsignedInt( streamProperties.getPropertyValue("heartbeatInterval" ) ) :
            defaultHeartBeatFluxInterval;
   }

   public int getChannelValueUpdateFluxInterval()
   {
      final WicaStreamProperties streamProperties = getWicaStreamProperties();
      return streamProperties.hasProperty( "channelValueUpdateInterval" ) ?
            Integer.parseUnsignedInt( streamProperties.getPropertyValue("channelValueUpdateInterval" ) ) :
            defaultChannelValueUpdateFluxInterval;
   }

   public Map<WicaChannelName,List<WicaChannelValue>> map( Map<WicaChannelName,List<WicaChannelValue>> inputMap )
   {
      Validate.isTrue(inputMap.keySet().stream().allMatch( channelMap::containsKey ), "One or more channels in the inputMap were unknown" );

      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
      inputMap.keySet().forEach( c -> {
         final List<WicaChannelValue> inputList = inputMap.get( c );
         final WicaChannel wicaChannel = this.channelMap.get( c );
         final List<WicaChannelValue> outputList = wicaChannel.map( inputList );
         if ( outputList.size() > 0 ) {
            outputMap.put( c, outputList );
         }
      } );

      return outputMap;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
