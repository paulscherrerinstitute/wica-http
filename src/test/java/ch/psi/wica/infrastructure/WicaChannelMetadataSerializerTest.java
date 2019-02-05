/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
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

import static org.junit.jupiter.api.Assertions.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelMetadataSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelMetadataSerializerTest.class );

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
      jsonDecoder.configure( JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS,true );
   }

   @Test
   void test_serializeMetadataUnknown()
   {
      final var serializer = new WicaChannelMetadataSerializer(5 );
      logger.info("JSON Metadata UNKNOWN serialisation like this: '{}'", serializer.serialize( unkMetadata ) );
   }
   @Test
   void test_serializeMetadataString() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer(5 );
      final var jsonStr =  serializer.serialize( strMetadata );
      logger.info("JSON Metadata STRING serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "STRING", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataStringArray() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer(5 );
      final var jsonStr =  serializer.serialize( strArrMetadata );
      logger.info("JSON Metadata STRING ARRAY serialisation like this: '{}'", serializer.serialize( strArrMetadata ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "STRING_ARRAY", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataInteger() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer(5 );
      final var jsonStr =  serializer.serialize( intMetadata );
      logger.info("JSON Metadata INTEGER serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "INTEGER", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataIntegerArray() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer(5 );
      final var jsonStr =  serializer.serialize( intArrMetadata );
      logger.info("JSON Metadata INTEGER ARRAY serialisation like this: '{}'", serializer.serialize( intArrMetadata ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "INTEGER_ARRAY", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataReal() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer(5 );
      final var jsonStr =  serializer.serialize( realMetadata );
      logger.info("JSON Metadata REAL serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( "90.12346", rootNode.get( "hopr" ).asText() );
   }

   @Test
   void test_serializeMetadataRealArray() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer(5 );
      final var jsonStr =  serializer.serialize( realArrMetadata );
      logger.info("JSON Metadata REAL ARRAY serialisation like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL_ARRAY", rootNode.get( "type" ).textValue() );
      assertEquals( "100.12312", rootNode.get( "hopr" ).asText() );
   }

   @Test
   void test_serializeMetadataRealIncludesNanSerializedAsNumber() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer( 5, false, false, "type", "hopr" );
      final var jsonStr =  serializer.serialize( realNanMetadata );
      logger.info("JSON Metadata REAL serialisation including NAN field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals(JsonNodeType.NUMBER, rootNode.get("hopr" ).getNodeType() );
      assertEquals( Double.NaN, rootNode.get( "hopr" ).asDouble() );
   }

   @Test
   void test_serializeMetadataRealIncludesNanSerializedAsString() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer( 5, true, false, "type", "hopr" );
      final var jsonStr =  serializer.serialize( realNanMetadata );
      logger.info("JSON Metadata REAL serialisation including NAN field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( JsonNodeType.STRING, rootNode.get("hopr" ).getNodeType() );
      assertEquals( Double.NaN, rootNode.get( "hopr" ).asDouble() );
   }

   @Test
   void test_serializeMetadataRealIncludesInfinitySerializesAsNumber() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer( 5, false, false, "type", "hopr" );
      final var jsonStr =  serializer.serialize( realInfMetadata );
      logger.info("JSON Metadata REAL serialisation including INFINITY field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( JsonNodeType.NUMBER, rootNode.get("hopr" ).getNodeType() );
      assertEquals( Double.POSITIVE_INFINITY, rootNode.get( "hopr" ).asDouble() );
   }

   @Test
   void test_serializeMetadataRealIncludesInfinitySerializesAsString() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer( 5, false, true, "type", "hopr" );
      final var jsonStr =  serializer.serialize( realInfMetadata );
      logger.info("JSON Metadata REAL serialisation including INFINITY field looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( JsonNodeType.STRING, rootNode.get("hopr" ).getNodeType() );
      assertEquals( Double.POSITIVE_INFINITY, rootNode.get( "hopr" ).asDouble() );
   }

   @Test
   void test_serializeMetadataRealCheckFieldsOfInterestFeature() throws IOException
   {
      final var serializer = new WicaChannelMetadataSerializer( 5, false, false,"lopr", "hopr" );
      final var jsonStr =  serializer.serialize( realMetadata );
      logger.info("JSON Metadata REAL serialisation of LOPR/HOPR fields looks like this: '{}'", jsonStr );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertEquals( 2,  rootNode.size() );
      assertTrue( rootNode.has( "lopr") );
      assertTrue( rootNode.has( "hopr") );
   }

   @Test
   void test_serializeMetadataRealWithIllegalPrecision()
   {
      assertDoesNotThrow( () -> new WicaChannelMetadataSerializer(0 ));
      assertThrows( IllegalArgumentException.class, () -> new WicaChannelMetadataSerializer(-1 ));
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithLargeIntegerPart()
   {
      final var real = WicaChannelMetadata.createRealArrayInstance( "units", 3, 123456789.123456, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=1' looks like this: '{}'", new WicaChannelMetadataSerializer( 1, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=2' looks like this: '{}'", new WicaChannelMetadataSerializer( 2, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=3' looks like this: '{}'", new WicaChannelMetadataSerializer( 3, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=4' looks like this: '{}'", new WicaChannelMetadataSerializer( 4, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=5' looks like this: '{}'", new WicaChannelMetadataSerializer( 5, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=6' looks like this: '{}'", new WicaChannelMetadataSerializer( 6, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=7' looks like this: '{}'", new WicaChannelMetadataSerializer( 7, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=8' looks like this: '{}'", new WicaChannelMetadataSerializer( 8, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=9' looks like this: '{}'", new WicaChannelMetadataSerializer( 9, "hopr" ).serialize( real) );
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithLargeNumericScale()
   {
      final var real = WicaChannelMetadata.createRealArrayInstance( "units", 30, 0.000000000012345678901234567890123456789, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON Metadata REAL serialisation of '0.012345678901234567890123456789' with 'numericScale=30' looks like this: '{}'", new WicaChannelMetadataSerializer( 30, "hopr" ).serialize( real) );
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithZeroIntegerPart()
   {
      final var real = WicaChannelMetadata.createRealArrayInstance( "units", 3, 0.000123456789, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=1' looks like this: '{}'", new WicaChannelMetadataSerializer( 1, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=2' looks like this: '{}'", new WicaChannelMetadataSerializer( 2, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=3' looks like this: '{}'", new WicaChannelMetadataSerializer( 3, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=4' looks like this: '{}'", new WicaChannelMetadataSerializer( 4, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=5' looks like this: '{}'", new WicaChannelMetadataSerializer( 5, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=6' looks like this: '{}'", new WicaChannelMetadataSerializer( 6, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=7' looks like this: '{}'", new WicaChannelMetadataSerializer( 7, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=8' looks like this: '{}'", new WicaChannelMetadataSerializer( 8, "hopr" ).serialize( real) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=9' looks like this: '{}'", new WicaChannelMetadataSerializer( 9, "hopr" ).serialize( real) );
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithDifferentPrecisions() throws IOException
   {
      final var real = WicaChannelMetadata.createRealInstance( "units", 3, 0.123456, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );

      final var num4Serializer = new WicaChannelMetadataSerializer( 4,"hopr" );
      final var jsonStr1 =  num4Serializer.serialize( real );
      logger.info("JSON Metadata REAL serialisation of '0.123456' with 'numericScale=4' looks like this: '{}'", jsonStr1 );
      final JsonNode rootNode1 = jsonDecoder.readTree( jsonStr1 );
      assertTrue( rootNode1.isObject() );
      assertEquals( 1,  rootNode1.size() );
      assertTrue( rootNode1.has( "hopr") );
      assertEquals( "0.1235", rootNode1.get( "hopr" ).asText() );

      final var num2Serializer = new WicaChannelMetadataSerializer( 2,"hopr" );
      final var jsonStr2 =  num2Serializer.serialize( real );
      logger.info("JSON Metadata REAL serialisation of '0.123456' with 'numericScale=2' looks like this: '{}'", jsonStr2 );
      final JsonNode rootNode2 = jsonDecoder.readTree( jsonStr2 );
      assertTrue( rootNode2.isObject() );
      assertEquals( 1,  rootNode2.size() );
      assertTrue( rootNode1.has( "hopr") );
      assertEquals( "0.12", rootNode2.get( "hopr" ).asText() );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
