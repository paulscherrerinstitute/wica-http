/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.WicaObjectToJsonSerializer;
import ch.psi.wica.infrastructure.WicaServerSentEventBuilder;
import ch.psi.wica.model.*;
import ch.psi.wica.services.epics.EpicsChannelDataService;
import ch.psi.wica.services.stream.WicaStreamCreator;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


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

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private final Logger logger = LoggerFactory.getLogger( WicaStreamCreateController.class );

   private final EpicsChannelDataService epicsChannelDataService;
   private final WicaStreamCreator wicaStreamCreator;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new controller which to handle all REST operations associated with EPICS event streams.
    *
    * @param epicsChannelDataService reference to the service object which provides
    *        the source of EPICS data for this controller.
    *
    * @param wicaStreamCreator reference to the service object which can be used
    *        to create the reactive streams.
    */
   private WicaStreamCreateController( @Autowired EpicsChannelDataService epicsChannelDataService,
                                       @Autowired WicaStreamCreator wicaStreamCreator )
   {
      //logger.info( "Created new event stream with heartbeat interval of {} seconds.", eventStreamHeartBeatInterval );
      this.epicsChannelDataService = Validate.notNull( epicsChannelDataService );
      this.wicaStreamCreator = Validate.notNull( wicaStreamCreator );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles an HTTP POST request to start monitoring (= observing the value
    * of) a list of EPICS Process Variables ("PV's").
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
         wicaStream = wicaStreamCreator.create( jsonStreamConfiguration );
      }
      catch( Exception ex )
      {
         final String errorMessage = ex.getMessage();
         logger.warn( "POST: Rejected request because {} ", errorMessage );
         return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
      }

      wicaStream.setLastPublicationTime();

      final Flux<ServerSentEvent<String>> heartbeatFlux = createHeartbeatFlux( wicaStream );
      final Flux<ServerSentEvent<String>> channelMetadataFlux = createChannelMetadataFlux( wicaStream );
      final Flux<ServerSentEvent<String>> channelValueFlux = createChannelValueFlux( wicaStream );
      final Flux<ServerSentEvent<String>> channelValueUpdateFlux = createChannelValueUpdateFlux( wicaStream );

      // Create a single Flux which merges all of the above.
      final Flux<ServerSentEvent<String>> eventStreamFlux = heartbeatFlux.mergeWith( channelMetadataFlux )
                                                                         .mergeWith( channelValueFlux )
                                                                         .mergeWith( channelValueUpdateFlux )
                                                                         .doOnCancel( () -> {
                                                                            logger.warn( "eventStreamFlux was cancelled" );
                                                                            handleErrors( wicaStream.getWicaStreamId() );
                                                                         } );
                                                                         //.log();
      // Store it in the stream map
      wicaStream.setFlux( eventStreamFlux );

      // Lastly set up monitors on all the channels of interest.
      epicsChannelDataService.startMonitoring( wicaStream );

      logger.info("POST: allocated stream with id: '{}'" , wicaStream.getWicaStreamId() );
      return new ResponseEntity<>( wicaStream.getWicaStreamId().asString(), HttpStatus.OK );
   }

   @ExceptionHandler( Exception.class )
   public void handleException( Exception ex)
   {
      logger.info( "SSE Exception handler called with exception '{}'", ex.toString() );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Creates the HEARTBEAT FLUX.
    *
    * The purpose of this flux is to periodically tell remote clients that the stream is
    * still alive. If remote clients do not receive heartbeat events within the expected
    * time intervals then they will conclude that the event stream is no longer active.
    * This may then lead to closing the event stream on the client side and sending a
    * request to the server to recreate the stream.
    *
    * @param wicaStream the stream.
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createHeartbeatFlux( WicaStream wicaStream )
   {
      return Flux.interval(Duration.ofMillis( wicaStream.getHeartbeatFluxInterval()))
            .map(l -> {
               logger.trace("heartbeat flux is publishing new SSE...");
               return WicaServerSentEventBuilder.EV_WICA_SERVER_HEARTBEAT.build( wicaStream.getWicaStreamId(), LocalDateTime.now().toString());
            })
            .doOnCancel(() -> logger.warn("heartbeat flux was cancelled"));
      //.log();
   }

   /**
    * Creates the CHANNEL METADATA FLUX.
    *
    * The purpose of this flux is to publish additional information about the nature of the
    * underlying EPICS channel. This may include the channel's type and where relevant the
    * channel's display, alarm and operator limits.
    *
    * This flux runs just once and delivers its payload on inital connection to the stream.
    *
    * @param wicaStream the stream.
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChannelMetadataFlux( WicaStream wicaStream )
   {
      return Flux.range( 1, 1 )
            .map( l -> {
               logger.trace( "channel-metadata flux is publishing new SSE..." );
               final Map<WicaChannelName, WicaChannelMetadata> channelMetadataMap = epicsChannelDataService.getChannelMetadata( wicaStream );
               return WicaServerSentEventBuilder.EV_WICA_CHANNEL_METADATA.build ( wicaStream.getWicaStreamId(), wicaStream.getSerializer().convertWicaChannelMetadataMapToJsonRepresentation( channelMetadataMap ) );
            } )
            .doOnCancel( () -> logger.warn( "channel-metadata flux was cancelled" ) );
      //.log();
   }

   /**
    *  Creates the CHANNEL VALUE UPDATE FLUX.
    *
    * The purpose of this flux is to publish the last received values for any channels
    * which have received monitor notifications since the last update.
    *
    * This flux runs with a periodicity defined by the channelValueUpdateFluxInterval.
    *
    * @param wicaStream the stream.
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChannelValueUpdateFlux( WicaStream wicaStream )
   {
      return Flux.interval( Duration.ofMillis( wicaStream.getChannelValueUpdateFluxInterval() ) )
            .map( l -> {
               logger.trace( "channel-value-update flux is publishing new SSE..." );
               final Map<WicaChannelName, List<WicaChannelValue>> updatedChannelValues = epicsChannelDataService.getLaterThan( wicaStream, wicaStream.getLastPublicationTime() );
               wicaStream.setLastPublicationTime();
               final Map<WicaChannelName, List<WicaChannelValue>> updatedChannelValuesMapped = wicaStream.map( updatedChannelValues );
               final WicaObjectToJsonSerializer serializer = wicaStream.getSerializer();
               final String jsonValueString = serializer.convertWicaChannelValueMapToJsonRepresentation( updatedChannelValuesMapped );
               final ServerSentEvent<String> str = WicaServerSentEventBuilder.EV_WICA_CHANNEL_VALUE_CHANGES.build(  wicaStream.getWicaStreamId(), jsonValueString );
               return str;
            } )
            .doOnCancel( () -> logger.warn( "channel-value-update flux was cancelled" ) );
      //.log();
   }

  /**
    * Create the CHANNEL VALUE FLUX.
    *
    * The purpose of this flux is to publish the last received value for ALL channels
    * in the stream.
    *
    * This flux runs with a  periodicity defined by the channelValueFluxInterval.
    *
    * @param wicaStream the stream.
    * @return the flux.
    */
   private Flux<ServerSentEvent<String>> createChannelValueFlux( WicaStream wicaStream )
   {
      return Flux.range(1, 1)
            .map(l -> {
               logger.trace("channel-value flux is publishing new SSE...");
               final Map<WicaChannelName, List<WicaChannelValue>> allChannelValues = epicsChannelDataService.getLaterThan( wicaStream, LONG_AGO );
               wicaStream.setLastPublicationTime();
               final Map<WicaChannelName, List<WicaChannelValue>> allChannelValuesMapped = wicaStream.map( allChannelValues );
               final String jsonValueString = wicaStream.getSerializer().convertWicaChannelValueMapToJsonRepresentation( allChannelValuesMapped );
               final ServerSentEvent<String> str = WicaServerSentEventBuilder.EV_WICA_CHANNEL_VALUE_ALLDATA.build( wicaStream.getWicaStreamId(), jsonValueString);
               return str;
            })
            .doOnCancel(() -> logger.warn("channel-value flux was cancelled"));
      //.log();
   }

   private void handleErrors( WicaStreamId id )
   {
      logger.info( "Some error occurred on monitor stream with Id: '{}' ", id );
      logger.info( "Probably the client navigated away from the webpage and the eventsource was closed by the browser !! " );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
