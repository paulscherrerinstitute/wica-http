/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamId;
import ch.psi.wica.services.epics.EpicsChannelDataService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


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
   private EpicsChannelDataService epicsService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void test() throws InterruptedException
   {
      final String testString = "{ \"props\" : {  \"daqType\" : \"monitorer\",  \"fields\" :  \"val;sevr;ts\"  }, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create( testString );

      final WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( stream, epicsService );
      final WicaStreamPublisher publisher = new WicaStreamPublisher( stream, supplier );
      publisher.activate();

      var flux = publisher.getFlux();

      final List<ServerSentEvent<String>> sseList = new ArrayList<>();

      var d = flux.subscribe(  (c) -> {
         logger.info( "c is: {}", c  );
         sseList.add( c );
      } );

      Thread.sleep( 250 );

      assertEquals(6, sseList.size() );
      var sse0 = sseList.get( 0 );
      assertEquals( "ev-wica-channel-metadata", sse0.event() );
      assertThat( sse0.comment(), containsString( "- initial channel metadata" ) );
      assertThat( sse0.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse0.data(), containsString( "MHC2:IST:2" ) );

      var sse1 = sseList.get( 1 );
      assertEquals( "ev-wica-channel-value",sse1.event() );
      assertThat( sse1.comment(), containsString( "- initial channel values" ) );
      assertThat( sse1.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse1.data(), containsString( "MHC2:IST:2" ) );

      // The polled channel value flux should yield nothing since the default
      // channel type is for monitoring
      var sse2 = sseList.get( 2 );
      assertEquals( "ev-wica-channel-value",sse2.event() );
      assertThat( sse2.comment(), containsString( "- polled channel values" ) );
      assertThat( sse2.data(), containsString( "{}" ) );

      // The first channel value update method should contain all the channels since
      // since everything is deemed to have changed.
      var sse3 = sseList.get( 3 );
      assertEquals( "ev-wica-channel-value",sse3.event() );
      assertThat( sse3.comment(), containsString( "- channel value changes" ) );
      assertThat( sse3.data(), containsString( "MHC1:IST:2" ) );
      assertThat( sse3.data(), containsString( "MHC2:IST:2" ) );

      // The second value update method should not contain any update information since
      // nothing has changed.
      var sse4 = sseList.get( 4 );
      assertEquals( "ev-wica-channel-value",sse4.event() );
      assertThat( sse4.comment(), containsString( "- channel value changes" ) );
      assertThat( sse4.data(), not( containsString( "MHC1:IST:2" ) ) );
      assertThat( sse4.data(), not( containsString( "MHC2:IST:2" ) ) );

      // AGain the polled channel value flux should yield nothing
      var sse5 = sseList.get( 5 );
      assertEquals( "ev-wica-channel-value",sse4.event() );
      assertThat( sse5.comment(), containsString( "- polled channel values" ) );
      assertThat( sse5.data(), containsString( "{}" ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

