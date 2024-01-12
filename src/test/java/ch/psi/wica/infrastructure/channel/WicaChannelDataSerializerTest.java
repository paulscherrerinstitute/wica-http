/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.value.WicaChannelValueBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
      final var val = WicaChannelValueBuilder.createChannelValueDisconnected();
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"UNKNOWN\",\"wsts\":" + this.getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"conn\":false," ) );
      assertThat( res, containsString( "\"val\":null}" ));
   }

   @Test
   void testSerializeString()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedString( "abcd" );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"STRING\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"val\":\"abcd\"}" ));
   }

   @Test
   void testSerializeStringArray()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedStringArray( new String[] { "abcd", "efgh" } );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"STRING_ARRAY\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"val\":[\"abcd\",\"efgh\"]}" ));
   }

   @Test
   void testSerializeInteger()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedInteger( 24 );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"INTEGER\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"val\":24}" ));
   }

   @Test
   void testSerializeIntegerArray()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedIntegerArray( new int[] { 24, 25 } );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"INTEGER_ARRAY\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":"  + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"val\":[24,25]}" ));
   }

   @Test
   void testDateTimeSerialization() throws JsonProcessingException
   {
      final ObjectMapper mapper = JsonMapper.builder()
            // Turn off the feature whereby date/time values are written as timestamps.
            .configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false )
            .configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false )
            .build();

      // Register the module which enables java.time objects (including LocalDateTime) to be serialized.
      mapper.registerModule( new JavaTimeModule() );
      
      var res = mapper.writeValueAsString( LocalDateTime.now() );
      assertThat( res, containsString( getDateTimeNowAsTruncatedIso8601String() ) );
   }

   @Test
   void testSerializeReal()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedReal( 19.12345678 );

      final var res1 = WicaChannelDataSerializerBuilder.create().withNumericScale( 5 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res1, containsString( "{\"type\":\"REAL\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res1, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
      assertThat( res1, containsString( "\"val\":19.12346}" ));

      final var res2 = WicaChannelDataSerializerBuilder.create().withNumericScale( 2 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res2, containsString( "{\"type\":\"REAL\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res2, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res2, containsString( "\"val\":19.12}" ));
   }

   @Test
   void testSerializeRealInf()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedReal( Double.POSITIVE_INFINITY );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"REAL\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"val\":Infinity}" ));
   }

   @Test
   void testSerializeRealNan()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedReal( Double.NaN );
      final var res = wicaChannelDataSerializer.writeToJson(val );
      assertThat( res, containsString( "{\"type\":\"REAL\",\"wsts\":" ));
      assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res, containsString( "\"val\":NaN}" ));

   }

   @Test
   void testSerializeRealArray()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedRealArray( new double[] { 24.12345678, 25.12345678 } );

      final var res1 = WicaChannelDataSerializerBuilder.create().withNumericScale( 5 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res1, containsString( "{\"type\":\"REAL_ARRAY\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res1, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res1, containsString( "\"val\":[24.12346,25.12346]}" ));

      final var res2 = WicaChannelDataSerializerBuilder.create().withNumericScale( 1 ).withQuotedNumericStrings( false ).build().writeToJson( val );
      assertThat( res2, containsString( "{\"type\":\"REAL_ARRAY\",\"wsts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res2, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" + getDateTimeNowAsTruncatedIso8601String() ) );
      assertThat( res2, containsString( "\"val\":[24.1,25.1]}" ));
   }

   @Test
   void testSerializeRealArrayWithRestrictedFieldsOfInterest()
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedRealArray( new double[] { 24.12345678, 25.12345678 } );
      final var res = new WicaChannelDataSerializer( Set.of( "val", "sevr"), 3, false ).writeToJson(val );
      assertThat( res, is( "{\"sevr\":\"0\",\"val\":[24.123,25.123]}" ));
   }

   @CsvSource( { "10000", "1", "10", "100", "1000", "1000", "1000", "10000", "10000", "10000" } )
   @ParameterizedTest
   void testPerformance( int times )
   {
      final var val = WicaChannelValueBuilder.createChannelValueConnectedReal( 25.12345678 );
      final StopWatch stopwatch = StopWatch.createStarted();

      for ( int i = 0 ; i < times; i++ )
      {
         final var res = WicaChannelDataSerializerBuilder.create().withNumericScale( 4 ).withQuotedNumericStrings( false ).build().writeToJson( val );

         // Note: in contrast to the other tests in this class there is no attempt here to check that
         // the format of the 'ts' and 'wsts' fields is correct. This is because the test may well
         // straddle a one-second boundary where the serialized timestamps and the truncated
         // time may differ.
         assertThat( res, containsString( "{\"type\":\"REAL\",\"wsts\":" ) );
         assertThat( res, containsString( "\"conn\":true,\"stat\":0,\"sevr\":\"0\",\"ts\":" ) );
         assertThat( res, containsString("\"val\":25.1235}"));
      }
      final long elapsedTime = stopwatch.getTime(TimeUnit.MILLISECONDS );

      logger.info( "Elapsed time for {} iterations was: {} ms", times, elapsedTime );
   }

/*- Private methods ----------------------------------------------------------*/

   private String getDateTimeNowAsTruncatedIso8601String()
   {
      // Note: a more accurate timeAndDateNowString could be constructed by including seconds in the
      // matcher pattern. Thus: "yyyy-MM-dd'T'HH:mm:ss". But there is a higher chance this would result
      // in test failures if the tests were performed very close to the end of an second boundary.
      final LocalDateTime now = LocalDateTime.now();
      final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm";
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( DATETIME_FORMAT_PATTERN );
      final String formattedTimeAndDateNow = now.format( formatter );
      return "\""+ formattedTimeAndDateNow;
   }

/*- Nested Classes -----------------------------------------------------------*/

}

