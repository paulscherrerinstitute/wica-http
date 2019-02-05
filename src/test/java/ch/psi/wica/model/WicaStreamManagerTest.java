/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.services.stream.WicaStreamManager;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith( SpringRunner.class)
@SpringBootTest
class WicaStreamManagerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   public WicaStreamManager creator;

   @Value( "${wica.default_heartbeat_flux_interval}" )
   private int defaultHeartBeatFluxInterval;

   @Value( "${wica.default_channel_value_update_flux_interval}" )
   private int defaultChannelValueUpdateFluxInterval;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testCreateStreamWithEmptyPropsObject()
   {
      String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = creator.create( testString );

      assertEquals( defaultHeartBeatFluxInterval, stream.getHeartbeatFluxInterval() );
      assertEquals( defaultChannelValueUpdateFluxInterval, stream.getChannelValueUpdateFluxInterval() );

      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertEquals( 2, channels.size() );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ) );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ) );

      final WicaStreamProperties streamProperties = stream.getWicaStreamProperties();
      assertEquals( 0, streamProperties.getPropertyNames().size() );

      final WicaStreamId wicaStreamId = stream.getWicaStreamId();
      assertEquals( "0", wicaStreamId.asString() );

      final LocalDateTime lastPublicationTime = stream.getLastPublicationTime();
      assertEquals( WicaStream.LONG_AGO, lastPublicationTime );
   }

   @Test
   void testCreateStreamWithNoPropsObject()
   {
      String testString = "{ \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = creator.create( testString );

      assertEquals( defaultHeartBeatFluxInterval, stream.getHeartbeatFluxInterval() );
      assertEquals( defaultChannelValueUpdateFluxInterval, stream.getChannelValueUpdateFluxInterval() );

      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertEquals( 2, channels.size() );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ) );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ) );

      final WicaStreamProperties streamProperties = stream.getWicaStreamProperties();
      assertEquals( 0, streamProperties.getPropertyNames().size() );

      final WicaStreamId wicaStreamId = stream.getWicaStreamId();
      assertEquals( "0", wicaStreamId.asString() );

      final LocalDateTime lastPublicationTime = stream.getLastPublicationTime();
      assertEquals( WicaStream.LONG_AGO, lastPublicationTime );
   }

   @Test
   void testCreateStreamWithPropsObject()
   {
      String testString = "{ \"props\" : { \"key1\":\"value1\", \"key2\":\"value2\" }, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = creator.create( testString );

      assertEquals( defaultHeartBeatFluxInterval, stream.getHeartbeatFluxInterval() );
      assertEquals( defaultChannelValueUpdateFluxInterval, stream.getChannelValueUpdateFluxInterval() );

      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertEquals( 2, channels.size() );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ) );
      assertTrue( channels.stream().map( c -> c.getName().toString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ) );

      final WicaStreamProperties streamProperties = stream.getWicaStreamProperties();
      assertEquals( 2, streamProperties.getPropertyNames().size() );
      assertTrue( streamProperties.hasProperty( "key1" ) );
      assertEquals( "value1", streamProperties.getPropertyValue( "key1" ) );
      assertEquals( "value2", streamProperties.getPropertyValue( "key2" ) );

      final WicaStreamId wicaStreamId = stream.getWicaStreamId();
      assertEquals( "0", wicaStreamId.asString() );

      final LocalDateTime lastPublicationTime = stream.getLastPublicationTime();
      assertEquals( WicaStream.LONG_AGO, lastPublicationTime );
   }


   @Test
   void testMap_inputMapContainsNoChannels()
   {
      String testString = "{ \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = creator.create( testString );

      final Map<WicaChannelName, List<WicaChannelValue>> myInputMap = Map.of();
      final Map<WicaChannelName, List<WicaChannelValue>> myOutputMap = stream.getWicaChannelValueMapTransformer().map( myInputMap );
      assertEquals( 0, myOutputMap.size() );
   }

   @Test
   void testMap_inputMapContainsAllStreamChannels()
   {
      String testString = "{ \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = creator.create( testString );

      final Map<WicaChannelName, List<WicaChannelValue>> myInputMap1 = Map.of( WicaChannelName.of( "MHC1:IST:2" ), Arrays.asList( WicaChannelValue.createChannelValueConnected( "abc" ) ),
                                                                               WicaChannelName.of( "MHC2:IST:2" ), Arrays.asList( WicaChannelValue.createChannelValueConnected( "def" ) ) );

      final Map<WicaChannelName, List<WicaChannelValue>> myOutputMap1 = stream.getWicaChannelValueMapTransformer().map( myInputMap1 );
      assertEquals( 2, myOutputMap1.size() );
      assertTrue( myOutputMap1.containsKey( WicaChannelName.of( "MHC1:IST:2" ) ) );
      assertEquals( "abc", ((WicaChannelValue.WicaChannelValueConnectedString) myOutputMap1.get( WicaChannelName.of( "MHC1:IST:2" ) ).get( 0 ) ).getValue() );

   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
