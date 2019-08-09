/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamPropertiesBuilderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testCreateAndBuild_ReturnsEmptyPropertiesObject()
   {
      final WicaStreamPropertiesBuilder builder = WicaStreamPropertiesBuilder.create();
      final WicaStreamProperties props = builder.build();

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().isPresent(),      is( false) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().isPresent(),       is( false) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().isPresent(), is( false) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isPresent(),    is( false) );
      assertThat( props.getOptionalDataAcquisitionMode().isPresent(),                is( false) );
      assertThat( props.getOptionalPollingIntervalInMillis().isPresent(),            is( false) );
      assertThat( props.getOptionalFieldsOfInterest().isPresent(),                   is( false) );
      assertThat( props.getOptionalNumericPrecision().isPresent(),                   is( false) );
      assertThat( props.getOptionalFilterType().isPresent(),                         is( false) );
      assertThat( props.getOptionalFilterNumSamples().isPresent(),                   is( false) );
      assertThat( props.getOptionalFilterCycleLength().isPresent(),                  is( false) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().isPresent(),     is( false) );
      assertThat( props.getOptionalFilterDeadband().isPresent(),                     is( false) );
   }

   @Test
   void testWithDefaultProperties_ReturnsDefaultPropertiesObject()
   {
      final WicaStreamPropertiesBuilder builder = WicaStreamPropertiesBuilder.create();
      final WicaStreamProperties props = builder.withDefaultProperties().build();
      assertThat( props, is( new WicaStreamProperties()) );
   }

   @Test
   void testWithXXX_AssignsFieldsAsExpected()
   {
      final WicaStreamProperties props = WicaStreamPropertiesBuilder.create()
         .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL )
         .withFilterType(WicaFilterType.RATE_LIMITER )
         .withFieldsOfInterest( "abc;def" )
         .withHeartbeatFluxInterval( 1 )
         .withMetadataFluxInterval( 2 )
         .withMonitoredValueFluxInterval( 3 )
         .withPolledValueFluxInterval( 4 )
         .withFilterCycleLength( 5 )
         .withFilterDeadband( 6.0 )
         .withFilterNumSamples( 7 )
         .withFilterSamplingIntervalInMillis( 8 )
         .withPollingIntervalInMillis( 9 )
         .withNumericPrecision( 10 )
         .build();

      assertThat( props.getDataAcquisitionMode(), is( WicaDataAcquisitionMode.POLL) );
      assertThat( props.getFilterType(), is( WicaFilterType.RATE_LIMITER) );
      assertThat( props.getFieldsOfInterest(), is( "abc;def" ) );
      assertThat( props.getHeartbeatFluxIntervalInMillis(), is(1 ) );
      assertThat( props.getMetadataFluxIntervalInMillis(), is(2 ) );
      assertThat( props.getMonitoredValueFluxIntervalInMillis(), is(3 ) );
      assertThat( props.getPolledValueFluxIntervalInMillis(), is(4 ) );
      assertThat( props.getFilterCycleLength(), is(5 ) );
      assertThat( props.getFilterDeadband(), is(6.0 ) );
      assertThat( props.getFilterNumSamples(), is(7 ) );
      assertThat( props.getFilterSamplingIntervalInMillis(), is(8 ) );
      assertThat( props.getPollingIntervalInMillis(), is(9 ) );
      assertThat( props.getNumericPrecision(), is(10 ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
