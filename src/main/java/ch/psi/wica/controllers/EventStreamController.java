/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.EpicsChannelName;
import ch.psi.wica.model.EpicsChannelValue;
import ch.psi.wica.model.EpicsChannelValueStream;
import ch.psi.wica.services.EpicsChannelValueService;
import ch.psi.wica.model.StreamId;
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
class EventStreamController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EventStreamController.class );

   private final EpicsChannelValueService epicsChannelValueService;

   private final int updateStreamHeartBeatInterval;
   private final int eventStreamHeartBeatInterval;

   private final Map<StreamId, Flux<ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>>>> eventStreamFluxMap = new ConcurrentHashMap<>();

   private LocalDateTime lastUpdateTime = LocalDateTime.MAX;


/*- Main ---------------------------------------------------------------------*/

   /**
    * Constructs a new controller which to handle all REST operations associated with EPICS event streams.
    *
    *
    * @param eventStreamHeartBeatInterval the interval in seconds with which a
    *                                     heartbeat event will be sent from the
    *                                     Controller to the client side stream
    *                                     handler.
    */
   @Autowired
   private EventStreamController( @Autowired EpicsChannelValueService epicsChannelValueService,
                                  @Value( "${wica.heartbeat_interval}" ) int eventStreamHeartBeatInterval,
                                  @Value( "${wica.update_interval}" ) int updateStreamHeartBeatInterval)
   {
      logger.info( "Created new event stream with heartbeat interval of {} seconds.", eventStreamHeartBeatInterval );

      this.epicsChannelValueService = epicsChannelValueService;
      this.eventStreamHeartBeatInterval = eventStreamHeartBeatInterval;
      this.updateStreamHeartBeatInterval = updateStreamHeartBeatInterval;
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

      // Create a new stream
      final EpicsChannelValueStream stream = new EpicsChannelValueStream( channels );
      final StreamId streamId = stream.getStreamId();

      // Create a new concurrent hash map to hold the value of any channels that are reported
      //final Map<EpicsChannelName,String> lastValueMap = Collections.synchronizedMap( new HashMap<>());

      // The replay processor is intended to ensure that clients can connect at any time and always
      // receive notification of the last known values of all the channels supported by the stream.
      // Republication will occur every time any of the monitored channels undergoes an update.
      //final ReplayProcessor<ServerSentEvent<Map<EpicsChannelName,String>>> replayProcessor = ReplayProcessor.cacheLast();
      //final Flux<ServerSentEvent<Map<EpicsChannelName,String>>> replayFlux = replayProcessor.doOnCancel(() -> logger.warn("replayFlux was cancelled" ) );
            //.log();

      // The heartbeat flux runs periodically to keep the connection alive even if none of the
      // monitored channels are changing. This facilitates a client reconnect policy if the
      // server goes down.
      final Flux<ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>>> heartbeatFlux = Flux.interval( Duration.ofMillis( eventStreamHeartBeatInterval ) )
                                                                          .map( l -> {
                                                                             logger.trace( "heartbeatFlux is publishing new SSE..." );
                                                                             final Map<EpicsChannelName, EpicsChannelValue> map = epicsChannelValueService.getChannelValues(stream );
                                                                             return buildServerSentMessageEvent( streamId, "heartbeat", map );
                                                                          } )
                                                                          .doOnCancel( () -> logger.warn( "heartbeatFlux was cancelled" ) );
                                                                          //.log();


      final Flux<ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>>> updateFlux = Flux.interval( Duration.ofMillis( updateStreamHeartBeatInterval ) )
                                                                                            .map( l -> {
                                                                                                logger.trace( "heartbeatFlux is publishing new SSE..." );
                                                                                                final Map<EpicsChannelName,EpicsChannelValue> updateMap = epicsChannelValueService.getChannelValuesUpdatedSince(stream, getLastUpdateTime() );
                                                                                                setLastUpdateTime();
                                                                                                return buildServerSentMessageEvent( streamId, "update", updateMap );
                                                                                            } )
                                                                                            .doOnCancel( () -> logger.warn( "updateFlux was cancelled" ) );
      //.log();

      // Store it in the stream map
      final Flux<ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>>> eventStreamFlux = updateFlux.mergeWith( heartbeatFlux )
                                                                                                       .doOnCancel( () -> {
                                                                                                          logger.warn( "evenStreamFlux was cancelled" );
                                                                                                          handleErrors( streamId );
                                                                                                       } );
                                                                                  //.log();

      // Note: this generates an IntelliJ warning about unassigned flux.
      // It's true the flux isn't assigned here so nothing will yet happen.
      // But eventually in the GET method the map entry will be retrieved
      // and the flux will be subscribed.
      // noinspection UnassignedFluxMonoInstance
      eventStreamFluxMap.put( streamId, eventStreamFlux );

      // Lastly set up monitors on all the channels of interest.
      epicsChannelValueService.startMonitoring(stream );

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
   public ResponseEntity<Flux<ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>>>> getServerSentEventStream( @PathVariable String id )
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
    * Publishes the state of the supplied map as a ServerSentEvent on the supplied
    * processor.
    *
    * @param lastValueMap the map containing the list of EPICS channels
    * @param replayProcessor refe
    */
   private void publish( StreamId streamId,
                         String comment,
                         Map<EpicsChannelName,EpicsChannelValue> lastValueMap,
                         ReplayProcessor<ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>>> replayProcessor )
   {
      Validate.notNull( lastValueMap );
      Validate.notNull( replayProcessor );

      if ( replayProcessor.downstreamCount() == 0 )
      {
         logger.trace( "ReplayProcessor - aborted publication because the stream has no subscribers ! " );
         return;
      }

      // Catch and log any exceptions that arise during publication.
      final ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>> sse = buildServerSentMessageEvent(streamId, comment, lastValueMap );
      try
      {
         logger.trace( "ReplayProcessor - is publishing new SSE: '{}'", sse );

         // Synchronization is required here to enforce the requirement that multiple
         // threads can only generate events on a subscriber when they are externally
         // synchronized. See Publisher Rule 3 in the reactive spec.
         // https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.2/README.md#specification
         synchronized ( this )
         {
            replayProcessor.onNext( sse );
         }
      }
      catch( Exception ex )
      {
         logger.error( "ReplayProcessor - Publication generated unhandled exception: '{}' ", ex.toString());
      }
   }

   /**
    * Utility method to transform the supplied value map into a Server Sent Event (SSE)
    * of event type 'message' suitable for returning as part of a publisher's event flux.
    *
    * @param id the event stream id.
    * @param comment the comment field.
    * @param valueMap the map of values that are to be transformed.
    * @return the generated SSE.
    */
   private ServerSentEvent<Map<EpicsChannelName,EpicsChannelValue>> buildServerSentMessageEvent( StreamId id, String comment, Map<EpicsChannelName,EpicsChannelValue> valueMap )
   {
      Validate.notNull( valueMap,"The valueMap field was null ");

      return ServerSentEvent.builder( valueMap ).id( id.asString() ).comment( comment ).event( "message" ).build();
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
