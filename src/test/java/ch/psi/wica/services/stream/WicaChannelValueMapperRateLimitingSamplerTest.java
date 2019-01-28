/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueMapperRateLimitingSamplerTest
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
      assertThrows( IllegalArgumentException.class, () -> new WicaChannelValueMapperRateLimitingSampler(0 ) );
   }

   @Test
   void testMapSampleAllValues() throws InterruptedException
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected( "jkl" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue5 = WicaChannelValue.createChannelValueConnected( "mno" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4, strValue5 );
      final WicaChannelValueMapper mapper = new WicaChannelValueMapperRateLimitingSampler(90 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 5 , outputList.size() );
      assertEquals( inputList.get( 0 ), outputList.get( 0 ) );
      assertEquals( inputList.get( 1 ), outputList.get( 1 ) );
      assertEquals( inputList.get( 2 ), outputList.get( 2 ) );
      assertEquals( inputList.get( 3 ), outputList.get( 3 ) );
      assertEquals( inputList.get( 4 ), outputList.get( 4 ) );
   }

   @Test
   void testMapSampleEveryOtherValue() throws InterruptedException
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected( "jkl" );
      Thread.sleep( 100 );
      final WicaChannelValue strValue5 = WicaChannelValue.createChannelValueConnected( "mno" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4, strValue5 );
      final WicaChannelValueMapper mapper = new WicaChannelValueMapperRateLimitingSampler(110 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 3 , outputList.size() );
      assertEquals( inputList.get( 0 ), outputList.get( 0 ) );
      assertEquals( inputList.get( 2 ), outputList.get( 1 ) );
      assertEquals( inputList.get( 4 ), outputList.get( 2 ) );
   }

   @Test
   void testSuccessiveMapOperations() throws InterruptedException
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final List<WicaChannelValue> inputList1 = List.of( strValue1 );
      final WicaChannelValueMapper mapper = new WicaChannelValueMapperRateLimitingSampler(110 );
      final List<WicaChannelValue> outputList1  = mapper.map( inputList1 );
      assertEquals( 1 , outputList1.size() );
      assertEquals( inputList1.get( 0 ), outputList1.get( 0 ) );

      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final List<WicaChannelValue> inputList2 = List.of( strValue2 );
      final List<WicaChannelValue> outputList2  = mapper.map( inputList2 );
      assertEquals( 0 , outputList2.size() );

      Thread.sleep( 120 );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final List<WicaChannelValue> inputList3 = List.of( strValue3 );
      final List<WicaChannelValue> outputList3  = mapper.map( inputList3 );
      assertEquals( 1 , outputList3.size() );
      assertEquals( inputList3.get( 0 ), outputList3.get( 0 ) );
   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
