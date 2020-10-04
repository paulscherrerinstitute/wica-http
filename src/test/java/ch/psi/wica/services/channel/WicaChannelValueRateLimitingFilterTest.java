/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueRateLimitingFilterTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testConstructorIllegalSamplingInterval()
   {
      assertThrows( IllegalArgumentException.class, () -> new WicaChannelValueRateLimitingFilter(0 ) );
   }

   @Test
   void testMapSampleAllValues() throws InterruptedException
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnectedString( "abc" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnectedString( "def" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnectedString( "ghi" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnectedString( "jkl" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue5 = WicaChannelValue.createChannelValueConnectedString( "mno" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4, strValue5 );
      final WicaChannelValueFilter mapper = new WicaChannelValueRateLimitingFilter(90 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );

      assertThat( outputList.size(), is( 5 ) );
      assertThat( outputList.get( 0 ), is( inputList.get( 0 ) ) );
      assertThat( outputList.get( 1 ), is( inputList.get( 1 ) ) );
      assertThat( outputList.get( 2 ), is( inputList.get( 2 ) ) );
      assertThat( outputList.get( 3 ), is( inputList.get( 3 ) ) );
      assertThat( outputList.get( 4 ), is( inputList.get( 4 ) ) );
   }

   @Test
   void testMapSampleEveryOtherValue() throws InterruptedException
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnectedString( "abc" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnectedString( "def" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnectedString( "ghi" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnectedString( "jkl" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue5 = WicaChannelValue.createChannelValueConnectedString( "mno" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4, strValue5 );
      final WicaChannelValueFilter mapper = new WicaChannelValueRateLimitingFilter(110 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );

      assertThat( outputList.size(), is( 3) );
      assertThat( outputList.get( 0 ), is( inputList.get( 0 ) ) );
      assertThat( outputList.get( 1 ), is( inputList.get( 2 ) ) );
      assertThat( outputList.get( 2 ), is( inputList.get( 4 ) ) );
   }

   @Test
   void testSuccessiveMapOperations() throws InterruptedException
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnectedString( "abc" );
      final List<WicaChannelValue> inputList1 = List.of( strValue1 );
      final WicaChannelValueFilter mapper = new WicaChannelValueRateLimitingFilter(110 );
      final List<WicaChannelValue> outputList1  = mapper.apply(inputList1 );

      assertThat( outputList1.size(), is( 1 ) );
      assertThat( outputList1.get( 0 ), is( inputList1.get( 0 ) ) );

      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnectedString( "def" );
      final List<WicaChannelValue> inputList2 = List.of( strValue2 );
      final List<WicaChannelValue> outputList2  = mapper.apply(inputList2 );

      assertThat( outputList2.size(), is( 0 ) );

      Thread.sleep( 120 );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnectedString( "ghi" );
      final List<WicaChannelValue> inputList3 = List.of( strValue3 );
      final List<WicaChannelValue> outputList3  = mapper.apply( inputList3 );

      assertThat( outputList3.size(), is( 1 ) );
      assertThat( outputList3.get( 0 ), is( inputList3.get( 0 ) ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
