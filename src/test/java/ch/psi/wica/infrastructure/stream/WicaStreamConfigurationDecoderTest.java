/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamConfigurationDecoderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamConfigurationDecoderTest.class );

   private static final String simpleStreamConfiguration =
         "{ \"props\": { \"prec\": 29, \"heartbeat\": 50, \"changeint\": 40 }," +
           "\"channels\": [ { \"name\": \"MHC1:IST:2\", \"props\": { \"filter\": \"changes\", \"deadband\": 19 } }," +
                           "{ \"name\": \"MYC2:IST:2\", \"props\": { \"filter\": \"changes\", \"deadband\": 10 } }," +
                           "{ \"name\": \"MBC1:IST:2\", \"props\": { \"filter\": \"rate-limiter\", \"interval\": 17 } } ] }";

   private static final String typicalStreamConfiguration =
         "{\"channels\":[" +
               "{\"name\":\"XPROSCAN:TIME:2\"}," +
               "{\"name\":\"XPROREG:STAB:1\"}," +
               "{\"name\":\"EMJCYV:STA3:2\"}," +
               "{\"name\":\"EMJCYV:CTRL:1\"}," +
               "{\"name\":\"MMAV6:IST:2\"}," +
               "{\"name\":\"MMAC3:STR:2\"}," +
               "{\"name\":\"EMJCYV:STAW:1\",\"props\":{\"fields\":\"val\"}}," +
               "{\"name\":\"EMJCYV:IST:2\",\"props\":{\"fields\":\"val\"}}," +
               "{\"name\":\"MMAC:SOL:2\"}," +
               "{\"name\":\"DMAD1:IST:2\"}," +
               "{\"name\":\"PRO:REG2D:Y:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"PRO:REG2D:X:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"PRO:REG2D:X\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"PRO:REG2D:Y\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"AMAKI1:IST:2\"}," +
               "{\"name\":\"CMJSEV:PWRF:2\"}," +
               "{\"name\":\"CMJLL:SOLA:2\"},{\"name\":\"EMJEC1V:IST:2\"}," +
               "{\"name\":\"EMJEC2V:IST:2\"}," +
               "{\"name\":\"AMJHS-I:IADC:2\"}," +
               "{\"name\":\"MMJF:IST:2\"}," +
               "{\"name\":\"IMJV:IST:2\"}," +
               "{\"name\":\"IMJI:IST:2\"}," +
               "{\"name\":\"IMJGF:IST:2\"}," +
               "{\"name\":\"XPROIONS:IST1:2\"}," +
               "{\"name\":\"QMA1:IST:2\"}," +
               "{\"name\":\"QMA2:IST:2\"}," +
               "{\"name\":\"QMA3:IST:2\"}," +
               "{\"name\":\"SMJ1X:IST:2\"}," +
               "{\"name\":\"SMJ2Y:IST:2\"}," +
               "{\"name\":\"SMA1X:IST:2\"}," +
               "{\"name\":\"SMA1Y:IST:2\"}," +
               "{\"name\":\"FMJEP:IST:2\"}," +
               "{\"name\":\"MMJP2:IST1:2\"}," +
               "{\"name\":\"FMJEPI:POS:2\"}," +
               "{\"name\":\"FMJEPI:BREI:2\"}," +
               "{\"name\":\"FMJIP:IST:2\"}," +
               "{\"name\":\"MMJP2:IST2:2\"}," +
               "{\"name\":\"FMJIPI:POS:2\"}," +
               "{\"name\":\"FMJIPI:BREI:2\"}," +
               "{\"name\":\"PRO:CURRENTALARM:1\"}," +
               "{\"name\":\"MMAC3:STR:2##2\",\"props\":{\"daqmode\":\"poll-and-monitor\",\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":5}}," +
               "{\"name\":\"EMJCYV:IST:2##2\",\"props\":{\"daqmode\":\"poll-and-monitor\",\"fields\":\"val;ts\",\"filter\":\"one-in-m\",\"m\":5}}," +
               "{\"name\":\"CMJSEV:PWRF:2##2\",\"props\":{\"daqmode\":\"poll-and-monitor\",\"fields\":\"val;ts\",\"filter\":\"one-in-m\",\"m\":5}}," +
               "{\"name\":\"BMA1:STA:2\"}," +
               "{\"name\":\"BMA1:STAR:2##1\"}," +
               "{\"name\":\"BMA1:STAR:2##2\"}," +
               "{\"name\":\"BMA1:STAP:2##1\"}," +
               "{\"name\":\"BMA1:STAP:2##2\"}," +
               "{\"name\":\"BME1:STA:2\"}," +
               "{\"name\":\"BME1:STAR:2##1\"}," +
               "{\"name\":\"BME1:STAR:2##2\"}," +
               "{\"name\":\"BME1:STAP:2##1\"}," +
               "{\"name\":\"BME1:STAP:2##2\"}," +
               "{\"name\":\"BMB1:STA:2\"}," +
               "{\"name\":\"BMB1:STAR:2##1\"}," +
               "{\"name\":\"BMB1:STAR:2##2\"}," +
               "{\"name\":\"BMB1:STAP:2##1\"}," +
               "{\"name\":\"BMB1:STAP:2##2\"}," +
               "{\"name\":\"BMC1:STA:2\"}," +
               "{\"name\":\"BMC1:STAR:2##1\"}," +
               "{\"name\":\"BMC1:STAR:2##2\"}," +
               "{\"name\":\"BMC1:STAP:2##1\"}," +
               "{\"name\":\"BMC1:STAP:2##2\"}," +
               "{\"name\":\"BMD1:STA:2\"}," +
               "{\"name\":\"BMD1:STAR:2##1\"}," +
               "{\"name\":\"BMD1:STAR:2##2\"}," +
               "{\"name\":\"BMD2:STA:2\"}," +
               "{\"name\":\"BMD2:STAR:2##1\"}," +
               "{\"name\":\"BMD2:STAR:2##2\"}," +
               "{\"name\":\"BMD2:STAP:2##1\"}," +
               "{\"name\":\"BMD2:STAP:2##2\"}," +
               "{\"name\":\"MMAP5X:PROF:2:P\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"MMAP5X:PROF:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"MMAP5X:SPB:2\"}," +
               "{\"name\":\"MMAP6Y:PROF:2:P\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"MMAP6Y:PROF:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
               "{\"name\":\"MMAP6Y:SPB:2\"}," +
               "{\"name\":\"YMJCS1K:IST:2\"}," +
               "{\"name\":\"YMJCS2K:IST:2\"}," +
               "{\"name\":\"YMJHH:SPARB:2\"}," +
               "{\"name\":\"YMJHH:STR:2\"}," +
               "{\"name\":\"YMJHL:IST:2\"}," +
               "{\"name\":\"YMJHG:IST:2\"}," +
               "{\"name\":\"YMJKKRT:IST:2\"}," +
               "{\"name\":\"RPS-IQ:STA:1\"}," +
               "{\"name\":\"UMJSSB:BIQX:1\"}," +
               "{\"name\":\"RPS-HFRD:STA:1\"}," +
               "{\"name\":\"UMJSSB:BHRX:1\"}," +
               "{\"name\":\"RPS-HF:STA:1\"}," +
               "{\"name\":\"UMJSSB:BHFX:1\"}," +
               "{\"name\":\"UMJSSB:BDEX:1\"}," +
               "{\"name\":\"XPROSCAN:STAB:2\"}" +
               "],\"props\":{" +
               "\"heartbeat\":15000," +
               "\"changeint\":100," +
               "\"pollint\":1000," +
               "\"daqmode\":" +
               "\"monitor\"," +
               "\"pollratio\":1," +
               "\"prec\":6," +
               "\"fields\":" +
               "\"val;sevr\"}" +
               "}";

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testGoodDecodeSequence1()
   {
      final String testString = "{ \"props\": { \"prec\": 29, \"heartbeat\": 50, \"changeint\": 40 }," +
            "\"channels\": [ { \"name\": \"MHC1:IST:2\", \"props\": { \"filter\": \"changes\", \"deadband\": 19 } }," +
                            "{ \"name\": \"MYC2:IST:2\", \"props\": { \"filter\": \"changes\", \"deadband\": 10 } }," +
                            "{ \"name\": \"MBC1:IST:2\", \"props\": { \"filter\": \"rate-limiter\", \"interval\": 17 } } ] }";

      final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder( testString );
      final var streamProps = decoder.getWicaStreamProperties();

      assertThat (streamProps.getHeartbeatFluxIntervalInMillis(), is(50 ) );
      assertThat( streamProps.getChangedValueFluxIntervalInMillis(), is( 40 ) );
      assertThat( decoder.getWicaChannels().size(), is( 3 ) );

      final Set<WicaChannel> channels = decoder.getWicaChannels();
      channels.forEach( c -> {
         if ( c.getName().equals(WicaChannelName.of("MHC1:IST:2" ) ) ) {
            assertThat( c.getProperties().getFilterType(), is( WicaChannelProperties.FilterType.CHANGE_FILTERER ) );
            assertThat( c.getProperties().getFilterDeadband(), is( 19.0) );
         }
         if ( c.getName().equals(WicaChannelName.of("MYC2:IST:2" ) ) ) {
            assertThat( c.getProperties().getFilterType(), is( WicaChannelProperties.FilterType.CHANGE_FILTERER ) );
            assertThat( c.getProperties().getFilterDeadband(), is(10.0 ) );
         }
         if ( c.getName().equals(WicaChannelName.of("MBC1:IST:2" ) ) ) {
            assertThat( c.getProperties().getFilterType(), is( WicaChannelProperties.FilterType.RATE_LIMITER ) );
            assertThat( c.getProperties().getFilterSamplingIntervalInMillis(), is(17) );
         }
      } );
   }

   @Test
   void testGoodDecodeSequence2()
   {
      String testString = "{\"channels\":[" +
         "{\"name\":\"ca://MMAC3A:STR:2##1\"}," +
         "{\"name\":\"ca://SLGJG-LMOT-M025:MOT-ACT-POS##1\"}," +
         "{\"name\":\"ca://SLGBV-LENG-BUV2_CS:VAL_GET\"}," +
         "{\"name\":\"ca://SLG-D-MPTEST3:FITCALC.VALN.FTVL\"}" +
      "]," +
          "\"props\":{\"heartbeat\":15000,\"changeint\":100,\"pollint\":1000,\"daqmode\":\"monitor\",\"pollratio\":1,\"prec\":6,\"fields\":\"val;sevr\"}}";

      final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder( testString );
      final var streamProps = decoder.getWicaStreamProperties();
      assertThat( streamProps.getHeartbeatFluxIntervalInMillis(), is(15000 ) );
      assertThat( streamProps.getChangedValueFluxIntervalInMillis(), is( 100 ) );
      assertThat( decoder.getWicaChannels().size(),is (4 ) );
      final Set<WicaChannel> channels = decoder.getWicaChannels();
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

   private static Stream<Arguments> getArgsForPerformanceTest()
   {
      return Stream.of( Arguments.of(    1, simpleStreamConfiguration ),
                        Arguments.of(    1, simpleStreamConfiguration ),
                        Arguments.of(    1, simpleStreamConfiguration ),
                        Arguments.of(   10, simpleStreamConfiguration ),
                        Arguments.of(   10, simpleStreamConfiguration ),
                        Arguments.of(   10, simpleStreamConfiguration ),
                        Arguments.of(  100, simpleStreamConfiguration ),
                        Arguments.of(  100, simpleStreamConfiguration ),
                        Arguments.of(  100, simpleStreamConfiguration ),
                        Arguments.of( 1000, simpleStreamConfiguration ),
                        Arguments.of( 1000, simpleStreamConfiguration ),
                        Arguments.of( 1000, simpleStreamConfiguration ),
                        Arguments.of(    1, typicalStreamConfiguration ),
                        Arguments.of(    1, typicalStreamConfiguration ),
                        Arguments.of(    1, typicalStreamConfiguration ),
                        Arguments.of(   10, typicalStreamConfiguration ),
                        Arguments.of(   10, typicalStreamConfiguration ),
                        Arguments.of(   10, typicalStreamConfiguration ),
                        Arguments.of(  100, typicalStreamConfiguration ),
                        Arguments.of(  100, typicalStreamConfiguration ),
                        Arguments.of(  100, typicalStreamConfiguration ),
                        Arguments.of( 1000, typicalStreamConfiguration ),
                        Arguments.of( 1000, typicalStreamConfiguration ),
                        Arguments.of( 1000, typicalStreamConfiguration ) );
   }

   @MethodSource( "getArgsForPerformanceTest" )
   @ParameterizedTest
   void testPerformance( int iterations, String testString )
   {
      final StopWatch stopWatch = StopWatch.createStarted();
      for( int  i= 0; i < iterations; i++)
      {
         final WicaStreamConfigurationDecoder decoder = new WicaStreamConfigurationDecoder( testString );
         assertNotNull( decoder.getWicaStreamProperties() );
         assertNotNull( decoder.getWicaChannels() );
      }
      final long decodeTimeInMillis = stopWatch.getTime();
      logger.info( "Decode time for {} iterations was {} ms. Throughput = {} requests per second.", iterations, decodeTimeInMillis, ( 1000 * iterations ) / decodeTimeInMillis  );
   }

   @Test
   void testBadDecodeSequence1()
   {
      assertThrows( IllegalArgumentException.class, () ->
      {
         // Note: the following string is bad because the JSON root does not contain a
         // collection of name/value pairs with an item named "channels" referencing
         // a value with a list of channels.
         String testString = "[ { \"name\": \"MHC1:IST:2\" } ]";
         new WicaStreamConfigurationDecoder( testString );
      } );
   }

   @Test
   void testBadDecodeSequence2()
   {
      assertThrows(IllegalArgumentException.class, () ->
      {
         // Note: the following string is bad because the JSON root does not contain a
         // collection of name/value pairs with an item named "channels" referencing
         // a value with a list of channels.
         String testString = "[ { \"name\": \"MHC1:IST:2\", \"props\": { \"filterType\": \"changes\", \"deadband\": 19 } }," +
               "{ \"name\": \"MYC2:IST:2\", \"props\": { { \"filterType\": \"changes\", \"deadband\": 10 } }," +
               "{ \"name\": \"MBC1:IST:2\", \"props\": { \"filterType\": \"special\", \"deadband\": 10 } } ]";

         new WicaStreamConfigurationDecoder(testString);
      });
   }

   @Test
   void testBadDecodeSequence3()
   {
      assertThrows( IllegalArgumentException.class, () ->
      {
         // Note: the following string is bad because the JSON root does not contain a
         // collection of name/value pairs with an item named "channels" referencing
         // a value with a list of channels.
         String testString = "[ { \"name\": \"MHC1:IST:2\", \"props\": ]";

         new WicaStreamConfigurationDecoder( testString );
      } );

   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
