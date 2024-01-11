/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.services.stream.WicaStreamLifecycleService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle POST operations on the
 * {code /ca/streams} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamCreateController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger( WicaStreamCreateController.class );
   private final WicaStreamLifecycleService wicaStreamLifecycleService;
   private final ControllerStatistics statisticsCollector;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling stream POST requests.
    *
    * @param wicaStreamLifecycleService reference to the service object which can be used
    *        to create the reactive stream.
    * @param statisticsCollectionService an object which will collect the statistics
    *        associated with this class instance.
    */
   public WicaStreamCreateController( @Autowired WicaStreamLifecycleService wicaStreamLifecycleService,
                                      @Autowired StatisticsCollectionService statisticsCollectionService )
   {
      this.wicaStreamLifecycleService = Validate.notNull( wicaStreamLifecycleService, "The 'wicaStreamLifecycleService' argument is null.");
      this.statisticsCollector = new ControllerStatistics("WICA STREAM CREATE CONTROLLER" );
      statisticsCollectionService.addCollectable( statisticsCollector );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP request to CREATE a new stream to monitor the
    * the wica channels specified in the JSON stream configuration. When
    * successful the HTTP response includes the ID of the stream which was
    * created. This can be used in subsequent stream GET requests to
    * subscribe to an event stream which tracks the evolving status of
    * the channels.
    *
    * @param optJsonStreamConfiguration JSON string providing the stream
    *        configuration.
    *
    * @param httpServletRequest contextual information for the request; used
    *     for statistics collection only.
    *
    * @return an HTTP response whose status code will be set to 'OK' (= 200)
    *      if the create operation completes successfully or 'Bad Request'
    *      (= 400) if some error occurs.  When successful the body of the
    *      HTTP response contains the ID of the stream which was created.
    *      When unsuccessful an additional response header 'X-WICA-ERROR'
    *      is written with a more detailed description of the error.
    */
   @SuppressWarnings( "OptionalUsedAsFieldOrParameterType" )
   @PostMapping( consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> create( @RequestBody( required = false ) Optional<String> optJsonStreamConfiguration,
                                         HttpServletRequest httpServletRequest )
   {
      logger.trace( "POST: Handling create stream request." );

      // Check that the Spring framework gives us something in the HttpServletRequest field.
      Validate.notNull( httpServletRequest, "The 'httpServletRequest' field was empty." );

      logger.trace( "POST: Handling create stream request from remote host '{}'", httpServletRequest.getRemoteHost() );

      // Update the usage statistics for this controller.
      statisticsCollector.incrementRequests();
      statisticsCollector.addClientIpAddr(httpServletRequest.getRemoteHost() );

      // Note: by NOT insisting that the RequestBody is provided we can process
      // its absence within this method and provide the appropriate handling.

      // Handle the situation where the Spring framework doesn't give us anything
      // in the stream configuration field.
      if( optJsonStreamConfiguration.isEmpty() )
      {
         final String errorMessage = "WICA SERVER: The stream configuration string was empty/null.";
         logger.warn( "POST: Rejected request because '{}'.", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      logger.trace( "POST: Handling create stream request with configuration string: '{}'", optJsonStreamConfiguration.get() );


      // Handle the situation where the stream configuration string is blank.
      if( optJsonStreamConfiguration.get().isBlank() )
      {
         final String errorMessage = "WICA SERVER: The stream configuration string was blank.";
         logger.warn( "POST: Rejected request because '{}'.", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Attempt to build a WicaStream based on the supplied configuration string
      final WicaStream wicaStream;
      try
      {
         wicaStream = wicaStreamLifecycleService.create( optJsonStreamConfiguration.get() );
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
         logger.warn( "POST: Rejected request because '{}'.", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      appLogger.info( "POST: allocated stream with id: '{}' following request from client with IP: '{}'", wicaStream.getWicaStreamId(), httpServletRequest.getRemoteHost() );
      logger.trace( "POST: allocated stream with id: '{}'" , wicaStream.getWicaStreamId() );

      statisticsCollector.incrementReplies();
      return new ResponseEntity<>( wicaStream.getWicaStreamId().asString(), HttpStatus.OK );
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
