/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.stream.WicaStream;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamConfigurationDecoderPerformanceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamConfigurationDecoderPerformanceTest.class );

   private static final String simpleStreamConfiguration =
         "{ \"props\": { \"prec\": 29, \"hbflux\": 50, \"monflux\": 40 }," +
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
               "\"hbflux\":15000," +
               "\"monflux\":100," +
               "\"pollflux\":1000," +
               "\"daqmode\":" +
               "\"monitor\"," +
               "\"pollint\":100," +
               "\"prec\":6," +
               "\"fields\":" +
               "\"val;sevr\"}" +
               "}";

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

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
         final WicaStream wicaStream = new WicaStreamConfigurationDecoder().decode( testString );
         assertNotNull( wicaStream.getWicaStreamProperties() );
         assertNotNull( wicaStream.getWicaChannels() );
      }
      final long decodeTimeInMicros = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Decode time for {} iterations was {} ms. Throughput = {} requests per second.", iterations, decodeTimeInMicros, ( 1_000_000 * iterations ) / decodeTimeInMicros  );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
