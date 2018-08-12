/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.ChannelName;
import ch.psi.wica.model.StreamId;
import ch.psi.wica.services.EpicsChannelMonitorService;
import org.apache.commons.lang3.Validate;
import org.epics.ca.data.Graphic;
import org.epics.ca.data.Metadata;
import org.epics.ca.data.Timestamped;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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

   private final int eventStreamHeartBeatInterval;
   private final Map<StreamId, Flux<ServerSentEvent<Map<ChannelName,String>>>> eventStreamFluxMap = new ConcurrentHashMap<>();
   private final EpicsChannelMonitorService epicsChannelMonitorService;

/*- Main ---------------------------------------------------------------------*/

   /**
    * Constructs a new controller which to handle all REST operations
    * associated with EPICS event streams.
    *
    * @param eventStreamHeartBeatInterval the interval in seconds with which a
    *                                     heartbeat event will be sent from the
    *                                     Controller to the client side stream
    *                                     handler.
    */
   @Autowired
   private EventStreamController( EpicsChannelMonitorService epicsChannelMonitorService,
                                  @Value( "${wica.heartbeat_interval}" ) int eventStreamHeartBeatInterval )
   {
      this.epicsChannelMonitorService = Validate.notNull( epicsChannelMonitorService );

      logger.info( "Created new event stream with heartbeat interval of {} seconds.", eventStreamHeartBeatInterval );

      this.eventStreamHeartBeatInterval = eventStreamHeartBeatInterval;
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
      Validate.notNull( channelNames, " The 'channelNames' information was null" );

      logger.info( "POST: Handling create stream request. Monitored channels: '{}'", channelNames );

      if ( channelNames.size() == 0 )
      {
         logger.info( "POST: Rejected request because channel list was empty." );
         return new ResponseEntity<>( "The channel list cannot be empty.", HttpStatus.BAD_REQUEST );
      }

      final StreamId streamId = StreamId.createNext();

      // Create a new concurrent hash map to hold the value of any channels that are reported
      final Map<ChannelName,String> lastValueMap = Collections.synchronizedMap( new HashMap<>());

      // The replay processor is intended to ensure that clients can connect at any time and always
      // receive notification of the last known values of all the channels supported by the stream.
      // Republication will occur every time any of the monitored channels undergoes an update.
      final ReplayProcessor<ServerSentEvent<Map<ChannelName,String>>> replayProcessor = ReplayProcessor.cacheLast();
      final Flux<ServerSentEvent<Map<ChannelName,String>>> replayFlux = replayProcessor.doOnCancel( () -> logger.warn( "replayFlux was cancelled" ) );
            //.log();

      // The heartbeat flux runs periodically to keep the connection alive even if none of the
      // monitored channels are changing. This facilitates a client reconnect policy if the
      // server goes down.
      final Flux<ServerSentEvent<Map<ChannelName,String>>> heartbeatFlux = Flux.interval( Duration.ofSeconds( eventStreamHeartBeatInterval ) )
                                                                          .map( l -> {
                                                                             logger.trace( "heartbeatFlux is publishing new SSE..." );
                                                                             return buildServerSentMessageEvent( streamId, "heartbeat", lastValueMap );
                                                                          } )
                                                                          .doOnCancel( () -> logger.warn( "heartbeatFlux was cancelled" ) );
                                                                          //.log();

      // Store it in the stream map
      final Flux<ServerSentEvent<Map<ChannelName,String>>> eventStreamFlux = replayFlux.mergeWith( heartbeatFlux )
                                                                                  .doOnCancel( () -> {
                                                                                     logger.warn( "evenStreamFlux was cancelled" );
                                                                                     handleErrors( streamId );
                                                                                  } );
                                                                                  //.log();

      // Note: this generates an IntelliJ warning about unassigned flux.
      // It's true the flux isn't assignd here so nothing will yet happen.
      // But eventually in the GET method the map entry will be retrieved
      // and the flux will be subscribed.
      // noinspection UnassignedFluxMonoInstance
      eventStreamFluxMap.put( streamId, eventStreamFlux );

      // Lastly set up monitors on all the channels of interest.
      for ( String channelNameAsString : channelNames )
      {
         final ChannelName channelName = new ChannelName( channelNameAsString );
         logger.info( "subscribing to: '{}'", channelName );
         final Consumer<Boolean> stateChangedHandler = b -> stateChanged( streamId, channelName, b, lastValueMap, replayProcessor );
         final Consumer<String> valueChangedHandler =  v -> valueChanged( streamId, channelName, v, lastValueMap, replayProcessor );
         epicsChannelMonitorService.startMonitoring( channelName, stateChangedHandler, valueChangedHandler );
      }
      logger.info( "POST: allocated stream with id: '{}'" , streamId.asString() );
      return new ResponseEntity<>( streamId.asString(), HttpStatus.OK );
   }

   /**
    * Handles an HTTP GET request to return the event stream associated with the specified ID.
    *
    * @param id the ID of the event stream to subscribe to.
    * @return the returned event stream.
    */
   @GetMapping( value="/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
   public ResponseEntity<Flux<ServerSentEvent<Map<ChannelName,String>>>> getServerSentEventStream( @PathVariable String id )
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
    * Handles a connection state change on the underlying EPICS channel monitor.
    *
    * @param streamId the streamId associated with this update.
    * @param channelName the name of the channel whose connection state changed.
    * @param isConnected the new connection state.
    * @param lastValueMap reference to a map which can be used to hold the new value.
    * @param replayProcessor reference to the flux which can be triggered to republish
    *                        the updated event map.
    */
   private void stateChanged( StreamId streamId,
                              ChannelName channelName,
                              Boolean isConnected,
                              Map<ChannelName,String> lastValueMap,
                              ReplayProcessor<ServerSentEvent<Map<ChannelName,String>>> replayProcessor )
   {
      Validate.notNull( channelName,"The 'channelName' argument was null" );
      Validate.notNull( isConnected, "The 'isConnected' argument was null"  );
      Validate.notNull( lastValueMap, "The 'lastValueMap' argument was null" );
      Validate.notNull( replayProcessor, "The 'replayProcessor' argument was null" );

      logger.info( "'{}' - connection state changed to '{}'.", channelName,  isConnected);

      if ( ! isConnected )
      {
         logger.debug( "'{}' - value changed to NULL to indicate the connection was lost.", channelName );
         lastValueMap.put( channelName, null );
         publish( streamId,"channel disconnected", lastValueMap, replayProcessor );
      }
   }

   /**
    * Handles a value change on the underlying EPICS channel monitor.
    *
    * @param streamId the streamId associated with this update.
    * @param channelName the name of the channel whose monitor changed.
    * @param newValue the new value
    * @param lastValueMap reference to a map which can be used to hold the new value.
    * @param replayProcessor reference to the flux which can be triggered to republish
    *                        the updated event map.
    */
   private void valueChanged( StreamId streamId,
                              ChannelName channelName,
                              String newValue,
                              Map<ChannelName,String> lastValueMap,
                              ReplayProcessor<ServerSentEvent<Map<ChannelName,String>>> replayProcessor )
   {
      Validate.notNull( streamId,"The 'streamId' argument was null");
      Validate.notNull( channelName,"The 'channelName' argument was null");
      Validate.notNull( newValue,"The 'newValue' argument was null");
      Validate.notNull( lastValueMap,"The 'lastValueMap' argument was null");
      Validate.notNull( replayProcessor,"The 'replayProcessor' argument was null");

      logger.trace( "'{}' - value changed to: '{}'", channelName, newValue );
      lastValueMap.put(channelName, newValue );
      publish( streamId,"value changed", lastValueMap, replayProcessor );
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
                         Map<ChannelName,String> lastValueMap,
                         ReplayProcessor<ServerSentEvent<Map<ChannelName,String>>> replayProcessor )
   {
      Validate.notNull( lastValueMap );
      Validate.notNull( replayProcessor );

      if ( replayProcessor.downstreamCount() == 0 )
      {
         logger.trace( "ReplayProcessor - aborted publication because the stream has no subscribers ! " );
         return;
      }

      // Catch and log any exceptions that arise during publication.
      final ServerSentEvent<Map<ChannelName,String>> sse = buildServerSentMessageEvent( streamId, comment, lastValueMap );
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
   private ServerSentEvent<Map<ChannelName,String>> buildServerSentMessageEvent( StreamId id, String comment, Map<ChannelName,String> valueMap )
   {
      Validate.notNull( valueMap,"The valueMap field was null ");
      return ServerSentEvent.builder( valueMap ).id( id.asString() ).comment( comment ).event( "message" ).build();
   }


/*- Nested Classes -----------------------------------------------------------*/

}
