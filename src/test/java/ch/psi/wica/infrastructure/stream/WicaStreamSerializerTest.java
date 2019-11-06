/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /****************************************************************************************
    * 1.0 WicaStreamProperties - Serialization Tests
    ****************************************************************************************/
   
   @Test
   void testSerializeWicaStreamProperties_DefaultProperties_ProducesEmptyObject()
   {
      final WicaStreamProperties inputProps = WicaStreamPropertiesBuilder
            .create()
            .withDefaultProperties()
            .build();

      final String resultStr = WicaStreamSerializer.writeToJson( inputProps);
      assertThat( resultStr, is( "{}" ) );
   }

   @Test
   void testSerializeWicaStreamProperties_EmptyProperties_ProducesNullValueEntriesForAllFields()
   {
      final WicaStreamProperties inputProps = WicaStreamPropertiesBuilder.create().build();
      final String resultStr = WicaStreamSerializer.writeToJson( inputProps );
      assertThat( resultStr, is( "{\"hbflux\":null,\"metaflux\":null,\"monflux\":null,\"pollflux\":null,\"daqmode\":null,\"pollint\":null,\"fields\":null,\"prec\":null,\"filter\":null,\"n\":null,\"m\":null,\"interval\":null,\"deadband\":null}" ) );
   }
   @Test
   void testSerializeWicaStreamProperties_SelectedProperties1_ProducesExpectedValues()
   {
      final WicaStreamProperties inputProps = WicaStreamPropertiesBuilder
            .create()
            .withHeartbeatFluxInterval( 22 )
            .withNumericPrecision( 65 )
            .withFilterDeadband( 14.3 )
            .build();
      final String resultStr = WicaStreamSerializer.writeToJson( inputProps );
      assertThat( resultStr, is( "{\"hbflux\":22,\"metaflux\":null,\"monflux\":null,\"pollflux\":null,\"daqmode\":null,\"pollint\":null,\"fields\":null,\"prec\":65,\"filter\":null,\"n\":null,\"m\":null,\"interval\":null,\"deadband\":14.3}" ) );
   }

   @Test
   void testSerializeWicaStreamProperties_SelectedProperties2_ProducesExpectedValues()
   {
      final WicaStreamProperties inputProps = WicaStreamPropertiesBuilder
              .create()
              .withHeartbeatFluxInterval( 22 )
              .withNumericPrecision( 65 )
              .withFilterDeadband( 14.3 )
              .withFilterType( WicaFilterType.AVERAGER )
              .build();
      final String resultStr = WicaStreamSerializer.writeToJson( inputProps );
      assertThat( resultStr, is( "{\"hbflux\":22,\"metaflux\":null,\"monflux\":null,\"pollflux\":null,\"daqmode\":null,\"pollint\":null,\"fields\":null,\"prec\":65,\"filter\":\"averager\",\"n\":null,\"m\":null,\"interval\":null,\"deadband\":14.3}" ) );
   }

   /****************************************************************************************
    * 2.0 WicaStreamProperties - Deserialization Tests
    ****************************************************************************************/

   @Test
   void testDeserializeWicaStreamProperties_NullFieldValues_ProducesEmptyObject()
   {
      final String inputString = "{\"hbflux\":null,\"metaflux\":null,\"monflux\":null,\"pollflux\":null,\"daqmode\":null,\"pollint\":null,\"fields\":null,\"prec\":null,\"filter\":null,\"n\":null,\"m\":null,\"interval\":null,\"deadband\":null}";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().isEmpty(),      is( true ) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().isEmpty(),       is( true ) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().isEmpty(), is( true ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isEmpty(),    is( true ) );
      assertThat( props.getOptionalDataAcquisitionMode().isEmpty(),                is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isEmpty(),            is( true ) );
      assertThat( props.getOptionalFieldsOfInterest().isEmpty(),                   is( true ) );
      assertThat( props.getOptionalNumericPrecision().isEmpty(),                   is( true ) );
      assertThat( props.getOptionalFilterType().isEmpty(),                         is( true ) );
      assertThat( props.getOptionalFilterNumSamples().isEmpty(),                   is( true ) );
      assertThat( props.getOptionalFilterCycleLength().isEmpty(),                  is( true ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().isEmpty(),     is( true ) );
      assertThat( props.getOptionalFilterDeadband().isEmpty(),                     is( true ) );
   }

   @Test
   void testDeserializeWicaStreamProperties_EmptyInputString_ProducesEmptyObject()
   {
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( "{}", WicaStreamProperties.class );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().isEmpty(),      is( true ) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().isEmpty(),       is( true ) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().isEmpty(), is( true ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isEmpty(),    is( true ) );
      assertThat( props.getOptionalDataAcquisitionMode().isEmpty(),                is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isEmpty(),            is( true ) );
      assertThat( props.getOptionalFieldsOfInterest().isEmpty(),                   is( true ) );
      assertThat( props.getOptionalNumericPrecision().isEmpty(),                   is( true ) );
      assertThat( props.getOptionalFilterType().isEmpty(),                         is( true ) );
      assertThat( props.getOptionalFilterNumSamples().isEmpty(),                   is( true ) );
      assertThat( props.getOptionalFilterCycleLength().isEmpty(),                  is( true ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().isEmpty(),     is( true ) );
      assertThat( props.getOptionalFilterDeadband().isEmpty(),                     is( true ) );
   }

   @Test
   void testDeserializeWicaStreamProperties_ConfiguredFieldValues1_ProducesExpectedObject()
   {
      final String inputString = "{\"hbflux\":1,\"metaflux\":2,\"monflux\":3,\"pollflux\":4,\"daqmode\":\"poll\",\"pollint\":5,\"fields\":\"abc\",\"prec\":6,\"filter\":\"last-n\",\"n\":7,\"m\":8,\"interval\":9,\"deadband\":10.0}";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().isPresent(),      is( true ) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().isPresent(),       is( true ) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().isPresent(), is( true ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isPresent(),    is( true ) );
      assertThat( props.getOptionalDataAcquisitionMode().isPresent(),                is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isPresent(),            is( true ) );
      assertThat( props.getOptionalFieldsOfInterest().isPresent(),                   is( true ) );
      assertThat( props.getOptionalNumericPrecision().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterType().isPresent(),                         is( true ) );
      assertThat( props.getOptionalFilterNumSamples().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterCycleLength().isPresent(),                  is( true ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().isPresent(),     is( true ) );
      assertThat( props.getOptionalFilterDeadband().isPresent(),                     is( true ) );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().get(),            is( 1 ) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().get(),             is( 2 ) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().get(),       is( 3 ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().get(),          is( 4 ) );
      assertThat( props.getOptionalDataAcquisitionMode().get(),                      is( WicaDataAcquisitionMode.POLL ) );
      assertThat( props.getOptionalPollingIntervalInMillis().get(),                  is( 5 ) );
      assertThat( props.getOptionalFieldsOfInterest().get(),                         is( "abc" ) );
      assertThat( props.getOptionalNumericPrecision().get(),                         is( 6 ) );
      assertThat( props.getOptionalFilterType().get(),                               is( WicaFilterType.LAST_N ) );
      assertThat( props.getOptionalFilterNumSamples().get(),                         is( 7 ) );
      assertThat( props.getOptionalFilterCycleLength().get(),                        is( 8 ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().get(),           is( 9 ) );
      assertThat( props.getOptionalFilterDeadband().get(),                           is( 10.0 ) );
   }

   @Test
   void testDeserializeWicaStreamProperties_ConfiguredFieldValues2_ProducesExpectedObject()
   {
      final String inputString = "{\"hbflux\":1,\"metaflux\":2,\"monflux\":3,\"pollflux\":4,\"daqmode\":\"poll\",\"pollint\":5,\"fields\":\"abc\",\"prec\":6,\"filter\":\"averager\",\"n\":7,\"m\":8,\"interval\":9,\"deadband\":10.0}";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().isPresent(),      is( true ) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().isPresent(),       is( true ) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().isPresent(), is( true ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isPresent(),    is( true ) );
      assertThat( props.getOptionalDataAcquisitionMode().isPresent(),                is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isPresent(),            is( true ) );
      assertThat( props.getOptionalFieldsOfInterest().isPresent(),                   is( true ) );
      assertThat( props.getOptionalNumericPrecision().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterType().isPresent(),                         is( true ) );
      assertThat( props.getOptionalFilterNumSamples().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterCycleLength().isPresent(),                  is( true ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().isPresent(),     is( true ) );
      assertThat( props.getOptionalFilterDeadband().isPresent(),                     is( true ) );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().get(),            is( 1 ) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().get(),             is( 2 ) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().get(),       is( 3 ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().get(),          is( 4 ) );
      assertThat( props.getOptionalDataAcquisitionMode().get(),                      is( WicaDataAcquisitionMode.POLL ) );
      assertThat( props.getOptionalPollingIntervalInMillis().get(),                  is( 5 ) );
      assertThat( props.getOptionalFieldsOfInterest().get(),                         is( "abc" ) );
      assertThat( props.getOptionalNumericPrecision().get(),                         is( 6 ) );
      assertThat( props.getOptionalFilterType().get(),                               is( WicaFilterType.AVERAGER ) );
      assertThat( props.getOptionalFilterNumSamples().get(),                         is( 7 ) );
      assertThat( props.getOptionalFilterCycleLength().get(),                        is( 8 ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().get(),           is( 9 ) );
      assertThat( props.getOptionalFilterDeadband().get(),                           is( 10.0 ) );
   }

   @Test
   void testDeserializeWicaStreamProperties_SparselyConfiguredFieldValues_ProducesExpectedObject()
   {
      final String inputString = "{\"hbflux\":\"15\",\"daqmode\":\"poll\",\"pollint\":5,\"fields\":\"abc\",\"prec\":6,\"filter\":\"last-n\",\"n\":7,\"m\":8,\"interval\":9,\"deadband\":10.0}";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().isPresent(),      is( true ) );
      assertThat( props.getOptionalMetadataFluxIntervalInMillis().isPresent(),       is( false ) );
      assertThat( props.getOptionalMonitoredValueFluxIntervalInMillis().isPresent(), is( false ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isPresent(),    is( false ) );

      assertThat( props.getOptionalDataAcquisitionMode().isPresent(),                is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isPresent(),            is( true ) );
      assertThat( props.getOptionalFieldsOfInterest().isPresent(),                   is( true ) );
      assertThat( props.getOptionalNumericPrecision().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterType().isPresent(),                         is( true ) );
      assertThat( props.getOptionalFilterNumSamples().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterCycleLength().isPresent(),                  is( true ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().isPresent(),     is( true ) );
      assertThat( props.getOptionalFilterDeadband().isPresent(),                     is( true ) );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().get(),            is( 15 ) );
      assertThat( props.getOptionalDataAcquisitionMode().get(),                      is( WicaDataAcquisitionMode.POLL ) );
      assertThat( props.getOptionalPollingIntervalInMillis().get(),                  is( 5 ) );
      assertThat( props.getOptionalFieldsOfInterest().get(),                         is( "abc" ) );
      assertThat( props.getOptionalNumericPrecision().get(),                         is( 6 ) );
      assertThat( props.getOptionalFilterType().get(),                               is( WicaFilterType.LAST_N ) );
      assertThat( props.getOptionalFilterNumSamples().get(),                         is( 7 ) );
      assertThat( props.getOptionalFilterCycleLength().get(),                        is( 8 ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().get(),           is( 9 ) );
      assertThat( props.getOptionalFilterDeadband().get(),                           is( 10.0 ) );
   }

   @Test
   void testDeserializeWicaStreamProperties_DeprecatedInterfaceTest_HeartBeatAliasWorks()
   {
      final String inputString = "{\"heartbeat\":\"15\"}";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().isPresent(), is( true ) );
      assertThat( props.getOptionalHeartbeatFluxIntervalInMillis().get(),       is( 15 ) );
   }

   @Test
   void testDeserializeWicaStreamProperties_DeprecatedInterfaceTest_PollRatioIsIgnoredWhenPollingIntervalPresent()
   {
      final String inputString = "{\"pollflux\":\"15\",\"pollratio\":4 }";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isPresent(),    is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isPresent(),            is( true ) );
      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().get(),          is( 15 ) );
      assertThat( props.getOptionalPollingIntervalInMillis().get(),                  is( 60 ) );
   }

   @Test
   void testDeserializeWicaStreamProperties_DeprecatedInterfaceTest_PollRatioIsUsedWhenPollingIntervalAbsent()
   {
      final String inputString = "{\"pollflux\":\"15\",\"pollint\":5,\"pollratio\":4 }";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().isPresent(),    is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isPresent(),            is( true ) );

      assertThat( props.getOptionalPolledValueFluxIntervalInMillis().get(),          is( 15 ) );
      assertThat( props.getOptionalPollingIntervalInMillis().get(),                  is( 5 ) );
   }


   /****************************************************************************************
    * 3.0 WicaChannelProperties - Serialization Tests
    ****************************************************************************************/

   @Test
   void testSerializeWicaChannelProperties_DefaultProperties_ProducesEmptyObject()
   {
      final WicaChannelProperties inputProps = WicaChannelPropertiesBuilder.create().withDefaultProperties().build();
      final String resultStr = WicaStreamSerializer.writeToJson( inputProps );
      assertThat( resultStr, is( "{}" ) );
   }

   @Test
   void testSerializeWicaChannelProperties_EmptyProperties_ProducesNullValueEntriesForAllFields()
   {
      final WicaChannelProperties inputProps = WicaChannelPropertiesBuilder.create().build();
      final String resultStr = WicaStreamSerializer.writeToJson( inputProps );
      assertThat( resultStr, is( "{\"daqmode\":null,\"pollint\":null,\"fields\":null,\"prec\":null,\"filter\":null,\"n\":null,\"m\":null,\"interval\":null,\"deadband\":null}" ) );
   }

   @Test
   void testSerializeWicaChannelProperties_SelectedProperties_ProducesExpectedValues()
   {
      final WicaChannelProperties inputProps = WicaChannelPropertiesBuilder
            .create()
            .withDefaultProperties()
            .withNumericPrecision( 65 )
            .withFilterDeadband( 14.3 )
            .build();
      final String resultStr = WicaStreamSerializer.writeToJson( inputProps );
      assertThat( resultStr, is( "{\"prec\":65,\"deadband\":14.3}" ) );
   }

   /****************************************************************************************
    * 4.0 WicaChannelProperties - Deserialization Tests
    ****************************************************************************************/

   @Test
   void testDeserializeWicaChannelProperties_SparselyConfiguredFieldValues_ProducesExpectedObject()
   {
      final String inputString = "{\"daqmode\":\"poll\",\"pollint\":5,\"fields\":\"abc\",\"prec\":6,\"filter\":\"last-n\",\"n\":7,\"m\":8,\"interval\":9,\"deadband\":10.0}";
      final WicaStreamProperties props = WicaStreamSerializer.readFromJson( inputString, WicaStreamProperties.class );

      assertThat( props.getOptionalDataAcquisitionMode().isPresent(),                is( true ) );
      assertThat( props.getOptionalPollingIntervalInMillis().isPresent(),            is( true ) );
      assertThat( props.getOptionalFieldsOfInterest().isPresent(),                   is( true ) );
      assertThat( props.getOptionalNumericPrecision().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterType().isPresent(),                         is( true ) );
      assertThat( props.getOptionalFilterNumSamples().isPresent(),                   is( true ) );
      assertThat( props.getOptionalFilterCycleLength().isPresent(),                  is( true ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().isPresent(),     is( true ) );
      assertThat( props.getOptionalFilterDeadband().isPresent(),                     is( true ) );

      assertThat( props.getOptionalDataAcquisitionMode().get(),                      is( WicaDataAcquisitionMode.POLL ) );
      assertThat( props.getOptionalPollingIntervalInMillis().get(),                  is( 5 ) );
      assertThat( props.getOptionalFieldsOfInterest().get(),                         is( "abc" ) );
      assertThat( props.getOptionalNumericPrecision().get(),                         is( 6 ) );
      assertThat( props.getOptionalFilterType().get(),                               is( WicaFilterType.LAST_N ) );
      assertThat( props.getOptionalFilterNumSamples().get(),                         is( 7 ) );
      assertThat( props.getOptionalFilterCycleLength().get(),                        is( 8 ) );
      assertThat( props.getOptionalFilterSamplingIntervalInMillis().get(),           is( 9 ) );
      assertThat( props.getOptionalFilterDeadband().get(),                           is( 10.0 ) );
   }

   /****************************************************************************************
    * 5.0 WicaStream - Serialization Tests
    ****************************************************************************************/

   @Test
   void testSerializeWicaStream_withDefaultStreamProperties_and_withDefaultChannelProperties_ProducesExpectedObject()
   {
      final WicaStream testStream = WicaStreamBuilder
            .create()
            .withDefaultStreamProperties()
            .withChannelNameAndDefaultProperties( "CHAN-X" )
            .build();

      final String resultStr = WicaStreamSerializer.writeToJson( testStream );
      assertThat( resultStr, is( "{\"channels\":[{\"name\":\"CHAN-X\"}]}" ) );
   }

   @Test
   void testSerializeWicaStream_withComplexProperties_ProducesExpectedObject()
   {
      final WicaStream testStream = WicaStreamBuilder
            .create()
            .withStreamProperties( WicaStreamPropertiesBuilder.create().withDefaultProperties().withPolledValueFluxInterval( 16 ).withDataAcquisitionMode( WicaDataAcquisitionMode.POLL ).build() )
            .withChannelNameAndCombinedProperties( "CHAN-X", WicaChannelPropertiesBuilder.create().withDefaultProperties().withNumericPrecision( 4 ).build() )
            .withChannelNameAndCombinedProperties( "CHAN-Y", WicaChannelPropertiesBuilder.create().withNumericPrecision( 4 ).build() )
            .build();

      final String resultStr = WicaStreamSerializer.writeToJson( testStream );
      assertThat( resultStr, containsString( "{\"props\":{\"pollflux\":16,\"daqmode\":\"poll\"},\"channels\"" ) );
      assertThat( resultStr, containsString( "{\"name\":\"CHAN-X\",\"props\":{\"prec\":4}}" ) );
      assertThat( resultStr, containsString( "{\"name\":\"CHAN-Y\",\"props\":{\"daqmode\":\"poll\",\"prec\":4}}" ) );
   }

   /****************************************************************************************
    * 6.0 WicaStream - Deserialization Tests
    ****************************************************************************************/

   @Test
   void testDeserializeWicaStream_Simple_Example()
   {
      final String inputString = "{\"props\":{\"pollflux\":251,\"daqmode\":\"poll\"},\"channels\":[{\"name\":\"CHAN-Y\",\"props\":{\"daqmode\":\"poll\",\"prec\":4}},{\"name\":\"CHAN-X\",\"props\":{\"prec\":4}}]}";
      final WicaStream stream = WicaStreamSerializer.readFromJson( inputString, WicaStream.class );
      assertThat( stream.getWicaStreamProperties().getPolledValueFluxIntervalInMillis(), is( 251 ) );
   }

   @Test
   void testDeserializeWicaStream_HIPA_Example()
   {
      final String inputString = "   {\"channels\":[{\"name\":\"XHIPA:TIME\"},{\"name\":\"EVEX:STR:2\"},{\"name\":\"EWBRI:IST:2\"},{\"name\":\"MXC1:IST:2\"},{\"name\":\"MYC2:IST:2\"},{\"name\":\"MHC4:IST:2\"},{\"name\":\"MHC6:IST:2\"},{\"name\":\"MHC1:IST:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollratio\":30,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":10}},{\"name\":\"MYC2:IST:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollratio\":30,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":10}},{\"name\":\"MBC1:IST:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollratio\":30,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":10}},{\"name\":\"MRI12:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollratio\":30,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":0.3}},{\"name\":\"MII7:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollratio\":30,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":0.3}},{\"name\":\"MRI13:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollratio\":30,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":8}},{\"name\":\"MRI14:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollratio\":30,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":8}},{\"name\":\"CIPHMO:SOL:1\"},{\"name\":\"CRPHFT:SOL:1\"},{\"name\":\"MXF1:IST:2\"},{\"name\":\"MRFEIN:IST:2\"},{\"name\":\"MRFAUS:IST:2\"},{\"name\":\"MII7:ILOG:2\"},{\"name\":\"MRI2:ILOG:2\"},{\"name\":\"MRI13:ILOG:2\"},{\"name\":\"MRI14:ILOG:2\"},{\"name\":\"CI1V:IST:2\"},{\"name\":\"CI3V:IST:2\"},{\"name\":\"CI2V:IST:2\"},{\"name\":\"CI4V:IST:2\"},{\"name\":\"CR1V:IST:2\"},{\"name\":\"CR2V:IST:2\"},{\"name\":\"CR3V:IST:2\"},{\"name\":\"CR4V:IST:2\"},{\"name\":\"CR5V:IST:2\"},{\"name\":\"UCNQ:BEAMREQ:STATUS\"},{\"name\":\"UCNQ:BEAMREQ:COUNTDOWN\"},{\"name\":\"EICV:IST:2\"},{\"name\":\"EICI:IST:2\"},{\"name\":\"EECV:IST:2\"},{\"name\":\"EECI:IST:2\"},{\"name\":\"CIREV:FIST:2\"},{\"name\":\"CRREV:FIST:2\"},{\"name\":\"AIHS:IST:2\"},{\"name\":\"HS:IST:2\"},{\"name\":\"M3ALT:IST:2\"},{\"name\":\"GLS:LEISTUNG_AKTUELL\"},{\"name\":\"ZSLP:TOTSAVEFAST\"}],\"props\":{\"heartbeat\":15000,\"monflux\":100,\"pollflux\":1000,\"daqmode\":\"monitor\",\"pollratio\":1,\"prec\":6,\"fields\":\"val;sevr\"}}";
      final WicaStream stream = WicaStreamSerializer.readFromJson( inputString, WicaStream.class );
      assertThat( stream.getWicaStreamProperties().getFieldsOfInterest(), is( "val;sevr") );
   }

   /****************************************************************************************
    * 7.0 WicaChannel - Serialization Tests
    ****************************************************************************************/

   @Test
   void testSerializeWicaChannel()
   {
      final WicaChannel testChannel = WicaChannelBuilder
            .create()
            .withChannelNameAndProperties( "CHAN-X", WicaChannelPropertiesBuilder.create().withNumericPrecision( 66 ).build() )
            .build();

      final String resultStr = WicaStreamSerializer.writeToJson( testChannel );
      assertThat( resultStr, is( "{\"name\":\"CHAN-X\",\"props\":{\"daqmode\":null,\"pollint\":null,\"fields\":null,\"prec\":66,\"filter\":null,\"n\":null,\"m\":null,\"interval\":null,\"deadband\":null}}") );
   }

   /****************************************************************************************
    * 8.0 WicaChannel - Deserialization Tests
    ****************************************************************************************/

   @Test
   void testDeserializeWicaChannel_Simple_Example()
   {
      final String inputString = "{\"name\":\"XHIPA:TIME\",\"props\":{\"prec\":77 }}";
      final WicaChannel channel = WicaStreamSerializer.readFromJson( inputString, WicaChannel.class );
      assertThat( channel.getName().asString(), is( "XHIPA:TIME" ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
