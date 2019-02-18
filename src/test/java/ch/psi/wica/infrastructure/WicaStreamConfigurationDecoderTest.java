/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannel;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelProperties;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamConfigurationDecoderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testGoodDecodeSequence1()
   {
       String testString = "{ \"props\": { \"prec\": 29, \"heartbeat\": 50, \"changeint\": 40 }," +
                           "\"channels\": [ { \"name\": \"MHC1:IST:2\", \"props\": { \"filter\": \"change-filterer\", \"deadband\": 19 } }," +
                                           "{ \"name\": \"MYC2:IST:2\", \"props\": { \"filter\": \"change-filterer\", \"deadband\": 10 } }," +
                                           "{ \"name\": \"MBC1:IST:2\", \"props\": { \"filter\": \"rate-limiter\", \"minsgap\": 17 } } ] }";

      final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder( testString );
      final var streamProps = decoder.getWicaStreamProperties();
      assertEquals( 50, streamProps.getHeartbeatFluxIntervalInMillis() );
      assertEquals( 40, streamProps.getValueChangeFluxIntervalInMillis() );
      assertEquals( 3, decoder.getWicaChannels().size() );
      final Set<WicaChannel> channels = decoder.getWicaChannels();
      channels.forEach( c -> {
         var chanProps = c.getProperties();
         assertFalse( chanProps.getFieldsOfInterest().isPresent() );
         if ( c.getName().equals( WicaChannelName.of( "MHC1:IST:2" ) ) ) {
            assertEquals( WicaChannelProperties.FilterType.CHANGE_FILTERER, c.getProperties().getFilterType() );
            assertEquals( 19.0, c.getProperties().getFilterDeadband() );
         }
         if ( c.getName().equals( WicaChannelName.of( "MYC2:IST:2" ) ) ) {
            assertEquals( WicaChannelProperties.FilterType.CHANGE_FILTERER, c.getProperties().getFilterType() );
            assertEquals( 10.0, c.getProperties().getFilterDeadband() );
         }
         if ( c.getName().equals( WicaChannelName.of( "MBC1:IST:2" ) ) ) {
            assertEquals( WicaChannelProperties.FilterType.RATE_LIMITER, c.getProperties().getFilterType() );
            assertEquals( 17, c.getProperties().getFilterMinSampleGapInMillis() );
         }
      } );
   }

   @Test
   void testGoodDecodeSequence2()
   {
      assertThrows( IllegalArgumentException.class, () ->
      {
         String testString = "[ { \"name\": \"MHC1:IST:2\" } ]";
         new WicaStreamConfigurationDecoder( testString );
      } );
   }

   @Test
   void testBadDecodeSequence1()
   {
      assertThrows(IllegalArgumentException.class, () ->
      {
         String testString = "[ { \"name\": \"MHC1:IST:2\", \"props\": { \"filterType\": \"changes\", \"deadband\": 19 } }," +
               "{ \"name\": \"MYC2:IST:2\", \"props\": { { \"filterType\": \"changes\", \"deadband\": 10 } }," +
               "{ \"name\": \"MBC1:IST:2\", \"props\": { \"filterType\": \"special\", \"deadband\": 10 } } ]";

         new WicaStreamConfigurationDecoder(testString);
      });
   }

   @Test
   void testBadDecodeSequence2()
   {
      assertThrows( IllegalArgumentException.class, () ->
      {
         String testString = "[ { \"name\": \"MHC1:IST:2\", \"props\": ]";

         new WicaStreamConfigurationDecoder( testString );
      } );

   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
