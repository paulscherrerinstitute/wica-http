/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.stream.WicaStreamServerSentEventBuilder;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.model.stream.WicaStreamProperties;
import ch.psi.wica.services.channel.WicaChannelMetadataMapSerializerService;
import ch.psi.wica.services.channel.WicaChannelValueMapSerializerService;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ThreadSafe
public class WicaStreamServerSentEventPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamServerSentEventPublisher.class );
   private final WicaStream wicaStream;
   private final WicaStreamId wicaStreamId;
   private final WicaStreamProperties wicaStreamProperties;

   private final WicaStreamMetadataCollectorService wicaStreamMetadataCollectorService;
   private final WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService;
   private final WicaStreamPolledValueCollectorService wicaStreamPolledValueCollectorService;

   private final WicaChannelMetadataMapSerializerService wicaChannelMetadataMapSerializerService;
   private final WicaChannelValueMapSerializerService wicaChannelValueMapSerializerService;
   private final AtomicBoolean shutdown = new AtomicBoolean( false );


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaStreamServerSentEventPublisher( WicaStream wicaStream,
                                       WicaStreamMetadataCollectorService wicaStreamMetadataCollectorService,
                                       WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService,
                                       WicaStreamPolledValueCollectorService wicaStreamPolledValueCollectorService,
                                       WicaChannelMetadataMapSerializerService wicaChannelMetadataMapSerializerService,
                                       WicaChannelValueMapSerializerService wicaChannelValueMapSerializerService )
   {
      this.wicaStream = Validate.notNull( wicaStream );
      this.wicaStreamMetadataCollectorService = Validate.notNull( wicaStreamMetadataCollectorService );
      this.wicaStreamMonitoredValueCollectorService = Validate.notNull( wicaStreamMonitoredValueCollectorService );
      this.wicaStreamPolledValueCollectorService = Validate.notNull( wicaStreamPolledValueCollectorService );
      this.wicaChannelMetadataMapSerializerService = Validate.notNull(wicaChannelMetadataMapSerializerService);
      this.wicaChannelValueMapSerializerService = Validate.notNull(wicaChannelValueMapSerializerService);

      this.wicaStreamId = Validate.notNull( wicaStream.getWicaStreamId() );
      this.wicaStreamProperties = Validate.notNull( wicaStream.getWicaStreamProperties() );

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
      return createCombinedFlux();
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
    * time intervals then they may typically conclude that the event stream is no longer
    * active. This may then lead them to close the event stream and to send new requests
    * to the server to recreate the stream.
    *
    * This flux runs periodically at a rate determined by the properties of the stream.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createHeartbeatFlux()
   {
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getHeartbeatFluxIntervalInMillis() ) )
            .onBackpressureBuffer()
            .map(l -> {
               logger.trace("heartbeat flux is publishing new SSE...");
               final String jsonHeartbeatString = LocalDateTime.now().toString();
               return WicaStreamServerSentEventBuilder.EV_WICA_SERVER_HEARTBEAT.build( wicaStreamId, jsonHeartbeatString );
            })
            .doOnComplete( () -> logger.warn( "heartbeat flux with id: '{}' completed.", wicaStreamId   ))
            .doOnCancel( () -> logger.warn( "heartbeat flux with id: '{}' was cancelled.", wicaStreamId  ))
            .doOnError( (e) -> logger.warn( "heartbeat flux with id: '{}' had error.", wicaStreamId, e ));
      //.log();
   }

   /**
    * Creates the CHANNEL METADATA FLUX.
    *
    * The purpose of this flux is to publish extra information (typically slow
    * changing or fixed) about the nature of the  underlying Wica Channel. This
    * may include the channel's type and where relevant the channel's display,
    * alarm and operator limits.
    *
    * This flux runs periodically at a rate determined by the properties of the stream.
    *
    * New subscribers to the flux receive firstly a Server-Sent-Event (SSE) message
    * containing the latest received metadata for all channels, then subsequent SSE messages
    * will be sent out periodically and will contain only only information for those channels
    * which have received new metadata information since the previous message.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createMetadataFlux()
   {
      final AtomicReference<LocalDateTime> lastUpdateTime = new AtomicReference<>( LocalDateTime.MIN );
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getMetadataFluxIntervalInMillis() ) )
         .onBackpressureBuffer()
         .map( l -> {
            logger.trace("channel-metadata flux with id: '{}' is publishing new SSE...", wicaStreamId);
            return wicaStreamMetadataCollectorService.get( wicaStream, lastUpdateTime.getAndSet( LocalDateTime.now()) );
         } )
         .filter( m -> m.keySet().size() > 0 )
         .map( map -> {
               final String jsonMetadataString = wicaChannelMetadataMapSerializerService.serialize ( map );
               return WicaStreamServerSentEventBuilder.EV_WICA_CHANNEL_METADATA.build( wicaStreamId, jsonMetadataString );
         } )
         .doOnComplete( () -> logger.warn( "channel-metadata flux with id: '{}' completed.", wicaStreamId  ))
         .doOnCancel( () -> logger.warn( "channel-metadata flux with id: '{}' was cancelled.", wicaStreamId  ) )
         .doOnError( (e) -> logger.warn( "heartbeat flux with id: '{}' had error.", wicaStreamId, e ));
      //.log();
   }

   /**
    * Create the WICA CHANNEL MONITORED VALUES FLUX.
    *
    * The purpose of this flux is to publish the latest received values for channels
    * in the stream which are configured with a data acquisition mode that supports
    * MONITORING.
    *
    * This flux runs periodically at a rate determined by the properties of the stream.
    *
    * New subscribers to the flux receive first a Server-Sent-Event (SSE) message
    * containing the latest received information for all monitored channels, then subsequent
    * SSE messages will be sent out periodically and will contain only only information
    * for those channels which have received new information since the previous message.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createMonitoredValueFlux()
   {
      final AtomicReference<LocalDateTime> lastUpdateTime = new AtomicReference<>( LocalDateTime.MIN  );
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getMonitoredValueFluxIntervalInMillis() ) )
         .onBackpressureDrop()
         .map(l -> {
            logger.trace("channel-value-monitor flux with id: '{}' is publishing new SSE...", wicaStreamId );
            final var timeOfLastUpdate = lastUpdateTime.getAndSet( LocalDateTime.now() );
            return timeOfLastUpdate.equals( LocalDateTime.MIN  ) ? wicaStreamMonitoredValueCollectorService.getLatest( wicaStream ) :
               wicaStreamMonitoredValueCollectorService.get( wicaStream, timeOfLastUpdate );
         } )
         .filter( (map) -> map.keySet().size() > 0 )
         .map( (map) -> {
            final var jsonServerSentEventString = wicaChannelValueMapSerializerService.serialize( map );
            return WicaStreamServerSentEventBuilder.EV_WICA_CHANNEL_MONITORED_VALUES.build(wicaStreamId, jsonServerSentEventString );
         } )
         .doOnComplete( () -> logger.warn( "channel-value-monitor flux with id: '{}' completed.", wicaStreamId ))
         .doOnCancel( () -> logger.warn("channel-value-monitor flux with id: '{}' was cancelled.", wicaStreamId ))
         .doOnError( (e) -> logger.warn( "channel-value-monitor flux with id: '{}' had error.", wicaStreamId, e ));
      //.log();
   }

   /**
    * Create the WICA CHANNEL POLLED VALUES FLUX.
    *
    * The purpose of this flux is to publish the latest received values for channels
    * in the stream which are configured with a data acquisition mode that supports
    * POLLING.
    *
    * This flux runs periodically at a rate determined by the properties of the stream.
    *
    * New subscribers to the flux receive first a Server-Sent-Event (SSE) message
    * containing the latest received information for all polled channels, then subsequent
    * SSE messages will be sent out periodically and will contain only only information
    * for those channels which have received new information since the previous message.
    *
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createPolledValueFlux()
   {
      final AtomicReference<LocalDateTime> lastUpdateTime = new AtomicReference<>( LocalDateTime.MIN  );
      return Flux.interval( Duration.ofMillis( wicaStreamProperties.getPolledValueFluxIntervalInMillis() ) )
         .onBackpressureDrop()
         .map(l -> {
            logger.trace("channel-value-poll flux with id: '{}' is publishing new SSE...", wicaStreamId );
            final var timeOfLastUpdate = lastUpdateTime.getAndSet( LocalDateTime.now() );
            return timeOfLastUpdate.equals( LocalDateTime.MIN  ) ? wicaStreamPolledValueCollectorService.getLatest( wicaStream ) :
               wicaStreamPolledValueCollectorService.get( wicaStream, timeOfLastUpdate );
         } )
         .filter( (map) -> map.keySet().size() > 0 )
         .map( (map) -> {
            final var jsonServerSentEventString = wicaChannelValueMapSerializerService.serialize( map );
            return WicaStreamServerSentEventBuilder.EV_WICA_CHANNEL_POLLED_VALUES.build(wicaStreamId, jsonServerSentEventString );
         })
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
      // Any flux can be suppressed by configuring its refresh rate to 0ms.
      final var heartbeatFlux = wicaStreamProperties.getHeartbeatFluxIntervalInMillis() > 0 ? createHeartbeatFlux() :
         Flux.<ServerSentEvent<String>>empty();
      final var metadataFlux = wicaStreamProperties.getMetadataFluxIntervalInMillis() > 0 ? createMetadataFlux() :
         Flux.<ServerSentEvent<String>>empty();
      final var monitoredValueFlux = wicaStreamProperties.getMonitoredValueFluxIntervalInMillis() > 0 ? createMonitoredValueFlux() :
         Flux.<ServerSentEvent<String>>empty();
      final var polledValueFlux = wicaStreamProperties.getPolledValueFluxIntervalInMillis() > 0 ?  createPolledValueFlux() :
         Flux.<ServerSentEvent<String>>empty();

      // Create a single Flux which merges all of the above.
      return heartbeatFlux
         .mergeWith( metadataFlux )
         .mergeWith( monitoredValueFlux )
         .mergeWith( polledValueFlux )
         .doOnComplete( () -> logger.warn( "combined flux with id: '{}' flux completed.", wicaStreamId ))
         .doOnCancel( () -> logger.warn("combined flux with id: '{}' was cancelled.", wicaStreamId ))
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
