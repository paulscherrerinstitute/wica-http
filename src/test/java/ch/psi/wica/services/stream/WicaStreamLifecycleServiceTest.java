/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelPropertiesDefaults;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.model.stream.WicaStreamPropertiesDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

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
   void testCreateStreamWithNullString()
   {
      final var ex = assertThrows( NullPointerException.class, () -> service.create( null ) );
      assertThat( ex.getMessage(), is( "The 'jsonStreamConfiguration' argument was null." ) );
   }

   @Test
   void testCreateStreamWithEmptyString()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> service.create( "" ) );
      assertThat( ex.getMessage(), is( "The 'jsonStreamConfiguration' argument was empty." ) );
   }

   @Test
   void testCreateStreamWithBlankString()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> service.create( " " ) );
      assertThat( ex.getMessage(), is( "The JSON configuration string ' ' was invalid." ) );
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
      assertThat( streamProperties.getHeartbeatFluxIntervalInMillis(), is(WicaStreamPropertiesDefaults.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );

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
      assertThat( streamProperties.getHeartbeatFluxIntervalInMillis(), is( WicaStreamPropertiesDefaults.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );

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
      assertThat( streamProperties.getHeartbeatFluxIntervalInMillis(), is( WicaStreamPropertiesDefaults.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );
      assertThat( streamProperties.getNumericPrecision(), is(WicaChannelPropertiesDefaults.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( streamProperties.getFieldsOfInterest(), is( "abc;def" ) );

      final var wicaStreamId = stream.getWicaStreamId();
      assertThat( wicaStreamId.asString(), is("0") );
   }

   @Test
   void testDeleteStreamWithNullString()
   {
      final var ex = assertThrows( NullPointerException.class, () -> service.delete( null ) );
      assertThat( ex.getMessage(), is( "The 'wicaStreamId' argument was null." ) );
   }

   @Test
   void testDeleteUnknownStream()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> service.delete( WicaStreamId.of( "NoSuchStream" ) ) );
      assertThat( ex.getMessage(), is( "The 'wicaStreamId' argument was not recognised." ) );
   }

   @Test
   void testDeleteStream()
   {
      // Create a simple stream
      final String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = service.create( testString );

      // The first delete should succeed.
      final var wicaStreamId = stream.getWicaStreamId();
      assertDoesNotThrow( () -> service.delete( wicaStreamId ) );

      // Attempting to delete the same stream a second time should result in an exception.
      final var ex = assertThrows( IllegalArgumentException.class, () -> service.delete( WicaStreamId.of( "NoSuchStream" ) ) );
      assertThat( ex.getMessage(), is( "The 'wicaStreamId' argument was not recognised." ) );
   }

   @Test
   void testRestartMonitoringWithNullString()
   {
      final var ex = assertThrows( NullPointerException.class, () -> service.restartMonitoring( null ) );
      assertThat( ex.getMessage(), is( "The 'wicaStreamId' argument was null." ) );
   }

   @Test
   void testRestartMonitoringOnUnknownStream()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> service.delete( WicaStreamId.of( "NoSuchStream" ) ) );
      assertThat( ex.getMessage(), is( "The 'wicaStreamId' argument was not recognised." ) );
   }

   @Test
   void testRestartMonitoring()
   {
      // Create a simple stream
      final String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = service.create( testString );

      // The restart the monitoring on it
      final var wicaStreamId = stream.getWicaStreamId();
      assertDoesNotThrow( () -> service.restartMonitoring( wicaStreamId ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
