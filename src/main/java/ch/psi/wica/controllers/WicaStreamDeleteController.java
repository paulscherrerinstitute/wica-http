/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle the {code /status} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamDeleteController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamDeleteController.class );
   private final Map<WicaStreamId, Flux<ServerSentEvent<String>>> eventStreamFluxMap = new ConcurrentHashMap<>();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

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

      logger.info( "DELETE: deleted stream with id: '{}'", id );
      return new ResponseEntity<>( id, HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "SSE Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
