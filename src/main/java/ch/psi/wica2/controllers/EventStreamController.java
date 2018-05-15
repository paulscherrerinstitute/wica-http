/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica2.controllers;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica2.epics.EpicsChannelMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle the {code /status} endpoint.
 */
@RestController
class EventStreamController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EventStreamController.class );

   private final ReplayProcessor<ServerSentEvent<String>> replayProcessor;
   private final Flux<ServerSentEvent<String>> heartBeatFlux;
   private final EpicsChannelMonitor<String> epicsChannelMonitor;


/*- Main ---------------------------------------------------------------------*/

   @Autowired
   public EventStreamController(@Value( "${wica2.heartbeat_interval}" )int eventStreamHeartBeatInterval )
   {
      logger.info( "Created new event stream with heartbeat interval of {} seconds.", eventStreamHeartBeatInterval );
      this.replayProcessor =  ReplayProcessor.create();

      this.heartBeatFlux = Flux.interval( Duration.ofSeconds( eventStreamHeartBeatInterval ) )
                               .map( l -> getHeartbeatMessage( l ) );

      this.epicsChannelMonitor = new EpicsChannelMonitor();

      //epicsChannelMonitor.connect( d -> replayProcessor.onNext( getMessage( String.valueOf( d )) ));
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @GetMapping( value="/stream/{channel}", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
   public Flux<ServerSentEvent<String>> getServerSentEventStream( @PathVariable( "channel" ) String channelName )
   {
      final ReplayProcessor<ServerSentEvent<String>> processor = ReplayProcessor.create( 1 );
      epicsChannelMonitor.connect( channelName, String.class, v -> publishEvent( processor, v ) );

      return processor;

      // The returned stream will generate a heartbeat event every N seconds. If the client
      // does not see the heartbeat it can then re-subscribe to the event stream. This
      // potentially facilitates reconnection on server outages.
      // return replayProcessor.mergeWith( heartBeatFlux );
   }


/*- Private methods ----------------------------------------------------------*/

   private void publishEvent( ReplayProcessor<ServerSentEvent<String>> processor, String value )
   {
      final ServerSentEvent<String> message = getMessage( value );
      processor.onNext( message );
   }


   private ServerSentEvent<String> getMessage( String s )
   {
      //logger.info( "Getting SSE...");
      return ServerSentEvent.builder( s ).event( "message" ).build();
   }



   private ServerSentEvent<String> getHeartbeatMessage( Long l )
   {
      return ServerSentEvent.builder( String.valueOf( l ) ).event( "heartbeat") .build();
   }


/*- Nested Classes -----------------------------------------------------------*/

}
