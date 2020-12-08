/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaChannelPropertiesTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testEmptyConstructorReturnsDefaultPropertyValues()
   {
      final var objectUnderTest = new WicaChannelProperties();

      assertThat( objectUnderTest.getDataAcquisitionMode(),                is( WicaChannelPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE ) );
      assertThat( objectUnderTest.getPollingIntervalInMillis(),            is( WicaChannelPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS) );
      assertThat( objectUnderTest.getFieldsOfInterest(),                   is( WicaChannelPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST ) );
      assertThat( objectUnderTest.getNumericPrecision(),                   is( WicaChannelPropertiesDefaults.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( objectUnderTest.getFilterType(),                         is( WicaChannelPropertiesDefaults.DEFAULT_FILTER_TYPE ) );
      assertThat( objectUnderTest.getFilterNumSamples(),                   is( WicaChannelPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES ) );
      assertThat( objectUnderTest.getFilterNumSamplesInAverage(),          is( WicaChannelPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES_IN_AVERAGE ) );
      assertThat( objectUnderTest.getFilterCycleLength(),                  is( WicaChannelPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH ) );
      assertThat( objectUnderTest.getFilterSamplingIntervalInMillis(),     is( WicaChannelPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS) );
      assertThat( objectUnderTest.getFilterDeadband(),                     is( WicaChannelPropertiesDefaults.DEFAULT_FILTER_DEADBAND ) );
   }

   @Test
   void testFullConstructorReturnsAssignedValues()
   {
      final var objectUnderTest = new WicaChannelProperties( WicaDataAcquisitionMode.MONITOR, 11,"fields", 12, WicaFilterType.LAST_N, 13, 14, 15, 16, 17.0 );

      assertThat( objectUnderTest.getDataAcquisitionMode(),                                is( WicaDataAcquisitionMode.MONITOR ) );
      assertThat( objectUnderTest.getPollingIntervalInMillis(),                            is(11 ) );
      assertThat( objectUnderTest.getFieldsOfInterest(),                                   is("fields" ));
      assertThat( objectUnderTest.getNumericPrecision(),                                   is(12 ) );
      assertThat( objectUnderTest.getFilterType(),                                         is( WicaFilterType.LAST_N) );
      assertThat( objectUnderTest.getFilterNumSamples(),                                   is(13 ) );
      assertThat( objectUnderTest.getFilterNumSamplesInAverage(),                          is(14 ) );
      assertThat( objectUnderTest.getFilterCycleLength(),                                  is(15 ) );
      assertThat( objectUnderTest.getFilterSamplingIntervalInMillis(),                     is(16 ) );
      assertThat( objectUnderTest.getFilterDeadband(),                                     is(17.0 ) );

      assertThat( objectUnderTest.getOptionalDataAcquisitionMode().isPresent(),            is(true ) );
      assertThat( objectUnderTest.getOptionalPollingIntervalInMillis().isPresent(),        is(true ) );
      assertThat( objectUnderTest.getOptionalFieldsOfInterest().isPresent(),               is(true ) );
      assertThat( objectUnderTest.getOptionalNumericPrecision().isPresent(),               is(true ) );
      assertThat( objectUnderTest.getOptionalFilterType().isPresent(),                     is(true ) );
      assertThat( objectUnderTest.getOptionalFilterNumSamples().isPresent(),               is(true ) );
      assertThat( objectUnderTest.getOptionalFilterNumSamplesInAverage().isPresent(),      is(true ) );
      assertThat( objectUnderTest.getOptionalFilterCycleLength().isPresent(),              is(true ) );
      assertThat( objectUnderTest.getOptionalFilterSamplingIntervalInMillis().isPresent(), is(true ) );
      assertThat( objectUnderTest.getOptionalFilterDeadband().isPresent(),                 is(true ) );

      assertThat( objectUnderTest.getOptionalDataAcquisitionMode().get(),                  is( WicaDataAcquisitionMode.MONITOR ) );
      assertThat( objectUnderTest.getOptionalPollingIntervalInMillis().get(),              is(11 ) );
      assertThat( objectUnderTest.getOptionalFieldsOfInterest().get(),                     is("fields" ));
      assertThat( objectUnderTest.getOptionalNumericPrecision().get(),                     is(12 ) );
      assertThat( objectUnderTest.getOptionalFilterType().get(),                           is( WicaFilterType.LAST_N) );
      assertThat( objectUnderTest.getOptionalFilterNumSamples().get(),                     is(13 ) );
      assertThat( objectUnderTest.getOptionalFilterNumSamplesInAverage().get(),            is(14 ) );
      assertThat( objectUnderTest.getOptionalFilterCycleLength().get(),                    is(15 ) );
      assertThat( objectUnderTest.getOptionalFilterSamplingIntervalInMillis().get(),       is(16 ) );
      assertThat( objectUnderTest.getOptionalFilterDeadband().get(),                       is(17.0 ) );
   }

   @Test
   void testConstructorWithNullValues()
   {
      final var objectUnderTest = new WicaChannelProperties( null, null, null, null, null, null, null, null, null, null );

      var ex01 = assertThrows( IllegalArgumentException.class, objectUnderTest::getDataAcquisitionMode );
      var ex02 = assertThrows( IllegalArgumentException.class, objectUnderTest::getPollingIntervalInMillis );
      var ex03 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFieldsOfInterest );
      var ex04 = assertThrows( IllegalArgumentException.class, objectUnderTest::getNumericPrecision );
      var ex05 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterType );
      var ex06 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterNumSamples );
      var ex07 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterNumSamplesInAverage );
      var ex08 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterCycleLength );
      var ex09 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterSamplingIntervalInMillis );
      var ex10 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterDeadband );

      assertThat( ex01.getMessage(), is("The data acquisition mode for this channel was not specified." ) );
      assertThat( ex02.getMessage(), is("The polling interval for this channel was not specified." ) );
      assertThat( ex03.getMessage(), is("The fields of interest for this channel were not specified." ) );
      assertThat( ex04.getMessage(), is("The numeric precision for this channel was not specified." ) );
      assertThat( ex05.getMessage(), is("The filter type for this channel was not specified." ) );
      assertThat( ex06.getMessage(), is("The number of samples for this channel's LAST_N filter was not specified." ) );
      assertThat( ex07.getMessage(), is("The number of samples for this channel's AVERAGING filter was not specified." ) );
      assertThat( ex08.getMessage(), is("The cycle length for this channel's ONE_IN_M filter was not specified." ) );
      assertThat( ex09.getMessage(), is("The sampling interval for this channel's RATE_LIMITER filter was not specified." ) );
      assertThat( ex10.getMessage(), is("The deadband for this channel's CHANGE_DETECTOR filter was not specified." ) );
   }

   @Test
   void testIsValueObject()
   {
      WicaChannelProperties props1 = WicaChannelPropertiesBuilder.create().withDefaultProperties().build();
      WicaChannelProperties props2 = WicaChannelPropertiesBuilder.create().withDefaultProperties().build();
      assertThat( props1, is( props2 ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
