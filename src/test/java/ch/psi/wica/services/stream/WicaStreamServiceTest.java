/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith( SpringRunner.class)
@SpringBootTest
class WicaStreamServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   public WicaStreamService service;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testCreateStreamWithEmptyPropsObject()
   {
      String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create( testString );
      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertEquals( 2, channels.size() );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ) );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ) );

      final WicaStreamProperties streamProperties = stream.getWicaStreamProperties();
      assertEquals( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL, streamProperties.getHeartbeatFluxInterval() );

      final WicaStreamId wicaStreamId = stream.getWicaStreamId();
      assertEquals( "0", wicaStreamId.asString() );
   }

   @Test
   void testCreateStreamWithNoPropsObject()
   {
      String testString = "{ \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create(testString );

      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertEquals( 2, channels.size() );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ) );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ) );

      final WicaStreamProperties streamProperties = stream.getWicaStreamProperties();
      assertEquals( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL, streamProperties.getHeartbeatFluxInterval() );

      final WicaStreamId wicaStreamId = stream.getWicaStreamId();
      assertEquals( "0", wicaStreamId.asString() );
   }

   @Test
   void testCreateStreamWithPropsObject()
   {
      String testString = "{ \"props\" : { \"prec\":8, \"fields\":\"abc;def\" }, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      WicaStreamId.resetAllocationSequencer();
      final WicaStream stream = service.create(testString );

      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertEquals( 2, channels.size() );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ) );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ) );

      final WicaStreamProperties streamProperties = stream.getWicaStreamProperties();
      assertEquals( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL, streamProperties.getHeartbeatFluxInterval() );
      assertEquals( WicaStreamProperties.DEFAULT_NUMERIC_PRECISION, streamProperties.getNumericPrecision() );
      assertEquals( Set.of( "abc", "def" ), streamProperties.getFieldsOfInterest() );

      final WicaStreamId wicaStreamId = stream.getWicaStreamId();
      assertEquals( "0", wicaStreamId.asString() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
