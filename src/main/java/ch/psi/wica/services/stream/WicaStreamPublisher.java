/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.*;
import ch.psi.wica.model.*;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class WicaStreamPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamPublisher.class );

   private final WicaStream wicaStream;
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
      this.wicaStream = wicaStream;
      this.wicaStreamId = wicaStream.getWicaStreamId();
      this.wicaStreamProperties = wicaStream.getWicaStreamProperties();
      this.wicaChannelMetadataMapSerializer = new WicaChannelMetadataMapSerializer( c -> Set.of(),
                                                                                    new WicaChannelDataNumericScaleSupplier( wicaStream ),
                                                                                   false );

      this.wicaChannelValueMapSerializer = new WicaChannelValueMapSerializer( new WicaChannelDataFieldsOfInterestSupplier( wicaStream ),
                                                                              new WicaChannelDataNumericScaleSupplier( wicaStream ),
                                                                             false );
      this.wicaStreamDataSupplier = wicaStreamDataSupplier;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the stream associated with this publisher.
    *
    * @return the stream
    */
   public WicaStream getStream()
   {
      return this.wicaStream;
   }

   /**
    * Activates this flux.
    *
    * This method should only be called once, subsequent attempts to activate
    * the flux will result in an IllegalStateException.
    *
    * @throws IllegalStateException if the flux has already been shutdown.
    */
   public void activate()
   {
      Validate.validState( combinedFlux == null, "the flux is already activated" );

      final Flux<ServerSentEvent<String>> heartbeatFlux = createHeartbeatFlux();
      final Flux<ServerSentEvent<String>> metadataFlux = createMetadataFlux();
      final Flux<ServerSentEvent<String>> initialValueFlux = createChannelInitialValueFlux();
      final Flux<ServerSentEvent<String>> changedValueFlux = createChangedValueFlux();
      final Flux<ServerSentEvent<String>> polledValueFlux = createPolledValueFlux();

      // Create a single Flux which merges all of the above.
      combinedFlux = heartbeatFlux.mergeWith( metadataFlux )
                                  .mergeWith( initialValueFlux )
                                  .mergeWith( changedValueFlux )
                                  .mergeWith( polledValueFlux )
                                  .doOnComplete( () -> logger.warn( "Wica combinedflux completed" ))
                                  .doOnCancel( () -> {
                                      logger.warn( "eventStreamFlux was cancelled" );
                                      handleErrors( wicaStreamId );
                                  } );
                                  //.log();
   }


   /**
    * Returns the combined flux.
    *
    * @return the flux
    * @throws IllegalStateException if the flux is not in an active state.
    */
   Flux<ServerSentEvent<String>> getFlux()
   {
      Validate.validState(combinedFlux != null, "The flux was not in a valid state" );
      return combinedFlux;
   }

   /**
    * Shuts down this publisher instance.
    *
    * This method should only be called once, subsequent attempts to shutdown
    * the flux will result in an IllegalStateException.
    *
    * @throws IllegalStateException if the flux has already been shutdown.
    */
   public void shutdown()
   {
      Validate.validState( combinedFlux != null,"The flux has already been shutdown" );

      final Disposable d = combinedFlux.subscribe( (c) -> {} );
      d.dispose();
      combinedFlux = null;
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
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getHeartbeatFluxIntervalInMillis() ) )
            .map(l -> {
               logger.info("heartbeat flux is publishing new SSE...");
               final String jsonHeartbeatString = LocalDateTime.now().toString();
               return WicaServerSentEventBuilder.EV_WICA_SERVER_HEARTBEAT.build( wicaStreamId, jsonHeartbeatString );
            })
            .doOnComplete( () -> logger.warn( "heartbeat flux completed" ))
            .doOnCancel( () -> logger.warn( "heartbeat flux was cancelled"))
            .doOnError( (e) -> logger.warn( "heartbeat flux had error {}", e ));
      //.log();
   }

   /**
    * Creates the CHANNEL METADATA FLUX.
    *
    * The purpose of this flux is to publish additional information about the nature of the
    * underlying EPICS channel. This may include the channel's type and where relevant the
    * channel's display, alarm and operator limits.
    *
    * This flux runs just once and delivers its payload on initial connection to the stream.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createMetadataFlux()
   {
      return Flux.range( 1, 1 )
            .map( l -> {
               logger.trace( "channel-metadata flux is publishing new SSE..." );
               final Map<WicaChannelName, WicaChannelMetadata> channelMetadataMap = wicaStreamDataSupplier.getMetadataMap( wicaStream );
               final String jsonMetadataString = wicaChannelMetadataMapSerializer.serialize( channelMetadataMap );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_METADATA.build ( wicaStreamId, jsonMetadataString );
            } )
            .doOnComplete( () -> logger.warn( "channel-metadata flux completed" ))
            .doOnCancel( () -> logger.warn( "channel-metadata flux was cancelled" ) )
            .doOnError( (e) -> logger.warn( "heartbeat flux had error {}", e ));
      //.log();
   }

   /**
    * Create the CHANNEL INITIAL VALUE FLUX.
    *
    * The purpose of this flux is to publish the last received value for ALL channels
    * in the stream.
    *
    * This flux runs just once and delivers its payload on initial connection to the stream.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChannelInitialValueFlux()
   {
      return Flux.range(1, 1)
            .map(l -> {
               logger.trace("channel-value flux is publishing new SSE...");
               var map = wicaStreamDataSupplier.getNotifiedValues( wicaStream );
               final String jsonServerSentEventString = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_VALUES_INITIAL.build( wicaStreamId, jsonServerSentEventString );
            } )
            .doOnComplete( () -> logger.warn( "channel-initial-values flux completed" ))
            .doOnCancel( () -> logger.warn("channel-initial-values flux was cancelled"))
            .doOnError( (e) -> logger.warn( "channel-initial-values flux had error {}", e ));
      //.log();
   }

   /**
    * Creates the CHANNEL CHANGED VALUE FLUX.
    *
    * The purpose of this flux is to publish the last received values for any channels
    * which have received monitor notifications since the last update.
    *
    * This flux runs with a periodicity defined by the channelValueChangeFluxReportingInterval.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChangedValueFlux()
   {
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getChangedValueFluxIntervalInMillis() ) )
            .map( l -> {
               logger.trace( "channel-value-change flux is publishing new SSE..." );
               final var map = wicaStreamDataSupplier.getNotifiedValueChanges( wicaStream );
               final var jsonServerSentEventString = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_CHANGED_VALUES.build( wicaStreamId, jsonServerSentEventString );
            } )
            .doOnComplete( () -> logger.warn( "channel-value-change flux completed" ))
            .doOnCancel( () -> logger.warn( "channel-value-change flux was cancelled" ) )
            .doOnError( (e) -> logger.warn( "channel-value-change flux had error {}", e ));
      //.log();
   }

   /**
    * Create the CHANNEL POLLED VALUES FLUX.
    *
    * The purpose of this flux is to publish the last received value for ALL channels
    * in the stream.
    *
    * This flux runs just once and delivers its payload on initial connection to the stream.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createPolledValueFlux()
   {
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getPolledValueFluxIntervalInMillis() ) )
            .map(l -> {
               logger.trace("channel-value-poll flux is publishing new SSE...");
               var map = wicaStreamDataSupplier.getPolledValues( wicaStream );
               final String jsonServerSentEventString = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_POLLED_VALUES.build(wicaStreamId, jsonServerSentEventString );
            } )
            .doOnComplete( () -> logger.warn( "channel-value-poll flux completed" ))
            .doOnCancel( () -> logger.warn("channel-value-poll flux was cancelled"))
            .doOnError( (e) -> logger.warn( "channel-value-poll flux had error {}", e ));
      //.log();
   }
   

   private void handleErrors( WicaStreamId id )
   {
      logger.info( "Some error occurred on monitor stream with Id: '{}' ", id );
      logger.info( "Probably the client navigated away from the webpage and the eventsource was closed by the browser !! " );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
