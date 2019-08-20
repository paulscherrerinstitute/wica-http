/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.stream.WicaStreamBuilder;
import ch.psi.wica.infrastructure.stream.WicaStreamPropertiesBuilder;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.model.stream.WicaStreamProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.codec.ServerSentEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamServerSentEventPublisherTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamServerSentEventPublisherTest.class );

   private WicaStreamServerSentEventPublisher objectUnderTest;

   @MockBean
   private WicaStreamMetadataCollectorService wicaStreamMetadataCollectorServiceMock;

   @MockBean
   private WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService;

   @MockBean
   private WicaStreamPolledValueCollectorService wicaStreamPolledValueCollectorService;

   private final ObjectMapper jsonDecoder = new ObjectMapper();

   private final WicaChannel wicaTestChannel1 =  WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CHAN_1").build();
   private final WicaChannel wicaTestChannel2 =  WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CHAN_2").build();

   private final Map<WicaChannel,WicaChannelMetadata> req1MetadataMap = Map.of( wicaTestChannel1, WicaChannelMetadata.createUnknownInstance(),
                                                                                wicaTestChannel2, WicaChannelMetadata.createUnknownInstance() );

   private final Map<WicaChannel,WicaChannelMetadata> req2MetadataMap = Map.of();

   private final Map<WicaChannel,List<WicaChannelValue>> req1PolledValueMap = Map.of( wicaTestChannel1, List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_1_Request_1_Value_Initial" ) ),
                                                                                      wicaTestChannel2, List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_2_Request_1_Value_Initial" ) ) );

   private final Map<WicaChannel,List<WicaChannelValue>> req2PolledValueMap = Map.of( wicaTestChannel1, List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_1_Request_2_Value_1" ),
                                                                                                                 WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_1_Request_2_Value_2" ) ),
                                                                                      wicaTestChannel2, List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_2_Request_2_Value_1" ),
                                                                                                                 WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_2_Request_2_Value_2" ) ) );

   private final Map<WicaChannel,List<WicaChannelValue>> req1MonitoredValueMap = Map.of( wicaTestChannel1, List.of( WicaChannelValue.createChannelValueConnected( "MonMap_CHAN_1_Request_1_Value_Initial" ) ),
                                                                                         wicaTestChannel2, List.of( WicaChannelValue.createChannelValueConnected( "MonMap_CHAN_2_Request_1_Value_Initial" ) ) );

   private final Map<WicaChannel,List<WicaChannelValue>> req2MonitoredValueMap = Map.of( wicaTestChannel1, List.of( WicaChannelValue.createChannelValueConnected( "MonMap_CHAN_1_Request_2_Value_1" ),
                                                                                                                    WicaChannelValue.createChannelValueConnected( "MonMap_CHAN_1_Request_2_Value_2" ) ),
                                                                                         wicaTestChannel2, List.of( WicaChannelValue.createChannelValueConnected( "MonMap_CHAN_2_Request_2_Value_1" ),
                                                                                                                    WicaChannelValue.createChannelValueConnected( "MonMap_CHAN_2_Request_1_Value_2" )) );

   private final AtomicReference<Map<WicaChannel,WicaChannelMetadata>> atomicMetadataMap = new AtomicReference<>();

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      WicaStreamId.resetAllocationSequencer();
      atomicMetadataMap .set( req1MetadataMap );

      final WicaStreamProperties wicaStreamProperties = WicaStreamPropertiesBuilder.create()
            .withMetadataFluxInterval( 200 )
            .withHeartbeatFluxInterval( 1400 )
            .withPolledValueFluxInterval( 500 )
            .withMonitoredValueFluxInterval( 640 )
            .withFieldsOfInterest( "val;ts" )
            .build();

      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withStreamProperties( wicaStreamProperties )
            .withChannelNameAndStreamProperties( "CHAN_1" )
            .withChannelNameAndStreamProperties( "CHAN_2" )
            .build();

      objectUnderTest = new WicaStreamServerSentEventPublisher( wicaStream,
         wicaStreamMetadataCollectorServiceMock,
         wicaStreamMonitoredValueCollectorService,
         wicaStreamPolledValueCollectorService );
   }

   @Test
   void testShutdown() throws InterruptedException
   {
      logger.info( "Starting Shutdown Test..." );

      // Set up the mock response.
      final ArgumentCaptor<WicaStream>captor1 = ArgumentCaptor.forClass( WicaStream.class );
      final ArgumentCaptor<LocalDateTime>captor2 = ArgumentCaptor.forClass( LocalDateTime.class );
      given( wicaStreamMetadataCollectorServiceMock.get( captor1.capture(), captor2.capture() ) ).willAnswer( rqst -> getMetadataMap() );
      given( wicaStreamMonitoredValueCollectorService.get(captor1.capture(), captor2.capture() ) ).willAnswer( rqst -> req1MonitoredValueMap );
      given( wicaStreamPolledValueCollectorService.get(captor1.capture(), captor2.capture() ) ).willAnswer(rqst -> req1MonitoredValueMap  );

      final var flux = objectUnderTest.getFlux();
      flux.subscribe( (c) -> logger.info( "c is: ------> {}", c ) );
      logger.info( "Subscribed to Object-Under-Test." );

      // Let things run for a while.
      logger.info( "Sleeping for 2s..." );
      Thread.sleep( 2000 );
      logger.info( "Sleep completed." );

      // Now shut down the publisher.
      logger.info( "Shutting down publisher..." );
      objectUnderTest.shutdown();
      logger.info( "Shutdown completed." );

      // Verify that attempts to retrieve the flux following shutdown result
      // in an illegal state exception
      final Exception ex1 = assertThrows( IllegalStateException.class, objectUnderTest::getFlux );
      assertThat( ex1.getMessage(), is("Call to getFlux(), but the publisher has already been shut down." ) );

      // Verify that attempts to retrieve the flux following shutdown result
      // in an illegal state exception
      final Exception ex2 = assertThrows( IllegalStateException.class, objectUnderTest::shutdown );
      assertThat( ex2.getMessage(), is("Call to shutdown(), but the publisher has already been shut down." ) );

      logger.info( "Test completed." );
   }

   @Test
   void testSubscribeStream() throws IOException, InterruptedException
   {
      //---------------------------------------------------------------------------------
      // 1.0 Set up test environment and run SSE Publisher for 1400ms
      //---------------------------------------------------------------------------------

      // Set up the mock response.
      final ArgumentCaptor<WicaStream>captor1 = ArgumentCaptor.forClass( WicaStream.class );
      final ArgumentCaptor<LocalDateTime>captor2 = ArgumentCaptor.forClass( LocalDateTime.class );
      given( wicaStreamMetadataCollectorServiceMock.get( captor1.capture(), captor2.capture() ) ).willAnswer( rqst -> getMetadataMap() );
      given( wicaStreamMonitoredValueCollectorService.getLatest( captor1.capture() ) ).willAnswer( rqst -> req1MonitoredValueMap );
      given( wicaStreamMonitoredValueCollectorService.get( captor1.capture(), captor2.capture() ) ).willAnswer( rqst -> req2MonitoredValueMap );
      given( wicaStreamPolledValueCollectorService.getLatest( captor1.capture() ) ).willAnswer(rqst -> req1PolledValueMap );
      given( wicaStreamPolledValueCollectorService.get( captor1.capture(), captor2.capture() ) ).willAnswer(rqst -> req2PolledValueMap );

      // Subscribe to the stream publisher,
      final List<ServerSentEvent<String>> sseList = new ArrayList<>();
      final var flux = objectUnderTest.getFlux();
      flux.subscribe( (c) -> {
         logger.info( "c is: ------> {}", c  );
         synchronized( this ) {
            sseList.add( c );
         }
      } );

      // Let things run for 1400ms. This should be long enough to receive the
      // following SSE's on the event stream:
      //
      //   1. t =  200ms initial channel metadata
      //   2. t =  500ms polled channel values (1)
      //   3. t =  640ms monitored channel values (1)
      //   4. t = 1000ms polled channel values (2)
      //   5. t = 1280ms monitored channel values (2)
      //   6. t = 1400ms server heartbeat
      Thread.sleep( 1450 );
      objectUnderTest.shutdown();

      // Verify that six notifications were received in the flux
      assertThat(sseList.size(), is(6) );

      //---------------------------------------------------------------------------------
      // 1.1 Verify that the FIRST notification contains channel metadata.
      //---------------------------------------------------------------------------------
      final var sse0 = sseList.get( 0 );
      assertThat( sse0.event(), is( "ev-wica-channel-metadata" )  );
      assertThat( sse0.comment(), containsString( "- channel metadata" ) );

      // Verify that all channels are included in the map and that the type of each channel is UNKNOWN.
      final JsonNode sse0Node = jsonDecoder.readTree( sse0.data() );
      assertThat( sse0Node.isObject(), is( true ) );
      assertThat( sse0Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse0Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse0Node.get( "CHAN_1" ).get( "type").textValue(), containsString( "UNKNOWN" ) );
      assertThat( sse0Node.get( "CHAN_2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );

      //---------------------------------------------------------------------------------
      // 1.2 Verify that the SECOND notification contains the channel's polled values
      //---------------------------------------------------------------------------------
      final var sse1 = sseList.get( 1 );
      assertThat( sse1.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse1.comment(), containsString( "- channel polled values" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse1Node = jsonDecoder.readTree( sse1.data() );
      assertThat( sse1Node.isObject(), is( true ) );
      assertThat( sse1Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse1Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).has( 1 ), is( false ) );
      assertThat( sse1Node.get( "CHAN_2" ).has( 1 ), is( false ) );
      assertThat( sse1Node.get( "CHAN_1" ).get( 0 ).get( "val" ).textValue(), is( "PollMap_CHAN_1_Request_1_Value_Initial" ) );
      assertThat( sse1Node.get( "CHAN_2" ).get( 0 ).get( "val" ).textValue(), is( "PollMap_CHAN_2_Request_1_Value_Initial" ) );

      //---------------------------------------------------------------------------------
      // 1.3 Verify that the THIRD notification contains the channel's monitored values.
      //---------------------------------------------------------------------------------
      final var sse2 = sseList.get( 2 );
      assertThat( sse2.event(), is("ev-wica-channel-value") );
      assertThat( sse2.comment(), containsString( "- channel monitored values" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse2Node = jsonDecoder.readTree( sse2.data() );
      assertThat( sse2Node.isObject(), is( true ) );
      assertThat( sse2Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse2Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse2Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "CHAN_1" ).has( 1 ), is( false ) );
      assertThat( sse2Node.get( "CHAN_2" ).has( 1 ), is( false ) );
      assertThat( sse2Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "MonMap_CHAN_1_Request_1_Value_Initial" ) );
      assertThat( sse2Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "MonMap_CHAN_2_Request_1_Value_Initial" ) );

      //---------------------------------------------------------------------------------
      // 1.4 Verify that the FOURTH notification contains the channel's polled values.
      //---------------------------------------------------------------------------------
      final var sse3 = sseList.get( 3 );
      assertThat( sse3.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse3.comment(), containsString( "- channel polled values" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse3Node = jsonDecoder.readTree( sse3.data() );
      assertThat( sse3Node.isObject(), is( true ) );
      assertThat( sse3Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse3Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse3Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse3Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse3Node.get( "CHAN_1" ).has( 1 ), is( true ) );
      assertThat( sse3Node.get( "CHAN_2" ).has( 1 ), is( true ) );
      assertThat( sse3Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_1_Request_2_Value_1" ) );
      assertThat( sse3Node.get( "CHAN_1" ).get( 1 ).get( "val").textValue(), is( "PollMap_CHAN_1_Request_2_Value_2" ) );
      assertThat( sse3Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_2_Request_2_Value_1" ) );
      assertThat( sse3Node.get( "CHAN_2" ).get( 1 ).get( "val").textValue(), is( "PollMap_CHAN_2_Request_2_Value_2" ) );

      //---------------------------------------------------------------------------------
      // 1.5 Verify that the FIFTH notification contains the channel's monitored values.
      //---------------------------------------------------------------------------------
      final var sse4 = sseList.get( 4 );
      assertThat( sse4.event(), is("ev-wica-channel-value") );
      assertThat( sse4.comment(), containsString( "- channel monitored values" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse4Node = jsonDecoder.readTree( sse4.data() );
      assertThat( sse4Node.isObject(), is( true ) );
      assertThat( sse4Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse4Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse4Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "CHAN_1" ).has( 1 ), is( true ) );
      assertThat( sse4Node.get( "CHAN_2" ).has( 1 ), is( true ) );
      assertThat( sse4Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "MonMap_CHAN_1_Request_2_Value_1" ) );
      assertThat( sse4Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "MonMap_CHAN_2_Request_2_Value_1" ) );

      //---------------------------------------------------------------------------------
      // 1.6 Verify that the SIXTH notification contain's the stream's heartbeat.
      //---------------------------------------------------------------------------------
      final var sse5 = sseList.get( 5 );
      assertThat( sse5.event(), is( "ev-wica-server-heartbeat") );
      assertThat( sse5.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse5.data(), containsString( LocalDate.now().toString() ) );
   }

/*- Private methods ----------------------------------------------------------*/

   private Map<WicaChannel,WicaChannelMetadata> getMetadataMap()
   {
      return atomicMetadataMap.getAndSet( req2MetadataMap );
   }



/*- Nested Classes -----------------------------------------------------------*/

}

