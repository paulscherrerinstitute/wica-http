/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;



/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaStreamConfigurationDecoderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamConfigurationDecoderTest.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

//   @Test
//   void testGoodDecodeSequence1()
//   {
//       String testString = "{ \"props\": { \"prop1\": 29, \"prop2\": 14 }," +
//                           "\"channels\": [ { \"name\": \"MHC1:IST:2\", \"props\": { \"filterType\": \"changes\", \"deadband\": 19 } }," +
//                                           "{ \"name\": \"MYC2:IST:2\", \"props\": { \"filterType\": \"changes\", \"deadband\": 10 } }," +
//                                           "{ \"name\": \"MBC1:IST:2\", \"props\": { \"filterType\": \"special\", \"deadband\": 10 } } ] }";
//
//      final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder( testString );
//      assertEquals( 3, decoder.getWicaChannels().size() );
//    //  assertEquals( 2, decoder.getWicaChannels()..getNumberOfProperties() );
//      assertEquals( 2, decoder.getChannelProperties ( WicaChannelName.of( "MBC1:IST:2") ).getNumberOfProperties() );
//      assertEquals( "special", decoder.getChannelProperties( WicaChannelName.of( "MBC1:IST:2") ).getPropertyValue( "filterType") );
//      assertEquals( "19", decoder.getChannelProperties(WicaChannelName.of( "MHC1:IST:2") ).getPropertyValue( "deadband") );
//
//      assertThrows( IllegalArgumentException.class, () -> {
//         decoder.getChannelProperties ( WicaChannelName.of( "MYC2:IST:2") ).getPropertyValue( "unknown");
//      } );
//
//      assertThrows( IllegalArgumentException.class, () -> {
//         decoder.getChannelProperties( WicaChannelName.of( "xxx") );
//      } );
//   }

   @Test
   void testGoodDecodeSequence2()
   {
      assertThrows( IllegalArgumentException.class, () ->
      {
         String testString = "[ { \"name\": \"MHC1:IST:2\" } ]";
         final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder( testString );
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

         final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder(testString);
      });
   }

   @Test
   void testBadDecodeSequence2()
   {
      assertThrows( IllegalArgumentException.class, () ->
      {
         String testString = "[ { \"name\": \"MHC1:IST:2\", \"props\": ]";

         final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder( testString );
      } );

   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
