/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.util.JsonStringFormatter;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import ch.psi.wica.model.channel.value.WicaChannelValueBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelDataSerializerChannelValuesTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelDataSerializerChannelValuesTest.class );

   private WicaChannelValue unconnValue;
   private WicaChannelValue intValue;
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
      unconnValue = WicaChannelValueBuilder.createChannelValueDisconnected();
      intValue = WicaChannelValueBuilder.createChannelValueConnectedInteger( 27 );
      strValue = WicaChannelValueBuilder.createChannelValueConnectedString( "abcdef" );
      realValue = WicaChannelValueBuilder.createChannelValueConnectedReal( 123456.6543212345 );
      realNanValue = WicaChannelValueBuilder.createChannelValueConnectedReal( NaN );
      realInfValue = WicaChannelValueBuilder.createChannelValueConnectedReal( POSITIVE_INFINITY );
      intArrValue = WicaChannelValueBuilder.createChannelValueConnectedIntegerArray( new int[] { 25, 12 } );
      strArrValue = WicaChannelValueBuilder.createChannelValueConnectedStringArray( new String[] { "abcdef", "ghijkl" } );
      realArrValue = WicaChannelValueBuilder.createChannelValueConnectedRealArray( new double[] { 1.23456789012345, 9.87654321012345, Double.NaN }  );

      // Set up decoder
      jsonDecoder = JsonMapper.builder().configure( JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true ).build();
   }

   @Test
   void test_serializeValueUnconnected()
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(), 5, false );
      final var jsonStr =  serializer.writeToJson( unconnValue );
      logger.info( "JSON Value UNCONNECTED serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat(jsonStr ) );
   }

   @Test
   void test_serializeValueString() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(), 5, false );
      final var jsonStr =  serializer.writeToJson( strValue );
      logger.info( "JSON Value STRING serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.STRING, rootNode.get( "val" ).getNodeType() );
      assertEquals( "abcdef", rootNode.get( "val" ).asText() );
   }

   @Test
   void test_serializeValueStringArray() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(), 5, false );
      final var jsonStr =  serializer.writeToJson( strArrValue );
      logger.info( "JSON Value STRING ARRAY serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
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
      final var serializer = new WicaChannelDataSerializer( Set.of(), 5, false );
      final var jsonStr =  serializer.writeToJson( intValue );
      logger.info("JSON Value INTEGER serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.NUMBER, rootNode.get( "val" ).getNodeType() );
      assertEquals( 27, rootNode.get( "val" ).asInt() );
   }

   @Test
   void test_serializeValueIntegerArray() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(), 5, false );
      final var jsonStr =  serializer.writeToJson( intArrValue );
      logger.info( "JSON Value INTEGER ARRAY serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr )  );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
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
      final var serializer = new WicaChannelDataSerializer( Set.of(), 3, false );
      final var jsonStr =  serializer.writeToJson( realValue );
      logger.info( "JSON Value REAL serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree( jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.NUMBER, rootNode.get( "val" ).getNodeType() );
      assertEquals( 123456.654, rootNode.get( "val" ).asDouble() );
   }

   @Test
   void test_serializeValueRealArray() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of(), 4, false );
      final var jsonStr =  serializer.writeToJson( realArrValue );
      logger.info( "JSON Value REAL ARRAY serialisation like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertTrue( rootNode.has( "val") );
      assertEquals( JsonNodeType.ARRAY, rootNode.get( "val" ).getNodeType() );
      assertEquals( 3, rootNode.get( "val" ).size());
      assertEquals( 1.2346, rootNode.get( "val" ).get( 0 ).asDouble());
      assertEquals( 9.8765, rootNode.get( "val" ).get( 1 ).asDouble() );
      assertEquals(JsonNodeType.NUMBER, rootNode.get("val" ).get(2).getNodeType() );
      assertEquals( NaN, rootNode.get( "val" ).get(2).asDouble() );
   }

   @Test
   void test_serializeValueRealIncludesNanSerializedAsNumber() throws IOException
   {
      final var serializer = new WicaChannelDataSerializer( Set.of("type", "val" ),5, false );
      final var jsonStr =  serializer.writeToJson( realNanValue );
      logger.info("JSON Value REAL serialisation including NAN field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
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
      final var serializer = new WicaChannelDataSerializer( Set.of( "type", "val" ), 5, true );
      final var jsonStr =  serializer.writeToJson( realNanValue );
      logger.info("JSON Value REAL serialisation including NAN field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
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
      final var serializer = new WicaChannelDataSerializer( Set.of( "type", "val" ), 5, false );
      final var jsonStr =  serializer.writeToJson( realInfValue );
      logger.info("JSON Value REAL serialisation including INFINITY field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr ) );
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
      final var serializer = new WicaChannelDataSerializer( Set.of("type", "val" ), 5, true );
      final var jsonStr =  serializer.writeToJson( realInfValue );
      logger.info("JSON Value REAL serialisation including INFINITY field looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr )  );
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
      final var serializer = new WicaChannelDataSerializer( Set.of( "sevr", "val" ),5, false );
      final var jsonStr =  serializer.writeToJson( realValue );
      logger.info("JSON Value REAL serialisation of VAL/SEVR fields looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr )  );
      final JsonNode rootNode = jsonDecoder.readTree(jsonStr );
      assertTrue( rootNode.isObject() );
      assertEquals( 2,  rootNode.size() );
      assertTrue( rootNode.has( "val") );
      assertTrue( rootNode.has( "sevr") );
   }

   @Test
   void test_serializeValueRealWithIllegalPrecision()
   {
      assertDoesNotThrow( () -> new WicaChannelDataSerializer( Set.of(), 0, false ));
      assertThrows( IllegalArgumentException.class, () -> new WicaChannelDataSerializer( Set.of(), -1, false ));
   }

   @Test
   void test_serializeValueRealPrecisionTestsWithDifferentPrecisions() throws IOException
   {
      final var real = WicaChannelValueBuilder.createChannelValueConnectedReal( 123456.654321 );

      final var num4Serializer = new WicaChannelDataSerializer( Set.of( "val" ), 4,false );
      final var jsonStr1 =  num4Serializer.writeToJson( real );
      logger.info("JSON Value REAL serialisation of '123456.654321' with 'numericScale=4' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr1 )  );
      final JsonNode rootNode1 = jsonDecoder.readTree( jsonStr1 );
      assertTrue( rootNode1.isObject() );
      assertEquals( 1,  rootNode1.size() );
      assertTrue( rootNode1.has( "val") );
      assertEquals( 123456.6543, rootNode1.get( "val" ).asDouble() );

      final var num2Serializer = new WicaChannelDataSerializer(  Set.of( "val" ), 2,false );

      final var jsonStr2 = num2Serializer.writeToJson( real );
      logger.info("JSON Value REAL serialisation of '123456.654321' with 'numericScale=2' looks like this: \n'{}'", JsonStringFormatter.prettyFormat( jsonStr2 )  );
      final JsonNode rootNode2 = jsonDecoder.readTree( jsonStr2 );
      assertTrue( rootNode2.isObject() );
      assertEquals( 1,  rootNode2.size() );
      assertTrue( rootNode2.has( "val") );
      assertEquals( 123456.65, rootNode2.get( "val" ).asDouble() );
   }

   @CsvSource( { "10000", "1", "10", "100", "1000", "1000", "1000", "10000", "10000", "10000" } )
   @ParameterizedTest
   void testPerformance( int times )
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedReal( 25.12345678 );
      final StopWatch stopwatch = StopWatch.createStarted();
      for ( int i = 0 ; i < times; i++ )
      {
         final var serializer = new WicaChannelDataSerializer( Set.of( "type", "val" ), 4, true );
         final var res = serializer.writeToJson( val );
         assertThat( res, is( "{\"type\":\"REAL\",\"val\":25.1235}" ) );
      }
      final long elapsedTime = stopwatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Elapsed time for {} iterations was: {} ms", times, elapsedTime );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
