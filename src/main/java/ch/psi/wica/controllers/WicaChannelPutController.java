/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.EpicsChannelGetAndPutService;
import ch.psi.wica.controlsystem.epics.EpicsChannelName;
import ch.psi.wica.model.app.StatisticsCollectable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle PUT operations on the
 * {code /ca/channel} endpoint.
 */
@RestController
@RequestMapping( "/ca/channel")
class WicaChannelPutController implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelPutController.class );
   private final EpicsChannelGetAndPutService epicsChannelGetAndPutService;
   private final int defaultTimeoutInMillis;
   private final ControllerStatisticsCollector statisticsCollector = new ControllerStatisticsCollector();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling channel PUT requests.
    *
    * @param defaultTimeoutInMillis the default timeout that will be used
    *        when putting data to the wica channel.

    * @param epicsChannelGetAndPutService reference to the service object which can be used
    *        to put values to a wica channel.
    */
   private WicaChannelPutController( @Value( "${wica.channel-get-timeout-interval-in-ms}") int defaultTimeoutInMillis,
                                     @Autowired EpicsChannelGetAndPutService epicsChannelGetAndPutService
   )
   {
      Validate.isTrue( defaultTimeoutInMillis > 0 );
      Validate.notNull(epicsChannelGetAndPutService);

      this.defaultTimeoutInMillis = defaultTimeoutInMillis;
      this.epicsChannelGetAndPutService = Validate.notNull(epicsChannelGetAndPutService);
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP PUT request to set the value of the specified channel.
    *
    * @param channelName the name of the channel whose value is to be changed.
    *
    * @param timeoutInMillis the timeout to be applied when attempting to
    *     get the channel value from the underlying data source. If this
    *     optional parameter is not provided then the configured default
    *     value will be used.
    *
    * @param channelValue the string representation of the new value.
    *
    * @param httpServletRequest contextual information for the request; used
    *     for statistics collection only.
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
                                                  @RequestParam( value="timeout", required = false ) Integer timeoutInMillis,
                                                  @RequestBody String channelValue,
                                                  HttpServletRequest httpServletRequest )
   {
      logger.info( "PUT: Handling channel put request." );

      // Check that the Spring framework gives us something in the HttpServletRequest and channelName fields.
      Validate.notNull( httpServletRequest, "The 'httpServletRequest' field was empty." );
      Validate.notNull( channelName, "The 'channelName' field was empty." );

      logger.info( "PUT: Handling channel put request to channel '{}' from remote host '{}'", channelName, httpServletRequest.getRemoteHost() );

      // Update the usage statistics for this controller.
      statisticsCollector.incrementRequests();
      statisticsCollector.addClient( httpServletRequest.getRemoteHost() );

      // Assign default values when not explicitly provided.
      timeoutInMillis = timeoutInMillis == null ? defaultTimeoutInMillis : timeoutInMillis;

      // Handle failure of the command.
      if ( ! epicsChannelGetAndPutService.put( EpicsChannelName.of( channelName ), channelValue, timeoutInMillis, TimeUnit.MILLISECONDS ) )
      {
         final String errorMessage = "a timeout occurred (channel = '" + channelName + "', value = '" + channelValue + "').";
         logger.warn( "PUT: Rejected request because {}", errorMessage  );
         statisticsCollector.incrementErrors();
         statisticsCollector.incrementReplies();
         return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).header( "X-WICA-ERROR", errorMessage ).build();
      }

      // Handle the normal situation.
      logger.info( "'{}' - OK: PUT channel request.", channelName );
      statisticsCollector.incrementReplies();
      return new ResponseEntity<>("OK", HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      statisticsCollector.incrementErrors();
      logger.warn( "Exception handler was called with exception '{}'", ex.toString() );
   }


/*- Package-level methods ----------------------------------------------------*/

   @Override
   public ControllerStatisticsCollector getStatistics()
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
