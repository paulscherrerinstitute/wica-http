/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelMetadataTest
{
   @Test
   void testMetadataTypeUnknown()
   {
      var wicaChannelMetadata = WicaChannelMetadata.createUnknownInstance();
      assertThat( wicaChannelMetadata.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelMetadata.getType(), is( WicaChannelType.UNKNOWN ) );
   }

   @Test
   void testMetadataString()
   {
      var wicaChannelMetadata = WicaChannelMetadata.createStringInstance();
      assertThat( wicaChannelMetadata.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelMetadata.getType(), is( WicaChannelType.STRING ) );
   }

   @Test
   void testMetadataStringArray()
   {
      var wicaChannelMetadata = WicaChannelMetadata.createStringArrayInstance();
      assertThat( wicaChannelMetadata.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelMetadata.getType(), is( WicaChannelType.STRING_ARRAY ) );
   }

   @Test
   void testMetadataInteger()
   {
      var wicaChannelMetadata = WicaChannelMetadata.createIntegerInstance("abc", 1, 2, 3, 4, 5, 6 , 7, 8 );
      assertThat( wicaChannelMetadata.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelMetadata.getType(), is( WicaChannelType.INTEGER ) );
      assertThat( wicaChannelMetadata.getUnits(), is( "abc") );
      assertThat( wicaChannelMetadata.getUpperDisplay(), is( 1) );
      assertThat( wicaChannelMetadata.getLowerDisplay(), is( 2) );
      assertThat( wicaChannelMetadata.getUpperControl(), is( 3) );
      assertThat( wicaChannelMetadata.getLowerControl(), is( 4) );
      assertThat( wicaChannelMetadata.getUpperAlarm(),   is( 5) );
      assertThat( wicaChannelMetadata.getLowerAlarm(),   is( 6) );
      assertThat( wicaChannelMetadata.getUpperWarning(), is( 7) );
      assertThat( wicaChannelMetadata.getLowerWarning(), is( 8) );
   }

   @Test
   void testMetadataIntegerArray()
   {
      var wicaChannelMetadata = WicaChannelMetadata.createIntegerArrayInstance("abc", 1, 2, 3, 4, 5, 6 , 7, 8 );
      assertThat( wicaChannelMetadata.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelMetadata.getType(), is( WicaChannelType.INTEGER_ARRAY ) );
      assertThat( wicaChannelMetadata.getUnits(), is( "abc") );
      assertThat( wicaChannelMetadata.getUpperDisplay(), is( 1) );
      assertThat( wicaChannelMetadata.getLowerDisplay(), is( 2) );
      assertThat( wicaChannelMetadata.getUpperControl(), is( 3) );
      assertThat( wicaChannelMetadata.getLowerControl(), is( 4) );
      assertThat( wicaChannelMetadata.getUpperAlarm(),   is( 5) );
      assertThat( wicaChannelMetadata.getLowerAlarm(),   is( 6) );
      assertThat( wicaChannelMetadata.getUpperWarning(), is( 7) );
      assertThat( wicaChannelMetadata.getLowerWarning(), is( 8) );
   }

   @Test
   void testMetadataReal()
   {
      var wicaChannelMetadata = WicaChannelMetadata.createRealInstance("abc", 1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9 );
      assertThat( wicaChannelMetadata.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelMetadata.getType(), is( WicaChannelType.REAL ) );
      assertThat( wicaChannelMetadata.getUnits(), is( "abc") );
      assertThat( wicaChannelMetadata.getPrecision(),    is( 1) );
      assertThat( wicaChannelMetadata.getUpperDisplay(), is( 2.2) );
      assertThat( wicaChannelMetadata.getLowerDisplay(), is( 3.3) );
      assertThat( wicaChannelMetadata.getUpperControl(), is( 4.4) );
      assertThat( wicaChannelMetadata.getLowerControl(), is( 5.5) );
      assertThat( wicaChannelMetadata.getUpperAlarm(),   is( 6.6) );
      assertThat( wicaChannelMetadata.getLowerAlarm(),   is( 7.7) );
      assertThat( wicaChannelMetadata.getUpperWarning(), is( 8.8) );
      assertThat( wicaChannelMetadata.getLowerWarning(), is( 9.9) );
   }

   @Test
   void testMetadataRealArray()
   {
      var wicaChannelMetadata = WicaChannelMetadata.createRealArrayInstance("abc", 1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9 );
      assertThat( wicaChannelMetadata.getWicaServerTimestamp().isBefore( LocalDateTime.now() ), is( true ) );
      assertThat( wicaChannelMetadata.getType(), is( WicaChannelType.REAL_ARRAY ) );
      assertThat( wicaChannelMetadata.getUnits(), is( "abc") );
      assertThat( wicaChannelMetadata.getPrecision(),    is( 1) );
      assertThat( wicaChannelMetadata.getUpperDisplay(), is( 2.2) );
      assertThat( wicaChannelMetadata.getLowerDisplay(), is( 3.3) );
      assertThat( wicaChannelMetadata.getUpperControl(), is( 4.4) );
      assertThat( wicaChannelMetadata.getLowerControl(), is( 5.5) );
      assertThat( wicaChannelMetadata.getUpperAlarm(),   is( 6.6) );
      assertThat( wicaChannelMetadata.getLowerAlarm(),   is( 7.7) );
      assertThat( wicaChannelMetadata.getUpperWarning(), is( 8.8) );
      assertThat( wicaChannelMetadata.getLowerWarning(), is( 9.9) );
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
