/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


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
   void testDefaultFieldValues()
   {
      final WicaChannelProperties props = WicaChannelProperties.of("{}" );
      assertThat( props.getDataAcquisitionMode(), is( WicaChannelProperties.DEFAULT_DATA_ACQUISITION_MODE ) );
      assertThat( props.getPolledValueSampleRatio(), is( WicaChannelProperties.DEFAULT_POLLED_VALUE_SAMPLE_RATIO ) );
      assertThat( props.getFieldsOfInterest(), is( Set.of( WicaChannelProperties.DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) ) );
      assertThat( props.getNumericPrecision(), is( WicaChannelProperties.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( props.getFilterType(), is( WicaChannelProperties.DEFAULT_FILTER_TYPE ) );
      assertThat( props.getFilterNumSamples(), is( WicaChannelProperties.DEFAULT_FILTER_NUM_SAMPLES ) );
      assertThat( props.getFilterCycleLength(), is( WicaChannelProperties.DEFAULT_FILTER_CYCLE_LENGTH ) );
      assertThat( props.getFilterSamplingIntervalInMillis(), is( WicaChannelProperties.DEFAULT_FILTER_SAMPLING_INTERVAL ) );
      assertThat( props.getFilterDeadband(), is( WicaChannelProperties.DEFAULT_FILTER_DEADBAND ) );
      assertThat( props, is( WicaChannelProperties.createDefaultInstance() ) );
   }

   @Test
   void testOf_EmptyStringSpecifier_buildsExpectedObject()
   {
      final WicaChannelProperties props = WicaChannelProperties.of( "{}" );
      assertThat( props.getDataAcquisitionMode(), is( WicaChannelProperties.DEFAULT_DATA_ACQUISITION_MODE ) );
      assertThat( props.getPolledValueSampleRatio(), is( WicaChannelProperties.DEFAULT_POLLED_VALUE_SAMPLE_RATIO ) );
      assertThat( props.getFieldsOfInterest(), is( Set.of( WicaChannelProperties.DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) ) );
      assertThat( props.getNumericPrecision(), is( WicaChannelProperties.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( props.getFilterType(), is( WicaChannelProperties.DEFAULT_FILTER_TYPE ) );
      assertThat( props.getFilterNumSamples(), is( WicaChannelProperties.DEFAULT_FILTER_NUM_SAMPLES ) );
      assertThat( props.getFilterCycleLength(), is( WicaChannelProperties.DEFAULT_FILTER_CYCLE_LENGTH ) );
      assertThat( props.getFilterSamplingIntervalInMillis(), is( WicaChannelProperties.DEFAULT_FILTER_SAMPLING_INTERVAL ) );
      assertThat( props.getFilterDeadband(), is( WicaChannelProperties.DEFAULT_FILTER_DEADBAND ) );
   }

   @Test
   void testOf_AllFieldsPresent_buildsExpectedObject()
   {
      final String inputString = "{" + "\"daqmode\""   + ":" + "\"poll\"" + "," +
                                       "\"pollratio\"" + ":" + 33 + "," +
                                       "\"fields\""    + ":" + "\"abc;def\"" + "," +
                                       "\"prec\""      + ":" + 9 + "," +
                                       "\"filter\""    + ":" + "\"last-n\"" + "," +
                                       "\"n\""         + ":" + 5 + "," +
                                       "\"m\""         + ":" + 77 + "," +
                                       "\"interval\""  + ":" + 4321 + "," +
                                       "\"deadband\""  + ":" + 15.8 +
                                 "}";

      final WicaChannelProperties props = WicaChannelProperties.of( inputString );
      assertThat( props.getDataAcquisitionMode(), is( WicaChannelProperties.DataAcquisitionMode.POLL) );
      assertThat( props.getPolledValueSampleRatio(), is( 33 ) );
      assertThat( props.getFieldsOfInterest(), is( Set.of( "abc", "def") ) );
      assertThat( props.getNumericPrecision(), is( 9 ) );
      assertThat( props.getFilterType(), is( WicaChannelProperties.FilterType.LAST_N) );
      assertThat( props.getFilterNumSamples(), is(5 ) );
      assertThat( props.getFilterCycleLength(), is( 77 ) );
      assertThat( props.getFilterSamplingIntervalInMillis(), is( 4321 ) );
      assertThat( props.getFilterDeadband(), is(15.8 ) );
   }

   @Test
   void testOf_withPartialStringSpecifier_buildsExpectedObject()
   {
      final String inputString = "{" + "\"daqmode\""   + ":" + "\"poll\"" + "," + "\"n\"" + ":" + 14 + "}";
      final WicaChannelProperties props = WicaChannelProperties.of( inputString );
      assertThat( props.getDataAcquisitionMode(), is(WicaChannelProperties.DataAcquisitionMode.POLL) );
      assertThat( props.getPolledValueSampleRatio(), is( WicaChannelProperties.DEFAULT_POLLED_VALUE_SAMPLE_RATIO ) );
      assertThat( props.getFieldsOfInterest(), is( Set.of( WicaChannelProperties.DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) ) );
      assertThat( props.getNumericPrecision(), is( WicaChannelProperties.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( props.getFilterType(), is( WicaChannelProperties.DEFAULT_FILTER_TYPE ) );
      assertThat( props.getFilterNumSamples(), is( 14 ) );
      assertThat( props.getFilterCycleLength(), is( WicaChannelProperties.DEFAULT_FILTER_CYCLE_LENGTH ) );
      assertThat( props.getFilterSamplingIntervalInMillis(), is( WicaChannelProperties.DEFAULT_FILTER_SAMPLING_INTERVAL ) );
      assertThat( props.getFilterDeadband(), is( WicaChannelProperties.DEFAULT_FILTER_DEADBAND ) );
   }

   @Test
   void testBuilder()
   {
      final WicaChannelProperties props = WicaChannelProperties.createBuilder()
            .withDataAcquisitionMode(WicaChannelProperties.DataAcquisitionMode.POLL)
            .withPolledValueSamplingRatio( 33 )
            .withFieldsOfInterest( "abc;def" )
            .withNumericPrecision( 9 )
            .withFilterType(WicaChannelProperties.FilterType.LAST_N )
            .withNumSamples( 5 )
            .withFilterCycleLength( 77)
            .withFilterSamplingInterval( 4321 )
            .withFilterDeadband( 15.8 )
            .build();

      assertThat( props.getDataAcquisitionMode(), is(WicaChannelProperties.DataAcquisitionMode.POLL) );
      assertThat( props.getPolledValueSampleRatio(), is( 33 ) );
      assertThat( props.getFieldsOfInterest(), is( Set.of( "abc", "def") ) );
      assertThat( props.getNumericPrecision(), is( 9 ) );
      assertThat( props.getFilterType(), is( WicaChannelProperties.FilterType.LAST_N) );
      assertThat( props.getFilterNumSamples(), is(5 ) );
      assertThat( props.getFilterCycleLength(), is( 77 ) );
      assertThat( props.getFilterSamplingIntervalInMillis(), is( 4321 ) );
      assertThat( props.getFilterDeadband(), is(15.8 ) );
   }

   @Test
   void testOf_InvalidValueRealInsteadOfInteger_GetsTruncatedToInteger()
   {
      final WicaChannelProperties props = WicaChannelProperties.of( "{" + "\"pollratio\"" + ":" + 99.99 + "}" );
      assertThat( props.getPolledValueSampleRatio(), is( 99 ) );
   }

   @Test
   void testOf_UnrecognisedKey_ThrowsIllegalArgumentException()
   {
      final Exception ex = assertThrows( IllegalArgumentException.class, () -> WicaChannelProperties.of("{" + "\"XXX\"" + ":" + 12345 + "}" ) );
      assertThat( ex.getMessage(), is("The input string: '{\"XXX\":12345}' was not a valid descriptor for the properties of a wica channel." ) );
   }

   @Test
   void testOf_InvalidValue_ThrowsIllegalArgumentException()
   {
      final Exception ex = assertThrows( IllegalArgumentException.class, () -> WicaChannelProperties.of("{" + "\"daqmode\"" + ":" + 99 + "}" ) );
      assertThat( ex.getMessage(), is("The input string: '{\"daqmode\":99}' was not a valid descriptor for the properties of a wica channel." ) );
   }

   @Test
   void testOf_InvalidValue_NaN_ThrowsIllegalArgumentException()
   {
      final Exception ex = assertThrows( IllegalArgumentException.class, () -> WicaChannelProperties.of("{" + "\"deadband\"" + ":" + "NaN" + "}" ) );
      assertThat( ex.getMessage(), is("The input string: '{\"deadband\":NaN}' was not a valid descriptor for the properties of a wica channel." ) );
   }

   @Test
   void testOf_InvalidValue_Inf_ThrowsIllegalArgumentException()
   {
      final Exception ex = assertThrows( IllegalArgumentException.class, () -> WicaChannelProperties.of("{" + "\"deadband\"" + ":" + "Inf" + "}" ) );
      assertThat( ex.getMessage(), is("The input string: '{\"deadband\":Inf}' was not a valid descriptor for the properties of a wica channel." ) );
   }

   @Test
   void testOf_InvalidValue_StringInNumberField_ThrowsIllegalArgumentException()
   {
      final Exception ex = assertThrows( IllegalArgumentException.class, () -> WicaChannelProperties.of("{" + "\"pollratio\"" + ":" + "IllegalString" + "}" ) );
      assertThat( ex.getMessage(), is("The input string: '{\"pollratio\":IllegalString}' was not a valid descriptor for the properties of a wica channel." ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
