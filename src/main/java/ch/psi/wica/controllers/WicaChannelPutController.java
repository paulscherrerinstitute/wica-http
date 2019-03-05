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
    * @param channelValue the string representation of the new value.
    *
    * @return the returned event stream.
    */
   @PutMapping( value="/{channelName}", produces = MediaType.TEXT_PLAIN_VALUE )
   public ResponseEntity<String> putChannelValue( @PathVariable String channelName,
                                                  @RequestBody String channelValue )
   {
      // Check that the Spring framework gives us something in the channelName field.
      Validate.notNull( channelName, "The 'channelName' field was empty." );

      logger.info( "PUT: Handling put channel request for channel named: '{}', value '{}'", channelName, channelValue );

      // Handle the normal case
      wicaChannelService.put( WicaChannelName.of( channelName ), channelValue );

      logger.info("PUT: handled request on channel '{}' ok" , channelName );
      return new ResponseEntity<>("OK", HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
