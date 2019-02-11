/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;



/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelMetadataMapSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelMetadataMapSerializerTest.class );

   private WicaChannelMetadata unkMetadata;
   private WicaChannelMetadata strMetadata ;
   private WicaChannelMetadata strArrMetadata;
   private WicaChannelMetadata intMetadata;
   private WicaChannelMetadata intArrMetadata;
   private WicaChannelMetadata realMetadata;
   private WicaChannelMetadata realArrMetadata;
   private WicaChannelMetadata realNanMetadata;
   private WicaChannelMetadata realInfMetadata;

   private ObjectMapper jsonDecoder;

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

      // Set up decoder
      jsonDecoder = new ObjectMapper();
      jsonDecoder.configure( JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true );
   }

   @Test
   void test_serialize()
   {
      final Map<WicaChannelName,WicaChannelMetadata> map = Map.of( WicaChannelName.of( "UnknownTypeChannel" ), unkMetadata,
                                                                   WicaChannelName.of( "StringTypeChannel" ), strMetadata,
                                                                   WicaChannelName.of( "StringArrayType" ), strArrMetadata,
                                                                   WicaChannelName.of( "IntegerTypeChannel" ), intMetadata,
                                                                   WicaChannelName.of( "IntegerArrayTypeChannel" ), intArrMetadata,
                                                                   WicaChannelName.of( "RealTypeChannel" ), realMetadata,
                                                                   WicaChannelName.of( "RealInfTypeChannel" ), realInfMetadata,
                                                                   WicaChannelName.of( "RealNanTypeChannel" ), realNanMetadata,
                                                                   WicaChannelName.of( "RealArrayTypeChannel" ), realArrMetadata );

      final var serializer = new WicaChannelMetadataMapSerializer( c -> Set.of(), c -> 5,  false );
      final String jsonStr = serializer.serialize( map );
      logger.info("JSON Metadata MAP serialisation like this: '{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
