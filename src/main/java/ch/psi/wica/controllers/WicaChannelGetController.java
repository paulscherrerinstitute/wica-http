/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.WicaChannelValueSerializer;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
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
 * Provides a SpringBoot REST Controller to handle GET operations on the
 * {code /ca/channel} endpoint.
 */
@RestController
@RequestMapping( "/ca/channel")
class WicaChannelGetController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelGetController.class );
   private final WicaChannelService wicaChannelService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling channel GET requests.
    *
    * @param wicaChannelService reference to the service object which can be used
    *        to get values to or from a wica channel.
    */
   private WicaChannelGetController( @Autowired WicaChannelService wicaChannelService )
   {
      this.wicaChannelService = Validate.notNull( wicaChannelService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP GET request to return the value of the specified channel.
    *
    * @param channelName the name of the channel whose value is to be fetched.
    *
    * @param timeoutInMilliseconds the timeout to be applied when attempting to
    *     get the channel value from the underlying data source.
    *
    * @return ResponseEntity set to return an HTTP status code of 'OK'
    *    (= 200) and a body which includes the JSON string representation of
    *    the current channel value. If a timeout occurred the JSON representation
    *    will be set to show that the channel is currently disconnected.
    */
   @GetMapping( value="/{channelName}", produces = MediaType.APPLICATION_JSON_VALUE )
   public ResponseEntity<String> getChannelValue( @PathVariable String channelName,
                                                  @RequestParam( value="timeout", required = false, defaultValue = "1000" ) int timeoutInMilliseconds )
   {
      // Check that the Spring framework gives us something in the channelName field.
      Validate.notNull( channelName, "The 'channelName' field was empty." );

      logger.info( "'{}' - Handling GET channel request...", channelName );

      // Handle the normal case
      final WicaChannelValue wicaChannelValue = wicaChannelService.get( WicaChannelName.of( channelName ), timeoutInMilliseconds, TimeUnit.MILLISECONDS );

      final WicaChannelValueSerializer wicaChannelValueSerializer = new WicaChannelValueSerializer( 8, false );

      logger.info( "'{}' - OK: Returning wica channel value.", channelName );
      return new ResponseEntity<>( wicaChannelValueSerializer.serialize( wicaChannelValue ), HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "ERROR: Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
