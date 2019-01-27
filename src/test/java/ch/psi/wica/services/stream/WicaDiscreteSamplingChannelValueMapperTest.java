/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaDiscreteSamplingChannelValueMapperTest
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
      assertThrows( IllegalArgumentException.class, () -> new WicaDiscreteSamplingChannelValueMapper( 0 ) );
   }

   @Test
   void testMapSampleAllValues()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3 );
      final WicaChannelValueMapper mapper = new WicaDiscreteSamplingChannelValueMapper( 1 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
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
      final WicaChannelValueMapper mapper = new WicaDiscreteSamplingChannelValueMapper( 2 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
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
      final WicaChannelValueMapper mapper = new WicaDiscreteSamplingChannelValueMapper(9 );
      final List<WicaChannelValue> outputList = mapper.map( inputList );
      assertEquals(1, outputList.size() );
      assertEquals( inputList.get(0), outputList.get(0) );
      final List<WicaChannelValue> outputList2 = mapper.map( inputList );
      assertEquals(1, outputList2.size());
      assertEquals( inputList.get(3), outputList2.get(0) );
   }

   @Test
   void testSampleOneInEight()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected("abc");
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected("def");
      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2 );
      final WicaChannelValueMapper mapper = new WicaDiscreteSamplingChannelValueMapper(8 );
      final List<WicaChannelValue> outputList1 = mapper.map( inputList );
      assertEquals(1, outputList1.size() );
      assertEquals( inputList.get(0), outputList1.get(0) );
      final List<WicaChannelValue> outputList2 = mapper.map( inputList );
      assertEquals(0, outputList2.size() );
      final List<WicaChannelValue> outputList3 = mapper.map( inputList );
      assertEquals(0, outputList3.size() );
      final List<WicaChannelValue> outputList4 = mapper.map( inputList );
      assertEquals(0, outputList4.size() );
      final List<WicaChannelValue> outputList5 = mapper.map( inputList );
      assertEquals(1, outputList5.size());
      assertEquals( inputList.get(0), outputList5.get(0) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
