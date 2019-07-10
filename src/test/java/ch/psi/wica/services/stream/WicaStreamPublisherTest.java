/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica.model.WicaChannelMetadataStash;
import ch.psi.wica.model.WicaChannelValueStash;
import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamPublisherTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamPublisherTest.class );

   @Autowired
   public WicaStreamService service;

   @Autowired
   private WicaChannelMetadataStash wicaChannelMetadataStash;

   @Autowired
   private WicaChannelValueStash wicaChannelValueStash;

   private ObjectMapper jsonDecoder;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      WicaStreamId.resetAllocationSequencer();
      jsonDecoder = new ObjectMapper();
   }

   @Test
   void testShutdown() throws InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"poll\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      final WicaStream stream = service.create( configString );

      // Create a stream publisher and subscribe to it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );
      final var flux = publisher.getFlux();
      flux.subscribe( (c) -> logger.info( "c is: ------> {}", c ) );

      // Let things run for a while
      Thread.sleep( 720 );

      // Now shut down the publisher
      publisher.shutdown();

      // Verify that attempts to retrieve the flux following shutdown result
      // in an illegal state exception
      assertThrows( IllegalStateException.class, publisher::getFlux);
   }


   @Test
   void testSubscribeToPollOnlyStream() throws IOException, InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"poll\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"pollratio\" : 1, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      final WicaStream stream = service.create( configString );

      // Create a stream publisher and activate it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );

      // Subscribe to the stream
      final List<ServerSentEvent<String>> sseList = new ArrayList<>();
      final var flux = publisher.getFlux();
      final var disposable = flux.subscribe( (c) -> {
         logger.info( "c is: ------> {}", c  );
         synchronized( this ) {
            sseList.add( c );
         }
      } );

      // Let things run for 720ms. This should be long enough to receive the
      // following SSE's on the event stream:
      //
      //   1. t =     0 initial channel metadata
      //   2. t =     0 initial channel values
      //   3. t = 250ms polled channel values (1)
      //   4. t = 320ms channel value changes (1)
      //   5. t = 500ms polled channel values (2)
      //   6. t = 640ms channel value changes (2)
      //   7. t = 700ms server heartbeat
      Thread.sleep( 720 );

      // Verify that seven notifications were received in the flux
      assertEquals(7, sseList.size() );

      // Verify that the FIRST notification contains channel metadata.
      final var sse0 = sseList.get( 0 );
      assertThat( sse0.event(), is( "ev-wica-channel-metadata" )  );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );

      // Verify that all channels are represented and that the type of each channel is UNKNOWN.
      final JsonNode sse0Node = jsonDecoder.readTree( sse0.data() );
      assertThat( sse0Node.isObject(), is( true ) );
      assertThat( sse0Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse0Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse0Node.get( "MHC1:IST:2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );
      assertThat( sse0Node.get( "MHC2:IST:2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );

      // Verify that the SECOND notification contains the channel's initial values.
      final var sse1 = sseList.get( 1 );
      assertThat( sse1.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse1.comment(), containsString( "- initial channel value" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
       final JsonNode sse1Node = jsonDecoder.readTree( sse1.data() );
      assertThat( sse1Node.isObject(), is( true ) );
      assertThat( sse1Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse1Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );

      // Verify that the THIRD notification contains the channel's polled values
      final var sse2 = sseList.get( 2 );
      assertThat( sse2.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse2.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse2Node = jsonDecoder.readTree( sse2.data() );
      assertThat( sse2Node.isObject(), is( true ) );
      assertThat( sse2Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse2Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse2Node.get( "MHC1:IST:2" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "MHC2:IST:2" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "MHC1:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse2Node.get( "MHC2:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse2Node.get( "MHC1:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );
      assertThat( sse2Node.get( "MHC2:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );

      // Verify that the FOURTH notification contains the channel's changed values.
      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is POLL mode only.
      final var sse3 = sseList.get( 3 );
      assertThat( sse3.event(), is("ev-wica-channel-value") );
      assertThat( sse3.comment(), containsString( "- channel value changes" ) );
      assertThat( sse3.data(), is( "{}" ) );

      // Verify that the FIFTH notification contains polled values again.
      final var sse4 = sseList.get( 4 );
      assertThat( sse4.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse4.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse4Node = jsonDecoder.readTree( sse4.data() );
      assertThat( sse4Node.isObject(), is( true ) );
      assertThat( sse4Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse4Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse4Node.get( "MHC1:IST:2" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "MHC2:IST:2" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "MHC1:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse4Node.get( "MHC2:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse4Node.get( "MHC1:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );
      assertThat( sse4Node.get( "MHC2:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );

      // Verify that the SIXTH notification contain's the channel's changed values.
      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is POLL mode only.
      final var sse5 = sseList.get( 5 );
      assertThat( sse5.event(), is("ev-wica-channel-value") );
      assertThat( sse5.comment(), containsString( "- channel value changes" ) );
      assertThat( sse5.data(), is( "{}" ) );

      // Verify that the SEVENTH notification contain's the stream's heartbeat.
      final var sse6 = sseList.get( 6 );
      assertThat( sse6.event(), is( "ev-wica-server-heartbeat") );
      assertThat( sse6.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse6.data(), containsString( LocalDate.now().toString() ) );

      // Close down the stream and shutdown the publisher
      disposable.dispose();
      publisher.shutdown();
   }

   @Test
   void testSubscribeToMonitorOnlyStream() throws IOException, InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"monitor\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      final WicaStream stream = service.create( configString );

      // Create a stream publisher and activate it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );

      // Subscribe to the stream
      final List<ServerSentEvent<String>> sseList = new ArrayList<>();
      final var flux = publisher.getFlux();
      final var disposable = flux.subscribe( (c) -> {
         logger.info( "c is: ------> {}", c  );
         synchronized( this ) {
            sseList.add( c );
         }
      } );

      // Let things run for 720ms. This should be long enough to receive the
      // following SSE's on the event stream:
      //
      //   1. t =     0 initial channel metadata
      //   2. t =     0 initial channel values
      //   3. t = 250ms polled channel values (1)
      //   4. t = 320ms channel value changes (1)
      //   5. t = 500ms polled channel values (2)
      //   6. t = 640ms channel value changes (2)
      //   7. t = 700ms server heartbeat
      Thread.sleep( 720 );

      // Verify that seven notifications were received in the flux
      assertEquals(7, sseList.size() );

      // Verify that the FIRST notification contains channel metadata.
      final var sse0 = sseList.get( 0 );
      assertThat( sse0.event(), is( "ev-wica-channel-metadata" )  );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );

      // Verify that all channels are represented and that the type of each channel is UNKNOWN.
      final JsonNode sse0Node = jsonDecoder.readTree( sse0.data() );
      assertThat( sse0Node.isObject(), is( true ) );
      assertThat( sse0Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse0Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse0Node.get( "MHC1:IST:2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );
      assertThat( sse0Node.get( "MHC2:IST:2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );

      // Verify that the SECOND notification contains the channel's initial values.
      final var sse1 = sseList.get( 1 );
      assertThat( sse1.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse1.comment(), containsString( "- initial channel value" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse1Node = jsonDecoder.readTree( sse1.data() );
      assertThat( sse1Node.isObject(), is( true ) );
      assertThat( sse1Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse1Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );

      // Verify that the THIRD notification contains the channel's polled values.
      final var sse2 = sseList.get( 2 );
      assertThat( sse2.event(), is("ev-wica-channel-value") );
      assertThat( sse2.comment(), containsString( "- polled channel values" ) );

      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is MONITOR mode only.
      assertThat( sse2.data(), is( "{}" ) );

      // Verify that the FOURTH notification contains the channel's changed values.
      final var sse3 = sseList.get( 3 );
      assertThat( sse3.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse3.comment(), containsString( "- channel value changes" ) );

      // Verify that NO channels are represented since nothing has changed since the
      // initial values notification.
      assertThat( sse3.data(), is( "{}" ) );

      // Verify that the FIFTH notification contains the channel's polled values again.
      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is MONITOR mode only.
      final var sse4 = sseList.get( 4 );
      assertEquals( "ev-wica-channel-value",sse3.event() );
      assertThat( sse4.comment(), containsString( "- polled channel values" ) );
      assertThat( sse4.data(), containsString( "{}" ) );

      // Verify that the SIXTH notification contain's the channel's changed values.
      // Verify that NO channels are represented since there have been no changes
      // to the value since the previous notification.
      final var sse5 = sseList.get( 5 );
      assertEquals( "ev-wica-channel-value", sse5.event() );
      assertThat( sse5.comment(), containsString( "- channel value changes" ) );
      assertThat( sse5.data(), containsString( "{}" ) );

      // Verify that the SEVENTH notification contain's the stream's heartbeat.
      final var sse6 = sseList.get( 6 );
      assertEquals( "ev-wica-server-heartbeat",sse6.event() );
      assertThat( sse6.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse6.data(), containsString( LocalDate.now().toString() ) );

      // Close down the stream and shutdown the publisher
      disposable.dispose();
      publisher.shutdown();
   }


   @Test
   void testSubscribeToPollAndMonitorStream() throws IOException, InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"poll-and-monitor\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"pollratio\" : 1, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create( configString );

      // Create a stream publisher and activate it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );

      // Subscribe to the stream
      final List<ServerSentEvent<String>> sseList = new ArrayList<>();
      final var flux = publisher.getFlux();
      final var disposable = flux.subscribe( (c) -> {
         logger.info( "c is: ------> {}", c  );
         synchronized( this ) {
            sseList.add( c );
         }
      } );

      // Let things run for 720ms. This should be long enough to receive the
      // following SSE's on the event stream:
      //
      //   1. t =     0 initial channel metadata
      //   2. t =     0 initial channel values
      //   3. t = 250ms polled channel values (1)
      //   4. t = 320ms channel value changes (1)
      //   5. t = 500ms polled channel values (2)
      //   6. t = 640ms channel value changes (2)
      //   7. t = 700ms server heartbeat
      Thread.sleep( 720 );

      // Verify that seven notifications were received in the flux
      assertEquals(7, sseList.size() );

      // Verify that the FIRST notification contains channel metadata.
      final var sse0 = sseList.get( 0 );
      assertThat( sse0.event(), is( "ev-wica-channel-metadata" )  );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );

      // Verify that all channels are represented and that the type of each channel is UNKNOWN.
      final JsonNode sse0Node = jsonDecoder.readTree( sse0.data() );
      assertThat( sse0Node.isObject(), is( true ) );
      assertThat( sse0Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse0Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse0Node.get( "MHC1:IST:2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );
      assertThat( sse0Node.get( "MHC2:IST:2" ).get( "type").textValue(), containsString( "UNKNOWN" ) );

      // Verify that the SECOND notification contains the channel's initial values.
      final var sse1 = sseList.get( 1 );
      assertThat( sse1.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse1.comment(), containsString( "- initial channel value" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse1Node = jsonDecoder.readTree( sse1.data() );
      assertThat( sse1Node.isObject(), is( true ) );
      assertThat( sse1Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse1Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).isArray(), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse1Node.get( "MHC1:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );
      assertThat( sse1Node.get( "MHC2:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );

      // Verify that the THIRD notification contains the channel's polled values
      final var sse2 = sseList.get( 2 );
      assertThat( sse2.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse2.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse2Node = jsonDecoder.readTree( sse2.data() );
      assertThat( sse2Node.isObject(), is( true ) );
      assertThat( sse2Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse2Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse2Node.get( "MHC1:IST:2" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "MHC2:IST:2" ).isArray(), is( true ) );
      assertThat( sse2Node.get( "MHC1:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse2Node.get( "MHC2:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse2Node.get( "MHC1:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );
      assertThat( sse2Node.get( "MHC2:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );


      // Verify that the FOURTH notification contains the channel's changed values.
      final var sse3 = sseList.get( 3 );
      assertThat( sse3.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse3.comment(), containsString( "- channel value changes" ) );

      // Verify that NO channels are represented since nothing has changed since the
      // initial values notification.
      assertThat( sse3.data(), is( "{}" ) );

      // Verify that the FIFTH notification contains polled values again.
      final var sse4 = sseList.get( 4 );
      assertThat( sse4.event(), is( "ev-wica-channel-value" )  );
      assertThat( sse4.comment(), containsString( "- polled channel values" ) );

      // Verify that all channels are represented and that all channel values show
      // that the channels are in the disconnected state.
      final JsonNode sse4Node = jsonDecoder.readTree( sse4.data() );
      assertThat( sse4Node.isObject(), is( true ) );
      assertThat( sse4Node.has( "MHC1:IST:2" ), is( true ) );
      assertThat( sse4Node.has( "MHC2:IST:2" ), is( true ) );
      assertThat( sse4Node.get( "MHC1:IST:2" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "MHC2:IST:2" ).isArray(), is( true ) );
      assertThat( sse4Node.get( "MHC1:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse4Node.get( "MHC2:IST:2" ).get( 0 ).has( "val"), is( true ) );
      assertThat( sse4Node.get( "MHC1:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );
      assertThat( sse4Node.get( "MHC2:IST:2" ).get( 0 ).get( "val").isNull(), is( true ) );

      // Verify that the SIXTH notification contain's the channel's changed values.
      // Verify that NO channels are represented since there have been no changes
      // to the value since the previous notification.
      final var sse5 = sseList.get( 5 );
      assertEquals( "ev-wica-channel-value", sse5.event() );
      assertThat( sse5.comment(), containsString( "- channel value changes" ) );
      assertThat( sse5.data(), containsString( "{}" ) );

      // Verify that the SEVENTH notification contain's the stream's heartbeat.
      final var sse6 = sseList.get( 6 );
      assertEquals( "ev-wica-server-heartbeat",sse6.event() );
      assertThat( sse6.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse6.data(), containsString( LocalDate.now().toString() ) );

      // Close down the stream and shutdown the publisher
      disposable.dispose();
      publisher.shutdown();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

