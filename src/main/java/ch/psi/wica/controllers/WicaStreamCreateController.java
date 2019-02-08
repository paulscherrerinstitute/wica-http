/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaStream;
import ch.psi.wica.services.epics.EpicsChannelDataService;
import ch.psi.wica.services.stream.WicaStreamService;
import ch.psi.wica.services.stream.WicaStreamPublisher;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle the {code /status} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamCreateController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamCreateController.class );
   private final WicaStreamService wicaStreamService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling stream POST requests.
    *
    * @param wicaStreamService reference to the service object which can be used
    *        to create the reactive stream.
    */
   private WicaStreamCreateController( @Autowired WicaStreamService wicaStreamService )
   {
      this.wicaStreamService = Validate.notNull( wicaStreamService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP POST request to start monitoring (= observing the value
    * of) a list of wica channels.
    *
    * @param jsonStreamConfiguration JSON string providing the stream configuration.
    *
    * @return the ID of the resource which was created. This ID can be used in
    *         one or more subsequent HTTP GET requests to return a stream of
    *         events which follows the evolving state of the underlying Wica
    *         channel.
    */
   @PostMapping( consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> createStream( @RequestBody String jsonStreamConfiguration )
   {
      // Check that the Spring framework gives us something in the stream configuration field.
      Validate.notEmpty( jsonStreamConfiguration,"The 'jsonStreamConfiguration' information was null" );

      logger.info( "POST: Handling create stream request with configuration string: '{}'", jsonStreamConfiguration );

      // Attempt to build a WicaStream based on the supplied configuration string
      final WicaStream wicaStream;
      try
      {
         wicaStream = wicaStreamService.create( jsonStreamConfiguration );
      }
      catch( Exception ex )
      {
         final String errorMessage = ex.getMessage();
         final String className = ex.getStackTrace().length > 1 ?  "C="  + ex.getStackTrace()[ 1 ].getClassName() + "," : "";
         final String methodName = ex.getStackTrace().length > 1 ? "M=" + ex.getStackTrace()[ 1 ].getMethodName() + "," : "";
         final String lineNumber = ex.getStackTrace().length > 1 ? "L="   + ex.getStackTrace()[ 1 ].getLineNumber() : "";
         logger.warn( "POST: Rejected request because {} [{} {} {}]", errorMessage, className, methodName, lineNumber );
         return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
      }

      logger.info("POST: allocated stream with id: '{}'" , wicaStream.getWicaStreamId() );
      return new ResponseEntity<>( wicaStream.getWicaStreamId().asString(), HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "SSE Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
