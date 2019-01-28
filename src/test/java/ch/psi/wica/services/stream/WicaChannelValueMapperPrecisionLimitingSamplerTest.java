/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueMapperPrecisionLimitingSamplerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testMap()
   {
      final WicaChannelValue strValue = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue intValue = WicaChannelValue.createChannelValueConnected( 1234 );
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 1234.56 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 12345.5678 );

      final List<WicaChannelValue> inputList = List.of( strValue, intValue, dblValue1, dblValue2 );

      final WicaChannelValueMapper mapper = new WicaChannelValueMapperPrecisionLimitingSampler(2 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 4, outputList.size() );
      assertEquals( inputList.get( 0 ), outputList.get( 0 ) );
      assertEquals( inputList.get( 1 ), outputList.get( 1 ) );
      double dblValue1In = (( WicaChannelValue.WicaChannelValueConnectedReal) dblValue1 ).getValue();
      double dblValue1Out = (( WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 2 ) ).getValue();
      assertEquals( dblValue1In, dblValue1Out );
      double dblValue2In = (( WicaChannelValue.WicaChannelValueConnectedReal) dblValue2 ).getValue();
      double dblValue2Out = (( WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 3 ) ).getValue();
      double dblValue2ExpectedOut = BigDecimal.valueOf( dblValue2Out ).setScale( 2, RoundingMode.HALF_UP ).doubleValue();
      assertEquals( dblValue2ExpectedOut, dblValue2Out );
   }

   @Test
   void testMapDoubleValueArray()
   {
      final WicaChannelValue dblValueArray = WicaChannelValue.createChannelValueConnected( new double[] { 1234.5678, 9876.5432 } );
      final List<WicaChannelValue> inputList = List.of( dblValueArray );
      final WicaChannelValueMapper mapper = new WicaChannelValueMapperPrecisionLimitingSampler(2 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 1, outputList.size() );
      Assertions.assertArrayEquals( new double[] { 1234.57, 9876.54 }, ( (WicaChannelValue.WicaChannelValueConnectedRealArray) outputList.get( 0 ) ).getValue()  );
   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
