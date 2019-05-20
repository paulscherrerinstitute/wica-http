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
import org.springframework.web.bind.annotation.*;

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
   private WicaStreamDeleteController( @Autowired WicaStreamService wicaStreamService )
   {
      this.wicaStreamService = Validate.notNull( wicaStreamService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP DELETE request to destroy a wica stream.
    *
    * @param id the ID of the stream to be deleted.
    * @return the ID.
    */
   @DeleteMapping( value="/{id}", produces=MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> deleteServerSentEventStream( @PathVariable String id )
   {
      // Check that the Spring framework gives us something in the channelNames field.
      Validate.notNull( id, "The event stream 'id' field was empty." );

      logger.info( "DELETE: Handling delete stream request for ID: '{}'", id );

      // Handle the situation where an unknown WicaStreamId is given
      final WicaStreamId wicaStreamId = WicaStreamId.of(id );
      if ( ! wicaStreamService.isKnownId( wicaStreamId ) )
      {
         logger.info( "DELETE: Rejected request because the event stream 'id' was not recognised." );
         return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
      }

      // TODO: need to add error handling here
      wicaStreamService.delete( wicaStreamId );

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
