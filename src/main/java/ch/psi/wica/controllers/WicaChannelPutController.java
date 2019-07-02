/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.services.channel.WicaChannelService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle PUT operations on the
 * {code /ca/channel} endpoint.
 */
@RestController
@RequestMapping( "/ca/channel")
class WicaChannelPutController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelPutController.class );
   private final WicaChannelService wicaChannelService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling channel PUT requests.
    *
    * @param wicaChannelService reference to the service object which can be used
    *        to put values to a wica channel.
    */
   private WicaChannelPutController( @Autowired WicaChannelService wicaChannelService )
   {
      this.wicaChannelService = Validate.notNull( wicaChannelService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP PUT request to set the value of the specified channel.
    *
    * @param channelName the name of the channel whose value is to be changed.
    *
    * @param timeoutInMilliseconds the timeout to be applied when attempting to
    *     put the channel value to the underlying data source. If a timeout
    *     occurs the returned value will be set to indicate an internal
    *     server error.
    *
    * @param channelValue the string representation of the new value.
    *
    * @return ResponseEntity set to return an HTTP status code of 'OK'
    *    (= 200) if the put operation completes successfully or
    *    'Internal Server Error' (= 500) if a timeout occurs.  When
    *    successful the body of the response contains the string "OK".
    *    When unsuccessful the response header 'X-WICA-ERROR' is
    *    written with a description of the error.
    */
   @PutMapping( value="/{channelName}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> putChannelValue( @PathVariable String channelName,
                                                  @RequestParam( value="timeout", required = false, defaultValue = "1000" ) int timeoutInMilliseconds,
                                                  @RequestBody String channelValue )
   {
      // Check that the Spring framework gives us something in the channelName field.
      Validate.notNull( channelName, "The 'channelName' field was empty." );

      logger.info( "'{}' - Handling PUT channel request...", channelName );

      // Handle failure of the command.
      if ( ! wicaChannelService.put( WicaChannelName.of( channelName ), channelValue, timeoutInMilliseconds, TimeUnit.MILLISECONDS ) )
      {
         final String errorMessage = "a timeout occurred (channel = '" + channelName + "', value = '" + channelValue + "').";
         logger.warn( "PUT: Rejected request because {}", errorMessage  );
         return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Handle the normal situation.
      logger.info( "'{}' - OK: PUT channel request.", channelName );
      return new ResponseEntity<>("OK", HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "ERROR: Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
