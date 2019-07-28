/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamPropertiesTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testOf_UnrecognisedKey_ThrowsIllegalArgumentException()
   {
      final Exception ex = assertThrows( IllegalArgumentException.class, () -> WicaStreamProperties.of( "{" + "\"XXX\"" + ":" + 12345 + "}" ) );
      assertThat( ex.getMessage(), is("The input string: '{\"XXX\":12345}' was not a valid descriptor for the properties of a wica stream." ) );
   }

   @Test
   void testOf_InvalidValue_ThrowsIllegalArgumentException()
   {
      final Exception ex = assertThrows( IllegalArgumentException.class, () -> WicaStreamProperties.of("{" + "\"daqmode\"" + ":" + 99 + "}" ));
      assertThat( ex.getMessage(), is("The input string: '{\"daqmode\":99}' was not a valid descriptor for the properties of a wica stream." ) );
   }

   @Test
   void testOf_AllFieldsPresentDecodedOk()
   {
      final String inputString = "{" + "\"heartbeat\"" + ":" + 12345 + "," +
                                       "\"monflux\"" + ":" + 99 + "," +
                                       "\"pollflux\""   + ":" + 101 + "," +
                                       "\"daqmode\""   + ":" + "\"poll-and-monitor\"" + "," +
                                       "\"fields\""    + ":" + "\"abc;def\"" + "," +
                                       "\"prec\""      + ":" + 9 + "}";

      final WicaStreamProperties props = WicaStreamProperties.of( inputString );
      assertThat( props.getHeartbeatFluxIntervalInMillis(), is( 12345 ) );
      assertThat(props.getMonitoredValueFluxIntervalInMillis(), is(99 ) );
      assertThat( props.getPolledValueFluxIntervalInMillis(), is( 101 ) );
      assertThat( props.getDataAcquisitionMode(), is ( WicaChannelProperties.DataAcquisitionMode.POLL_AND_MONITOR) );
      assertThat( props.getNumericPrecision(), is( 9 ) );
      assertThat( props.getFieldsOfInterest(), is( Set.of( "abc", "def" ) ) );
   }

   @Test
   void testDefaultFieldValues()
   {
      final WicaStreamProperties props = WicaStreamProperties.of( "{}" );
      assertThat( props.getHeartbeatFluxIntervalInMillis(), is ( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS ) );
      assertThat(props.getMonitoredValueFluxIntervalInMillis(), is (WicaStreamProperties.DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS) );
      assertThat( props.getPolledValueFluxIntervalInMillis(), is ( WicaStreamProperties.DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS ) );

      assertThat( props.getDataAcquisitionMode(), is( WicaChannelProperties.DEFAULT_DATA_ACQUISITION_MODE ) );
      assertThat( props.getPolledValueSampleRatio(), is( WicaChannelProperties.DEFAULT_POLLING_INTERVAL) );
      assertThat( props.getFieldsOfInterest(), is( Set.of( WicaChannelProperties.DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) ) );
      assertThat( props.getNumericPrecision(), is( WicaChannelProperties.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( props.getFilterType(), is( WicaChannelProperties.DEFAULT_FILTER_TYPE ) );
      assertThat( props.getFilterNumSamples(), is( WicaChannelProperties.DEFAULT_FILTER_NUM_SAMPLES ) );
      assertThat( props.getFilterCycleLength(), is( WicaChannelProperties.DEFAULT_FILTER_CYCLE_LENGTH ) );
      assertThat( props.getFilterSamplingIntervalInMillis(), is( WicaChannelProperties.DEFAULT_FILTER_SAMPLING_INTERVAL ) );
      assertThat( props.getFilterDeadband(), is( WicaChannelProperties.DEFAULT_FILTER_DEADBAND ) );

      assertThat( props, is( WicaStreamProperties.createDefaultInstance() ) );
      assertThat( props, is( WicaStreamProperties.createBuilder().build() ) );

   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
