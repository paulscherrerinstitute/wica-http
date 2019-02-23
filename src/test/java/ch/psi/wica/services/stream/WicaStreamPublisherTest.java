/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica.model.WicaChannelMetadataStash;
import ch.psi.wica.model.WicaChannelValueStash;
import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamId;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith( SpringRunner.class)
@SpringBootTest
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

//   private WicaStreamPublisher publisher;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testShutdown() throws InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"poll\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create( configString );

      // Create a stream publisher and activate it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );
      publisher.activate();

      Thread.sleep( 720 );

      // Now shut it down
      publisher.shutdown();

      // Verify that attempts to retrieve the flux following shutdown result
      // in an illegal state exception
      assertThrows( IllegalStateException.class, publisher::getFlux);
   }


   @Test
   void testSubscribeToPollOnlyStream() throws InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"poll\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"pollrat\" : 1, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create( configString );

      // Create a stream publisher and activate it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );
      publisher.activate();

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
      //   2. t = 250ms polled channel values (1)
      //   3. t = 320ms channel value changes (1)
      //   4. t = 500ms polled channel values (2)
      //   5. t = 640ms channel value changes (2)
      //   6. t = 700ms server heartbeat (1)
      Thread.sleep( 720 );

      // Verify that seven notifications were received in the flux
      assertEquals(6, sseList.size() );

      // Verify that the first notification contains channel metadata.
      final var sse0 = sseList.get( 0 );
      assertEquals( "ev-wica-channel-metadata", sse0.event() );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );
      assertThat( sse0.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse0.data(), containsString( "MHC2:IST:2" ) );

       // Verify that the second notification contains the channel's polled values
      final var sse1 = sseList.get( 1 );
      assertEquals( "ev-wica-channel-value",sse1.event() );
      assertThat( sse1.comment(), containsString( "- polled channel values" ) );
      assertThat( sse1.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse1.data(), containsString( "MHC2:IST:2" ) );

      // Verify that the third notification contains the channel's changed values.
      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is POLL mode only.
      final var sse2 = sseList.get( 2 );
      assertEquals( "ev-wica-channel-value",sse2.event() );
      assertThat( sse2.comment(), containsString( "- channel value changes" ) );
      assertThat( sse2.data(), containsString( "{}" ) );

      // Verify that the fourth notification contains polled values again.
      final var sse3 = sseList.get( 3 );
      assertEquals( "ev-wica-channel-value",sse3.event() );
      assertThat( sse3.comment(), containsString( "- polled channel values" ) );
      assertThat( sse3.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse3.data(), containsString( "MHC2:IST:2" ) );

      // Verify that the fifth notification contain's the channel's changed values.
      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is POLL mode only.
      final var sse4 = sseList.get( 4 );
      assertEquals( "ev-wica-channel-value", sse4.event() );
      assertThat( sse4.comment(), containsString( "- channel value changes" ) );
      assertThat( sse4.data(), containsString( "{}" ) );

      // Verify that the sixth notification contain's the stream's heartbeat.
      final var sse5 = sseList.get( 5 );
      assertEquals( "ev-wica-server-heartbeat",sse5.event() );
      assertThat( sse5.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse5.data(), containsString( LocalDate.now().toString() ) );

      // Close down the stream and shutdown the publisher
      disposable.dispose();
      publisher.shutdown();
   }

   @Test
   void testSubscribeToMonitorOnlyStream() throws InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"monitor\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create( configString );

      // Create a stream publisher and activate it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );
      publisher.activate();

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
      //   2. t = 250ms polled channel values (1)
      //   3. t = 320ms channel value changes (1)
      //   4. t = 500ms polled channel values (2)
      //   5. t = 640ms channel value changes (2)
      //   6. t = 700ms server heartbeat (1)
      Thread.sleep( 720 );

      // Verify that seven notifications were received in the flux
      assertEquals(6, sseList.size() );

      // Verify that the first notification contains channel metadata.
      final var sse0 = sseList.get( 0 );
      assertEquals( "ev-wica-channel-metadata", sse0.event() );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );
      assertThat( sse0.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse0.data(), containsString( "MHC2:IST:2" ) );

      // Verify that the second notification contains the channel's polled values.
      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is MONITOR mode only.
      final var sse1 = sseList.get( 1 );
      assertEquals( "ev-wica-channel-value",sse1.event() );
      assertThat( sse1.comment(), containsString( "- polled channel values" ) );
      assertThat( sse1.data(), containsString( "{}" ) );

      // Verify that the third notification contains the channel's changed values.
      // Verify that all channels are represented since this is the initial value
      // notification. Verify that all values are in the disconnected state.
      final var sse2 = sseList.get( 2 );
      assertEquals( "ev-wica-channel-value",sse2.event() );
      assertThat( sse2.comment(), containsString( "- channel value changes" ) );
      assertThat( sse2.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse2.data(), containsString( "MHC2:IST:2" ) );
      assertThat( sse2.data(), containsString( "\"val\":null" ) );

      // Verify that the fourth notification contains the channel's polled values again.
      // Verify that NO channels are represented since the data acquisition mode
      // for the stream is MONITOR mode only.
      final var sse3 = sseList.get( 3 );
      assertEquals( "ev-wica-channel-value",sse3.event() );
      assertThat( sse3.comment(), containsString( "- polled channel values" ) );
      assertThat( sse3.data(), containsString( "{}" ) );

      // Verify that the fifth notification contain's the channel's changed values.
      // Verify that NO channels are represented since there have been no changes
      // to the value since the previous notification.
      final var sse4 = sseList.get( 4 );
      assertEquals( "ev-wica-channel-value", sse4.event() );
      assertThat( sse4.comment(), containsString( "- channel value changes" ) );
      assertThat( sse4.data(), containsString( "{}" ) );

      // Verify that the sixth notification contain's the stream's heartbeat.
      final var sse5 = sseList.get( 5 );
      assertEquals( "ev-wica-server-heartbeat",sse5.event() );
      assertThat( sse5.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse5.data(), containsString( LocalDate.now().toString() ) );

      // Close down the stream and shutdown the publisher
      disposable.dispose();
      publisher.shutdown();
   }


   @Test
   void testSubscribeToPollAndMonitorStream() throws InterruptedException
   {
      final String configString = "{ \"props\" :   {  \"daqmode\" : \"poll-and-monitor\",  \"heartbeat\" : 700,  \"pollint\" : 250, \"pollrat\" : 1, \"changeint\" : 320 }, " +
                                    "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" } ] }";

      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create( configString );

      // Create a stream publisher and activate it.
      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( wicaChannelMetadataStash, wicaChannelValueStash );
      final var publisher = new WicaStreamPublisher( stream, supplier );
      publisher.activate();

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
      //   2. t = 250ms polled channel values (1)
      //   3. t = 320ms channel value changes (1)
      //   4. t = 500ms polled channel values (2)
      //   5. t = 640ms channel value changes (2)
      //   6. t = 700ms server heartbeat (1)
      Thread.sleep( 720 );

      // Verify that seven notifications were received in the flux
      assertEquals(6, sseList.size() );

      // Verify that the first notification contains channel metadata.
      final var sse0 = sseList.get( 0 );
      assertEquals( "ev-wica-channel-metadata", sse0.event() );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );
      assertThat( sse0.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse0.data(), containsString( "MHC2:IST:2" ) );

      // Verify that the second notification contains the channel's polled values.
      // Verify that ALL channels are represented.
      final var sse1 = sseList.get( 1 );
      assertEquals( "ev-wica-channel-value",sse1.event() );
      assertThat( sse1.comment(), containsString( "- polled channel values" ) );
      assertThat( sse1.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse1.data(), containsString( "MHC2:IST:2" ) );
      assertThat( sse1.data(), containsString( "\"val\":null" ) );

      // Verify that the third notification contains the channel's changed values.
      // Verify that all channels are represented since this is the initial value
      // notification. Verify that all values are in the disconnected state.
      final var sse2 = sseList.get( 2 );
      assertEquals( "ev-wica-channel-value",sse2.event() );
      assertThat( sse2.comment(), containsString( "- channel value changes" ) );
      assertThat( sse2.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse2.data(), containsString( "MHC2:IST:2" ) );
      assertThat( sse2.data(), containsString( "\"val\":null" ) );

      // Verify that the fourth notification contains the channel's polled values again.
      // Verify that ALL channels are represented.
      final var sse3 = sseList.get( 3 );
      assertEquals( "ev-wica-channel-value",sse3.event() );
      assertThat( sse3.comment(), containsString( "- polled channel values" ) );
      assertThat( sse2.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse2.data(), containsString( "MHC2:IST:2" ) );
      assertThat( sse2.data(), containsString( "\"val\":null" ) );

      // Verify that the fifth notification contain's the channel's changed values.
      // Verify that NO channels are represented since there have been no changes
      // to the value since the previous notification.
      final var sse4 = sseList.get( 4 );
      assertEquals( "ev-wica-channel-value", sse4.event() );
      assertThat( sse4.comment(), containsString( "- channel value changes" ) );
      assertThat( sse4.data(), containsString( "{}" ) );

      // Verify that the sixth notification contain's the stream's heartbeat.
      final var sse5 = sseList.get( 5 );
      assertEquals( "ev-wica-server-heartbeat",sse5.event() );
      assertThat( sse5.comment(), containsString( "- server heartbeat" ) );
      assertThat( sse5.data(), containsString( LocalDate.now().toString() ) );

      // Close down the stream and shutdown the publisher
      disposable.dispose();
      publisher.shutdown();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

