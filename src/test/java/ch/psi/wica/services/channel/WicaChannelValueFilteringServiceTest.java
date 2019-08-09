/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;


/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
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
   void testFilterValues_LatestValueFilter()
   {
      final var dblValue1 = WicaChannelValue.createChannelValueConnected( 129.123456 );
      final var dblValue2 = WicaChannelValue.createChannelValueConnected(  14.123456 );
      final var dblValue3 = WicaChannelValue.createChannelValueConnected(  15.123456 );
      final var dblValue4 = WicaChannelValue.createChannelValueConnected( 111.123456 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType(WicaFilterType.LAST_N )
            .withFilterNumSamples( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                                 .withChannelNameAndProperties( "abc", props )
                                                                                 .build(), inputList );

      assertEquals(2, outputList.size() );
      assertEquals( 15.123456, ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue() );
      assertEquals(111.123456, ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue() );
   }

   @Test
   void testFilterValues_FixedSamplingCycleFilter()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType(WicaFilterType.ONE_IN_M )
            .withFilterCycleLength( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                                 .withChannelNameAndProperties( "abc", props )
                                                                                 .build(), inputList );
      assertEquals( 2, outputList.size() );
      assertEquals( 129, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 15, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
   }

   @Test
   void testFilterValues_RateLimitingFilter() throws InterruptedException
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnected( 1 );
      final var intValue2 = WicaChannelValue.createChannelValueConnected( 2 );
      final var intValue3 = WicaChannelValue.createChannelValueConnected( 3 );
      final var intValue4 = WicaChannelValue.createChannelValueConnected( 4 );
      Thread.sleep( 600 );
      final var intValue5 = WicaChannelValue.createChannelValueConnected( 5 );
      final var intValue6 = WicaChannelValue.createChannelValueConnected( 6 );
      final var intValue7 = WicaChannelValue.createChannelValueConnected( 7 );
      final var intValue8 = WicaChannelValue.createChannelValueConnected( 8 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType( WicaFilterType.RATE_LIMITER )
            .withFilterSamplingIntervalInMillis( 500 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 , intValue5, intValue6, intValue7, intValue8 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                               .withChannelNameAndProperties( "abc", props )
                                                                               .build(), inputList );
      assertEquals( 2, outputList.size() );
      assertEquals( 1, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 5, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
   }
   @Test
   void testFilterValues_PassEverythingFilter()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnected( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnected( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnected( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnected( 111 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType(WicaFilterType.ALL_VALUE )
            .withFilterCycleLength( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                                     .withChannelNameAndProperties( "abc", props )
                                                                                     .build(), inputList );

      assertEquals( 4, outputList.size() );
      assertEquals( 129, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue() );
      assertEquals( 14,  ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue() );
      assertEquals( 15,  ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 2 ) ).getValue() );
      assertEquals( 111, ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 3 ) ).getValue() );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
