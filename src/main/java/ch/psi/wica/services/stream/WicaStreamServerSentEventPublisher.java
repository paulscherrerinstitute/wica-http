/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelMetadataMapSerializer;
import ch.psi.wica.infrastructure.channel.WicaChannelValueMapSerializer;
import ch.psi.wica.infrastructure.stream.WicaServerSentEventBuilder;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.model.stream.WicaStreamProperties;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@ThreadSafe
public class WicaStreamServerSentEventPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamServerSentEventPublisher.class );
   private final WicaStream wicaStream;
   private final WicaStreamId wicaStreamId;
   private final WicaStreamProperties wicaStreamProperties;
   private final WicaStreamDataCollectorService wicaStreamDataCollectorService;
   private final WicaChannelMetadataMapSerializer wicaChannelMetadataMapSerializer;
   private final WicaChannelValueMapSerializer wicaChannelValueMapSerializer;
   private final Flux<ServerSentEvent<String>> combinedFlux;
   private final AtomicBoolean shutdown = new AtomicBoolean( false );


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance that will work with the properties of the
    * specified stream using the data from the specified stream data supplier.
    *
    * @param wicaStream the stream ID.
    *
    * @param wicaStreamDataCollectorService reference to the service which
    *     provides the data for this publisher.
    */
   WicaStreamServerSentEventPublisher( WicaStream wicaStream, WicaStreamDataCollectorService wicaStreamDataCollectorService )
   {
      this.wicaStream = Validate.notNull( wicaStream );
      this.wicaStreamDataCollectorService = Validate.notNull(wicaStreamDataCollectorService);

      this.wicaStreamId = Validate.notNull( wicaStream.getWicaStreamId() );
      this.wicaStreamProperties = Validate.notNull( wicaStream.getWicaStreamProperties() );

      this.wicaChannelMetadataMapSerializer = new WicaChannelMetadataMapSerializer(false );

      this.wicaChannelValueMapSerializer = new WicaChannelValueMapSerializer( false );

      this.combinedFlux = createCombinedFlux();
      shutdown.set( false );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns this publisher's stream.
    *
    * @return the stream
    */
   public WicaStream getStream()
   {
      return this.wicaStream;
   }

   /**
    * Returns a reference to this publisher's combined flux.
    *
    * @return the flux.
    *
    * @throws IllegalStateException if the flux has been shutdown.
    */
   Flux<ServerSentEvent<String>> getFlux()
   {
      if ( shutdown.get() )
      {
         logger.error( "Programming error: unexpected state - attempt to get flux after publisher has been shut down." );
         throw new IllegalStateException( "Call to getFlux(), but the publisher has already been shut down." );
      }
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
      if ( shutdown.getAndSet( true ) )
      {
         logger.error( "Programming error: unexpected state - attempt to shutdown the same publisher twice." );
         throw new IllegalStateException( "Call to shutdown(), but the publisher has already been shut down." );
      }
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
               return WicaServerSentEventBuilder.EV_WICA_SERVER_HEARTBEAT.build(wicaStreamId, jsonHeartbeatString );
            })
            .doOnComplete( () -> logger.warn( "heartbeat flux with id: '{}' completed.", wicaStreamId   ))
            .doOnCancel( () -> logger.warn( "heartbeat flux with id: '{}' was cancelled.", wicaStreamId  ))
            .doOnError( (e) -> logger.warn( "heartbeat flux with id: '{}' had error.", wicaStreamId, e ));
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
               logger.trace( "channel-metadata flux with id: '{}' is publishing new SSE...", wicaStreamId  );
               final Map<WicaChannel, WicaChannelMetadata> channelMetadataMap = wicaStreamDataCollectorService.getMetadataMap(wicaStream );
               final String jsonMetadataString = wicaChannelMetadataMapSerializer.serialize( channelMetadataMap );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_METADATA.build ( wicaStreamId, jsonMetadataString );
            } )
            .doOnComplete( () -> logger.warn( "channel-metadata flux with id: '{}' completed.", wicaStreamId  ))
            .doOnCancel( () -> logger.warn( "channel-metadata flux with id: '{}' was cancelled.", wicaStreamId  ) )
            .doOnError( (e) -> logger.warn( "heartbeat flux with id: '{}' had error.", wicaStreamId, e ));
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
               logger.trace("channel-value flux with id: '{}' is publishing new SSE...", wicaStreamId );
               final Map<WicaChannel, List<WicaChannelValue>> map = wicaStreamDataCollectorService.getInitialValueMap( wicaStream );
               final String jsonServerSentEventString = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_VALUES_INITIAL.build( wicaStreamId, jsonServerSentEventString );
            } )
            .doOnComplete( () -> logger.warn( "channel-initial-values flux with id: '{}' completed.", wicaStreamId ))
            .doOnCancel( () -> logger.warn("channel-initial-values flux with id: '{}' was cancelled.", wicaStreamId))
            .doOnError( (e) -> logger.warn( "channel-initial-values flux with id: '{}' had error.", wicaStreamId, e ));
      //.log();
   }

   /**
    * Creates the CHANNEL CHANGED VALUE FLUX.
    *
    * The purpose of this flux is to publish the last received values of any channels
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
               logger.trace( "channel-value-change flux with id: '{}' is publishing new SSE...", wicaStreamId );
               final Map<WicaChannel, List<WicaChannelValue>> map = wicaStreamDataCollectorService.getChangedValueMap(wicaStream );
               final var jsonServerSentEventString = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_CHANGED_VALUES.build( wicaStreamId, jsonServerSentEventString );
            } )
            .doOnComplete( () -> logger.warn( "channel-value-change flux with id: '{}' completed.", wicaStreamId ))
            .doOnCancel( () -> logger.warn( "channel-value-change flux with id: '{}' was cancelled.", wicaStreamId ) )
            .doOnError( (e) -> logger.warn( "channel-value-change flux with id: '{}' had error.", wicaStreamId, e ));
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
               logger.trace("channel-value-poll flux with id: '{}' is publishing new SSE...", wicaStreamId );
               var map = wicaStreamDataCollectorService.getPolledValueMap(wicaStream );
               final String jsonServerSentEventString = wicaChannelValueMapSerializer.serialize( map );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_POLLED_VALUES.build(wicaStreamId, jsonServerSentEventString );
            } )
            .doOnComplete( () -> logger.warn( "channel-value-poll flux with id: '{}' completed.", wicaStreamId ))
            .doOnCancel( () -> logger.warn("channel-value-poll flux with id: '{}' was cancelled.", wicaStreamId ))
            .doOnError( (e) -> logger.warn( "channel-value-poll flux with id: '{}' had error.", wicaStreamId, e ));
      //.log();
   }

   /**
    * Creates the COMBINED FLUX.
    *
    * The purpose of this flux is to merge together all the individual fluxes in
    * this publisher, returning a reference to a flux which can be cancelled
    * by a call to the shutdown method.
    */
   private Flux<ServerSentEvent<String>> createCombinedFlux()
   {
      final Flux<ServerSentEvent<String>> heartbeatFlux = createHeartbeatFlux();
      final Flux<ServerSentEvent<String>> metadataFlux = createMetadataFlux();
      final Flux<ServerSentEvent<String>> initialValueFlux = createChannelInitialValueFlux();
      final Flux<ServerSentEvent<String>> changedValueFlux = createChangedValueFlux();
      final Flux<ServerSentEvent<String>> polledValueFlux = createPolledValueFlux();

      // Create a single Flux which merges all of the above.
      return heartbeatFlux.mergeWith( metadataFlux )
            .mergeWith( initialValueFlux )
            .mergeWith( changedValueFlux )
            .mergeWith( polledValueFlux )
            .doOnComplete( () -> logger.warn( "combined with id: '{}' flux completed.", wicaStreamId ))
            .doOnCancel( () -> {
               logger.warn( "combined flux with id '{}' was cancelled.", wicaStreamId );
            } )
            .doOnError( (e) -> logger.warn( "combined flux with id: '{}' had error: '{}'", wicaStreamId, e ) )
            .takeUntil( (sse) -> {
               final boolean shutdownRequest = shutdown.get();
               if ( shutdownRequest)
               {
                  logger.warn( "combined flux with id: '{}' discovered shutdown request when delivering SSE: '{}'",  wicaStreamId, sse );
               }
               return shutdownRequest;
            } );
      //.log();
   }

/*- Nested Classes -----------------------------------------------------------*/

}
