/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamConfigurationDecoderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaStreamConfigurationDecoder decoder;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      decoder = new WicaStreamConfigurationDecoder();
   }


   @Test
   void testGoodDecodeSequence_hipaExample()
   {
      final String testString = "{ \"props\"   : { \"prec\": 29, \"hbflux\": 50, \"monflux\": 40 }," +
                                  "\"channels\": [ { \"name\": \"MHC1:IST:2\", \"props\": { \"filter\": \"changes\", \"deadband\": 19 } }," +
                                                  "{ \"name\": \"MYC2:IST:2\", \"props\": { \"filter\": \"changes\", \"deadband\": 10 } }," +
                                                  "{ \"name\": \"MBC1:IST:2\", \"props\": { \"filter\": \"rate-limiter\", \"interval\": 17 } } ] }";

      final WicaStream wicaStream = decoder.decode(testString );
      final WicaStreamProperties wicaStreamProperties = wicaStream.getWicaStreamProperties();
      final Set<WicaChannel> channels = wicaStream.getWicaChannels();

      assertThat (wicaStreamProperties.getHeartbeatFluxIntervalInMillis(), is(50 ) );
      assertThat( wicaStreamProperties.getMonitoredValueFluxIntervalInMillis(), is(40 ) );
      assertThat( channels.size(), is( 3 ) );

      channels.forEach( c -> {
         if ( c.getName().equals(WicaChannelName.of("MHC1:IST:2" ) ) ) {
            assertThat( c.getProperties().getFilterType(), is(WicaFilterType.CHANGE_DETECTOR) );
            assertThat( c.getProperties().getFilterDeadband(), is( 19.0) );
         }
         if ( c.getName().equals(WicaChannelName.of("MYC2:IST:2" ) ) ) {
            assertThat( c.getProperties().getFilterType(), is( WicaFilterType.CHANGE_DETECTOR) );
            assertThat( c.getProperties().getFilterDeadband(), is(10.0 ) );
         }
         if ( c.getName().equals(WicaChannelName.of("MBC1:IST:2" ) ) ) {
            assertThat( c.getProperties().getFilterType(), is( WicaFilterType.RATE_LIMITER ) );
            assertThat( c.getProperties().getFilterSamplingIntervalInMillis(), is(17) );
         }
      } );
   }

   @Test
   void testGoodDecodeSequence_edwinStyleChannelNameExample()
   {
      String testString = "{\"channels\":[" +
         "{\"name\":\"ca://MMAC3A:STR:2##1\"}," +
         "{\"name\":\"ca://SLGJG-LMOT-M025:MOT-ACT-POS##1\"}," +
         "{\"name\":\"ca://SLGBV-LENG-BUV2_CS:VAL_GET\"}," +
         "{\"name\":\"ca://SLG-D-MPTEST3:FITCALC.VALN.FTVL\"}" + "]," +
          "\"props\":{\"hbflux\":15000,\"monflux\":100,\"pollflux\":1000,\"daqmode\":\"monitor\",\"pollint\":100,\"prec\":6,\"fields\":\"val;sevr\"}}";


      final WicaStream wicaStream = decoder.decode( testString );
      final WicaStreamProperties wicaStreamProperties = wicaStream.getWicaStreamProperties();
      final Set<WicaChannel> channels = wicaStream.getWicaChannels();

      assertThat(wicaStreamProperties.getHeartbeatFluxIntervalInMillis(), is(15000 ) );
      assertThat( wicaStreamProperties.getMonitoredValueFluxIntervalInMillis(), is(100 ) );
      assertThat( channels.size(), is( 4) );

      channels.forEach( c -> {
         boolean channelRecognised = c.getName().equals( WicaChannelName.of( "ca://MMAC3A:STR:2##1" ) ) ||
                                     c.getName().equals( WicaChannelName.of( "ca://SLGBV-LENG-BUV2_CS:VAL_GET" ) ) ||
                                     c.getName().equals( WicaChannelName.of( "ca://SLG-D-MPTEST3:FITCALC.VALN.FTVL" ) ) ||
                                     c.getName().equals( WicaChannelName.of( "ca://SLGJG-LMOT-M025:MOT-ACT-POS##1" ) );
         if ( ! channelRecognised )
         {
            fail( "channel was not recognised: " + c.getName() );
         }
      } );
   }

   @Test
   void testGoodDecodeSequence_hipaStyleChannelNameExample()
   {
      String testString = "{\"channels\":" +
            "[{\"name\":\"XHIPA:TIME\"},{\"name\":\"EVEX:STR:2\"},{\"name\":\"EWBRI:IST:2\"}," +
            "{\"name\":\"MXC1:IST:2\"},{\"name\":\"MYC2:IST:2\"},{\"name\":\"MHC4:IST:2\"},{\"name\":\"MHC6:IST:2\"}," +
            "{\"name\":\"MHC1:IST:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollint\":30000,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":10}}," +
            "{\"name\":\"MYC2:IST:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollint\":30000,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":10}}," +
            "{\"name\":\"MBC1:IST:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollint\":30000,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":10}}," +
            "{\"name\":\"MRI12:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollint\":30000,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":0.3}}," +
            "{\"name\":\"MII7:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollint\":30000,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":0.3}}," +
            "{\"name\":\"MRI13:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollint\":30000,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":8}}," +
            "{\"name\":\"MRI14:ILOG:2##2\",\"props\":{\"daqmode\":\"poll\",\"pollint\":30000,\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":8}}," +
            "{\"name\":\"CIPHMO:SOL:1\"},{\"name\":\"CRPHFT:SOL:1\"},{\"name\":\"MXF1:IST:2\"},{\"name\":\"MRFEIN:IST:2\"},{\"name\":\"MRFAUS:IST:2\"}," +
            "{\"name\":\"MII7:ILOG:2\"},{\"name\":\"MRI2:ILOG:2\"},{\"name\":\"MRI13:ILOG:2\"},{\"name\":\"MRI14:ILOG:2\"},{\"name\":\"CI1V:IST:2\"}," +
            "{\"name\":\"CI3V:IST:2\"},{\"name\":\"CI2V:IST:2\"},{\"name\":\"CI4V:IST:2\"},{\"name\":\"CR1V:IST:2\"},{\"name\":\"CR2V:IST:2\"}," +
            "{\"name\":\"CR3V:IST:2\"},{\"name\":\"CR4V:IST:2\"},{\"name\":\"CR5V:IST:2\"},{\"name\":\"UCNQ:BEAMREQ:STATUS\"}," +
            "{\"name\":\"UCNQ:BEAMREQ:COUNTDOWN\"},{\"name\":\"EICV:IST:2\"},{\"name\":\"EICI:IST:2\"},{\"name\":\"EECV:IST:2\"}," +
            "{\"name\":\"EECI:IST:2\"},{\"name\":\"CIREV:FIST:2\"},{\"name\":\"CRREV:FIST:2\"},{\"name\":\"AIHS:IST:2\"}," +
            "{\"name\":\"HS:IST:2\"},{\"name\":\"M3ALT:IST:2\"},{\"name\":\"GLS:LEISTUNG_AKTUELL\"}," +
            "{\"name\":\"ZSLP:TOTSAVEFAST\"}]," +
            "\"props\":{\"hbflux\":15000,\"monflux\":100,\"pollflux\":1000,\"daqmode\":\"monitor\",\"pollint\":1000,\"prec\":6,\"fields\":\"val;sevr\"}}";

      final WicaStream wicaStream = decoder.decode( testString );
      final WicaStreamProperties wicaStreamProperties = wicaStream.getWicaStreamProperties();
      final Set<WicaChannel> channels = wicaStream.getWicaChannels();

      assertThat(wicaStreamProperties.getHeartbeatFluxIntervalInMillis(), is(15000 ) );
      assertThat( wicaStreamProperties.getMonitoredValueFluxIntervalInMillis(), is(100 ) );
      assertThat( channels.size(), is( 45 ) );
   }

   @Test
   void testBadDecodeSequence_nullString()
   {
      final var ex = assertThrows( NullPointerException.class, () -> decoder.decode( null ));
      assertThat( ex.getMessage(), is( "The JSON input string was null." ) );
   }

   @Test
   void testBadDecodeSequence_inputStringIsEmpty()
   {
      final var ex = assertThrows(IllegalArgumentException.class, () -> decoder.decode( "" ) );
      assertThat( ex.getMessage(), is( "The JSON input string was empty." ) );
   }

   @Test
   void testBadDecodeSequence_testBadDecodeSequence_inputStringIsBlank()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( " " ) );
      assertThat( ex.getMessage(), is( "The JSON input string was blank." ) );
   }

   @Test
   void testBadDecodeSequence_rootNodeIsNotJsonObject()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode("[]" ) );
      assertThat( ex.getMessage(), is( "The root node of the JSON configuration string was not a JSON Object." ) );
   }

   @Test
   void testBadDecodeSequence_rootNodeIsEmpty()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode("{}" ) );
      assertThat( ex.getMessage(), is( "The root node of the JSON configuration string did not contain a field named 'channels'." ) );
   }

   @Test
   void testBadDecodeSequence_rootNodeChannelsFieldIsMissing()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode("{ \"key\": 1 }" ) );
      assertThat( ex.getMessage(), is( "The root node of the JSON configuration string did not contain a field named 'channels'." ) );
   }

   @Test
   void testBadDecodeSequence_rootNodeChannelsFieldIsNull()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode("{ \"channels\": null }" ) );
      assertThat( ex.getMessage(), is( "The root node of the JSON configuration string did not contain a value for field named 'channels'." ) );
   }

   @Test
   void testBadDecodeSequence_rootNodeChannelsFieldIsMissingValue()
   {
      final String testString = "{ \"channels\": }";
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( testString ) );
      assertThat( ex.getMessage(), is( "The JSON channel configuration string: '" + testString + "' was invalid." ) );
      assertThat( ex.getCause().toString(), containsString( "Unexpected character ('}' (code 125))" ) );
   }

   @Test
   void testBadDecodeSequence_rootNodeChannelsFieldIsNotAnArray_1()
   {
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( "{ \"channels\": 1 }" ) );
      assertThat( ex.getMessage(), is( "The root node of the JSON configuration string contained a field named 'channels', but it wasn't an array." ) );
   }

   @Test
   void testBadDecodeSequence_channelsFieldIsNotAnArray_2()
   {
      final String testString = "{ \"channels\": {} }";
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( testString ) );
      assertThat( ex.getMessage(), is( "The root node of the JSON configuration string contained a field named 'channels', but it wasn't an array." ) );
   }


   @Test
   void testBadDecodeSequence_channelsFieldIsDuplicated()
   {
      final String testString = "{ \"channels\": [], \"channels\": [] }";
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( testString ) );
      assertThat( ex.getMessage(), is( "The JSON channel configuration string: '" + testString + "' was invalid." ) );
      assertThat( ex.getCause().toString(), containsString( "Duplicate field 'channels'" ) );
   }

   @Test
   void testBadDecodeSequence_channelNameFieldIsDuplicated()
   {
      final String testString = "{ \"channels\": [ { \"name\": \"ABC\", \"name\": \"ABC\" } ] }";
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( testString ) );
      assertThat( ex.getMessage(), is( "The JSON channel configuration string: '" + testString + "' was invalid." ) );
      assertThat( ex.getCause().toString(), containsString( "Duplicate field 'name'" ) );
   }

   @Test
   void testBadDecodeSequence_twoChannelsHaveTheSameName()
   {
      final String testString = "{ \"channels\": [ { \"name\": \"ABC\" }, { \"name\": \"ABC\" } ] }";
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( testString ) );
      assertThat( ex.getMessage(), is( "The JSON configuration string did not contain a valid and/or unique channel specification." ) );
   }

   @Test
   void testBadDecodeSequence_channelNameFieldIsMissing()
   {
      final String testString = "{ \"channels\": [ { \"props\": \"ABC\" } ] }";
      final var ex = assertThrows( IllegalArgumentException.class, () -> decoder.decode( testString ) );
      assertThat( ex.getMessage(), is( "The JSON configuration string did not specify the name of one or more channels (missing 'name' field)." ) );
   }


   @Test
   void testGoodDecodeSequence_streamPropertiesOverrideDefaultChannelProperties()
   {
      final String testString =
            "{  \"props\":    { \"n\" : 8 }," +
               "\"channels\": [ { \"name\": \"MHC1:IST:2\" }, " +
                    "           { \"name\": \"MHC2:IST:2\", \"props\" : { \"n\" : 4} }," +
                    "           { \"name\": \"MHC3:IST:2\", \"props\" : { \"filter\": \"averager\", \"x\" : 11 } } ] }";

      final var stream = decoder.decode( testString );
      assertThat( stream.getWicaChannels().size(), is( 3 ) );
      for (WicaChannel channel : stream.getWicaChannels() )
      {
         switch (channel.getName().asString())
         {
            case "MHC1:IST:2":
               assertThat(channel.getProperties().getFilterNumSamples(), is(8));
               break;
            case "MHC2:IST:2":
               assertThat(channel.getProperties().getFilterNumSamples(), is(4));
               break;
            case "MHC3:IST:2":
               assertThat(channel.getProperties().getFilterType(), is(WicaFilterType.AVERAGER ));
               assertThat(channel.getProperties().getFilterNumSamplesInAverage(), is(11 ));
               break;
            default:
               fail();
               break;
         }
      }
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
