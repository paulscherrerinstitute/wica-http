/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelValueTest
{
   @Test
   void testChannelValueDisconnected()
   {
      var wicaChannelValue = WicaChannelValue.createChannelValueDisconnected();
      assertThat( wicaChannelValue.isConnected(), is( false ) );
      assertThat( wicaChannelValue.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
   }
   
   @Test
   void testChannelValueConnectedString()
   {
      var wicaChannelValue = WicaChannelValue.createChannelValueConnected( "abc" );
      assertThat( wicaChannelValue.isConnected(), is( true ) );
      assertThat( wicaChannelValue.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getWicaChannelType(), is( WicaChannelType.STRING ) );
      assertThat( wicaChannelValue.getWicaAlarmSeverity(), is( WicaChannelAlarmSeverity.NO_ALARM ) );
      assertThat( wicaChannelValue.getWicaChannelAlarmStatus().getStatusCode(), is( WicaChannelAlarmStatus.ofNoError().getStatusCode() ) );
      assertThat( wicaChannelValue.getDataSourceTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getValue(), is( "abc" ) );
   }

   @Test
   void testChannelValueConnectedStringArray()
   {
      var wicaChannelValue = WicaChannelValue.createChannelValueConnected( new String[] { "abc", "def"} );
      assertThat( wicaChannelValue.isConnected(), is( true ) );
      assertThat( wicaChannelValue.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getWicaChannelType(), is( WicaChannelType.STRING_ARRAY ) );
      assertThat( wicaChannelValue.getWicaAlarmSeverity(), is( WicaChannelAlarmSeverity.NO_ALARM ) );
      assertThat( wicaChannelValue.getWicaChannelAlarmStatus().getStatusCode(), is( WicaChannelAlarmStatus.ofNoError().getStatusCode() ) );
      assertThat( wicaChannelValue.getDataSourceTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getValue(), arrayContaining( "abc", "def" ) );
   }

   @Test
   void testChannelValueConnectedInteger()
   {
      var wicaChannelValue = WicaChannelValue.createChannelValueConnected( 123 );
      assertThat( wicaChannelValue.isConnected(), is( true ) );
      assertThat( wicaChannelValue.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getWicaChannelType(), is( WicaChannelType.INTEGER ) );
      assertThat( wicaChannelValue.getWicaAlarmSeverity(), is( WicaChannelAlarmSeverity.NO_ALARM ) );
      assertThat( wicaChannelValue.getWicaChannelAlarmStatus().getStatusCode(), is( WicaChannelAlarmStatus.ofNoError().getStatusCode() ) );
      assertThat( wicaChannelValue.getDataSourceTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getValue(), is( 123 ) );
   }

   @Test
   void testChannelValueConnectedIntegerArray()
   {
      var wicaChannelValue = WicaChannelValue.createChannelValueConnected( new int[] { 123, 456 } );
      assertThat( wicaChannelValue.isConnected(), is( true ) );
      assertThat( wicaChannelValue.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getWicaChannelType(), is( WicaChannelType.INTEGER_ARRAY ) );
      assertThat( wicaChannelValue.getWicaAlarmSeverity(), is( WicaChannelAlarmSeverity.NO_ALARM ) );
      assertThat( wicaChannelValue.getWicaChannelAlarmStatus().getStatusCode(), is( WicaChannelAlarmStatus.ofNoError().getStatusCode() ) );
      assertThat( wicaChannelValue.getDataSourceTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( Arrays.stream(wicaChannelValue.getValue()).boxed().toArray( Integer[]::new ), arrayContaining( 123, 456 ) );
   }

   @Test
   void testChannelValueConnectedReal()
   {
      var wicaChannelValue = WicaChannelValue.createChannelValueConnected( 123.2 );
      assertThat( wicaChannelValue.isConnected(), is( true ) );
      assertThat( wicaChannelValue.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getWicaChannelType(), is( WicaChannelType.REAL ) );
      assertThat( wicaChannelValue.getWicaAlarmSeverity(), is( WicaChannelAlarmSeverity.NO_ALARM ) );
      assertThat( wicaChannelValue.getWicaChannelAlarmStatus().getStatusCode(), is( WicaChannelAlarmStatus.ofNoError().getStatusCode() ) );
      assertThat( wicaChannelValue.getDataSourceTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getValue(), is( 123.2 ) );
   }

   @Test
   void testChannelValueConnectedRealArray()
   {
      var wicaChannelValue = WicaChannelValue.createChannelValueConnected( new double[] { 123.56, 456.78 } );
      assertThat( wicaChannelValue.isConnected(), is( true ) );
      assertThat( wicaChannelValue.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelValue.getWicaChannelType(), is( WicaChannelType.REAL_ARRAY ) );
      assertThat( wicaChannelValue.getWicaAlarmSeverity(), is( WicaChannelAlarmSeverity.NO_ALARM ) );
      assertThat( wicaChannelValue.getWicaChannelAlarmStatus().getStatusCode(), is( WicaChannelAlarmStatus.ofNoError().getStatusCode() ) );
      assertThat( wicaChannelValue.getDataSourceTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( Arrays.stream( wicaChannelValue.getValue()).boxed().toArray( Double[]::new ), arrayContaining( 123.56, 456.78 ) );
   }

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
