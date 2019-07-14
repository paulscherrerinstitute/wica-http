/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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
      assertEquals( inputList.size() , outputList.size() );
      assertEquals( inputList.get( 0 ), outputList.get( 0 ) );
      assertEquals( inputList.get( 1 ), outputList.get( 1 ) );
      assertEquals( inputList.get( 2 ), outputList.get( 2 ) );
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
      assertEquals( inputList.size() / 2 , outputList.size() );
      assertEquals( inputList.get( 0 ), outputList.get( 0 ) );
      assertEquals( inputList.get( 2 ), outputList.get( 1 ) );
      assertEquals( inputList.get( 4 ), outputList.get( 2 ) );
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
      final WicaChannelValueFilter mapper = new WicaChannelValueFixedSamplingCycleFilter(9 );
      final List<WicaChannelValue> outputList = mapper.apply(inputList );
      assertEquals(1, outputList.size() );
      assertEquals( inputList.get(0), outputList.get(0) );
      final List<WicaChannelValue> outputList2 = mapper.apply(inputList );
      assertEquals(1, outputList2.size());
      assertEquals( inputList.get(3), outputList2.get(0) );
   }

   @Test
   void testSampleOneInEight()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected("abc");
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected("def");
      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2 );
      final WicaChannelValueFilter mapper = new WicaChannelValueFixedSamplingCycleFilter(8 );
      final List<WicaChannelValue> outputList1 = mapper.apply(inputList );
      assertEquals(1, outputList1.size() );
      assertEquals( inputList.get(0), outputList1.get(0) );
      final List<WicaChannelValue> outputList2 = mapper.apply(inputList );
      assertEquals(0, outputList2.size() );
      final List<WicaChannelValue> outputList3 = mapper.apply(inputList );
      assertEquals(0, outputList3.size() );
      final List<WicaChannelValue> outputList4 = mapper.apply(inputList );
      assertEquals(0, outputList4.size() );
      final List<WicaChannelValue> outputList5 = mapper.apply(inputList );
      assertEquals(1, outputList5.size());
      assertEquals( inputList.get(0), outputList5.get(0) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
