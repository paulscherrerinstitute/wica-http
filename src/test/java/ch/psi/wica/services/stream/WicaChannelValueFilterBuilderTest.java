/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

//@SpringBootTest
class WicaChannelValueFilterBuilderTest
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
      final WicaChannelValue intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final WicaChannelValue intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final WicaChannelValue intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final WicaChannelValue intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      WicaChannelValueFilter mapper = WicaChannelValueFilterBuilder.createDefault();
      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );
      assertEquals( 1, outputList.size() );
      assertEquals(111, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get(0 ) ).getValue() );
   }

   @Test
   void testLatestValueSampler()
   {
      final WicaChannelValue intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final WicaChannelValue intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final WicaChannelValue intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final WicaChannelValue intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final WicaChannelProperties.DataAcquisitionMode daqMode = WicaChannelProperties.DataAcquisitionMode.MONITOR;
      final WicaChannelValueFilter mapper = WicaChannelValueFilterBuilder.createFilterForMonitoredChannels(new WicaChannelProperties(daqMode,
                                                                                                                                     100,
                                                                                                                                     "val;sevr",
                                                                                                                                     5,
                                                                                                                                     WicaChannelProperties.FilterType.LAST_N,
                                                                                                                                     2, null, null, null ) );
      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );
      assertEquals(2, outputList.size() );
      assertEquals(15, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals(111, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
   }

   @Test
   void testFixedCycleSampler()
   {
      final WicaChannelValue intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final WicaChannelValue intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final WicaChannelValue intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final WicaChannelValue intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final WicaChannelProperties.DataAcquisitionMode daqMode = WicaChannelProperties.DataAcquisitionMode.MONITOR;
      final WicaChannelValueFilter mapper = WicaChannelValueFilterBuilder.createFilterForMonitoredChannels(new WicaChannelProperties(daqMode,
                                                                                                                                     100,
                                                                                                                                     "val;sevr",
                                                                                                                                     5,
                                                                                                                                     WicaChannelProperties.FilterType.ONE_IN_N,
                                                                                                                                     null, 2, null, null ) );


      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList  = mapper.apply( inputList );
      assertEquals( 2, outputList.size() );
      assertEquals( 129, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 15, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
