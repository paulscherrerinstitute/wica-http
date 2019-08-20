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
class WicaChannelValueFixedSamplingCycleFilterTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testConstructorIllegalSampleLength()
   {
      assertThrows( IllegalArgumentException.class, () -> new WicaChannelValueFixedSamplingCycleFilter(0 ) );
   }

   @Test
   void testMapSampleAllValues()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3 );
      final WicaChannelValueFilter mapper = new WicaChannelValueFixedSamplingCycleFilter(1 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );
      assertThat( outputList.size(), is( inputList.size() ) );
      assertThat( outputList.get( 0 ), is( inputList.get( 0 ) ) );
      assertThat( outputList.get( 1 ), is( inputList.get( 1 ) ) );
      assertThat( outputList.get( 2 ), is( inputList.get( 2 ) ) );
   }

   @Test
   void testMapSampleEveryOtherValue()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected( "jkl" );
      final WicaChannelValue strValue5 = WicaChannelValue.createChannelValueConnected( "mno" );
      final WicaChannelValue strValue6 = WicaChannelValue.createChannelValueConnected( "pqr" );
      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4, strValue5, strValue6 );
      final WicaChannelValueFilter mapper = new WicaChannelValueFixedSamplingCycleFilter(2 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );
      assertThat( outputList.size(), is( inputList.size() / 2 ) );
      assertThat( outputList.get( 0 ), is( inputList.get( 0 ) ) );
      assertThat( outputList.get( 1 ), is( inputList.get( 2 ) ) );
      assertThat( outputList.get( 2 ), is( inputList.get( 4 ) ) );
   }

   @Test
   void testSampleOneInNine()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected("abc");
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected("def");
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected("ghi");
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected("jkl");
      final WicaChannelValue strValue5 = WicaChannelValue.createChannelValueConnected("mno");
      final WicaChannelValue strValue6 = WicaChannelValue.createChannelValueConnected("pqr");
      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4, strValue5, strValue6 );
      final WicaChannelValueFilter filter = new WicaChannelValueFixedSamplingCycleFilter(9 );
      final List<WicaChannelValue> outputList1 = filter.apply(inputList );
      assertThat( outputList1.size(), is( 1  ) );
      assertThat( outputList1.get( 0 ), is( inputList.get( 0 ) ) );

      final List<WicaChannelValue> outputList2 = filter.apply(inputList );
      assertThat( outputList2.size(), is( 1  ) );
      assertThat( outputList2.get( 0 ), is( inputList.get( 3 ) ) );
   }

   @Test
   void testSampleOneInEight()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected("abc");
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected("def");
      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2 );
      final WicaChannelValueFilter filter = new WicaChannelValueFixedSamplingCycleFilter(8 );
      final List<WicaChannelValue> outputList1 = filter.apply( inputList );

      assertThat( outputList1.size(), is( 1  ) );
      assertThat( outputList1.get( 0 ), is( inputList.get( 0 ) ) );

      final List<WicaChannelValue> outputList2 = filter.apply(inputList );
      assertThat( outputList2.size(), is( 0  ) );

      final List<WicaChannelValue> outputList3 = filter.apply(inputList );
      assertThat( outputList3.size(), is( 0  ) );

      final List<WicaChannelValue> outputList4 = filter.apply(inputList );
      assertThat( outputList4.size(), is( 0  ) );

      final List<WicaChannelValue> outputList5 = filter.apply(inputList );
      assertThat( outputList5.size(), is( 1  ) );
      assertThat( outputList5.get( 0 ), is( inputList.get( 0 ) ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
