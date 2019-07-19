/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith( SpringRunner.class)
@SpringBootTest
class WicaStreamLifecycleServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   public WicaStreamLifecycleService service;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      WicaStreamId.resetAllocationSequencer();
   }

   @Test
   void testCreateStreamWithEmptyPropsObject()
   {
      final String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = service.create( testString );
      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertThat( channels.size(), is( 2) );
      assertThat( channels.stream().map( c -> c.getName().getControlSystemName().asString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ), is( true) );
      assertThat( channels.stream().map( c -> c.getName().getControlSystemName().asString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ), is (true) );

      final var streamProperties = stream.getWicaStreamProperties();
      assertThat( streamProperties.getHeartbeatFluxIntervalInMillis(), is( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );

      final var wicaStreamId = stream.getWicaStreamId();
      assertThat( wicaStreamId.asString(), is("0") );
   }

   @Test
   void testCreateStreamWithNoPropsObject()
   {
      final String testString = "{ \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = service.create(testString );
      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertThat( channels.size(), is( 2) );
      assertThat( channels.stream().map( c -> c.getName().getControlSystemName().asString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ), is( true) );
      assertThat( channels.stream().map( c -> c.getName().getControlSystemName().asString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ), is (true) );

      final var streamProperties = stream.getWicaStreamProperties();
      assertThat( streamProperties.getHeartbeatFluxIntervalInMillis(), is( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );

      final var wicaStreamId = stream.getWicaStreamId();
      assertThat( wicaStreamId.asString(), is("0") );
   }

   @Test
   void testCreateStreamWithPropsObject()
   {
      final String testString = "{ \"props\" : { \"prec\":8, \"fields\":\"abc;def\" }, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = service.create(testString );

      final Set<WicaChannel> channels = stream.getWicaChannels();
      assertEquals( 2, channels.size() );
      assertThat( channels.stream().map( c -> c.getName().getControlSystemName().asString() ).anyMatch( s -> s.equals( "MHC1:IST:2" ) ), is( true) );
      assertThat( channels.stream().map( c -> c.getName().getControlSystemName().asString() ).anyMatch( s -> s.equals( "MHC2:IST:2" ) ), is (true) );

      final var streamProperties = stream.getWicaStreamProperties();
      assertThat( streamProperties.getHeartbeatFluxIntervalInMillis(), is( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );
      assertThat( streamProperties.getNumericPrecision(), is(WicaChannelProperties.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( streamProperties.getFieldsOfInterest(), is( Set.of( "abc", "def" ) ) );

      final var wicaStreamId = stream.getWicaStreamId();
      assertThat( wicaStreamId.asString(), is("0") );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
