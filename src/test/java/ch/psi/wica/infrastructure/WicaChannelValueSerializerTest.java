/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelValueSerializerTest.class );

   private WicaChannelValue unconnValue;
   private WicaChannelValue intValue ;
   private WicaChannelValue strValue;
   private WicaChannelValue realValue;
   private WicaChannelValue realNanValue;
   private WicaChannelValue realInfValue;
   private WicaChannelValue intArrValue;
   private WicaChannelValue strArrValue;
   private WicaChannelValue realArrValue;

   private ObjectMapper jsonDecoder;

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
      jsonDecoder = new ObjectMapper();
      jsonDecoder.configure( JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS,true );
   }

   @Test
   void test_serializeValueUnconnected()
   {
      final var serializer = new WicaChannelValueSerializer(5 );
      logger.info("JSON Value UNCONNECTED serialisation like this: '{}'", serializer.serialize( unconnValue ) );
   }

   @Test
   void test_serializeValueString() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer(5 );
      final var jsonStr =  serializer.serialize( strValue );
      logger.info("JSON Value STRING serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.STRING, rootNode.get( "val" ).getNodeType() );
      assertEquals( "abcdef", rootNode.get( "val" ).asText() );
   }

   @Test
   void test_serializeValueStringArray() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer(5 );
      final var jsonStr =  serializer.serialize( strArrValue );
      logger.info("JSON Value STRING ARRAY serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.ARRAY, rootNode.get( "val" ).getNodeType() );
      assertEquals( 2, rootNode.get( "val" ).size());
      assertEquals( "abcdef", rootNode.get( "val" ).get( 0 ).asText() );
      assertEquals( "ghijkl", rootNode.get( "val" ).get( 1 ).asText() );
   }

   @Test
   void test_serializeValueInteger() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer(5 );
      final var jsonStr =  serializer.serialize( intValue );
      logger.info("JSON Value INTEGER serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.NUMBER, rootNode.get( "val" ).getNodeType() );
      assertEquals( 27, rootNode.get( "val" ).asInt() );
   }

   @Test
   void test_serializeValueIntegerArray() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer(5 );
      final var jsonStr =  serializer.serialize( intArrValue );
      logger.info("JSON Value INTEGER ARRAY serialisation like this: '{}'", serializer.serialize( intArrValue ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.ARRAY, rootNode.get( "val" ).getNodeType() );
      assertEquals( 2, rootNode.get( "val" ).size());
      assertEquals( 25, rootNode.get( "val" ).get( 0 ).asInt() );
      assertEquals( 12, rootNode.get( "val" ).get( 1 ).asInt() );
   }

   @Test
   void test_serializeValueReal() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer(6 );
      final var jsonStr =  serializer.serialize( realValue );
      logger.info("JSON Value REAL serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.NUMBER, rootNode.get( "val" ).getNodeType() );
      assertEquals( 123456.654321, rootNode.get( "val" ).asDouble() );
   }

   @Test
   void test_serializeValueRealArray() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer(5 );
      final var jsonStr =  serializer.serialize( realArrValue );
      logger.info("JSON Value REAL ARRAY serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.ARRAY, rootNode.get( "val" ).getNodeType() );
      assertEquals( 2, rootNode.get( "val" ).size());
      assertEquals( 2.5, rootNode.get( "val" ).get( 0 ).asDouble());
      assertEquals( 1.2, rootNode.get( "val" ).get( 1 ).asDouble() );
   }

   @Test
   void test_serializeValueRealIncludesNanSerializedAsNumber() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer( 5, false, false, "type", "val" );
      final var jsonStr =  serializer.serialize( realNanValue );
      logger.info("JSON Value REAL serialisation including NAN field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals(JsonNodeType.NUMBER, rootNode.get("val" ).getNodeType() );
      assertEquals( NaN, rootNode.get( "val" ).asDouble() );
   }

   @Test
   void test_serializeValueRealIncludesNanSerializedAsString() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer( 5, true, false, "type", "val" );
      final var jsonStr =  serializer.serialize( realNanValue );
      logger.info("JSON Value REAL serialisation including NAN field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( JsonNodeType.STRING, rootNode.get("val" ).getNodeType() );
      assertEquals( NaN, rootNode.get( "val" ).asDouble() );
   }

   @Test
   void test_serializeValueRealIncludesInfinitySerializesAsNumber() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer( 5, false, false, "type", "val" );
      final var jsonStr =  serializer.serialize( realInfValue );
      logger.info("JSON Value REAL serialisation including INFINITY field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( JsonNodeType.NUMBER, rootNode.get("val" ).getNodeType() );
      assertEquals( Double.POSITIVE_INFINITY, rootNode.get( "val" ).asDouble() );
   }

   @Test
   void test_serializeValueRealIncludesInfinitySerializesAsString() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer( 5, false, true, "type", "val" );
      final var jsonStr =  serializer.serialize( realInfValue );
      logger.info("JSON Value REAL serialisation including INFINITY field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( JsonNodeType.STRING, rootNode.get("val" ).getNodeType() );
      assertEquals( Double.POSITIVE_INFINITY, rootNode.get( "val" ).asDouble() );
   }

   @Test
   void test_serializeValueRealCheckFieldsOfInterestFeature() throws IOException
   {
      final var serializer = new WicaChannelValueSerializer( 5, false, false,"val", "sevr" );
      final var jsonStr =  serializer.serialize( realValue );
      logger.info("JSON Value REAL serialisation of VAL/SEVR fields looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertEquals( 2,  rootNode.size() );
      assertTrue( rootNode.has( "val") );
      assertTrue( rootNode.has( "sevr") );
   }

   @Test
   void test_serializeValueRealWithIllegalPrecision()
   {
      assertDoesNotThrow( () -> new WicaChannelValueSerializer(0 ));
      assertThrows( IllegalArgumentException.class, () -> new WicaChannelValueSerializer(-1 ));
   }

   @Test
   void test_serializeValueRealPrecisionTestsWithDifferentPrecisions() throws IOException
   {
      final var real = WicaChannelValue.createChannelValueConnected( 123456.654321 );

      final var num4Serializer = new WicaChannelValueSerializer( 4,"val" );
      final var jsonStr1 =  num4Serializer.serialize( real );
      logger.info("JSON Value REAL serialisation of ' 123456.654321' with 'numericScale=4' looks like this: '{}'", jsonStr1 );
      final JsonNode rootNode1 = jsonDecoder.readTree( jsonStr1 );
      assertTrue( rootNode1.isObject() );
      assertEquals( 1,  rootNode1.size() );
      assertTrue( rootNode1.has( "val") );
      assertEquals( 123456.6543, rootNode1.get( "val" ).asDouble() );

      final var num2Serializer = new WicaChannelValueSerializer( 2,"val" );
      final var jsonStr2 = num2Serializer.serialize( real );
      logger.info("JSON Value REAL serialisation of ' 123456.654321' with 'numericScale=2' looks like this: '{}'", jsonStr2 );
      final JsonNode rootNode2 = jsonDecoder.readTree( jsonStr2 );
      assertTrue( rootNode2.isObject() );
      assertEquals( 1,  rootNode2.size() );
      assertTrue( rootNode2.has( "val") );
      assertEquals( 123456.65, rootNode2.get( "val" ).asDouble() );
   }

//   @Test
//   void test_serializeValueMap()
//   {
//      final Map<WicaChannelName,WicaChannelValue> map = Map.of( WicaChannelName.of( "UnknownTypeChannel" ), unkValue,
//                                                                   WicaChannelName.of( "StringTypeChannel" ), strValue,
//                                                                   WicaChannelName.of( "StringArrayType" ), strArrValue,
//                                                                   WicaChannelName.of( "IntegerTypeChannel" ), intValue,
//                                                                   WicaChannelName.of( "IntegerArrayTypeChannel" ), intArrValue,
//                                                                   WicaChannelName.of( "RealTypeChannel" ), realValue,
//                                                                   WicaChannelName.of( "RealInfTypeChannel" ), realInfValue,
//                                                                   WicaChannelName.of( "RealNanTypeChannel" ), realNanValue,
//                                                                   WicaChannelName.of( "RealArrayTypeChannel" ), realArrValue );
//
//      final var serializer = new WicaChannelValueSerializer(5 );
//      logger.info("JSON Value MAP serialisation like this: '{}'", serializer.serialize( map ) );
//   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
