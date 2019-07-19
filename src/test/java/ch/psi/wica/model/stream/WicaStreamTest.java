/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testBuilder1()
   {
      final WicaStream wicaStream1 = WicaStream.createBuilder().build();
      assertThat( wicaStream1.getWicaStreamId(), is(  WicaStreamId.of( "0" ) ) );
      assertThat( wicaStream1.getWicaChannels().size(), is( 0 ) );
      assertThat( wicaStream1.getWicaStreamProperties(), is ( WicaStreamProperties.createDefaultInstance() ) );
      final WicaStream wicaStream2 = WicaStream.createBuilder().build();
      assertThat( wicaStream2.getWicaStreamId(), is(  WicaStreamId.of( "1" ) ) );
      assertThat( wicaStream2.getWicaChannels().size(), is( 0 ) );
      assertThat( wicaStream2.getWicaStreamProperties(), is ( WicaStreamProperties.createDefaultInstance() ) );
   }

   @Test
   void testBuilder2()
   {
      final WicaStream wicaStream = WicaStream.createBuilder().withId( "streamABC" ).build();
      assertThat( wicaStream.getWicaStreamProperties(), is ( WicaStreamProperties.createDefaultInstance() ) );
      assertThat( wicaStream.getWicaStreamId(), is(  WicaStreamId.of( "streamABC" ) ) );
      assertThat( wicaStream.getWicaChannels().size(), is( 0 ) );
   }

   @Test
   void testBuilder3()
   {
      final WicaStream wicaStream = WicaStream.createBuilder().withId( "streamDEF" )
         .withChannelName("channel123" )
         .build();
      assertThat( wicaStream.getWicaStreamProperties(), is ( WicaStreamProperties.createDefaultInstance() ) );
      assertThat( wicaStream.getWicaStreamId(), is(  WicaStreamId.of( "streamDEF" ) ) );
      assertThat( wicaStream.getWicaChannels().size(), is( 1 ) );
   }

   @Test
   void testBuilder4()
   {
      final WicaStream wicaStream = WicaStream.createBuilder().withId( "streamDEF" )
         .withChannelName("channel123" )
         .build();
      assertThat( wicaStream.getWicaStreamProperties(), is ( WicaStreamProperties.createDefaultInstance() ) );
      assertThat( wicaStream.getWicaStreamId(), is(  WicaStreamId.of( "streamDEF" ) ) );
      assertThat( wicaStream.getWicaChannels().size(), is( 1 ) );
      assertThat( wicaStream.getWicaChannels(), is( Set.of( WicaChannel.createFromName( "channel123" ) ) ) );
   }

   @Test
   void testBuilder5()
   {
      final WicaStream wicaStream = WicaStream.createBuilder()
            .withChannelNameAndProperties("chan1", WicaChannelProperties.createBuilder().withNumericPrecision( 4 ).build()  )
            .withChannelName("chan2" )
            .build();

      assertThat( wicaStream.getWicaStreamProperties().getNumericPrecision(), is (WicaChannelProperties.DEFAULT_NUMERIC_PRECISION ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).get().getProperties().getNumericPrecision(), is( 4 ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).get().getProperties().getNumericPrecision(), is( WicaChannelProperties.DEFAULT_NUMERIC_PRECISION ) );
   }

   @Test
   void testBuilder6()
   {
       final WicaStream wicaStream = WicaStream.createBuilder()
            .withStreamProperties( WicaStreamProperties.createBuilder().withNumericPrecision( 11 ).build() )
            .withChannelNameAndProperties("chan1", WicaChannelProperties.createBuilder().withNumericPrecision(4 ).build() )
            .withChannelName("chan2" )
            .build();

      assertThat( wicaStream.getWicaStreamProperties().getNumericPrecision(), is (11 ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan1" ).get().getProperties().getNumericPrecision(), is( 4 ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).isPresent(), is( true ) );
      assertThat( wicaStream.getWicaChannel( "chan2" ).get().getProperties().getNumericPrecision(), is( 11) );
   }

   @Test
   void testBuilder7()
   {
      final WicaStream wicaStream = WicaStream.createBuilder()
            .withStreamProperties( WicaStreamProperties.createBuilder().withNumericPrecision( 11 ).build() )
            .withChannelName("chan1" )
            .withChannelNameAndProperties("chan2", WicaChannelProperties.createBuilder().withNumericPrecision(4 ).build() )
            .withChannelName("chan3" )
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
