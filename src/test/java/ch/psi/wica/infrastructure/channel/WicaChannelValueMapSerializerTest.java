/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.util.JsonStringFormatter;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;



/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

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
      final Map<WicaChannel,List<WicaChannelValue>> map = Map.of( WicaChannel.createFromName("UnconnChannel"), List.of(unconnValue ),
                                                                  WicaChannel.createFromName("StringTypeChannel" ), List.of(strValue ),
                                                                  WicaChannel.createFromName("StringArrayType" ), List.of(strArrValue ),
                                                                  WicaChannel.createFromName("IntegerTypeChannel" ), List.of(intValue ),
                                                                  WicaChannel.createFromName("IntegerArrayTypeChannel" ), List.of(intArrValue ),
                                                                  WicaChannel.createFromName("RealTypeChannel" ), List.of(realValue ),
                                                                  WicaChannel.createFromName("RealInfTypeChannel" ), List.of(realInfValue ),
                                                                  WicaChannel.createFromName("RealNanTypeChannel" ), List.of(realNanValue ),
                                                                  WicaChannel.createFromName("RealArrayTypeChannel" ), List.of(realArrValue  ) );

      final var serializer = new WicaChannelValueMapSerializer( false );
      final String jsonStr = serializer.serialize( map );
      logger.info("JSON Value MAP serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat(jsonStr ) );
   }

/*- Private methods ----------------------------------------------------------*/


/*- Nested Classes -----------------------------------------------------------*/

}
