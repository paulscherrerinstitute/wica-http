/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.util.JsonStringFormatter;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelDataSerializerTestWithMetadata
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelDataSerializerTest.class );

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
      final var serializer = new WicaChannelDataSerializer( Set.of(),5, false );
      final var jsonStr =  serializer.writeToJson( unkMetadata );
      logger.info("JSON Metadata UNKNOWN serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat(jsonStr ) );
   }
   @Test
   void test_serializeMetadataString() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(),5, false );
      final var jsonStr =  serializer.writeToJson( strMetadata );
      logger.info("JSON Metadata STRING serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "STRING", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataStringArray() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(),5, false );
      final var jsonStr =  serializer.writeToJson( strArrMetadata );
      logger.info("JSON Metadata STRING ARRAY serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "STRING_ARRAY", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataInteger() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(),5, false );
      final var jsonStr =  serializer.writeToJson( intMetadata );
      logger.info("JSON Metadata INTEGER serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "INTEGER", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataIntegerArray() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(),5, false );
      final var jsonStr =  serializer.writeToJson( intArrMetadata );
      logger.info("JSON Metadata INTEGER ARRAY serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "INTEGER_ARRAY", rootNode.get( "type" ).textValue() );
   }

   @Test
   void test_serializeMetadataReal() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(),5, false );
      final var jsonStr =  serializer.writeToJson( realMetadata );
      logger.info("JSON Metadata REAL serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL", rootNode.get( "type" ).textValue() );
      assertEquals( "90.12346", rootNode.get( "hopr" ).asText() );
   }

   @Test
   void test_serializeMetadataRealArray() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(),5, false );
      final var jsonStr =  serializer.writeToJson( realArrMetadata );
      logger.info("JSON Metadata REAL ARRAY serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "type") );
      assertEquals( "REAL_ARRAY", rootNode.get( "type" ).textValue() );
      assertEquals( "100.12312", rootNode.get( "hopr" ).asText() );
   }

   @Test
   void test_serializeMetadataRealIncludesNanSerializedAsNumber() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of( "type", "hopr" ), 5, false );
      final var jsonStr =  serializer.writeToJson( realNanMetadata );
      logger.info("JSON Metadata REAL serialisation including NAN field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
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
      final var serializer = new WicaChannelDataSerializer( Set.of( "type", "hopr" ), 5, true );
      final var jsonStr =  serializer.writeToJson( realNanMetadata );
      logger.info("JSON Metadata REAL serialisation including NAN field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
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
      final var serializer = new WicaChannelDataSerializer( Set.of( "type", "hopr" ),5, false );
      final var jsonStr =  serializer.writeToJson( realInfMetadata );
      logger.info("JSON Metadata REAL serialisation including INFINITY field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
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
      final var serializer = new WicaChannelDataSerializer(Set.of( "type", "hopr" ),5, true );
      final var jsonStr =  serializer.writeToJson( realInfMetadata );
      logger.info("JSON Metadata REAL serialisation including INFINITY field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr )  );
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
      final var serializer = new WicaChannelDataSerializer( Set.of( "lopr", "hopr" ),5, false );
      final var jsonStr =  serializer.writeToJson( realMetadata );
      logger.info("JSON Metadata REAL serialisation of LOPR/HOPR fields looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertEquals( 2,  rootNode.size() );
      assertTrue( rootNode.has( "lopr") );
      assertTrue( rootNode.has( "hopr") );
   }

   @Test
   void test_serializeMetadataRealWithIllegalPrecision()
   {
      assertDoesNotThrow( () -> new WicaChannelDataSerializer( Set.of(), 0, false ) );
      assertThrows( IllegalArgumentException.class, () -> new WicaChannelDataSerializer( Set.of(), -1, false ) );
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithLargeIntegerPart()
   {
      final var real = WicaChannelMetadata.createRealArrayInstance( "units", 3, 123456789.123456, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=1' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),1, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=2' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),2, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=3' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),3, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=4' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),5, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=6' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),6, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=7' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),7, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=8' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),8, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '123456789.123456789' with 'numericScale=9' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),9, false ).writeToJson( real) ) );
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithLargeNumericScale()
   {
      final var real = WicaChannelMetadata.createRealArrayInstance( "units", 30, 0.000000000012345678901234567890123456789, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON Metadata REAL serialisation of '0.012345678901234567890123456789' with 'numericScale=30' looks like this: \n'{}'", new WicaChannelDataSerializer( Set.of( "hopr" ),30, false ).writeToJson( real) );
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithZeroIntegerPart()
   {
      final var real = WicaChannelMetadata.createRealArrayInstance( "units", 3, 0.000123456789, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=1' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),1, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=2' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),2, false).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=3' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),3, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=4' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),4, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=5' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),5, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=6' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),6, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=7' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),7, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=8' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),8, false ).writeToJson( real) ) );
      logger.info("JSON Metadata REAL serialisation of '0.000123456789' with 'numericScale=9' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( new WicaChannelDataSerializer( Set.of( "hopr" ),9, false ).writeToJson( real) ) );
   }

   @Test
   void test_serializeMetadataRealPrecisionTestsWithDifferentPrecisions() throws IOException
   {
      final var real = WicaChannelMetadata.createRealInstance( "units", 3, 0.123456, 0.0, 90.4, 10.6, 97.6, 2.2, 95.3, 5.1 );

      final var num4Serializer = new WicaChannelDataSerializer( Set.of( "hopr" ),4, false );
      final var jsonStr1 =  num4Serializer.writeToJson( real );
      logger.info("JSON Metadata REAL serialisation of '0.123456' with 'numericScale=4' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr1 ) );
      final JsonNode rootNode1 = jsonDecoder.readTree( jsonStr1 );
      assertTrue( rootNode1.isObject() );
      assertEquals( 1,  rootNode1.size() );
      assertTrue( rootNode1.has( "hopr") );
      assertEquals( "0.1235", rootNode1.get( "hopr" ).asText() );

      final var num2Serializer = new WicaChannelDataSerializer(  Set.of( "hopr" ),2, false );
      final var jsonStr2 =  num2Serializer.writeToJson( real );
      logger.info("JSON Metadata REAL serialisation of '0.123456' with 'numericScale=2' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr2 ) );
      final JsonNode rootNode2 = jsonDecoder.readTree( jsonStr2 );
      assertTrue( rootNode2.isObject() );
      assertEquals( 1,  rootNode2.size() );
      assertTrue( rootNode1.has( "hopr") );
      assertEquals( "0.12", rootNode2.get( "hopr" ).asText() );
   }


   @CsvSource( { "10000", "1", "10", "100", "1000", "1000", "1000", "10000", "10000", "10000" } )
   @ParameterizedTest
   void testPerformance( int times )
   {
      final var val = WicaChannelMetadata.createIntegerInstance( "egu", 10,0,10,0,10,0, 10, 0  );
      final StopWatch stopwatch = StopWatch.createStarted();
      for ( int i = 0 ; i < times; i++ )
      {
         final var serializer = new WicaChannelDataSerializer( Set.of( "lopr", "hopr" ), 4, true );
         final var res = serializer.writeToJson( val );
         assertThat( res, is( "{\"hopr\":10,\"lopr\":0}" ) );
      }
      final long elapsedTime = stopwatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Elapsed time for {} iterations was: {} ms", times, elapsedTime );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
