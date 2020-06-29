/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.util.JsonStringFormatter;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelMetadataMapSerializerServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelMetadataMapSerializerServiceTest.class );

   private WicaChannelMetadata unkMetadata;
   private WicaChannelMetadata strMetadata ;
   private WicaChannelMetadata strArrMetadata;
   private WicaChannelMetadata intMetadata;
   private WicaChannelMetadata intArrMetadata;
   private WicaChannelMetadata realMetadata;
   private WicaChannelMetadata realArrMetadata;
   private WicaChannelMetadata realNanMetadata;
   private WicaChannelMetadata realInfMetadata;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setup()
   {
      // Setup values shared across many tests
      unkMetadata = WicaChannelMetadata.createUnknownInstance();
      strMetadata = WicaChannelMetadata.createStringInstance();
      strArrMetadata = WicaChannelMetadata.createStringArrayInstance();
      intMetadata = WicaChannelMetadata.createIntegerInstance( "units", 100, 0, 90, 10, 98, 2, 95, 5 );
      intArrMetadata = WicaChannelMetadata.createIntegerArrayInstance( "units", 100, 0, 90, 10, 98, 2, 95, 5 );
      realMetadata = WicaChannelMetadata.createRealInstance( "units", 3, 90.12345678, 0.0000123, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      realArrMetadata= WicaChannelMetadata.createRealArrayInstance( "units", 3, 100.123117, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      realNanMetadata = WicaChannelMetadata.createRealInstance( "units", 3, Double.NaN, 0.0, 4.5, 10.6, 97.6, 2.2, 95.3, 5.1 );
      realInfMetadata = WicaChannelMetadata.createRealInstance( "units", 3, Double.POSITIVE_INFINITY, 0.0, 9.7, 10.6123, 97.61234, 2.2, 95.3, 5.1 );
   }

   @Test
   void test_serialize()
   {
      final Map<WicaChannel,WicaChannelMetadata> map = Map.of( WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "UnknownTypeChannel" ).build(), unkMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringTypeChannel" ).build(), strMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringArrayType" ).build(), strArrMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerTypeChannel" ).build(), intMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerArrayTypeChannel" ).build(), intArrMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealTypeChannel" ).build(), realMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealInfTypeChannel" ).build(), realInfMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealNanTypeChannel" ).build(), realNanMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealArrayTypeChannel" ).build(), realArrMetadata );

      final var serializer = new WicaChannelMetadataMapSerializerService("type;wsts;egu;prec;hopr;lopr;drvh;drvl;hihi;lolo;high;low", false );
      final String jsonStr = serializer.serialize( map );
      logger.info("JSON Metadata MAP serialisation like this: '{}'", JsonStringFormatter.prettyFormat(jsonStr ) );
   }

   @Test
   void test_serializeTypeOnly()
   {
      final Map<WicaChannel,WicaChannelMetadata> map = Map.of( WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "UnknownTypeChannel" ).build(), unkMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringTypeChannel" ).build(), strMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "StringArrayType" ).build(), strArrMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerTypeChannel" ).build(), intMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "IntegerArrayTypeChannel" ).build(), intArrMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealTypeChannel" ).build(), realMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealInfTypeChannel" ).build(), realInfMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealNanTypeChannel" ).build(), realNanMetadata,
                                                               WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "RealArrayTypeChannel" ).build(), realArrMetadata );

      final var serializer = new WicaChannelMetadataMapSerializerService("type", false );
      final String jsonStr = serializer.serialize( map );
      logger.info("JSON Metadata MAP serialisation like this: '{}'", JsonStringFormatter.prettyFormat(jsonStr ) );
   }

   @CsvSource( { "10000", "1", "10", "100", "1000", "1000", "1000", "10000", "10000", "10000" } )
   @ParameterizedTest
   void test_serializePerformance( int times )
   {
      final Map<WicaChannel, WicaChannelMetadata> map = Map.of( WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "unkMetadataChannel"     ).build(), unkMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "strMetadataChannel"     ).build(), strMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "strArrMetadataChannel"  ).build(), strArrMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "intMetadataChannel"     ).build(), intMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "intArrMetadataChannel"  ).build(), intArrMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "realMetadataChannel"    ).build(), realMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "realArrMetadataChannel" ).build(), realArrMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "realNanMetadataChannel" ).build(), realNanMetadata,
                                                                WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "realInfMetadataChannel" ).build(), realInfMetadata );

      final var serializer = new WicaChannelMetadataMapSerializerService("type;wsts;egu;prec;hopr;lopr;drvh;drvl;hihi;lolo;high;low", false );

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
