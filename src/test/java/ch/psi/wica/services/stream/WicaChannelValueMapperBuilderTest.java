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
   void testDefault()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2  );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.12345678 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createDefault();
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 1, outputList.size() );
      assertEquals("111.123457", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get(0 )).getValue() );
   }

   @Test
   void testPrecisionLimitingSampler1()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2  );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "1", "filterType", "all-value" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 4, outputList.size() );
      assertEquals( "129.4", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue() );
      assertEquals(  "14.2", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue() );
      assertEquals(  "15.9", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 2 ) ).getValue() );
      assertEquals( "111.1", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 3 ) ).getValue() );
   }

   @Test
   void testPrecisionLimitingSampler2()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.3 );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.8766666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "10", "filterType", "all-value" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 4, outputList.size() );
      assertEquals( "129.3860000000", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue() );
      assertEquals(  "14.2000000000", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue() );
      assertEquals(  "15.8766666600", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 2 ) ).getValue() );
      assertEquals( "111.1111000000", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 3 ) ).getValue() );
   }

   @Test
   void testLatestValueSampler()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2  );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "1", "filterType", "last-n", "n", "2" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 2, outputList.size() );
      assertEquals(  "15.9", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue() );
      assertEquals( "111.1", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue() );
   }

   @Test
   void testFixedCycleSampler()
   {
      final WicaChannelValue dblValue1 = WicaChannelValue.createChannelValueConnected( 129.386 );
      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 14.2 );
      final WicaChannelValue dblValue3 = WicaChannelValue.createChannelValueConnected( 15.87666666 );
      final WicaChannelValue dblValue4 = WicaChannelValue.createChannelValueConnected( 111.1111 );

      WicaChannelValueMapper mapper = WicaChannelValueMapperBuilder.createFromChannelProperties( WicaChannelProperties.of( Map.of( "prec", "1", "filterType", "one-in-n", "n", "2" ) ) );
      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( 2, outputList.size() );
      assertEquals( "129.4", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue() );
      assertEquals(  "15.9", ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue() );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
