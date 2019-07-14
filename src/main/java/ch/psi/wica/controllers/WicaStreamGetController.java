/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import ch.psi.wica.model.app.StatisticsCollector;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.services.stream.WicaStreamLifecycleService;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle GET operations on the
 * {code /ca/streams} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamGetController implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamGetController.class );
   private final WicaStreamLifecycleService wicaStreamLifecycleService;
   private final StatisticsCollector statisticsCollector = new StatisticsCollector();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling stream GET requests.
    *
    * @param wicaStreamLifecycleService reference to the service object which can be used
    *        to fetch the reactive streams.
    */
   public WicaStreamGetController( @Autowired WicaStreamLifecycleService wicaStreamLifecycleService )
   {
      this.wicaStreamLifecycleService = Validate.notNull(wicaStreamLifecycleService);
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP request to GET (= subscribe to) the wica stream with the
    * specified ID.
    *
    * @param optStreamId the ID of the stream to be subscribed to.
    *
    * @param httpServletRequest contextual information for the request; used
    *     for statistics collection only.
    *
    * @return an HTTP response whose status code will be set to 'OK' (= 200)
    *     if the operation completes successfully or 'Bad Request' (= 400) if
    *     some error occurs.  When successful the the HTTP response remains
    *     open and the evolving state of the stream's channels are written
    *     to the response body as a sequence of Server Sent Events (SSE's).
    *     When unsuccessful the response header 'X-WICA-ERROR' is written
    *     with a more detailed description of the error.
    */
   @SuppressWarnings( "OptionalUsedAsFieldOrParameterType" )
   @GetMapping( value = { "", "/{optStreamId}"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE )
   public ResponseEntity<Flux<ServerSentEvent<String>>> get( @PathVariable Optional<String> optStreamId,
                                                             HttpServletRequest httpServletRequest )
   {
      logger.trace( "GET: Handling subscribe stream request." );

      // Check that the Spring framework gives us something in the HttpServletRequest field.
      Validate.notNull( httpServletRequest, "The 'httpServletRequest' field was empty." );

      logger.trace( "GET: Handling subscribe stream request from remote host '{}'", httpServletRequest.getRemoteHost() );

      // Update the usage statistics for this controller.
      statisticsCollector.incrementRequests();
      statisticsCollector.addClient( httpServletRequest.getRemoteHost() );

      // Note: by NOT insisting that the RequestBody is provided we can process
      // its absence within this method and provide the appropriate handling.

      // Handle the situation where the Spring framework doesn't give us anything
      // in the stream ID field.
      if( optStreamId.isEmpty() )
      {
         final String errorMessage = "WICA SERVER: The stream ID was empty/null.";
         logger.warn( "GET: Rejected request because '{}'.", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      logger.trace( "GET: Handling subscribe stream request for ID: '{}'", optStreamId );

      // Handle the situation where the stream ID string is blank.
      if( optStreamId.get().isBlank() )
      {
         final String errorMessage = "WICA SERVER: The stream ID was blank.";
         logger.warn( "GET: Rejected request because '{}'.", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Handle the situation where an unknown stream ID is given
      final WicaStreamId wicaStreamId = WicaStreamId.of( optStreamId.get() );
      if ( ! wicaStreamLifecycleService.isKnown(wicaStreamId ) )
      {
         final String errorMessage = "WICA SERVER: The stream ID '" + optStreamId.get() + "' was not recognised.";
         logger.warn( "GET: Rejected request because {}", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Attempt to get the specified stream.
      final Flux<ServerSentEvent<String>> wicaStreamFlux;
      try
      {
         wicaStreamFlux = wicaStreamLifecycleService.getFlux(wicaStreamId );
      }
      catch( Exception ex )
      {
         final String errorMessage = "WICA SERVER: " + ex.getMessage();
         logger.warn( "GET: Rejected request because '{}'.", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      logger.trace( "Returning stream with id: '{}'", optStreamId );
      return new ResponseEntity<>( wicaStreamFlux, HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.warn( "Exception handler was called with exception '{}'", ex.toString() );
   }

/*- Package-level methods ----------------------------------------------------*/

   @Override
   public StatisticsCollector getStatistics()
   {
      return statisticsCollector;
   }

   @Override
   public void resetStatistics()
   {
      statisticsCollector.reset();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
