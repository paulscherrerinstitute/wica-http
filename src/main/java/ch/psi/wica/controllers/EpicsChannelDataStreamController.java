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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle the {code /status} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class EpicsChannelDataStreamController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelDataStreamController.class );

   private final EpicsChannelDataService epicsChannelDataService;

   private final int heartBeatFluxInterval;
   private final int channelMetadataFluxInterval;
   private final int channelValueFluxInterval;
   private final int channelValueUpdateFluxInterval;

   private final Map<StreamId, Flux<ServerSentEvent<String>>> eventStreamFluxMap = new ConcurrentHashMap<>();

   private LocalDateTime lastUpdateTime = LocalDateTime.MAX;


/*- Main ---------------------------------------------------------------------*/

   /**
    * Constructs a new controller which to handle all REST operations associated with EPICS event streams.
    */
   @Autowired
   private EpicsChannelDataStreamController( @Autowired EpicsChannelDataService epicsChannelDataService,
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
      final Set<EpicsChannelName> channels = channelNames.stream()
                                                         .map( EpicsChannelName::new )
                                                         .collect( Collectors.toSet() );

      // Create a new stream whose locus of interest is the specified set of channels
      final EpicsChannelDataStream stream = new EpicsChannelDataStream( channels );
      final StreamId streamId = stream.getStreamId();

      // Create the HEARTBEAT FLUX. The purpose of this flux is to periodically tell remote
      // clients that the stream is still alive. If remote clients do not receive heartbeat
      // events within the expected time intervals then they will conclude that the event
      // stream is no longer active. This may then lead to closing the event stream on the
      // client side and sending a request to the server to recreate the stream.
      final Flux<ServerSentEvent<String>> heartbeatFlux = Flux.interval( Duration.ofMillis( heartBeatFluxInterval ) )
         .map( l -> {
            logger.trace( "heartbeatFlux is publishing new SSE..." );
            return buildServerSentMessageEvent( streamId, "ev-wica-server-heartbeat", "heartbeat", getLastUpdateTime().toString() );
         } )
         .doOnCancel( () -> logger.warn( "heartbeat flux was cancelled" ) );
      //.log();

      // Create the CHANNEL METADATA FLUX. The purpose of this flux is to provide additional
      // information about the nature of the underlying EPICS channel. This may include the
      // channel's type and where relevant the channel's display, alarm and operator limits.
      // This flux runs with a periodicity defined by the channelMetadataFluxInterval.
      final Flux<ServerSentEvent<String>> channelMetadataFlux = Flux.interval( Duration.ofMillis( channelMetadataFluxInterval ) )
         .map( l -> {
            logger.trace( "metadataFlux is publishing new SSE..." );
            final Map<EpicsChannelName,EpicsChannelMetadata> channelMetadataMap = epicsChannelDataService.getChannelMetadata( stream );
            return buildServerSentMessageEvent( streamId, "ev-wica-channel-metadata", "channel metadata", EpicsChannelMetadata.convertMapToJsonRepresentation( channelMetadataMap ) );
         } )
         .doOnCancel( () -> logger.warn( "channel-metadata flux was cancelled" ) );
      //.log();

      // Create the CHANNEL VALUE FLUX. The purpose of this flux is to provide the last received
      // value for ALL channels specified in the event stream. This flux runs with a  periodicity
      // defined by the channelValueFluxInterval.
      final Flux<ServerSentEvent<String>> channelValueFlux = Flux.interval( Duration.ofMillis( channelValueFluxInterval ) )
         .map( l -> {
            logger.trace( "heartbeatFlux is publishing new SSE..." );
            final Map<EpicsChannelName,EpicsChannelValue> channelValueMap = epicsChannelDataService.getChannelValues( stream );
            return buildServerSentMessageEvent( streamId, "ev-wica-channel-value","channel values", EpicsChannelValue.convertMapToJsonRepresentation(channelValueMap ) );
         } )
         .doOnCancel( () -> logger.warn( "channel-value flux was cancelled" ) );
      //.log();

      // Create the CHANNEL VALUE UPDATE FLUX. The purpose of this flux is to provide the last received
      // value for any channels which have changed since the last update. This flux runs with a periodicity
      // defined by the channelValueUpdateFluxInterval.
      final Flux<ServerSentEvent<String>> channelValueUpdateFlux = Flux.interval( Duration.ofMillis( channelValueUpdateFluxInterval ) )
            .map( l -> {
               logger.trace( "heartbeatFlux is publishing new SSE..." );
               final Map<EpicsChannelName,EpicsChannelValue> channelValueUpdateMap = epicsChannelDataService.getChannelValuesUpdatedSince(stream, getLastUpdateTime() );
               setLastUpdateTime();
               return buildServerSentMessageEvent( streamId, "ev-wica-channel-value","channel value changes", EpicsChannelValue.convertMapToJsonRepresentation(channelValueUpdateMap ) );
            } )
            .doOnCancel( () -> logger.warn( "channel-value-update flux was cancelled" ) );
            //.log();

      // Create a single Flux which merges all of the above.
      final Flux<ServerSentEvent<String>> eventStreamFlux = heartbeatFlux.mergeWith( channelMetadataFlux )
                                                                         .mergeWith( channelValueFlux )
                                                                         .mergeWith( channelValueUpdateFlux )
                                                                         .doOnCancel( () -> {
                                                                            logger.warn( "evenStreamFlux was cancelled" );
                                                                            handleErrors( streamId );
                                                                         } );
                                                                         //.log();
      // Store it in the stream map
      // Note: this generates an IntelliJ warning about unassigned flux.
      // It's true the flux isn't assigned here so nothing will yet happen.
      // But eventually in the GET method the map entry will be retrieved
      // and the flux will be subscribed.
      // noinspection UnassignedFluxMonoInstance
      eventStreamFluxMap.put( streamId, eventStreamFlux );

      // Lastly set up monitors on all the channels of interest.
      epicsChannelDataService.startMonitoring( stream );

      logger.info( "POST: allocated stream with id: '{}'" , streamId.asString() );
      return new ResponseEntity<>( streamId.asString(), HttpStatus.OK );
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

      // Handle the situation where an unknown StreamId is given
      if ( ! eventStreamFluxMap.containsKey( StreamId.of( id ) ) )
      {
         logger.info( "GET: Rejected request because the event stream 'id' was not recognised." );
         return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
      }

      // Handle the normal case
      logger.info( "Returning event stream with id: '{}'", id );
      return new ResponseEntity<>( eventStreamFluxMap.get( StreamId.of( id ) ),HttpStatus.OK );
   }


   @DeleteMapping( value="/{id}", produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> deleteServerSentEventStream( @PathVariable String id )
   {
      // Check that the Spring framework gives us something in the channelNames field.
      Validate.notNull( id, "The event stream 'id' field was empty." );

      logger.info( "DELETE: Handling get stream request for ID: '{}'", id );

      // Handle the situation where an unknown StreamId is given
      final StreamId streamId = StreamId.of( id );
      if ( ! eventStreamFluxMap.containsKey( streamId ) )
      {
         logger.info( "GET: Rejected request because the event stream 'id' was not recognised." );
         return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
      }

      // Handle the normal case
      // Remove all monitors on all the channels of interest.
      // TODO: work out best way of getting stream back from Id
      //epicsChannelValueStashService.stopMonitoring( eventStreamFluxMap.get( streamId ) );

      // Note: this generates an IntelliJ warning about unassigned flux. But it is ok.
      eventStreamFluxMap.remove( streamId );

      logger.info( "DELETE: deleted stream with id: '{}'" , id.toString() );
      return new ResponseEntity<>( id, HttpStatus.OK );
   }


   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "SSE Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/

   private void handleErrors( StreamId id )
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
   private <T> ServerSentEvent<T> buildServerSentMessageEvent( StreamId id, String event, String comment, T data )
   {
      Validate.notNull( data,"The valueMap field was null ");

      return ServerSentEvent.builder( data ).id( id.asString() ).comment( comment ).event( event ).build();
   }


   private LocalDateTime getLastUpdateTime()
   {
      return lastUpdateTime;
   }

   private void setLastUpdateTime()
   {
      lastUpdateTime = LocalDateTime.now();
   }


/*- Nested Classes -----------------------------------------------------------*/

}
