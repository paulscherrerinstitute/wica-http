/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.util.JsonStringFormatter;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelValueMapSerializerServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelValueMapSerializerServiceTest.class );


   private WicaChannelValue unconnValue;
   private WicaChannelValue intValue ;
   private WicaChannelValue strValue;
   private WicaChannelValue realValue;
   private WicaChannelValue realNanValue;
   private WicaChannelValue realInfValue;
   private WicaChannelValue intArrValue;
   private WicaChannelValue strArrValue;
   private WicaChannelValue realArrValue;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setup()
   {
      // Setup values shared across many tests
      unconnValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueDisconnected();
      intValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( 27 );
      strValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( "abcdef" );
      realValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( 123456.654321 );
      realNanValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( NaN );
      realInfValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( POSITIVE_INFINITY );
      intArrValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( new int[] { 25, 12 } );
      strArrValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( new String[] { "abcdef", "ghijkl" } );
      realArrValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( new double[] { 2.5000, 1.20000 }  );
   }

   @Test
   void test_serialize()
   {
      final Map<WicaChannel,List<WicaChannelValue>> map = Map.of(WicaChannelBuilder.create().withChannelNameAndDefaultProperties("UnconnChannel").build(), List.of(unconnValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringTypeChannel" ).build(), List.of(strValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringArrayType" ).build(), List.of(strArrValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerTypeChannel" ).build(), List.of(intValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerArrayTypeChannel" ).build(), List.of(intArrValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealTypeChannel" ).build(), List.of(realValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealInfTypeChannel" ).build(), List.of(realInfValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealNanTypeChannel" ).build(), List.of(realNanValue ),
                                                                 WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealArrayTypeChannel" ).build(), List.of(realArrValue  ) );

      final var serializer = new WicaChannelValueMapSerializerService(false );
      final String jsonStr = serializer.serialize( map );
      logger.info("JSON Value MAP serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat(jsonStr ) );
   }

   @CsvSource( { "10000", "1", "10", "100", "1000", "1000", "1000", "10000", "10000", "10000" } )
   @ParameterizedTest
   void test_serializePerformance( int times )
   {
      final Map<WicaChannel,List<WicaChannelValue>> map = Map.of( WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "UnconnChannel").build(), List.of(unconnValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringTypeChannel" ).build(), List.of(strValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringArrayType" ).build(), List.of(strArrValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerTypeChannel" ).build(), List.of(intValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerArrayTypeChannel" ).build(), List.of(intArrValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealTypeChannel" ).build(), List.of(realValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealInfTypeChannel" ).build(), List.of(realInfValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealNanTypeChannel" ).build(), List.of(realNanValue ),
                                                                  WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealArrayTypeChannel" ).build(), List.of(realArrValue  ) );


      final var serializer = new WicaChannelValueMapSerializerService(false );

      final StopWatch stopwatch = StopWatch.createStarted();
      for ( int i= 0; i < times; i++ )
      {
         serializer.serialize( map );
      }
      long elapsedTime = stopwatch.getTime(TimeUnit.MILLISECONDS);
      logger.info( "Elapsed time for {} iterations was: {} ms", times, elapsedTime );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
