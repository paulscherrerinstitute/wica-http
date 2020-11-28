/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.services.stream.WicaStreamLifecycleService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle DELETE and POST operations
 * on the {code /ca/streams} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamPutController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger( WicaStreamPutController.class );
   private final WicaStreamLifecycleService wicaStreamLifecycleService;
   private final ControllerStatistics statisticsCollector;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling stream RELOAD requests.
    *
    * @param wicaStreamLifecycleService reference to the service object which can be used
    *        to delete the reactive stream.
    * @param statisticsCollectionService an object which will collect the statistics
    *        associated with this class instance.
    */
   public WicaStreamPutController( @Autowired WicaStreamLifecycleService wicaStreamLifecycleService,
                                   @Autowired StatisticsCollectionService statisticsCollectionService)
   {
      this.wicaStreamLifecycleService = Validate.notNull(wicaStreamLifecycleService);

      this.statisticsCollector = new ControllerStatistics("WICA STREAM PUT CONTROLLER" );
      statisticsCollectionService.addCollectable( statisticsCollector );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP request to PUT (= reconfigure the resource) associated
    * with the specified wica stream ID.
    *
    * @param optStreamId the ID of the stream to be reloaded.
    * @param action keyword specifying the action that is to take place.
    *    Currently the only supported action is "RELOAD"
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
   //@SuppressWarnings( "OptionalUsedAsFieldOrParameterType" )
   @PutMapping( value = { "", "/{optStreamId}"}, produces = MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> put( @PathVariable Optional<String> optStreamId,
                                      @RequestParam( "action" ) String action,
                                      HttpServletRequest httpServletRequest )
   {
      // Check that the Spring framework gives us something in the HttpServletRequest field.
      Validate.notNull( httpServletRequest, "The 'httpServletRequest' field was empty." );

      final String triggerMethod = httpServletRequest.getMethod();
      logger.trace( "PUT: Handling put stream request triggered by HTTP {} method.", triggerMethod );
      logger.trace( "PUT: The request originated from remote host '{}'", httpServletRequest.getRemoteHost() );

      // Update the usage statistics for this controller.
      statisticsCollector.incrementRequests();
      statisticsCollector.addClientIpAddr( httpServletRequest.getRemoteHost() );

      // Note: by NOT insisting that the Path Variable is provided we can process
      // its absence within this method and provide the appropriate handling.
      // Handle the situation where the Spring framework doesn't give us anything
      // in the stream ID field.
      if( optStreamId.isEmpty() )
      {
         final String errorMessage = "WICA SERVER: The stream ID was empty.";
         logger.warn( "PUT: Rejected request because '{}'.", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      logger.trace( "PUT: Handling put stream request for ID: '{}'", optStreamId.get() );

      // Handle the situation where the stream ID string is blank.
      if( optStreamId.get().isBlank() )
      {
         final String errorMessage = "WICA SERVER: The stream ID was blank.";
         logger.warn( "PUT: Rejected request because '{}'.", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Handle the situation where an unknown stream ID is given
      final WicaStreamId wicaStreamId = WicaStreamId.of( optStreamId.get() );
      if ( ! wicaStreamLifecycleService.isKnown( wicaStreamId ) )
      {
         final String errorMessage = "WICA SERVER: The stream ID '" + optStreamId.get() + "' was not recognised.";
         logger.warn( "PUT: Rejected request because {}", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Ensure the action argument specifies "RELOAD" (which is the only
      // action that is currently supported).
      if ( ! action.equalsIgnoreCase( "RELOAD" ) )
      {
         final String errorMessage = "WICA SERVER: The action '" + action + "' was not recognised.";
         logger.warn( "PUT: Rejected request because {}", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Attempt to reload the monitoring and polling on the specified stream.
      try
      {
         wicaStreamLifecycleService.restartMonitoring( wicaStreamId );
         wicaStreamLifecycleService.restartPolling( wicaStreamId );
      }
      catch( Exception ex )
      {
         final String errorMessage;
         if ( ex.getMessage() == null )
         {
            final String exceptionClass = ex.getClass().toString();
            errorMessage = "WICA SERVER: An exception occurred of class: '" + exceptionClass + "'.";
         }
         else
         {
            errorMessage = "WICA SERVER: " + ex.getMessage();
         }
         logger.warn( "PUT: Rejected request because '{}'.", errorMessage  );
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      logger.trace( "PUT: reloaded stream with id: '{}'" , wicaStreamId );
      appLogger.info( "PUT: reloaded stream with id: '{}' following request from client with IP: '{}'", wicaStreamId, httpServletRequest.getRemoteHost() );
      statisticsCollector.incrementReplies();
      return new ResponseEntity<>( wicaStreamId.asString(), HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      statisticsCollector.incrementErrors();
      logger.warn( "Exception handler was called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
