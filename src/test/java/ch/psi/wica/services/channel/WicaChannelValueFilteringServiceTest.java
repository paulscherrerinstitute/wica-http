/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;


/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


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
      final var dblValue1 = WicaChannelValue.createChannelValueConnectedReal( 129.123456 );
      final var dblValue2 = WicaChannelValue.createChannelValueConnectedReal(  14.123456 );
      final var dblValue3 = WicaChannelValue.createChannelValueConnectedReal(  15.123456 );
      final var dblValue4 = WicaChannelValue.createChannelValueConnectedReal( 111.123456 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType(WicaFilterType.LAST_N )
            .withFilterNumSamples( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( dblValue1, dblValue2, dblValue3, dblValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                                 .withChannelNameAndProperties( "abc", props )
                                                                                 .build(), inputList );

      assertThat(outputList.size(), is( 2 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue(), is(15.123456 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue(), is(111.123456 ) );
   }

   @Test
   void testFilterValues_FixedSamplingCycleFilter()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnectedInteger( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnectedInteger( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnectedInteger( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnectedInteger( 111 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType(WicaFilterType.ONE_IN_M )
            .withFilterCycleLength( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                                 .withChannelNameAndProperties( "abc", props )
                                                                                 .build(), inputList );

      assertThat( outputList.size(), is( 2 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue(), is(129 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue(), is(15 ) );
   }

   @Test
   void testFilterValues_RateLimitingFilter() throws InterruptedException
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnectedInteger( 1 );
      final var intValue2 = WicaChannelValue.createChannelValueConnectedInteger( 2 );
      final var intValue3 = WicaChannelValue.createChannelValueConnectedInteger( 3 );
      final var intValue4 = WicaChannelValue.createChannelValueConnectedInteger( 4 );
      Thread.sleep( 600 );
      final var intValue5 = WicaChannelValue.createChannelValueConnectedInteger( 5 );
      final var intValue6 = WicaChannelValue.createChannelValueConnectedInteger( 6 );
      final var intValue7 = WicaChannelValue.createChannelValueConnectedInteger( 7 );
      final var intValue8 = WicaChannelValue.createChannelValueConnectedInteger( 8 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType( WicaFilterType.RATE_LIMITER )
            .withFilterSamplingIntervalInMillis( 500 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 , intValue5, intValue6, intValue7, intValue8 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                               .withChannelNameAndProperties( "abc", props )
                                                                               .build(), inputList );
      assertThat( outputList.size(), is( 2 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue(), is(1 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue(), is(5 ) );
   }

   @Test
   void testFilterValues_PassEverythingFilter()
   {
      final var intValue1 = WicaChannelValue.createChannelValueConnectedInteger( 129 );
      final var intValue2 = WicaChannelValue.createChannelValueConnectedInteger( 14 );
      final var intValue3 = WicaChannelValue.createChannelValueConnectedInteger( 15 );
      final var intValue4 = WicaChannelValue.createChannelValueConnectedInteger( 111 );

      final var props =  WicaChannelPropertiesBuilder.create()
            .withFilterType(WicaFilterType.ALL_VALUE )
            .withFilterCycleLength( 2 )
            .build();

      final List<WicaChannelValue> inputList = List.of( intValue1, intValue2, intValue3, intValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
                                                                                     .withChannelNameAndProperties( "abc", props )
                                                                                     .build(), inputList );

      assertThat( outputList.size(), is( 4 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 0 ) ).getValue(), is(129 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 1 ) ).getValue(), is(14 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 2 ) ).getValue(), is(15 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedInteger) outputList.get( 3 ) ).getValue(), is(111 ) );
   }

   @Test
   void testFilterValues_AveragingFilter()
   {
      final var realValue1 = WicaChannelValue.createChannelValueConnectedReal( 1.0 );
      final var realValue2 = WicaChannelValue.createChannelValueConnectedReal( 2.0 );
      final var realValue3 = WicaChannelValue.createChannelValueConnectedReal( 3.0 );
      final var realValue4 = WicaChannelValue.createChannelValueConnectedReal( 4.0 );

      final var props =  WicaChannelPropertiesBuilder.create()
              .withFilterType(WicaFilterType.AVERAGER )
              .withFilterNumSamplesInAverage( 2 )
              .build();

      final List<WicaChannelValue> inputList = List.of( realValue1, realValue2, realValue3, realValue4 );
      final List<WicaChannelValue> outputList = serviceUnderTest.filterValues( WicaChannelBuilder.create()
              .withChannelNameAndProperties( "abc", props )
              .build(), inputList );

      assertThat( outputList.size(), is( 2 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 0 ) ).getValue(), is(1.5 ) );
      assertThat( ( (WicaChannelValue.WicaChannelValueConnectedReal) outputList.get( 1 ) ).getValue(), is(3.5 ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
