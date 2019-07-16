/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;

import ch.psi.wica.model.channel.WicaChannelProperties;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.codec.ServerSentEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

   @Autowired
   public WicaStreamLifecycleService service;


   @MockBean
   private WicaStreamDataCollectorService wicaStreamDataCollectorServiceMock;

   private final ObjectMapper jsonDecoder = new ObjectMapper();

   private final Map<WicaChannel,List<WicaChannelValue>> emptyValueMap = Map.of();

   private final Map<WicaChannel,WicaChannelMetadata> metadataMap = Map.of( WicaChannel.createFromName( "CHAN_1"),
                                                                      WicaChannelMetadata.createUnknownInstance(),
                                                                      WicaChannel.createFromName( "CHAN_2"),
                                                                      WicaChannelMetadata.createUnknownInstance() );

   private final Map<WicaChannel,List<WicaChannelValue>> initialValueMap = Map.of( WicaChannel.createFromName( "CHAN_1"),
                                                                             List.of( WicaChannelValue.createChannelValueConnected( "InitMap_CHAN_1_Value" ) ),
                                                                             WicaChannel.createFromName( "CHAN_2"),
                                                                             List.of( WicaChannelValue.createChannelValueConnected( "InitMap_CHAN_2_Value" ) ) );

   private final Map<WicaChannel,List<WicaChannelValue>> req1PolledValueMap = Map.of( WicaChannel.createFromName( "CHAN_1"),
                                                                                List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_1_Request_1_Value" ) ),
                                                                                WicaChannel.createFromName( "CHAN_2"),
                                                                                List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_2_Request_1_Value" ) ) );

   private final Map<WicaChannel,List<WicaChannelValue>> req2PolledValueMap = Map.of( WicaChannel.createFromName( "CHAN_1"),
                                                                                List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_1_Request_2_Value" ) ),
                                                                                WicaChannel.createFromName( "CHAN_2"),
                                                                                List.of( WicaChannelValue.createChannelValueConnected( "PollMap_CHAN_2_Request_2_Value" ) ) );

   private final Map<WicaChannel,List<WicaChannelValue>> req1ChangedValueMap = Map.of( WicaChannel.createFromName( "CHAN_1"),
                                                                                 List.of( WicaChannelValue.createChannelValueConnected( "ChangeMap_CHAN_1_Request_1_Value" ) ),
                                                                                 WicaChannel.createFromName( "CHAN_2"),
                                                                                 List.of( WicaChannelValue.createChannelValueConnected( "ChangeMap_CHAN_2_Request_1_Value" ) ) );

   private final Map<WicaChannel,List<WicaChannelValue>> req2ChangedValueMap = Map.of( WicaChannel.createFromName( "CHAN_1"),
                                                                                 List.of( WicaChannelValue.createChannelValueConnected( "ChangeMap_CHAN_1_Request_2_Value" ) ),
                                                                                 WicaChannel.createFromName( "CHAN_2"),
                                                                                 List.of( WicaChannelValue.createChannelValueConnected( "ChangeMap_CHAN_2_Request_2_Value" ) ) );

   private final AtomicReference<Map<WicaChannel,List<WicaChannelValue>>> atomicPolledValueMap = new AtomicReference<>();
   private final AtomicReference<Map<WicaChannel,List<WicaChannelValue>>> atomicChangedValueMap = new AtomicReference<>();
   

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      WicaStreamId.resetAllocationSequencer();
      atomicPolledValueMap .set( req1PolledValueMap );
      atomicChangedValueMap.set( req1ChangedValueMap );
   }

   @Test
   void testShutdown() throws InterruptedException
   {
      logger.info( "STARTING SHUTDOWN TEST..." );

      // Get object-under-test configured for a stream with channels in POLL mode.
      final var objectUnderTest = getObjectUnderTestForStreamWithDaqMode( WicaChannelProperties.DataAcquisitionMode.POLL );
      logger.info( "CREATED OBJECT-UNDER-TEST." );

      final var flux = objectUnderTest.getFlux();
      flux.subscribe( (c) -> logger.info( "c is: ------> {}", c ) );
      logger.info( "SUBSCRIBED TO OBJECT-UNDER-TEST." );

      // Let things run for a while.
      logger.info( "SLEEPING FOR 5S..." );
      Thread.sleep( 5000 );
      logger.info( "SLEEP COMPLETED." );

      // Now shut down the publisher.
      logger.info( "SHUTTING DOWN PUBLISHER..." );
      objectUnderTest.shutdown();
      logger.info( "SHUTTING DOWN COMPLETED." );

      // Verify that attempts to retrieve the flux following shutdown result
      // in an illegal state exception
      final Exception ex1 = assertThrows( IllegalStateException.class, objectUnderTest::getFlux );
      assertThat( ex1.getMessage(), is("Call to getFlux(), but the publisher has already been shut down." ) );

      // Verify that attempts to retrieve the flux following shutdown result
      // in an illegal state exception
      final Exception ex2 = assertThrows( IllegalStateException.class, objectUnderTest::shutdown );
      assertThat( ex2.getMessage(), is("Call to shutdown(), but the publisher has already been shut down." ) );

      logger.info( "TEST COMPLETED." );
   }

   @Test
   void testSubscribeToPollOnlyStream() throws IOException, InterruptedException
   {
      //---------------------------------------------------------------------------------
      // 1.0 Set up test environment and run SSE Publisher for 1400ms
      //---------------------------------------------------------------------------------
      
      // Get object-under-test configured for a stream with channels in POLL mode.
      final var objectUnderTest = getObjectUnderTestForStreamWithDaqMode( WicaChannelProperties.DataAcquisitionMode.POLL );

      final ArgumentCaptor<WicaStream>captor = ArgumentCaptor.forClass( WicaStream.class );
      given( wicaStreamDataCollectorServiceMock.getMetadataMap( captor.capture() ) ).willReturn( metadataMap );
      given( wicaStreamDataCollectorServiceMock.getInitialValueMap( captor.capture()  ) ).willReturn( initialValueMap );
      given( wicaStreamDataCollectorServiceMock.getPolledValueMap(captor.capture() ) ).willAnswer(rqst -> getPolledValueMap() );
      given( wicaStreamDataCollectorServiceMock.getChangedValueMap(captor.capture() ) ).willReturn(emptyValueMap );

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
      //   1. t =     0 initial channel metadata
      //   2. t =     0 initial channel values
      //   3. t = 250ms polled channel values (1)
      //   4. t = 320ms channel value changes (1)
      //   5. t = 500ms polled channel values (2)
      //   6. t = 640ms channel value changes (2)
      //   7. t = 700ms server heartbeat
      Thread.sleep( 1400 );
      objectUnderTest.shutdown();

      // Verify that seven notifications were received in the flux
      assertEquals(7, sseList.size() );
      
      //---------------------------------------------------------------------------------
      // 1.1 Verify that the FIRST notification contains channel metadata.
      //---------------------------------------------------------------------------------
      final var sse0 = sseList.get( 0 );
      assertThat( sse0.event(), is( "ev-wica-channel-metadata" )  );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );

      // Verify that all channels are included in the map and that the type of each channel is UNKNOWN.
      final JsonNode sse0Node = jsonDecoder.readTree( sse0.data() );
      assertThat( sse0Node.isObject(), is( true ) );
      assertThat( sse0Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse0Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse0Node.get( "CHAN_1" ).get( "type").textValue(), containsString( "UNKNOWN" ) );
      assertThat( sse0Node.get( "CHAN_2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );

      //---------------------------------------------------------------------------------
      // 1.2 Verify that the SECOND notification contains the channel's initial values.
      //---------------------------------------------------------------------------------
      final var sse1 = sseList.get( 1 );
      assertThat( sse1.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse1.comment(), containsString( "- initial channel value" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse1Node = jsonDecoder.readTree( sse1.data() );
      assertThat( sse1Node.isObject(), is( true ) );
      assertThat( sse1Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse1Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "InitMap_CHAN_1_Value" ) );
      assertThat( sse1Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "InitMap_CHAN_2_Value" ) );

      //---------------------------------------------------------------------------------
      // 1.3 Verify that the THIRD notification contains the channel's polled values
      //---------------------------------------------------------------------------------
      final var sse2 = sseList.get( 2 );
      assertThat( sse2.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse2.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse2Node = jsonDecoder.readTree( sse2.data() );
      assertThat( sse2Node.isObject(), is( true ) );
      assertThat( sse2Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse2Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse2Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_1_Request_1_Value" ) );
      assertThat( sse2Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_2_Request_1_Value" ) );

      //---------------------------------------------------------------------------------
      // 1.4 Verify that the FOURTH notification contains the channel's changed values.
      //---------------------------------------------------------------------------------
      final var sse3 = sseList.get( 3 );
      assertThat( sse3.event(), is("ev-wica-channel-value") );
      assertThat( sse3.comment(), containsString( "- channel value changes" ) );

      // Verify that NO channels are included in the map since the data acquisition mode
      // for the stream is POLL mode only.
      assertThat( sse3.data(), is( "{}" ) );

      //---------------------------------------------------------------------------------
      // 1.5 Verify that the FIFTH notification contains polled values again.
      //---------------------------------------------------------------------------------
      final var sse4 = sseList.get( 4 );
      assertThat( sse4.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse4.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse4Node = jsonDecoder.readTree( sse4.data() );
      assertThat( sse4Node.isObject(), is( true ) );
      assertThat( sse4Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse4Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse4Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_1_Request_2_Value" ) );
      assertThat( sse4Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_2_Request_2_Value" ) );

      //---------------------------------------------------------------------------------
      // 1.6 Verify that the SIXTH notification contain's the channel's changed values.
      //---------------------------------------------------------------------------------
      final var sse5 = sseList.get( 5 );
      assertThat( sse5.event(), is("ev-wica-channel-value") );
      assertThat( sse5.comment(), containsString( "- channel value changes" ) );

      // Verify that NO channels are included in the map since the data acquisition mode
      // for the stream is POLL mode only.
      assertThat( sse5.data(), is( "{}" ) );

      //---------------------------------------------------------------------------------
      // 1.7 Verify that the SEVENTH notification contain's the stream's heartbeat.
      //---------------------------------------------------------------------------------
      final var sse6 = sseList.get( 6 );
      assertThat( sse6.event(), is( "ev-wica-server-heartbeat") );
      assertThat( sse6.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse6.data(), containsString( LocalDate.now().toString() ) );
   }

   @Test
   void testSubscribeToMonitorOnlyStream() throws IOException, InterruptedException
   {
      //---------------------------------------------------------------------------------
      // 2.0 Set up test environment and run SSE Publisher for 1400ms
      //---------------------------------------------------------------------------------

      // Get object-under-test configured for a stream with channels in MONITOR mode.
      final var objectUnderTest = getObjectUnderTestForStreamWithDaqMode( WicaChannelProperties.DataAcquisitionMode.MONITOR );

      // Set up the mock response.
      final ArgumentCaptor<WicaStream>captor = ArgumentCaptor.forClass( WicaStream.class );
      given( wicaStreamDataCollectorServiceMock.getMetadataMap( captor.capture() ) ).willReturn( metadataMap );
      given( wicaStreamDataCollectorServiceMock.getInitialValueMap( captor.capture()  ) ).willReturn( initialValueMap );
      given( wicaStreamDataCollectorServiceMock.getPolledValueMap(captor.capture() ) ).willReturn(emptyValueMap );
      given( wicaStreamDataCollectorServiceMock.getChangedValueMap(captor.capture() ) ).willAnswer(rqst -> getChangedValueMap() );

      // Subscribe to the stream publisher.
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
      //   1. t =     0 initial channel metadata
      //   2. t =     0 initial channel values
      //   3. t = 250ms polled channel values (1)
      //   4. t = 320ms channel value changes (1)
      //   5. t = 500ms polled channel values (2)
      //   6. t = 640ms channel value changes (2)
      //   7. t = 700ms server heartbeat
      Thread.sleep( 1400 );
      objectUnderTest.shutdown();

      // Verify that seven notifications were received in the flux
      assertEquals(7, sseList.size() );

      //---------------------------------------------------------------------------------
      // 2.1 Verify that the FIRST notification contains channel metadata.
      //---------------------------------------------------------------------------------
      final var sse0 = sseList.get( 0 );
      assertThat( sse0.event(), is( "ev-wica-channel-metadata" )  );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );

      // Verify that all channels are included in the map and that the type of each channel is UNKNOWN.
      final JsonNode sse0Node = jsonDecoder.readTree( sse0.data() );
      assertThat( sse0Node.isObject(), is( true ) );
      assertThat( sse0Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse0Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse0Node.get( "CHAN_1" ).get( "type").textValue(), containsString( "UNKNOWN" ) );
      assertThat( sse0Node.get( "CHAN_2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );

      //---------------------------------------------------------------------------------
      // 2.2 Verify that the SECOND notification contains the channel's initial values.
      //---------------------------------------------------------------------------------
      final var sse1 = sseList.get( 1 );
      assertThat( sse1.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse1.comment(), containsString( "- initial channel value" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse1Node = jsonDecoder.readTree( sse1.data() );
      assertThat( sse1Node.isObject(), is( true ) );
      assertThat( sse1Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse1Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "InitMap_CHAN_1_Value" ) );
      assertThat( sse1Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "InitMap_CHAN_2_Value" ) );

      //---------------------------------------------------------------------------------
      // 2.3 Verify that the THIRD notification contains the channel's polled values.
      //---------------------------------------------------------------------------------
      final var sse2 = sseList.get( 2 );
      assertThat( sse2.event(), is("ev-wica-channel-value") );
      assertThat( sse2.comment(), containsString( "- polled channel values" ) );

      // Verify that NO channels are included in the map since the data acquisition mode
      // for the stream is MONITOR mode only.
      assertThat( sse2.data(), is( "{}" ) );

      //---------------------------------------------------------------------------------
      // 2.4 Verify that the FOURTH notification contains the channel's changed values.
      //---------------------------------------------------------------------------------
      final var sse3 = sseList.get( 3 );
      assertThat( sse3.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse3.comment(), containsString( "- channel value changes" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse3Node = jsonDecoder.readTree( sse3.data() );
      assertThat( sse3Node.isObject(), is( true ) );
      assertThat( sse3Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse3Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse3Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse3Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse3Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_1_Request_1_Value" ) );
      assertThat( sse3Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_2_Request_1_Value" ) );

      //---------------------------------------------------------------------------------
      // 2.5 Verify that the FIFTH notification contains the channel's polled values again.
      //---------------------------------------------------------------------------------
      final var sse4 = sseList.get( 4 );
      assertEquals( sse4.event(), "ev-wica-channel-value");
      assertThat( sse4.comment(), containsString( "- polled channel values" ) );

      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is MONITOR mode only.
      assertThat( sse4.data(), containsString( "{}" ) );

      //---------------------------------------------------------------------------------
      // 2.6 Verify that the SIXTH notification contain's the channel's changed values.
      //---------------------------------------------------------------------------------
      final var sse5 = sseList.get( 5 );
      assertThat( sse5.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse5.comment(), containsString( "- channel value changes" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse5Node = jsonDecoder.readTree( sse5.data() );
      assertThat( sse5Node.isObject(), is( true ) );
      assertThat( sse5Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse5Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse5Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse5Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse5Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_1_Request_2_Value" ) );
      assertThat( sse5Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_2_Request_2_Value" ) );

      //---------------------------------------------------------------------------------
      // 2.7 Verify that the SEVENTH notification contain's the stream's heartbeat.
      //---------------------------------------------------------------------------------

      final var sse6 = sseList.get( 6 );
      assertEquals( "ev-wica-server-heartbeat",sse6.event() );
      assertThat( sse6.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse6.data(), containsString( LocalDate.now().toString() ) );
   }

   @Test
   void testSubscribeToPollAndMonitorStream() throws IOException, InterruptedException
   {
      //---------------------------------------------------------------------------------
      // 3.0 Set up test environment and run SSE Publisher for 1400ms
      //---------------------------------------------------------------------------------

      // Get object-under-test configured for a stream with channels in POLL_AND_MONITOR mode.
      final var objectUnderTest = getObjectUnderTestForStreamWithDaqMode( WicaChannelProperties.DataAcquisitionMode.POLL_AND_MONITOR );

      // Setup the mock response.
      final ArgumentCaptor<WicaStream>captor = ArgumentCaptor.forClass( WicaStream.class );
      given( wicaStreamDataCollectorServiceMock.getMetadataMap( captor.capture() ) ).willReturn( metadataMap );
      given( wicaStreamDataCollectorServiceMock.getInitialValueMap( captor.capture()  ) ).willReturn( initialValueMap );
      given( wicaStreamDataCollectorServiceMock.getPolledValueMap(captor.capture() ) ).willAnswer(rqst -> getPolledValueMap() );
      given( wicaStreamDataCollectorServiceMock.getChangedValueMap(captor.capture() ) ).willAnswer(rqst -> getChangedValueMap() );

      // Subscribe to the stream publisher.
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
      //   1. t =     0 initial channel metadata
      //   2. t =     0 initial channel values
      //   3. t = 500ms polled channel values (1)
      //   4. t = 640ms channel value changes (1)
      //   5. t = 1000ms polled channel values (2)
      //   6. t = 1280ms channel value changes (2)
      //   7. t = 1400ms server heartbeat
      Thread.sleep( 1400 );
      objectUnderTest.shutdown();

      // Verify that seven notifications were received in the flux
      assertEquals(7, sseList.size() );

      //---------------------------------------------------------------------------------
      // 3.1 Verify that the FIRST notification contains channel metadata.
      //---------------------------------------------------------------------------------
      final var sse0 = sseList.get( 0 );
      assertThat( sse0.event(), is( "ev-wica-channel-metadata" )  );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );

      // Verify that all channels are included in the map and that the type of each channel is UNKNOWN.
      final JsonNode sse0Node = jsonDecoder.readTree( sse0.data() );
      assertThat( sse0Node.isObject(), is( true ) );
      assertThat( sse0Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse0Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse0Node.get( "CHAN_1" ).get( "type").textValue(), containsString( "UNKNOWN" ) );
      assertThat( sse0Node.get( "CHAN_2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );

      //---------------------------------------------------------------------------------
      // 3.2 Verify that the SECOND notification contains the channel's initial values.
      //---------------------------------------------------------------------------------
      final var sse1 = sseList.get( 1 );
      assertThat( sse1.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse1.comment(), containsString( "- initial channel value" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse1Node = jsonDecoder.readTree( sse1.data() );
      assertThat( sse1Node.isObject(), is( true ) );
      assertThat( sse1Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse1Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "InitMap_CHAN_1_Value" ) );
      assertThat( sse1Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "InitMap_CHAN_2_Value" ) );

      //---------------------------------------------------------------------------------
      // 3.3 Verify that the THIRD notification contains the channel's polled values
      //---------------------------------------------------------------------------------
      final var sse2 = sseList.get( 2 );
      assertThat( sse2.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse2.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse2Node = jsonDecoder.readTree( sse2.data() );
      assertThat( sse2Node.isObject(), is( true ) );
      assertThat( sse2Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse2Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse2Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_1_Request_1_Value" ) );
      assertThat( sse2Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_2_Request_1_Value" ) );

      //---------------------------------------------------------------------------------
      // 3.4 Verify that the FOURTH notification contains the channel's changed values.
      //---------------------------------------------------------------------------------
      final var sse3 = sseList.get( 3 );
      assertThat( sse3.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse3.comment(), containsString( "- channel value changes" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse3Node = jsonDecoder.readTree( sse3.data() );
      assertThat( sse3Node.isObject(), is( true ) );
      assertThat( sse3Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse3Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse3Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse3Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse3Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_1_Request_1_Value" ) );
      assertThat( sse3Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_2_Request_1_Value" ) );

      //---------------------------------------------------------------------------------
      // 1.5 Verify that the FIFTH notification contains polled values again.
      //---------------------------------------------------------------------------------
      final var sse4 = sseList.get( 4 );
      assertThat( sse4.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse4.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse4Node = jsonDecoder.readTree( sse4.data() );
      assertThat( sse4Node.isObject(), is( true ) );
      assertThat( sse4Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse4Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse4Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_1_Request_2_Value" ) );
      assertThat( sse4Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "PollMap_CHAN_2_Request_2_Value" ) );

      //---------------------------------------------------------------------------------
      // 3.6 Verify that the SIXTH notification contain's the channel's changed values.
      //---------------------------------------------------------------------------------
      final var sse5 = sseList.get( 5 );
      assertThat( sse5.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse5.comment(), containsString( "- channel value changes" ) );

      // Verify that all channels are included in the map and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse5Node = jsonDecoder.readTree( sse5.data() );
      assertThat( sse5Node.isObject(), is( true ) );
      assertThat( sse5Node.has( "CHAN_1" ), is( true ) );
      assertThat( sse5Node.has( "CHAN_2" ), is( true ) );
      assertThat( sse5Node.get( "CHAN_1" ).isArray(), is( true ) );
      assertThat( sse5Node.get( "CHAN_2" ).isArray(), is( true ) );
      assertThat( sse5Node.get( "CHAN_1" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_1_Request_2_Value" ) );
      assertThat( sse5Node.get( "CHAN_2" ).get( 0 ).get( "val").textValue(), is( "ChangeMap_CHAN_2_Request_2_Value" ) );

      //---------------------------------------------------------------------------------
      // 3.7 Verify that the SEVENTH notification contain's the stream's heartbeat.
      //---------------------------------------------------------------------------------

      final var sse6 = sseList.get( 6 );
      assertEquals( "ev-wica-server-heartbeat",sse6.event() );
      assertThat( sse6.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse6.data(), containsString( LocalDate.now().toString() ) );
   }


/*- Private methods ----------------------------------------------------------*/
   
   private WicaStreamServerSentEventPublisher getObjectUnderTestForStreamWithDaqMode( WicaChannelProperties.DataAcquisitionMode daqMode )
   {
      final WicaStreamProperties wicaStreamProperties = new WicaStreamProperties.Builder()
            .withDataAcquisitionMode( daqMode )
            .withHeartbeatFluxInterval( 1400 )
            .withPolledValueFluxInterval( 500 )
            .withChangedValueFluxInterval( 640 )
            .withPolledValueSamplingRatio( 1 )
            .build();

      WicaStream wicaStream = new WicaStream.Builder().withStreamProperties( wicaStreamProperties)
            .withChannelNamed( "CHAN_1" )
            .withChannelNamed( "CHAN_2" )
            .build();

      return new WicaStreamServerSentEventPublisher( wicaStream, wicaStreamDataCollectorServiceMock );
   }

   private Map<WicaChannel,List<WicaChannelValue>> getPolledValueMap()
   {
      return atomicPolledValueMap.getAndSet( req2PolledValueMap );
   }

   private Map<WicaChannel,List<WicaChannelValue>> getChangedValueMap()
   {
      return atomicChangedValueMap.getAndSet( req2ChangedValueMap );
   }


/*- Nested Classes -----------------------------------------------------------*/

}

