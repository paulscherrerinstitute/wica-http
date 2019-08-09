/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelPropertiesDefaults;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamBuilderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      WicaStreamId.resetAllocationSequencer();
   }

   @Test
   void testCreateAndBuild_AutoIdAllocation()
   {
      final WicaStream wicaStream1 = WicaStreamBuilder.create().build();

      assertThat(wicaStream1.getWicaStreamId(), is(WicaStreamId.of( "0" ) ) );
      assertThat( wicaStream1.getWicaChannels().size(), is( 0 ) );
      assertThat( wicaStream1.getWicaStreamProperties(), is (WicaStreamPropertiesBuilder.create().build() ) );

      final WicaStream wicaStream2 = WicaStreamBuilder.create().build();

      assertThat( wicaStream2.getWicaStreamId(), is(  WicaStreamId.of( "1" ) ) );
      assertThat( wicaStream2.getWicaChannels().size(), is( 0 ) );
      assertThat( wicaStream2.getWicaStreamProperties(), is (WicaStreamPropertiesBuilder.create().build() ) );
   }

   @Test
   void testCreateAndBuild_withId()
   {
      final WicaStream wicaStream = WicaStreamBuilder.create().withId( "streamABC" ).build();

      assertThat( wicaStream.getWicaStreamProperties(), is ( WicaStreamPropertiesBuilder.create().build() ) );
      assertThat( wicaStream.getWicaStreamId(), is(  WicaStreamId.of( "streamABC" ) ) );
      assertThat( wicaStream.getWicaChannels().size(), is( 0 ) );
   }

   @Test
   void testCreateAndBuild_withId_and_withStreamProperties()
   {
      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withId( "streamABC" )
            .withStreamProperties( WicaStreamPropertiesBuilder.create().withNumericPrecision( 17 ).build() )
            .build();

      assertThat( wicaStream.getWicaStreamProperties().getNumericPrecision(), is ( 17 ) );
      assertThat( wicaStream.getWicaStreamId(), is(  WicaStreamId.of( "streamABC" ) ) );
      assertThat( wicaStream.getWicaChannels().size(), is( 0 ) );
   }

   @Test
   void testCreateAndBuild_withId_and_withChannel()
   {
      final WicaChannel channel123 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "channel123" ).build();

      final WicaStream wicaStream = WicaStreamBuilder.create()
         .withId( "streamDEF" )
         .withChannel( channel123 )
         .build();

      assertThat( wicaStream.getWicaStreamProperties(), is ( WicaStreamPropertiesBuilder.create().build() ) );
      assertThat( wicaStream.getWicaStreamId(), is(  WicaStreamId.of( "streamDEF" ) ) );
      assertThat( wicaStream.getWicaChannels().size(), is( 1 ) );
      assertThat( wicaStream.getWicaChannels(), containsInAnyOrder( channel123) );
   }

   @Test
   void testCreateAndBuild_withId_and_with_two_channels()
   {
      final WicaChannel channel123 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "channel123" ).build();
      final WicaChannel channel456 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "channel456" ).build();

      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withId( "streamDEF" )
            .withChannel( channel123 )
            .withChannel( channel456 )
            .build();

      assertThat( wicaStream.getWicaStreamProperties(), is ( WicaStreamPropertiesBuilder.create().build() ) );
      assertThat( wicaStream.getWicaStreamId(), is(  WicaStreamId.of( "streamDEF" ) ) );
      assertThat( wicaStream.getWicaChannels().size(), is( 2 ) );
      assertThat( wicaStream.getWicaChannels(), containsInAnyOrder( channel123, channel456 ) );
   }

   @Test
   void testCreateAndBuild_withEmptyStreamProperties()
   {
      final WicaStream wicaStream = WicaStreamBuilder.create().build();
      assertThat( wicaStream.getWicaStreamProperties(), is (WicaStreamPropertiesBuilder.create().build() ) );
   }

   @Test
   void testCreate_withDefaultStreamProperties()
   {
      final WicaStream wicaStream = WicaStreamBuilder.create().withDefaultStreamProperties().build();
      assertThat( wicaStream.getWicaStreamProperties(), is (WicaStreamPropertiesBuilder.create().withDefaultProperties().build() ) );
   }

   @Test
   void testCreateAndBuild_withDefinedStreamProperties()
   {
      final WicaStreamProperties testStreamProperties = WicaStreamPropertiesBuilder.create().withNumericPrecision(7 ).build();
      final WicaStream wicaStream = WicaStreamBuilder.create().withStreamProperties( testStreamProperties ).build();
      assertThat( wicaStream.getWicaStreamProperties(), is ( testStreamProperties ) );
   }

   @Test
   void testCreateAndBuild_withDefaultStreamProperties_and_channelNameAndCombinedProperties_channelNameAndDefaultProperties()
   {
      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withDefaultStreamProperties()
            .withChannelNameAndCombinedProperties("chan1", WicaChannelPropertiesBuilder.create().withNumericPrecision( 4 ).build()  )
            .withChannelNameAndDefaultProperties( "chan2" )
            .build();

      assertThat( wicaStream.getWicaStreamProperties().getNumericPrecision(), is (WicaChannelPropertiesDefaults.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).get().getProperties().getNumericPrecision(), is( 4 ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).get().getProperties().getNumericPrecision(), is(WicaChannelPropertiesDefaults.DEFAULT_NUMERIC_PRECISION ) );
   }

   @Test
   void testCreateAndBuild_withDefaultStreamProperties_PropertiesOfStream()
   {
       final WicaStream wicaStream = WicaStreamBuilder.create()
            .withStreamProperties( WicaStreamPropertiesBuilder.create().withDefaultProperties().withNumericPrecision( 11 ).build() )
            .withChannelNameAndCombinedProperties("chan1", WicaChannelPropertiesBuilder.create().withNumericPrecision( 4 ).build() )
            .withChannelNameAndStreamProperties( "chan2" )
            .build();

      assertThat( wicaStream.getWicaStreamProperties().getNumericPrecision(), is (11 ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).get().getProperties().getNumericPrecision(), is( 4 ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).get().getProperties().getNumericPrecision(), is( 11) );
   }

   @Test
   void testCreateAndBuild_withStreamProperties_and_withChannelNameAndStreamProperties_and_withChannelNameAndCombinedProperties()
   {
      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withStreamProperties( WicaStreamPropertiesBuilder.create().withDefaultProperties().withNumericPrecision( 11 ).build() )
            .withChannelNameAndStreamProperties( "chan1" )
            .withChannelNameAndCombinedProperties("chan2", WicaChannelPropertiesBuilder.create().withNumericPrecision( 4 ).build() )
            .withChannelNameAndStreamProperties( "chan3" )
            .build();

      assertThat( wicaStream.getWicaStreamProperties().getNumericPrecision(), is (11 ) );

      assertThat( wicaStream.getWicaChannel( "chan1" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).get().getProperties().getNumericPrecision(), is( 11 ) );

      assertThat( wicaStream.getWicaChannel( "chan2" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).get().getProperties().getNumericPrecision(), is( 4 ) );

      assertThat( wicaStream.getWicaChannel( "chan3" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan3" ).get().getProperties().getNumericPrecision(), is( 11 ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
