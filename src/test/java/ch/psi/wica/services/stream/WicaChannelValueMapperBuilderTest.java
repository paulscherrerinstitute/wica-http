/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueMapperBuilderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testDefaultMap()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2  );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.12345678 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createDefault();
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 1, outputList.size() );
      assertEquals("111.123457", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get(0 )).getValueAsBigDecimal().toPlainString() );
   }

   @Test
   void testPrecisionMapper1()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2  );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "1", "filterType", "allValue" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 4, outputList.size() );
      assertEquals( "129.4", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals(  "14.2", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals(  "15.9", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 2 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals( "111.1", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 3 ) ).getValueAsBigDecimal().toPlainString() );
   }

   @Test
   void testPrecisionMapper2()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386, 10 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2, 10 );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666, 10 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111, 10 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "10", "filterType", "allValue" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 4, outputList.size() );
      assertEquals( "129.3860000000", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals(  "14.2000000000", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals(  "15.8766666600", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 2 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals( "111.1111000000", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 3 ) ).getValueAsBigDecimal().toPlainString() );
   }

   @Test
   void testLatestValueMapper()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2  );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "1", "filterType", "last-n", "n", "2" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 2, outputList.size() );
      assertEquals(  "15.9", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals( "111.1", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValueAsBigDecimal().toPlainString() );
   }

   @Test
   void testDiscreteValueMapper()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2 );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "1", "filterType", "1-in-n", "n", "2" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 2, outputList.size() );
      assertEquals( "129.4", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValueAsBigDecimal().toPlainString() );
      assertEquals(  "15.9", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValueAsBigDecimal().toPlainString() );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
