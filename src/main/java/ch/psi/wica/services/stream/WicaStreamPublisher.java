/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.*;
import ch.psi.wica.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class WicaStreamPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamPublisher.class );

   private final WicaStreamId wicaStreamId;
   private final WicaStreamProperties wicaStreamProperties;
   private final WicaStreamDataSupplier wicaStreamDataSupplier;
   private final WicaChannelMetadataMapSerializer wicaChannelMetadataMapSerializer;
   private final WicaChannelValueMapSerializer wicaChannelValueMapSerializer;

   private Flux<ServerSentEvent<String>> combinedFlux;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaStreamPublisher( WicaStream wicaStream, WicaStreamDataSupplier wicaStreamDataSupplier )
   {
      this.wicaStreamId = wicaStream.getWicaStreamId();
      this.wicaStreamProperties = wicaStream.getWicaStreamProperties();
      this.wicaChannelMetadataMapSerializer = new WicaChannelMetadataMapSerializer( new WicaChannelDataFieldsOfInterestSupplier( wicaStream ),
                                                                                    new WicaChannelDataNumericScaleSupplier( wicaStream ), false );

      this.wicaChannelValueMapSerializer = new WicaChannelValueMapSerializer( new WicaChannelDataFieldsOfInterestSupplier( wicaStream ),
                                                                              new WicaChannelDataNumericScaleSupplier( wicaStream ), false );

      this.wicaStreamDataSupplier = wicaStreamDataSupplier;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public void activate()
   {
      final Flux<ServerSentEvent<String>> heartbeatFlux = createHeartbeatFlux();
      final Flux<ServerSentEvent<String>> channelMetadataFlux = createChannelMetadataFlux();
      final Flux<ServerSentEvent<String>> channelValueFlux = createChannelValueFlux();
      final Flux<ServerSentEvent<String>> channelValueUpdateFlux = createChannelValueUpdateFlux();

      // Create a single Flux which merges all of the above.
      combinedFlux = heartbeatFlux.mergeWith( channelMetadataFlux )
                                  .mergeWith( channelValueFlux )
                                  .mergeWith( channelValueUpdateFlux )
                                  .doOnComplete( () -> logger.warn( "eventStreamFlux flux completed" ))
                                  .doOnCancel( () -> {
                                      logger.warn( "eventStreamFlux was cancelled" );
                                      handleErrors( wicaStreamId );
                                  } );
                                  //.log();
   }


   Flux<ServerSentEvent<String>> getFlux()
   {
      return combinedFlux;
   }

   public void shutdown()
   {
      // TODO - implement
   }


/*- Private methods ----------------------------------------------------------*/

   /**
    * Creates the HEARTBEAT FLUX.
    *
    * The purpose of this flux is to periodically tell remote clients that the stream is
    * still alive. If remote clients do not receive heartbeat events within the expected
    * time intervals then they will conclude that the event stream is no longer active.
    * This may then lead to closing the event stream on the client side and sending a
    * request to the server to recreate the stream.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createHeartbeatFlux()
   {
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getHeartbeatFluxInterval() ) )
            .map(l -> {
               logger.trace("heartbeat flux is publishing new SSE...");
               final String jsonHeartbeatString = LocalDateTime.now().toString();
               return WicaServerSentEventBuilder.EV_WICA_SERVER_HEARTBEAT.build( wicaStreamId, jsonHeartbeatString );
            })
         //   .doOnComplete( () -> logger.warn( "heartbeat flux completed" ))
            .doOnCancel(() -> logger.warn("heartbeat flux was cancelled"));
      //.log();
   }

   /**
    * Creates the CHANNEL METADATA FLUX.
    *
    * The purpose of this flux is to publish additional information about the nature of the
    * underlying EPICS channel. This may include the channel's type and where relevant the
    * channel's display, alarm and operator limits.
    *
    * This flux runs just once and delivers its payload on inital connection to the stream.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChannelMetadataFlux()
   {
      return Flux.range( 1, 1 )
            .map( l -> {
               logger.trace( "channel-metadata flux is publishing new SSE..." );
               final Map<WicaChannelName, WicaChannelMetadata> channelMetadataMap = wicaStreamDataSupplier.getMetadataMap();
               final String jsonMetadataString = wicaChannelMetadataMapSerializer.serialize( channelMetadataMap );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_METADATA.build ( wicaStreamId, jsonMetadataString );
            } )
          //  .doOnComplete( () -> logger.warn( "channel-metadata flux completed" ))
            .doOnCancel( () -> logger.warn( "channel-metadata flux was cancelled" ) );
      //.log();
   }

   /**
    *  Creates the CHANNEL VALUE UPDATE FLUX.
    *
    * The purpose of this flux is to publish the last received values for any channels
    * which have received monitor notifications since the last update.
    *
    * This flux runs with a periodicity defined by the channelValueUpdateFluxInterval.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChannelValueUpdateFlux()
   {
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getChannelValueUpdateFluxInterval() ) )
            .map( l -> {
               logger.trace( "channel-value-update flux is publishing new SSE..." );
               final var map = wicaStreamDataSupplier.getValueMapLatest();
               final var jsonSseStr = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_VALUE_CHANGES.build(  wicaStreamId, jsonSseStr );
            } )
            .doOnComplete( () -> logger.warn( "channel-value-update flux completed" ))
            .doOnCancel( () -> logger.warn( "channel-value-update flux was cancelled" ) );
      //.log();
   }

   /**
    * Create the CHANNEL VALUE FLUX.
    *
    * The purpose of this flux is to publish the last received value for ALL channels
    * in the stream.
    *
    * This flux runs with a  periodicity defined by the channelValueFluxInterval.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChannelValueFlux()
   {
      return Flux.range(1, 1)
            .map(l -> {
               logger.trace("channel-value flux is publishing new SSE...");
               var map = wicaStreamDataSupplier.getValueMapAll();
               final String jsonSseStr = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_VALUE_ALLDATA.build( wicaStreamId, jsonSseStr );
            })
            .doOnComplete( () -> logger.warn( "channel-value flux completed" ))
            .doOnCancel( () -> logger.warn("channel-value flux was cancelled"));
      //.log();
   }

   private void handleErrors( WicaStreamId id )
   {
      logger.info( "Some error occurred on monitor stream with Id: '{}' ", id );
      logger.info( "Probably the client navigated away from the webpage and the eventsource was closed by the browser !! " );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
