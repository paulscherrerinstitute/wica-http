/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;


/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueFilteringServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private WicaChannelValueFilteringService serviceUnderTest;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testFilterMonitoredValues_LatestValueFilter()
   {
      final var dblValue1 = WicaChannelValue.createChannelValueConnected( 129.123456 );
      final var dblValue2 = WicaChannelValue.createChannelValueConnected(  14.123456 );
      final var dblValue3 = WicaChannelValue.createChannelValueConnected(  15.123456 );
      final var dblValue4 = WicaChannelValue.createChannelValueConnected( 111.123456 );

      final var props =  new WicaChannelProperties.ChannelPropertyBuilder()
            .withFilterType(WicaChannelProperties.FilterType.LAST_N )
            .withNumSamples( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterMonitoredValues( WicaChannel.createFromNameAndProperties( "abc", props ) , inputList );

      assertEquals(2, outputList.size() );
      assertEquals( 15.123456, ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue() );
      assertEquals(111.123456, ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue() );
   }

   @Test
   void testFilterMonitoredValues_FixedSamplingCycleFilter()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final var props =  new WicaChannelProperties.ChannelPropertyBuilder()
            .withFilterType(WicaChannelProperties.FilterType.ONE_IN_M )
            .withFilterCycleLength( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterMonitoredValues( WicaChannel.createFromNameAndProperties( "abc", props ) , inputList );

      assertEquals( 2, outputList.size() );
      assertEquals( 129, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 15, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
   }

   @Test
   void testFilterMonitoredValues_PassEverythingFilter()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final var props =  new WicaChannelProperties.ChannelPropertyBuilder()
            .withFilterType(WicaChannelProperties.FilterType.ALL_VALUE )
            .withFilterCycleLength( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterMonitoredValues( WicaChannel.createFromNameAndProperties( "abc", props ) , inputList );

      assertEquals( 4, outputList.size() );
      assertEquals( 129, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 14,  ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
      assertEquals( 15,  ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 2 ) ).getValue() );
      assertEquals( 111, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 3 ) ).getValue() );
   }

   @Test
   void testFilterPolledValues_FixedSamplingCycleFilterWithSamplingRatio1()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final var props =  new WicaChannelProperties.ChannelPropertyBuilder()
            .withPolledValueSamplingRatio( 1 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterPolledValues( WicaChannel.createFromNameAndProperties( "abc", props ) , inputList );

      assertEquals( 4, outputList.size() );
      assertEquals( 129, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 14,  ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
      assertEquals( 15,  ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 2 ) ).getValue() );
      assertEquals( 111, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 3 ) ).getValue() );
   }

   @Test
   void testFilterPolledValues_FixedSamplingCycleFilterWithSamplingRatio3()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final var props =  new WicaChannelProperties.ChannelPropertyBuilder()
            .withPolledValueSamplingRatio( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterPolledValues( WicaChannel.createFromNameAndProperties( "abc", props ) , inputList );

      assertEquals( 2, outputList.size() );
      assertEquals( 129, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 15,  ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
