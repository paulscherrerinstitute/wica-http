/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaStreamId;
import ch.psi.wica.services.stream.WicaStreamService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle the {code /status} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamGetController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamGetController.class );
   private final WicaStreamService wicaStreamService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling stream GET requests.
    *
    * @param wicaStreamService reference to the service object which can be used
    *        to fetch the reactive streams.
    */
   private WicaStreamGetController( @Autowired WicaStreamService wicaStreamService )
   {
      this.wicaStreamService = Validate.notNull(wicaStreamService);
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP GET request to return the event stream associated with
    * the specified ID.
    *
    * @param id the ID of the event stream to fetch.
    *
    * @return the returned event stream.
    */
   @GetMapping( value="/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
   public ResponseEntity<Flux<ServerSentEvent<String>>> getServerSentEventStream( @PathVariable String id )
   {
      // Check that the Spring framework gives us something in the id field.
      Validate.notNull( id, "The event stream 'id' field was empty." );

      logger.info( "GET: Handling get stream request for ID: '{}'", id );

      // Handle the situation where an unknown WicaStreamId is given
      if ( ! wicaStreamService.isKnownId( WicaStreamId.of( id ) ) )
      {
         final String errorMessage = "the event stream 'id' was not recognised";
         logger.warn( "GET: Rejected request because {}", errorMessage  );
         return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
      }

      // Handle the normal case
      final Flux<ServerSentEvent<String>> wicaStreamFlux = wicaStreamService.getFromId( WicaStreamId.of(id ) );

      logger.info( "Returning event stream with id: '{}'", id );
      return new ResponseEntity<>( wicaStreamFlux, HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "SSE Exception handler called with exception '{}'", ex.toString() );
      ex.printStackTrace();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
