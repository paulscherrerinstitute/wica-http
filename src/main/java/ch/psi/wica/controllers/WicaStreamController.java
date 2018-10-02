/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.EpicsChannelDataService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle the {code /status} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * The format which will be used when making String representations
    * of the times/dates in this class.
    */
   private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

   private final Logger logger = LoggerFactory.getLogger(WicaStreamController.class );

   private final EpicsChannelDataService epicsChannelDataService;

   private final int heartBeatFluxInterval;
   private final int channelMetadataFluxInterval;
   private final int channelValueFluxInterval;
   private final int channelValueUpdateFluxInterval;

   private final Map<WicaStreamId, Flux<ServerSentEvent<String>>> eventStreamFluxMap = new ConcurrentHashMap<>();

   private LocalDateTime lastUpdateTime = LocalDateTime.MAX;


/*- Main ---------------------------------------------------------------------*/

   /**
    * Constructs a new controller which to handle all REST operations associated with EPICS event streams.
    */
   @Autowired
   private WicaStreamController( @Autowired EpicsChannelDataService epicsChannelDataService,
                                 @Value( "${wica.heartbeat_flux_interval_in_ms}" ) int heartBeatFluxInterval,
                                 @Value( "${wica.channel_metadata_flux_interval_in_ms}" ) int channelMetadataFluxInterval,
                                 @Value( "${wica.channel_value_flux_interval_in_ms}" ) int channelValueFluxInterval,
                                 @Value( "${wica.channel_value_update_flux_interval_in_ms}" ) int channelValueUpdateFluxInterval)
   {
      //logger.info( "Created new event stream with heartbeat interval of {} seconds.", eventStreamHeartBeatInterval );
      this.epicsChannelDataService = epicsChannelDataService;
      this.heartBeatFluxInterval = heartBeatFluxInterval;
      this.channelMetadataFluxInterval = channelMetadataFluxInterval;
      this.channelValueFluxInterval = channelValueFluxInterval;
      this.channelValueUpdateFluxInterval = channelValueUpdateFluxInterval;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP POST request to start monitoring (= observing the value
    * of) a targeted list of EPICS Process Variables ("PV's").
    *
    * @param channelNames the names of the EPICS process variables that are to
    *                     be monitored.
    *
    * @return the ID of the resource which was created. This ID can be used in
    *         one or more subsequent HTTP GET requests to return a stream of
    *         events which follows the value of the underlying EPICS channel.
    */
   @PostMapping( consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> createStream( @RequestBody List<String> channelNames )
   {
      // Check that the Spring framework gives us something in the channelNames field.
      Validate.notNull( channelNames," The 'channelNames' information was null" );

      logger.info( "POST: Handling create stream request. Monitored channels: '{}'", channelNames );

      if ( channelNames.size() == 0 )
      {
         logger.info( "POST: Rejected request because channel list was empty." );
         return new ResponseEntity<>( "The channel list cannot be empty.", HttpStatus.BAD_REQUEST );
      }

      // Create a set of channels of interest from the request
      final Set<WicaChannelName> channels = channelNames.stream()
                                                         .map(WicaChannelName::new )
                                                         .collect( Collectors.toSet() );

      // Create a new stream whose locus of interest is the specified set of channels
      final WicaStream stream = new WicaStream(channels );
      final WicaStreamId wicaStreamId = stream.getWicaStreamId();

      // Create the HEARTBEAT FLUX. The purpose of this flux is to periodically tell remote
      // clients that the stream is still alive. If remote clients do not receive heartbeat
      // events within the expected time intervals then they will conclude that the event
      // stream is no longer active. This may then lead to closing the event stream on the
      // client side and sending a request to the server to recreate the stream.
      final Flux<ServerSentEvent<String>> heartbeatFlux = Flux.interval( Duration.ofMillis( heartBeatFluxInterval ) )
         .map( l -> {
            logger.trace( "heartbeat flux is publishing new SSE..." );
            return buildServerSentMessageEvent(wicaStreamId, "ev-wica-server-heartbeat", "heartbeat", LocalDateTime.now().toString() );
         } )
         .doOnCancel( () -> logger.warn( "heartbeat flux was cancelled" ) );
      //.log();

      // Create the CHANNEL METADATA FLUX. The purpose of this flux is to provide additional
      // information about the nature of the underlying EPICS channel. This may include the
      // channel's type and where relevant the channel's display, alarm and operator limits.
      // This flux runs with a periodicity defined by the channelMetadataFluxInterval.
      final Flux<ServerSentEvent<String>> channelMetadataFlux = Flux.range( 1, 1 )
         .map( l -> {
            logger.trace( "channel-metadata flux is publishing new SSE..." );
            final Map<WicaChannelName, WicaChannelMetadata> channelMetadataMap = epicsChannelDataService.getChannelMetadata( stream );
            return buildServerSentMessageEvent(wicaStreamId, "ev-wica-channel-metadata", "channel metadata", WicaChannelMetadata.convertMapToJsonRepresentation(channelMetadataMap ) );
         } )
         .doOnCancel( () -> logger.warn( "channel-metadata flux was cancelled" ) );
      //.log();

      // Create the CHANNEL VALUE FLUX. The purpose of this flux is to provide the last received
      // value for ALL channels specified in the event stream. This flux runs with a  periodicity
      // defined by the channelValueFluxInterval.
      final Flux<ServerSentEvent<String>> channelValueFlux = Flux.range( 1, 1 ) //( Duration.ofMillis( channelValueFluxInterval ) )
         .map( l -> {
            logger.trace( "channel-value flux is publishing new SSE..." );
            final Map<WicaChannelName, WicaChannelValue> channelValueMap = epicsChannelDataService.getChannelValues( stream );
            return buildServerSentMessageEvent(wicaStreamId, "ev-wica-channel-value", "channel values", WicaChannelValue.convertMapToJsonRepresentation( channelValueMap ) );
         } )
         .doOnCancel( () -> logger.warn( "channel-value flux was cancelled" ) );
      //.log();

      // Create the CHANNEL VALUE UPDATE FLUX. The purpose of this flux is to provide the last received
      // value for any channels which have changed since the last update. This flux runs with a periodicity
      // defined by the channelValueUpdateFluxInterval.
      LocalDateTime lastUpdateTime;
      final Flux<ServerSentEvent<String>> channelValueUpdateFlux = Flux.interval( Duration.ofMillis( channelValueUpdateFluxInterval ) )
            .map( l -> {
               logger.trace( "channel-value-update flux is publishing new SSE..." );
               final Map<WicaChannelName, WicaChannelValue> channelValueUpdateMap = epicsChannelDataService.getChannelValuesUpdatedSince( stream, getLastUpdateTime(wicaStreamId) );
               setLastUpdateTime(wicaStreamId);
               return buildServerSentMessageEvent(wicaStreamId, "ev-wica-channel-value", "channel value changes", WicaChannelValue.convertMapToJsonRepresentation(channelValueUpdateMap ) );
            } )
            .doOnCancel( () -> logger.warn( "channel-value-update flux was cancelled" ) );
            //.log();

      // Create a single Flux which merges all of the above.
      final Flux<ServerSentEvent<String>> eventStreamFlux = heartbeatFlux.mergeWith( channelMetadataFlux )
                                                                         .mergeWith( channelValueFlux )
                                                                         .mergeWith( channelValueUpdateFlux )
                                                                         .doOnCancel( () -> {
                                                                            logger.warn( "eventStreamFlux was cancelled" );
                                                                            handleErrors( wicaStreamId );
                                                                         } );
                                                                         //.log();
      // Store it in the stream map
      // Note: this generates an IntelliJ warning about unassigned flux.
      // It's true the flux isn't assigned here so nothing will yet happen.
      // But eventually in the GET method the map entry will be retrieved
      // and the flux will be subscribed.
      // noinspection UnassignedFluxMonoInstance
      eventStreamFluxMap.put(wicaStreamId, eventStreamFlux );

      // Lastly set up monitors on all the channels of interest.
      epicsChannelDataService.startMonitoring( stream );

      logger.info("POST: allocated stream with id: '{}'" , wicaStreamId.asString() );
      return new ResponseEntity<>(wicaStreamId.asString(), HttpStatus.OK );
   }

   /**
    * Handles an HTTP GET request to return the event stream associated with the specified ID.
    *
    * @param id the ID of the event stream to startMonitoring to.
    * @return the returned event stream.
    */
   @GetMapping( value="/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
   public ResponseEntity<Flux<ServerSentEvent<String>>> getServerSentEventStream( @PathVariable String id )
   {
      // Check that the Spring framework gives us something in the channelNames field.
      Validate.notNull( id, "The event stream 'id' field was empty." );

      logger.info( "GET: Handling get stream request for ID: '{}'", id );

      // Handle the situation where an unknown WicaStreamId is given
      if ( ! eventStreamFluxMap.containsKey(WicaStreamId.of(id ) ) )
      {
         logger.info( "GET: Rejected request because the event stream 'id' was not recognised." );
         return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
      }

      // Handle the normal case
      logger.info( "Returning event stream with id: '{}'", id );
      return new ResponseEntity<>(eventStreamFluxMap.get(WicaStreamId.of(id ) ), HttpStatus.OK );
   }


   @DeleteMapping( value="/{id}", produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> deleteServerSentEventStream( @PathVariable String id )
   {
      // Check that the Spring framework gives us something in the channelNames field.
      Validate.notNull( id, "The event stream 'id' field was empty." );

      logger.info( "DELETE: Handling get stream request for ID: '{}'", id );

      // Handle the situation where an unknown WicaStreamId is given
      final WicaStreamId wicaStreamId = WicaStreamId.of(id );
      if ( ! eventStreamFluxMap.containsKey(wicaStreamId) )
      {
         logger.info( "GET: Rejected request because the event stream 'id' was not recognised." );
         return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
      }

      // Handle the normal case
      // Remove all monitors on all the channels of interest.
      // TODO: work out best way of getting stream back from Id
      //epicsChannelValueStashService.stopMonitoring( eventStreamFluxMap.get( wicaStreamId ) );

      // Note: this generates an IntelliJ warning about unassigned flux. But it is ok.
      eventStreamFluxMap.remove(wicaStreamId);

      logger.info( "DELETE: deleted stream with id: '{}'" , id.toString() );
      return new ResponseEntity<>( id, HttpStatus.OK );
   }


   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "SSE Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/

   private void handleErrors( WicaStreamId id )
   {
      logger.info( "Some error occurred on monitor stream with Id: '{}' ", id );
      logger.info( "Probably the client navigated away from the webpage and the eventsource was closed by the browser !! " );
   }

   /**
    * Utility method to transform the supplied value map into a Server Sent Event (SSE)
    * of event type 'message' suitable for returning as part of a publisher's event flux.
    *
    * @param id the event stream id.
    * @param comment the comment field.
    * @param data
    * @return the generated SSE.
    */
   private <T> ServerSentEvent<T> buildServerSentMessageEvent( WicaStreamId id, String event, String comment, T data )
   {
      Validate.notNull( data,"The valueMap field was null ");

      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( DATETIME_FORMAT_PATTERN );
      final String formattedTimeAndDateNow =  LocalDateTime.now().format( formatter );
      return ServerSentEvent.builder( data )
                            .id( id.asString() )
                            .comment( formattedTimeAndDateNow + " - " + comment )
                            .event( event )
                            .build();
   }

   private Map<WicaStreamId,LocalDateTime> lastUpdateTimeMap = new HashMap<>();

   private LocalDateTime getLastUpdateTime( WicaStreamId wicaStreamId )
   {
      return lastUpdateTimeMap.getOrDefault(wicaStreamId, LocalDateTime.MIN );
   }

   private void setLastUpdateTime( WicaStreamId wicaStreamId )
   {
      lastUpdateTimeMap.put(wicaStreamId, LocalDateTime.now() );
   }


/*- Nested Classes -----------------------------------------------------------*/

}
