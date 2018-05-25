/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.epics.EpicsChannelMonitor;
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

   private static int nextAllocationId;

   private final int eventStreamHeartBeatInterval;
   private final Logger logger = LoggerFactory.getLogger( EventStreamController.class );

   private final Map<String,Flux<ServerSentEvent<Map<String,String>>>> eventStreamFluxMap;
   private final Map<String,EpicsChannelMonitor> eventStreamMonitorMap;

/*- Main ---------------------------------------------------------------------*/

   @Autowired
   public EventStreamController(@Value( "${wica2.heartbeat_interval}" ) int eventStreamHeartBeatInterval,  EpicsChannelMonitor epicsChannelMonitor )
   {
      logger.info( "Created new event stream with heartbeat interval of {} seconds.", eventStreamHeartBeatInterval );

      this.eventStreamHeartBeatInterval = eventStreamHeartBeatInterval;
      this.eventStreamFluxMap = new ConcurrentHashMap<>();
      this.eventStreamMonitorMap = new ConcurrentHashMap<>();

   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @PostMapping( consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> createStream( @RequestBody List<String> channelNames )
   {
      Validate.notNull( channelNames, " The 'channelNames' information was null" );
      logger.info( "subscribing to: '{}'", channelNames );

      final String streamId = String.valueOf( nextAllocationId++ );

      // Create a new concurrent hash map to hold the value of any channels that are reported
      final Map<String,String> lastValueMap = new ConcurrentHashMap<>();

      // The replay processor is intended to ensure that clients can connect at any time and always
      // receive notification of the last known values of all the channels supported by the stream.
      // Republication will occur every time any of the monitored channels undergoes an update.
      final ReplayProcessor<ServerSentEvent<Map<String,String>>> replayProcessor = ReplayProcessor.cacheLast();
      final Flux<ServerSentEvent<Map<String,String>>> replayFlux = replayProcessor.doOnCancel( () -> logger.warn( "replayFlux was cancelled" ) ).log();

      // The heartbeat flux runs periodically to keep the connection alive even if none of the
      // monitored channels are changing. This facilitates a client reconnect policy if the
      // server goes down.
      final Flux<ServerSentEvent<Map<String,String>>> heartbeatFlux = Flux.interval( Duration.ofSeconds( eventStreamHeartBeatInterval ) )
                                                                          .map( l -> {
                                                                             logger.info( "heartbeatFlux is publishing new SSE..." );
                                                                             return buildServerSentEvent( lastValueMap );
                                                                          } )
                                                                          .doOnCancel( () -> logger.warn( "heartbeatFlux was cancelled" ) ).log();

      // Store it in the next slot in the stream map
      final Flux<ServerSentEvent<Map<String,String>>> eventStreamFlux = replayFlux.mergeWith( heartbeatFlux )
                                                                                  .doOnCancel( () -> {
                                                                                     logger.warn( "evenStreamFlux was cancelled" );
                                                                                     handleErrors( streamId );
                                                                                  } )
                                                                                  .log() ;
      eventStreamFluxMap.put( streamId, eventStreamFlux );

      // Lastly set up monitors on all the channels of interest.
      final EpicsChannelMonitor epicsChannelMonitor = new EpicsChannelMonitor();
      for ( String channelName : channelNames )
      {
         logger.info( "subscribing to: '{}'", channelName );
         final Consumer<String> consumer = v -> publishNewValue( channelName, v, lastValueMap, replayProcessor );
         epicsChannelMonitor.connect( channelName, String.class, consumer );
      }
      eventStreamMonitorMap.put( streamId, epicsChannelMonitor );

      return new ResponseEntity<>( streamId, HttpStatus.OK);
   }

   @GetMapping( value="/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
   public Flux<ServerSentEvent<Map<String,String>>> getServerSentEventStream( @PathVariable String id )
   {
      Validate.notEmpty( id, "The event stream 'id' field was empty." );

      if ( ! eventStreamFluxMap.containsKey( id ) )
      {
         Validate.notEmpty( id, "The event stream 'id' field was not recognised." );
         final ServerSentEvent<Map<String,String>> sse = buildServerSentEvent( new HashMap<>() );
         return Flux.just( sse );
      }

      logger.info( "Returning event stream with id: '{}'", id );
      final Flux<ServerSentEvent<Map<String,String>>> eventStreamFlux = eventStreamFluxMap.get( id );
      return eventStreamFlux;
   }


/*- Private methods ----------------------------------------------------------*/

   @ExceptionHandler( Exception.class )
   private void handleException()
   {
      logger.info( "************* MY EXCEPTION HANDLER WAS CALLED ****************" );
   }

   private void handleErrors( String id )
   {
      logger.info( "SOME ERROR OCCURRED ON MONITOR STREAM WITH ID: '{}' ", id );
      logger.info( "Probably the client navigated away from the webpage and the eventsource was closed by the browser !! " );

      eventStreamMonitorMap.get( id ).destroy();
   }

   private void publishNewValue( String channelName, String newValue,
                                 Map<String,String> lastValueMap,
                                 ReplayProcessor<ServerSentEvent<Map<String,String>>> replayProcessor )
   {
      // Quietly return if there is no new value here... It seems this can occur if the
      // Epics data source goes down in the middle of an update.
      if ( newValue == null )
      {
         logger.warn( "Notification but without new value" );
         return;
      }

      Validate.notNull( channelName ,"The 'channelName' argument was null" );
      Validate.notNull( lastValueMap, "The 'lastValueMap' argument was null" );
      Validate.notNull( replayProcessor, "The 'replayProcessor' argument was null" );

      logger.info("The value of channelName: '{}' was updated to: '{}'", channelName, newValue);
      lastValueMap.put( channelName, newValue);

      if ( replayProcessor.downstreamCount() == 0 )
      {
         logger.warn( "Publication was not completed because the stream has no subscribers ! " );
         return;
      }

      // Catch and log any exceptions that arise during publication.
      final ServerSentEvent<Map<String,String>> sse = buildServerSentEvent( lastValueMap );
      try
      {
         logger.info( "ReplayProcessor is publishing new SSE: '{}'", sse );

         // Synchronization is required here to enforce the requirement that multiple
         // threads can only generate events on a subscriber when they are externallly
         // synchronized. See Publisher Rule 3 in the reactive spec.
         synchronized ( this ) {
            replayProcessor.onNext(sse);
         }
      }
      catch( Exception ex )
      {
         logger.error( "Publication generated unhandled exception: '{}' ", ex.toString());
         return;
      }
   }

   private ServerSentEvent<Map<String,String>> buildServerSentEvent( Map<String,String> valueMap )
   {
      Validate.notNull( valueMap,"The valueMap field was null ");
      final ServerSentEvent<Map<String,String>> sse = ServerSentEvent.builder( valueMap ).event( "message" ).build();
      return sse;
   }


/*- Nested Classes -----------------------------------------------------------*/

}
