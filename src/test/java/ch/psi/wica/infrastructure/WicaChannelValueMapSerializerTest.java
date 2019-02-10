/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;



/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueMapSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelValueMapSerializerTest.class );

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
      realArrValue = WicaChannelValue.WicaChannelValueConnected.createChannelValueConnected( new double[] { 2.5, 1.2 }  );

      // Set up decoder
      final ObjectMapper jsonDecoder = new ObjectMapper();
      jsonDecoder.configure( JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true );
   }

   @Test
   void test_serialize()
   {
      final Map<WicaChannelName,List<WicaChannelValue>> map = Map.of( WicaChannelName.of( "UnconnChannel" ), List.of( unconnValue ),
                                                                      WicaChannelName.of( "StringTypeChannel" ),  List.of( strValue ),
                                                                      WicaChannelName.of( "StringArrayType" ),  List.of( strArrValue ),
                                                                      WicaChannelName.of( "IntegerTypeChannel" ),  List.of( intValue ),
                                                                      WicaChannelName.of( "IntegerArrayTypeChannel" ),  List.of( intArrValue ),
                                                                      WicaChannelName.of( "RealTypeChannel" ),  List.of( realValue ),
                                                                      WicaChannelName.of( "RealInfTypeChannel" ),  List.of( realInfValue ),
                                                                      WicaChannelName.of( "RealNanTypeChannel" ),  List.of( realNanValue ),
                                                                      WicaChannelName.of( "RealArrayTypeChannel" ),  List.of( realArrValue  ) );

      final var serializer = new WicaChannelValueMapSerializer( (c) -> Set.of( "val" ), (c) -> 5, false );
      final String jsonStr = serializer.serialize( map );
      logger.info("JSON Value MAP serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
   }

/*- Private methods ----------------------------------------------------------*/


/*- Nested Classes -----------------------------------------------------------*/

}
