/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaObjectToJsonSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaObjectToJsonSerializerTest.class );

   private WicaObjectToJsonSerializer wicaObjectToJsonSerializer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setUp()
   {
      wicaObjectToJsonSerializer = new WicaObjectToJsonSerializer( Set.of("val" ) );
   }

   @Test
   void testValueSerialisation()
   {
      final WicaChannelValue disconnValue = WicaChannelValue.createChannelValueDisconnected();
      logger.info("JSON DISCONNECTED Value serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueToJsonRepresentation( disconnValue ) );

      final WicaChannelValue strValue = WicaChannelValue.createChannelValueConnected( "abc" );
      logger.info("JSON STRING Value serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueToJsonRepresentation(strValue ) );

      final WicaChannelValue intValue = WicaChannelValue.createChannelValueConnected ( 123 );
      logger.info("JSON INTEGER Value serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueToJsonRepresentation(intValue ) );

      final WicaChannelValue dblValue = WicaChannelValue.createChannelValueConnected( 123.794 );
      logger.info("JSON REAL Value serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueToJsonRepresentation(dblValue ) );

      final WicaChannelValue strArrayValue = WicaChannelValue.createChannelValueConnected( new String[] { "abc", "def" } );
      logger.info("JSON STRING Value serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueToJsonRepresentation(strArrayValue ) );

      final int[] intArr = { 123, 456 };
      final WicaChannelValue intArrayValue = WicaChannelValue.createChannelValueConnected( intArr );
      logger.info("JSON INTEGER Value serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueToJsonRepresentation(intArrayValue ) );

      final double[] dblArr = { 123.0, 456.0 };
      final WicaChannelValue dblArrayValue = WicaChannelValue.createChannelValueConnected( dblArr );
      logger.info("JSON REAL Value serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueToJsonRepresentation(dblArrayValue ) );
   }

   @Test
   void testMetadataSerialisation()
   {
      final WicaChannelMetadata unkMetadata = WicaChannelMetadata.createUnknownInstance();
      logger.info("JSON UNKNOWN Metadata serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(unkMetadata ) );

      final WicaChannelMetadata strMetadata = WicaChannelMetadata.createStringInstance();
      logger.info("JSON STRING Metadata serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(strMetadata ) );

      final WicaChannelMetadata strArrMetadata = WicaChannelMetadata.createStringArrayInstance();
      logger.info("JSON STRING ARRAY Metadata serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(strArrMetadata ) );

      final WicaChannelMetadata intMetadata = WicaChannelMetadata.createIntegerInstance( "units", 100, 0, 90, 10, 98, 2, 95, 5 );
      logger.info("JSON INT Metadata serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(intMetadata ) );

      final WicaChannelMetadata intArrMetadata = WicaChannelMetadata.createIntegerInstance( "units", 100, 0, 90, 10, 98, 2, 95, 5 );
      logger.info("JSON INT ARRAY Metadata serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(intArrMetadata ) );

      final WicaChannelMetadata dblMetadata = WicaChannelMetadata.createRealInstance( "units", 3, 100.0, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON REAL Metadata serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(dblMetadata ) );

      final WicaChannelMetadata dblArrMetadata= WicaChannelMetadata.createRealArrayInstance( "units", 3, 100.0, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON REAL ARRAY Metadata serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(dblArrMetadata ) );

      final WicaChannelMetadata dblMetadata2 = WicaChannelMetadata.createRealInstance( "units", 3, Double.NaN, 0.0, Double.POSITIVE_INFINITY, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON REAL Metadata serialisation with Nan/Infinity looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMetadataToJsonRepresentation(dblMetadata2 ) );
   }


   @Test
   void testChannelValueListSerialisation1()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );

      final List<WicaChannelValue> list = List.of( strValue1, strValue2 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueListToJsonRepresentation( list ));
   }

   @Test
   void testChannelValueListSerialisation2()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( 56.78 );

      final List<WicaChannelValue> list = List.of( strValue1, strValue2 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueListToJsonRepresentation( list ));
   }

   @Test
   void testChannelValueListSerialisation3()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( 56 );

      final List<WicaChannelValue> list = List.of( strValue1, strValue2 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueListToJsonRepresentation( list ));
   }

   @Test
   void testChannelValueListSerialisation4()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( 56 );
      final List<WicaChannelValue> list = List.of( strValue1, strValue2, strValue3 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueListToJsonRepresentation( list ));
   }



   @Test
   void testChannelMapSerialisation1()
   {
      final Map<WicaChannelName,Map<String,String>> map = new HashMap<WicaChannelName, Map<String, String>>();
      map.put( WicaChannelName.of( "chan1:abc" ), Map.of( "strProp", "strProp", "intProp", "459" ) );
      logger.info("JSON Map serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelMapToJsonRepresentation(map ) );

   }

   @Test
   void testChannelValueMapSerialisation2()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );

      final Map<WicaChannelName,List<WicaChannelValue>> map = Map.of( WicaChannelName.of( "chan1:abc" ), List.of( strValue1, strValue2 ) );
      logger.info("JSON List serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueListToJsonRepresentation( map ) );
   }

   @Test
   void testChannelValueMapSerialisation3()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );

      final Map<WicaChannelName,List<WicaChannelValue>> map = Map.of( WicaChannelName.of( "chan1" ), List.of( strValue1, strValue2 ),
                                                                      WicaChannelName.of( "chan2" ), List.of( strValue1, strValue2, strValue1 ));
      logger.info("JSON List serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueListToJsonRepresentation( map ) );
   }

   @Test
   void testChannelValueMapSerialisation4()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );

      final Map<WicaChannelName,List<WicaChannelValue>> map = Map.of( WicaChannelName.of( "chan1" ), List.of( strValue1, strValue2 ),
                                                                      WicaChannelName.of( "chan1" ), List.of( strValue1, strValue2, strValue1 ));
      logger.info("JSON List serialisation looks like this: '{}'", wicaObjectToJsonSerializer.convertWicaChannelValueListToJsonRepresentation( map ) );
   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
