/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamId;
import ch.psi.wica.services.stream.WicaStreamService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle DELETE operations on the
 * {code /ca/streams} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaStreamDeleteController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamDeleteController.class );
   private final WicaStreamService wicaStreamService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling stream DELETE requests.
    *
    * @param wicaStreamService reference to the service object which can be used
    *        to delete the reactive stream.
    */
   public WicaStreamDeleteController( @Autowired WicaStreamService wicaStreamService )
   {
      this.wicaStreamService = Validate.notNull( wicaStreamService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP request to DELETE the wica stream with the specified ID.
    *
    * @param optStreamId the ID of the stream to be deleted.
    *
    * @return an HTTP response whose status code will be set to 'OK' (= 200)
    *     if the delete operation completes successfully or 'Bad Request'
    *     (= 400) if some error occurs.  When successful the body of the HTTP
    *     response contains the ID of the resource which was deleted. When
    *     unsuccessful an additional response header 'X-WICA-ERROR' is written
    *     with a more detailed description of the error.
    */
   @SuppressWarnings( "OptionalUsedAsFieldOrParameterType" )
   @DeleteMapping( value = { "", "/{optStreamId}"}, produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> deleteStream( @PathVariable( required=false ) Optional<String> optStreamId )
   {
      logger.info( "DELETE: Handling delete stream request." );

      // Note: by NOT insisting that the RequestBody is provided we can process
      // its absence within this method and provide the appropriate handling.

      // Handle the situation where the Spring framework doesn't give us anything
      // in the stream ID field.
      if( optStreamId.isEmpty() )
      {
         final String errorMessage = "WICA SERVER: The stream ID was empty/null.";
         logger.warn( "DELETE: Rejected request because '{}'.", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      logger.info( "DELETE: Handling delete stream request for ID: '{}'", optStreamId.get() );

      // Handle the situation where the stream ID string is blank.
      if( optStreamId.get().isBlank() )
      {
         final String errorMessage = "WICA SERVER: The stream ID was blank.";
         logger.warn( "DELETE: Rejected request because '{}'.", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Handle the situation where an unknown stream ID is given
      final WicaStreamId wicaStreamId = WicaStreamId.of( optStreamId.get() );
      if ( ! wicaStreamService.isKnownId( wicaStreamId ) )
      {
         final String errorMessage = "WICA SERVER: The stream ID '" + optStreamId.get() + "' was not recognised.";
         logger.warn( "DELETE: Rejected request because {}", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Attempt to delete the specified stream.
      try
      {
         wicaStreamService.delete( wicaStreamId );
      }
      catch( Exception ex )
      {
         final String errorMessage = "WICA SERVER: " + ex.getMessage();
         logger.warn( "DELETE: Rejected request because '{}'.", errorMessage  );
         return ResponseEntity.status( HttpStatus.BAD_REQUEST ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      logger.info("DELETE: deleted stream with id: '{}'" , optStreamId.get()  );
      return new ResponseEntity<>(  optStreamId.get() , HttpStatus.OK );
   }


   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.warn( "Exception handler was called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
