/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.EpicsChannelDataService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a SpringBoot REST Controller to handle the {code /status} endpoint.
 */
@RestController
@RequestMapping( "/ca/streams")
class WicaChannelController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * The format which will be used when making String representations
    * of the times/dates in this class.
    */
   private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

   private final Logger logger = LoggerFactory.getLogger(WicaChannelController.class );

   private final EpicsChannelDataService epicsChannelDataService;

   private final int channelGetTimeoutInterval;

   private final Map<WicaStreamId, Flux<ServerSentEvent<String>>> eventStreamFluxMap = new ConcurrentHashMap<>();

   private LocalDateTime lastUpdateTime = LocalDateTime.MAX;


/*- Main ---------------------------------------------------------------------*/

   /**
    * Constructs a new controller which to handle all REST operations associated with EPICS event streams.
    */
   @Autowired
   private WicaChannelController( @Autowired EpicsChannelDataService epicsChannelDataService,
                                  @Value( "${wica.channel_get_timeout_interval_in_ms}" ) int channelGetTimeoutInterval )
   {
      //logger.info( "Created new event stream with heartbeat interval of {} seconds.", eventStreamHeartBeatInterval );
      this.epicsChannelDataService = epicsChannelDataService;
      this.channelGetTimeoutInterval = channelGetTimeoutInterval;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP GET request to return the channel value.
    *
    * @param channelName the name of the channel.
    *
    * @return the value of the channel.
    */
   @GetMapping( value="/channels/{channelName}", produces = MediaType.APPLICATION_JSON_VALUE )
   public ResponseEntity<WicaChannelValue> getChannel( @PathVariable String channelName )
   {
      // Check that the Spring framework gives us something in the channel field.
      Validate.notBlank(channelName, "The 'channelName' field was blank" );

      logger.info("GET: Handling get channel request for channel: '{}'", channelName );

      final WicaChannelName wicaChannelName = new WicaChannelName( channelName );
      epicsChannelDataService.startMonitoring(wicaChannelName);

      try
      {
         if ( epicsChannelDataService.waitForFirstValue( wicaChannelName, TimeUnit.MILLISECONDS, channelGetTimeoutInterval ) )
         {
            logger.warn( "EPICS get on channelName '{}' timed out after {} seconds", wicaChannelName, channelGetTimeoutInterval );
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR );
         }
      }
      catch ( InterruptedException ex )
      {
         logger.warn( "EPICS get on channel '{}' was interrupted", wicaChannelName );
         return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR );
      }

      final WicaChannelValue wicaChannelValue = epicsChannelDataService.getChannelValue( wicaChannelName );
      return new ResponseEntity<>( wicaChannelValue, HttpStatus.OK );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
