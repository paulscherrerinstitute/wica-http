/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.stream.WicaStreamPropertiesBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamPropertiesTest
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
      final var objectUnderTest = new WicaStreamProperties();

      assertThat( objectUnderTest.getHeartbeatFluxIntervalInMillis(),      is( WicaStreamPropertiesDefaults.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );
      assertThat( objectUnderTest.getMetadataFluxIntervalInMillis(),       is( WicaStreamPropertiesDefaults.DEFAULT_METADATA_FLUX_INTERVAL_IN_MILLIS ) );
      assertThat( objectUnderTest.getMonitoredValueFluxIntervalInMillis(), is( WicaStreamPropertiesDefaults.DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS ) );
      assertThat( objectUnderTest.getPolledValueFluxIntervalInMillis(),    is( WicaStreamPropertiesDefaults.DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS ) );
      assertThat( objectUnderTest.getDataAcquisitionMode(),                is( WicaStreamPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE ) );
      assertThat( objectUnderTest.getPollingIntervalInMillis(),            is( WicaStreamPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS) );
      assertThat( objectUnderTest.getFieldsOfInterest(),                   is( WicaStreamPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST ) );
      assertThat( objectUnderTest.getNumericPrecision(),                   is( WicaStreamPropertiesDefaults.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( objectUnderTest.getFilterType(),                         is( WicaStreamPropertiesDefaults.DEFAULT_FILTER_TYPE ) );
      assertThat( objectUnderTest.getFilterNumSamples(),                   is( WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES ) );
      assertThat( objectUnderTest.getFilterCycleLength(),                  is( WicaStreamPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH ) );
      assertThat( objectUnderTest.getFilterSamplingIntervalInMillis(),     is( WicaStreamPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS) );
      assertThat( objectUnderTest.getFilterDeadband(),                     is( WicaStreamPropertiesDefaults.DEFAULT_FILTER_DEADBAND ) );
   }

   @Test
   void testFullConstructorReturnsAssignedValues()
   {
      final var objectUnderTest = new WicaStreamProperties(20,
                                                           21,
                                                           22,
                                                           23,
                                                            WicaDataAcquisitionMode.MONITOR,
                                                           11,
                                                           "fields",
                                                           12,
                                                            WicaFilterType.LAST_N,
                                                           13,
                                                           14,
                                                           15,
                                                           16.0 );

      assertThat( objectUnderTest.getHeartbeatFluxIntervalInMillis(),      is(20 ) );
      assertThat( objectUnderTest.getMetadataFluxIntervalInMillis(),       is(21 ) );
      assertThat( objectUnderTest.getMonitoredValueFluxIntervalInMillis(), is(22 ) );
      assertThat( objectUnderTest.getPolledValueFluxIntervalInMillis(),    is(23 ) );
      assertThat( objectUnderTest.getDataAcquisitionMode(),                is( WicaDataAcquisitionMode.MONITOR ) );
      assertThat( objectUnderTest.getPollingIntervalInMillis(),            is(11 ) );
      assertThat( objectUnderTest.getFieldsOfInterest(),                   is("fields" ));
      assertThat( objectUnderTest.getNumericPrecision(),                   is(12 ) );
      assertThat( objectUnderTest.getFilterType(),                         is( WicaFilterType.LAST_N) );
      assertThat( objectUnderTest.getFilterNumSamples(),                   is(13 ) );
      assertThat( objectUnderTest.getFilterCycleLength(),                  is(14 ) );
      assertThat( objectUnderTest.getFilterSamplingIntervalInMillis(),     is(15 ) );
      assertThat( objectUnderTest.getFilterDeadband(),                     is(16.0 ) );

      assertThat( objectUnderTest.getOptionalHeartbeatFluxIntervalInMillis().isPresent(),      is(true ) );
      assertThat( objectUnderTest.getOptionalMetadataFluxIntervalInMillis().isPresent(),       is(true ) );
      assertThat( objectUnderTest.getOptionalMonitoredValueFluxIntervalInMillis().isPresent(), is(true ) );
      assertThat( objectUnderTest.getOptionalPolledValueFluxIntervalInMillis().isPresent(),    is(true ) );
      assertThat( objectUnderTest.getOptionalDataAcquisitionMode().isPresent(),                is(true ) );
      assertThat( objectUnderTest.getOptionalPollingIntervalInMillis().isPresent(),            is(true ) );
      assertThat( objectUnderTest.getOptionalFieldsOfInterest().isPresent(),                   is(true ) );
      assertThat( objectUnderTest.getOptionalNumericPrecision().isPresent(),                   is(true ) );
      assertThat( objectUnderTest.getOptionalFilterType().isPresent(),                         is(true ) );
      assertThat( objectUnderTest.getOptionalFilterNumSamples().isPresent(),                   is(true ) );
      assertThat( objectUnderTest.getOptionalFilterCycleLength().isPresent(),                  is(true ) );
      assertThat( objectUnderTest.getOptionalFilterSamplingIntervalInMillis().isPresent(),     is(true ) );
      assertThat( objectUnderTest.getOptionalFilterDeadband().isPresent(),                     is(true ) );

      assertThat( objectUnderTest.getOptionalHeartbeatFluxIntervalInMillis().get(),      is(20 ) );
      assertThat( objectUnderTest.getOptionalMetadataFluxIntervalInMillis().get(),       is(21 ) );
      assertThat( objectUnderTest.getOptionalMonitoredValueFluxIntervalInMillis().get(), is(22 ) );
      assertThat( objectUnderTest.getOptionalPolledValueFluxIntervalInMillis().get(),    is(23 ) );
      assertThat( objectUnderTest.getOptionalDataAcquisitionMode().get(),                is( WicaDataAcquisitionMode.MONITOR ) );
      assertThat( objectUnderTest.getOptionalPollingIntervalInMillis().get(),            is(11 ) );
      assertThat( objectUnderTest.getOptionalFieldsOfInterest().get(),                   is("fields" ));
      assertThat( objectUnderTest.getOptionalNumericPrecision().get(),                   is(12 ) );
      assertThat( objectUnderTest.getOptionalFilterType().get(),                         is( WicaFilterType.LAST_N) );
      assertThat( objectUnderTest.getOptionalFilterNumSamples().get(),                   is(13 ) );
      assertThat( objectUnderTest.getOptionalFilterCycleLength().get(),                  is(14 ) );
      assertThat( objectUnderTest.getOptionalFilterSamplingIntervalInMillis().get(),     is(15 ) );
      assertThat( objectUnderTest.getOptionalFilterDeadband().get(),                     is(16.0 ) );
   }

   @Test
   void testConstructorWithNullValues()
   {
      final var objectUnderTest = new WicaStreamProperties(null,null,
                                                           null,null,
                                                           null,null,null,
                                                           null,null,null,
                                                           null,null, null );

      var ex01 = assertThrows( IllegalArgumentException.class, objectUnderTest::getHeartbeatFluxIntervalInMillis );
      var ex02 = assertThrows( IllegalArgumentException.class, objectUnderTest::getMetadataFluxIntervalInMillis );
      var ex03 = assertThrows( IllegalArgumentException.class, objectUnderTest::getMonitoredValueFluxIntervalInMillis );
      var ex04 = assertThrows( IllegalArgumentException.class, objectUnderTest::getPolledValueFluxIntervalInMillis );
      var ex05 = assertThrows( IllegalArgumentException.class, objectUnderTest::getDataAcquisitionMode );
      var ex06 = assertThrows( IllegalArgumentException.class, objectUnderTest::getPollingIntervalInMillis );
      var ex07 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFieldsOfInterest );
      var ex08 = assertThrows( IllegalArgumentException.class, objectUnderTest::getNumericPrecision );
      var ex09 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterType );
      var ex10 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterNumSamples );
      var ex11 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterCycleLength );
      var ex12 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterSamplingIntervalInMillis );
      var ex13 = assertThrows( IllegalArgumentException.class, objectUnderTest::getFilterDeadband );

      assertThat( ex01.getMessage(), is("The heartbeat flux interval for this stream was not specified." ) );
      assertThat( ex02.getMessage(), is("The metadata flux interval for this stream was not specified." ) );
      assertThat( ex03.getMessage(), is("The monitored value flux interval for this stream was not specified." ) );
      assertThat( ex04.getMessage(), is("The polled value flux interval for this stream was not specified." ) );
      assertThat( ex05.getMessage(), is("The data acquisition mode for this stream was not specified." ) );
      assertThat( ex06.getMessage(), is("The polling interval for this stream was not specified." ) );
      assertThat( ex07.getMessage(), is("The fields of interest for this stream were not specified." ) );
      assertThat( ex08.getMessage(), is("The numeric precision for this stream was not specified." ) );
      assertThat( ex09.getMessage(), is("The filter type for this stream was not specified." ) );
      assertThat( ex10.getMessage(), is("The number of samples for this stream's LAST_N filter was not specified." ) );
      assertThat( ex11.getMessage(), is("The cycle length for this stream's ONE_IN_M filter was not specified." ) );
      assertThat( ex12.getMessage(), is("The sampling interval for this stream's RATE_LIMITER filter was not specified." ) );
      assertThat( ex13.getMessage(), is("The deadband for this stream's CHANGE_FILTERER was not specified." ) );
   }

   @Test
   void testIsValueObject()
   {
      WicaStreamProperties props1 = WicaStreamPropertiesBuilder.create().withDefaultProperties().build();
      WicaStreamProperties props2 = WicaStreamPropertiesBuilder.create().withDefaultProperties().build();
      assertThat( props1, is( props2 ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
