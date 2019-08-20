/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelDataSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelDataSerializerTest.class );
   private WicaChannelDataSerializer wicaChannelDataSerializer;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      this.wicaChannelDataSerializer = WicaChannelDataSerializerBuilder.create()
         .withNumericScale( 6 )
         .withQuotedNumericStrings( false ).build();
   }

   @Test
   void testSerializeDisconnected()
   {
      final var val = WicaChannelValue.createChannelValueDisconnected();
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"UNKNOWN\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":false," ) );
      assertThat( res, containsString( "\"val\":null}" ));
   }

   @Test
   void testSerializeString()
   {
      final var val = WicaChannelValue.createChannelValueConnected( "abcd" );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"STRING\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res, containsString( "\"val\":\"abcd\"}" ));
   }

   @Test
   void testSerializeStringArray()
   {
      final var val = WicaChannelValue.createChannelValueConnected( new String[] { "abcd", "efgh" } );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"STRING_ARRAY\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res, containsString( "\"val\":[\"abcd\",\"efgh\"]}" ));
   }

   @Test
   void testSerializeInteger()
   {
      final var val = WicaChannelValue.createChannelValueConnected( 24 );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"INTEGER\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res, containsString( "\"val\":24}" ));
   }

   @Test
   void testSerializeIntegerArray()
   {
      final var val = WicaChannelValue.createChannelValueConnected( new int[] { 24, 25 } );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"INTEGER_ARRAY\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res, containsString( "\"val\":[24,25]}" ));
   }

   @Test
   void testSerializeReal()
   {
      final var val = WicaChannelValue.createChannelValueConnected( 19.12345678 );

      final var res1 = WicaChannelDataSerializerBuilder.create().withNumericScale( 5 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res1, containsString( "{\"type\":\"REAL\",\"wsts\":" ));
      assertThat( res1, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res1, containsString( "\"val\":19.12346}" ));

      final var res2 = WicaChannelDataSerializerBuilder.create().withNumericScale( 2 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res2, containsString( "{\"type\":\"REAL\",\"wsts\":" ));
      assertThat( res2, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res2, containsString( "\"val\":19.12}" ));
   }

   @Test
   void testSerializeRealInf()
   {
      final var val = WicaChannelValue.createChannelValueConnected( Double.POSITIVE_INFINITY );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"REAL\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res, containsString( "\"val\":Infinity}" ));
   }

   @Test
   void testSerializeRealNan()
   {
      final var val = WicaChannelValue.createChannelValueConnected( Double.NaN );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"REAL\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res, containsString( "\"val\":NaN}" ));

   }

   @Test
   void testSerializeRealArray()
   {
      final var val = WicaChannelValue.createChannelValueConnected( new double[] { 24.12345678, 25.12345678 } );

      final var res1 = WicaChannelDataSerializerBuilder.create().withNumericScale( 5 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res1, containsString( "{\"type\":\"REAL_ARRAY\",\"wsts\":" ));
      assertThat( res1, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res1, containsString( "\"val\":[24.12346,25.12346]}" ));

      final var res2 = WicaChannelDataSerializerBuilder.create().withNumericScale( 1 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res2, containsString( "{\"type\":\"REAL_ARRAY\",\"wsts\":" ));
      assertThat( res2, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res2, containsString( "\"val\":[24.1,25.1]}" ));
   }

   @Test
   void testSerializeRealArrayWithRestrictedFieldsOfInterest()
   {
      final var val = WicaChannelValue.createChannelValueConnected( new double[] { 24.12345678, 25.12345678 } );
      final var res = new WicaChannelDataSerializer( Set.of( "val", "sevr"), 3, false ).writeToJson(val );
      assertThat( res, is( "{\"sevr\":\"0\",\"val\":[24.123,25.123]}" ));
   }

   @CsvSource( { "10000", "1", "10", "100", "1000", "1000", "1000", "10000", "10000", "10000" } )
   @ParameterizedTest
   void testPerformance( int times )
   {
      final var val = WicaChannelValue.createChannelValueConnected( 25.12345678 );
      final StopWatch stopwatch = StopWatch.createStarted();

      for ( int i = 0 ; i < times; i++ )
      {
         final var res = WicaChannelDataSerializerBuilder.create().withNumericScale( 4 ).withQuotedNumericStrings( false ).build().writeToJson( val );
         assertThat( res, containsString( "{\"type\":\"REAL\",\"wsts\":" ));
         assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
         assertThat( res, containsString("\"val\":25.1235}"));
      }
      final long elapsedTime = stopwatch.getTime(TimeUnit.MILLISECONDS );

      logger.info( "Elapsed time for {} iterations was: {} ms", times, elapsedTime );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
