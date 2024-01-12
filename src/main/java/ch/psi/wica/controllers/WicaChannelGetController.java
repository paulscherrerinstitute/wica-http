/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.io.EpicsChannelReaderService;
import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import ch.psi.wica.infrastructure.channel.WicaChannelDataSerializerBuilder;
import ch.psi.wica.model.app.StatisticsCollectionService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
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
   private final EpicsChannelReaderService epicsChannelReaderService;
   private final int defaultTimeoutInMillis;
   private final int defaultNumericScale;
   private final String channelValueDefaultFieldsOfInterest;
   private final String channelMetadataDefaultFieldsOfInterest;

   private final ControllerStatistics statisticsCollector;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller for handling channel GET requests.
    *
    * @param defaultTimeoutInMillis the default timeout that will be used
    *        when getting data from the wica channel.
    *
    * @param defaultNumericScale the default numeric scale that will be used
    *        when returning the value of the channel.
    *
    * @param epicsChannelReaderService reference to the service object which can be used
    *        to get values to or from a wica channel.
    * @param statisticsCollectionService an object which will collect the statistics
    *        associated with this class instance.
    */
   private WicaChannelGetController( @Value( "${wica.channel-get-timeout-interval-in-ms}") int defaultTimeoutInMillis,
                                     @Value( "${wica.channel-get-numeric-scale}") int defaultNumericScale,
                                     @Value( "${wica.channel-get-value-default-fields-of-interest}") String channelValueDefaultFieldsOfInterest,
                                     @Value( "${wica.channel-get-metadata-default-fields-of-interest}") String channelMetadataDefaultFieldsOfInterest,
                                     @Autowired EpicsChannelReaderService epicsChannelReaderService,
                                     @Autowired StatisticsCollectionService statisticsCollectionService )
   {
      Validate.isTrue( defaultTimeoutInMillis > 0 );
      Validate.isTrue( defaultNumericScale > 0 );
      Validate.notNull( epicsChannelReaderService, "The 'epicsChannelReaderService' argument is null." );

      this.defaultTimeoutInMillis = defaultTimeoutInMillis;
      this.defaultNumericScale = defaultNumericScale;
      this.channelValueDefaultFieldsOfInterest = channelValueDefaultFieldsOfInterest;
      this.channelMetadataDefaultFieldsOfInterest = channelMetadataDefaultFieldsOfInterest;
      this.epicsChannelReaderService = epicsChannelReaderService;

      this.statisticsCollector = new ControllerStatistics("WICA CHANNEL GET CONTROLLER" );
      statisticsCollectionService.addCollectable( statisticsCollector );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP GET request to return the metadata of the specified channel.
    *
    * @param channelName the name of the channel whose metadata is to be fetched.
    *
    * @param timeoutInMillis the timeout to be applied when attempting to
    *     get the channel metadata from the underlying data source. If this
    *     optional parameter is not provided then the configured default
    *     value will be used.
    *
    * @param numericScale the default number of digits after the decimal
    *     point when getting the current metadata of a wica channel. If this
    *     optional parameter is not provided then the configured default
    *     value will be used.
    *
    * @param fieldsOfInterest the default fields of interest to be returned
    *      when getting the current metadata of a wica channel. If this
    *     optional parameter is not provided then the configured default
    *     value will be used.
    *
    * @param httpServletRequest contextual information for the request; used
    *     for statistics collection only.
    *
    * @return ResponseEntity set to return an HTTP status code of 'OK'
    *    (= 200) and a body which includes the JSON string representation of
    *    the current channel value. If a timeout occurred the JSON representation
    *    will be set to show that the channel metedata is currently UNKNOWN.
    */
   @GetMapping( value="/{channelName}", produces = MediaType.APPLICATION_JSON_VALUE )
   public ResponseEntity<String> getChannelValue( @PathVariable String channelName,
                                                  @RequestParam( value="timeout", required = false ) Integer timeoutInMillis,
                                                  @RequestParam( value="numericScale", required = false ) Integer numericScale,
                                                  @RequestParam( value="fieldsOfInterest", required = false ) String fieldsOfInterest,
                                                  HttpServletRequest httpServletRequest )
   {
      logger.info( "GET: Handling channel get request." );

      // Check that the Spring framework gives us something in the HttpServletRequest and channelName fields.
      Validate.notNull( httpServletRequest, "The 'httpServletRequest' field was empty." );
      Validate.notNull( channelName, "The 'channelName' field was empty." );

      logger.info( "GET: Handling channel get request for channel '{}' from remote host '{}'", channelName, httpServletRequest.getRemoteHost() );

      // Update the usage statistics for this controller.
      statisticsCollector.incrementRequests();
      statisticsCollector.addClientIpAddr(httpServletRequest.getRemoteHost() );

      // Assign default values when not explicitly provided.
      timeoutInMillis = timeoutInMillis == null ? defaultTimeoutInMillis : timeoutInMillis;
      numericScale = numericScale == null ? defaultNumericScale : numericScale;
      fieldsOfInterest = fieldsOfInterest == null ? channelValueDefaultFieldsOfInterest : fieldsOfInterest;

      final var wicaChannelValue = epicsChannelReaderService.readChannelValue( EpicsChannelName.of( channelName ), timeoutInMillis, TimeUnit.MILLISECONDS );
      final var fieldsOfInterestSet = Set.of( fieldsOfInterest.split( ";" ) );

      final var serializer = WicaChannelDataSerializerBuilder
            .create()
            .withFieldsOfInterest( fieldsOfInterestSet )
            .withNumericScale( numericScale )
            .withQuotedNumericStrings( false )
            .build();

      logger.info( "'{}' - OK: Returning wica channel value.", channelName );
      statisticsCollector.incrementReplies();
      return new ResponseEntity<>( serializer.writeToJson( wicaChannelValue ), HttpStatus.OK );
   }

   /**
    * Handles an HTTP GET request to return the metadata of the specified channel.
    *
    * @param channelName the name of the channel whose metadata is to be fetched.
    *
    * @param timeoutInMillis the timeout to be applied when attempting to
    *     get the channel metadata from the underlying data source. If this
    *     optional parameter is not provided then the configured default
    *     will be used.
    *
    * @param numericScale the default number of digits after the decimal
    *     point when getting the current metadata for a wica channel. If this
    *     optional parameter is not provided then the configured default
    *     will be used.
    *
    * @param fieldsOfInterest the default fields of interest to be returned
    *     when getting the current metadata for a wica channel. If this
    *     optional parameter is not provided then the configured default
    *     will be used.
    *
    * @param httpServletRequest contextual information for the request; used
    *     for statistics collection only.
    *
    * @return ResponseEntity set to return an HTTP status code of 'OK'
    *    (= 200) and a body which includes the JSON string representation of
    *    the current channel value. If a timeout occurred the JSON representation
    *    will be set to show that the channel is currently disconnected.
    */
   @GetMapping( value="/metadata/{channelName}", produces = MediaType.APPLICATION_JSON_VALUE )
   public ResponseEntity<String> getChannelMetadata( @PathVariable String channelName,
                                                     @RequestParam( value="timeout", required = false ) Integer timeoutInMillis,
                                                     @RequestParam( value="numericScale", required = false ) Integer numericScale,
                                                     @RequestParam( value="fieldsOfInterest", required = false ) String fieldsOfInterest,
                                                     HttpServletRequest httpServletRequest )
   {
      logger.info( "GET: Handling channel get metadata request." );

      // Check that the Spring framework gives us something in the HttpServletRequest and channelName fields.
      Validate.notNull( httpServletRequest, "The 'httpServletRequest' field was empty." );
      Validate.notNull( channelName, "The 'channelName' field was empty." );

      logger.info( "GET: Handling channel get metadata request for channel '{}' from remote host '{}'", channelName, httpServletRequest.getRemoteHost() );

      // Update the usage statistics for this controller.
      statisticsCollector.incrementRequests();
      statisticsCollector.addClientIpAddr(httpServletRequest.getRemoteHost() );

      // Assign default values when not explicitly provided.
      timeoutInMillis = timeoutInMillis == null ? defaultTimeoutInMillis : timeoutInMillis;
      numericScale = numericScale == null ? defaultNumericScale : numericScale;
      fieldsOfInterest = fieldsOfInterest == null ? channelMetadataDefaultFieldsOfInterest : fieldsOfInterest;

      final var wicaChannelValue = epicsChannelReaderService.readChannelMetadata( EpicsChannelName.of( channelName ), timeoutInMillis, TimeUnit.MILLISECONDS );
      final var fieldsOfInterestSet = Set.of( fieldsOfInterest.split( ";" ) );

      final var serializer = WicaChannelDataSerializerBuilder
            .create()
            .withFieldsOfInterest( fieldsOfInterestSet )
            .withNumericScale( numericScale )
            .withQuotedNumericStrings( false )
            .build();

      logger.info( "'{}' - OK: Returning wica channel metadata.", channelName );
      statisticsCollector.incrementReplies();
      return new ResponseEntity<>( serializer.writeToJson( wicaChannelValue ), HttpStatus.OK );
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
