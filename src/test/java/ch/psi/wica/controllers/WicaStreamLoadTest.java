/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
class WicaStreamLoadTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamLoadTest.class );

   private final String wicaStreamUri = "https://gfa-wica.psi.ch";

   private static final String lightConsumerConfig = "{ \"channels\": [ { \"name\" : \"MMAC3:STR:2\" } ] }";

   // The PROSCAN Status Display may be typical of the type of consumer that will
   // use the wica service. This consumer has 94 channels-of-interest.
   private static final String typicalConsumerConfig =
      "{\"channels\":[" +
         "{\"name\":\"XPROSCAN:TIME:2\"}," +
         "{\"name\":\"XPROREG:STAB:1\"}," +
         "{\"name\":\"EMJCYV:STA3:2\"}," +
         "{\"name\":\"EMJCYV:CTRL:1\"}," +
         "{\"name\":\"MMAV6:IST:2\"}," +
         "{\"name\":\"MMAC3:STR:2\"}," +
         "{\"name\":\"EMJCYV:STAW:1\",\"props\":{\"fields\":\"val\"}}," +
         "{\"name\":\"EMJCYV:IST:2\",\"props\":{\"fields\":\"val\"}}," +
         "{\"name\":\"MMAC:SOL:2\"}," +
         "{\"name\":\"DMAD1:IST:2\"}," +
         "{\"name\":\"PRO:REG2D:Y:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"PRO:REG2D:X:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"PRO:REG2D:X\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"PRO:REG2D:Y\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"AMAKI1:IST:2\"}," +
         "{\"name\":\"CMJSEV:PWRF:2\"}," +
         "{\"name\":\"CMJLL:SOLA:2\"},{\"name\":\"EMJEC1V:IST:2\"}," +
         "{\"name\":\"EMJEC2V:IST:2\"}," +
         "{\"name\":\"AMJHS-I:IADC:2\"}," +
         "{\"name\":\"MMJF:IST:2\"}," +
         "{\"name\":\"IMJV:IST:2\"}," +
         "{\"name\":\"IMJI:IST:2\"}," +
         "{\"name\":\"IMJGF:IST:2\"}," +
         "{\"name\":\"XPROIONS:IST1:2\"}," +
         "{\"name\":\"QMA1:IST:2\"}," +
         "{\"name\":\"QMA2:IST:2\"}," +
         "{\"name\":\"QMA3:IST:2\"}," +
         "{\"name\":\"SMJ1X:IST:2\"}," +
         "{\"name\":\"SMJ2Y:IST:2\"}," +
         "{\"name\":\"SMA1X:IST:2\"}," +
         "{\"name\":\"SMA1Y:IST:2\"}," +
         "{\"name\":\"FMJEP:IST:2\"}," +
         "{\"name\":\"MMJP2:IST1:2\"}," +
         "{\"name\":\"FMJEPI:POS:2\"}," +
         "{\"name\":\"FMJEPI:BREI:2\"}," +
         "{\"name\":\"FMJIP:IST:2\"}," +
         "{\"name\":\"MMJP2:IST2:2\"}," +
         "{\"name\":\"FMJIPI:POS:2\"}," +
         "{\"name\":\"FMJIPI:BREI:2\"}," +
         "{\"name\":\"PRO:CURRENTALARM:1\"}," +
         "{\"name\":\"MMAC3:STR:2##2\",\"props\":{\"daqmode\":\"poll-and-monitor\",\"fields\":\"val;ts\",\"filter\":\"changes\",\"deadband\":5}}," +
         "{\"name\":\"EMJCYV:IST:2##2\",\"props\":{\"daqmode\":\"poll-and-monitor\",\"fields\":\"val;ts\",\"filter\":\"one-in-m\",\"m\":5}}," +
         "{\"name\":\"CMJSEV:PWRF:2##2\",\"props\":{\"daqmode\":\"poll-and-monitor\",\"fields\":\"val;ts\",\"filter\":\"one-in-m\",\"m\":5}}," +
         "{\"name\":\"BMA1:STA:2\"}," +
         "{\"name\":\"BMA1:STAR:2##1\"}," +
         "{\"name\":\"BMA1:STAR:2##2\"}," +
         "{\"name\":\"BMA1:STAP:2##1\"}," +
         "{\"name\":\"BMA1:STAP:2##2\"}," +
         "{\"name\":\"BME1:STA:2\"}," +
         "{\"name\":\"BME1:STAR:2##1\"}," +
         "{\"name\":\"BME1:STAR:2##2\"}," +
         "{\"name\":\"BME1:STAP:2##1\"}," +
         "{\"name\":\"BME1:STAP:2##2\"}," +
         "{\"name\":\"BMB1:STA:2\"}," +
         "{\"name\":\"BMB1:STAR:2##1\"}," +
         "{\"name\":\"BMB1:STAR:2##2\"}," +
         "{\"name\":\"BMB1:STAP:2##1\"}," +
         "{\"name\":\"BMB1:STAP:2##2\"}," +
         "{\"name\":\"BMC1:STA:2\"}," +
         "{\"name\":\"BMC1:STAR:2##1\"}," +
         "{\"name\":\"BMC1:STAR:2##2\"}," +
         "{\"name\":\"BMC1:STAP:2##1\"}," +
         "{\"name\":\"BMC1:STAP:2##2\"}," +
         "{\"name\":\"BMD1:STA:2\"}," +
         "{\"name\":\"BMD1:STAR:2##1\"}," +
         "{\"name\":\"BMD1:STAR:2##2\"}," +
         "{\"name\":\"BMD2:STA:2\"}," +
         "{\"name\":\"BMD2:STAR:2##1\"}," +
         "{\"name\":\"BMD2:STAR:2##2\"}," +
         "{\"name\":\"BMD2:STAP:2##1\"}," +
         "{\"name\":\"BMD2:STAP:2##2\"}," +
         "{\"name\":\"MMAP5X:PROF:2:P\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"MMAP5X:PROF:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"MMAP5X:SPB:2\"}," +
         "{\"name\":\"MMAP6Y:PROF:2:P\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"MMAP6Y:PROF:2\",\"props\":{\"fields\":\"val\",\"prec\":2}}," +
         "{\"name\":\"MMAP6Y:SPB:2\"}," +
         "{\"name\":\"YMJCS1K:IST:2\"}," +
         "{\"name\":\"YMJCS2K:IST:2\"}," +
         "{\"name\":\"YMJHH:SPARB:2\"}," +
         "{\"name\":\"YMJHH:STR:2\"}," +
         "{\"name\":\"YMJHL:IST:2\"}," +
         "{\"name\":\"YMJHG:IST:2\"}," +
         "{\"name\":\"YMJKKRT:IST:2\"}," +
         "{\"name\":\"RPS-IQ:STA:1\"}," +
         "{\"name\":\"UMJSSB:BIQX:1\"}," +
         "{\"name\":\"RPS-HFRD:STA:1\"}," +
         "{\"name\":\"UMJSSB:BHRX:1\"}," +
         "{\"name\":\"RPS-HF:STA:1\"}," +
         "{\"name\":\"UMJSSB:BHFX:1\"}," +
         "{\"name\":\"UMJSSB:BDEX:1\"}," +
         "{\"name\":\"XPROSCAN:STAB:2\"}" +
      "],\"props\":{" +
         "\"heartbeat\":15000," +
         "\"changeint\":100," +
         "\"pollint\":1000," +
         "\"daqmode\":" +
         "\"monitor\"," +
         "\"pollratio\":1," +
         "\"prec\":6," +
         "\"fields\":" +
         "\"val;sevr\"}" +
   "}";

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   private static Stream<Arguments> getArgsForTestStreamCreateAndDeleteSingleThreadedThroughput()
   {
      return Stream.of( Arguments.of(   1, lightConsumerConfig   ),
                        Arguments.of(   2, lightConsumerConfig   ),
                        Arguments.of(   5, lightConsumerConfig   ),
                        Arguments.of(  10, lightConsumerConfig   ),
                        Arguments.of(  20, lightConsumerConfig   ),
                        Arguments.of(  50, lightConsumerConfig   ),
                        Arguments.of( 100, lightConsumerConfig   ),
                        Arguments.of(   1, typicalConsumerConfig ),
                        Arguments.of(   2, typicalConsumerConfig ),
                        Arguments.of(   5, typicalConsumerConfig ),
                        Arguments.of(  10, typicalConsumerConfig ),
                        Arguments.of(  20, typicalConsumerConfig ),
                        Arguments.of(  50, typicalConsumerConfig ),
                        Arguments.of( 100, typicalConsumerConfig ) );
   }

   @MethodSource( "getArgsForTestStreamCreateAndDeleteSingleThreadedThroughput" )
   @ParameterizedTest
   void testStreamCreateAndDeleteSingleThreadedThroughput( int iterations, String jsonStreamConfig )
   {
      logger.info( "Starting Single-Threaded Stream Create test..." );
      final StopWatch stopWatch = StopWatch.createStarted();
      final String firstResult = createStream( jsonStreamConfig );
      assertNotNull( firstResult );
      // Sequentially execute all the CREATE tasks.
      for ( int i = 1; i < iterations; i++ )
      {
         assertNotNull( createStream( jsonStreamConfig ) );
      }
      final long createStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Stream Create test completed {} iterations in {} ms. Throughput = {} requests/second.", iterations, createStreamCycleTime, (1000 * iterations ) / createStreamCycleTime );

      logger.info( "Starting Single-Threaded Stream Delete test..." );
      int firstId = Integer.parseInt( firstResult );
      stopWatch.reset();
      stopWatch.start();
      // Sequentially execute all the DELETE tasks.
      for ( int i = 0; i < iterations; i++ )
      {
         assertNotNull( deleteStream( String.valueOf( firstId + i ) ) );
      }
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Stream Delete Test completed {} iterations in {} ms. Throughput = {} requests/second.", iterations, deleteStreamCycleTime, (1000 * iterations ) / deleteStreamCycleTime );
   }

   private static Stream<Arguments> getArgsForTestStreamCreateAndDeleteMultiThreadedThroughput()
   {
      return Stream.of( Arguments.of(   1, lightConsumerConfig   ),
                        Arguments.of(   2, lightConsumerConfig   ),
                        Arguments.of(   5, lightConsumerConfig   ),
                        Arguments.of(  10, lightConsumerConfig   ),
                        Arguments.of(  20, lightConsumerConfig   ),
                        Arguments.of(  50, lightConsumerConfig   ),
                        Arguments.of( 100, lightConsumerConfig   ),
                        Arguments.of(   1, typicalConsumerConfig ),
                        Arguments.of(   2, typicalConsumerConfig ),
                        Arguments.of(   5, typicalConsumerConfig ),
                        Arguments.of(  10, typicalConsumerConfig ),
                        Arguments.of(  20, typicalConsumerConfig ),
                        Arguments.of(  50, typicalConsumerConfig ),
                        Arguments.of( 100, typicalConsumerConfig ) );
   }

   @MethodSource( "getArgsForTestStreamCreateAndDeleteMultiThreadedThroughput" )
   @ParameterizedTest
   void testStreamCreateAndDeleteMultiThreadedThroughput( int iterations, String jsonStreamConfig ) throws InterruptedException, ExecutionException
   {
      logger.info( "Starting Multi-Threaded Stream Create test..." );
      final CompletionService<String> executor = new ExecutorCompletionService<>( Executors.newFixedThreadPool( 100 ) );
      final StopWatch stopWatch = StopWatch.createStarted();
      final String firstResult = createStream( jsonStreamConfig );
      assertNotNull( firstResult );

      // Submit all the CREATE tasks.
      for ( int i = 1; i < iterations; i++ )
      {
         executor.submit(() -> createStream( jsonStreamConfig ));
      }

      // Get all the results and check that each CREATE operation succeeded.
      for ( int counter = 1; counter < iterations; counter++ )
      {
         final String result = executor.take().get();
         assertNotNull( result );
      }
      final long createStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Multi-Threaded Stream Create test completed {} iterations in {} ms. Throughput = {} requests/second.", iterations, createStreamCycleTime, (1000 * iterations ) / createStreamCycleTime );

      logger.info( "Starting Multi-Threaded Stream Delete test..." );
      int firstId = Integer.parseInt( firstResult );

      Thread.sleep( 100 );

      stopWatch.reset();
      stopWatch.start();
      // Submit all the DELETE tasks.
      for ( int i = 0; i < iterations; i++ )
      {
         final String id = String.valueOf( firstId + i );
         executor.submit( () -> deleteStream( id ) );
      }

      // Get all the results and check that each DELETE operation succeeded.
      for ( int i = 0; i < iterations; i++ )
      {
         final String result = executor.take().get();
         assertNotNull( result );
      }
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Multi-Threaded Stream Delete test completed {} iterations in {} ms. Throughput = {} requests/second.", iterations, deleteStreamCycleTime, (1000 * iterations ) / deleteStreamCycleTime );
   }

   private static Stream<Arguments> getArgsForTestGetSingleThreadedThroughput()
   {
      return Stream.of( Arguments.of( 30, 5000, lightConsumerConfig ) );
   }

   @MethodSource( "getArgsForTestGetSingleThreadedThroughput" )
   @ParameterizedTest
   void testGetSingleThreadedThroughput( int numberOfStreams, int applyLoadForDurationInMillis, String jsonStreamConfig ) throws InterruptedException
   {
      logger.info( "Starting Single-Threaded Stream Create test..." );
      final StopWatch stopWatch = StopWatch.createStarted();
      final String firstResult = createStream( jsonStreamConfig );
      assertNotNull( firstResult );

      // Sequentially execute all the CREATE tasks.
      for ( int i = 1; i < numberOfStreams; i++ )
      {
         assertNotNull( createStream( jsonStreamConfig ) );
      }
      final long createStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Stream Create test completed {} iterations in {} ms. Throughput = {} requests/second.", numberOfStreams, createStreamCycleTime, (1000 * numberOfStreams ) / createStreamCycleTime );

      logger.info( "Starting Single-Threaded Stream Get test..." );
      int firstId = Integer.parseInt( firstResult );
      stopWatch.reset();
      stopWatch.start();

      List<Disposable> disposables = new ArrayList<>();

      // Sequentially execute all the GET tasks.
      for ( int i = 0; i < numberOfStreams; i++ )
      {
         var f = getStreamFlux( String.valueOf( firstId + i ) )
                 .doOnCancel(() -> logger.info( "FLUX CANCELLED !!" ) )
                 .subscribe( (sse) -> logger.trace( "Received SSE from stream with id: {}, event: {}",sse.id(), sse.event()  ),
                               (e) -> logger.error( "FLUX ERROR: {} ", e.toString() ) );
         disposables.add( f );
      }

      final long getStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Stream Get Test completed {} iterations in {} ms. Throughput = {} requests/second. ", numberOfStreams, getStreamCycleTime, (1000 * numberOfStreams ) / getStreamCycleTime );

      logger.info( "Pausing for {} ms.", applyLoadForDurationInMillis );
      Thread.sleep( applyLoadForDurationInMillis );

      logger.info( "Disposing subscribers..." );
      // Sequentially execute all the DISPOSE tasks.
      for ( int i = 0; i < numberOfStreams; i++ )
      {
         disposables.get( i ).dispose();
      }
      logger.info( "OK - diposed subscribers..." );

      logger.info( "Starting Single-Threaded Stream Delete test..." );
      stopWatch.reset();
      stopWatch.start();

      // Sequentially execute all the DELETE tasks.
      for ( int i = 0; i < numberOfStreams; i++ )
      {
         assertNotNull( deleteStream( String.valueOf( firstId + i ) ) );
      }
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Stream Delete Test completed {} iterations in {} ms. Throughput = {} requests/second.", numberOfStreams, deleteStreamCycleTime, (1000 * numberOfStreams ) / deleteStreamCycleTime );
   }


/*- Private methods ----------------------------------------------------------*/

   private String createStream( String jsonStreamConfiguration )
   {
      logger.trace( "Creating new Stream..." );
      final ClientResponse postResponse = WebClient.create(wicaStreamUri + "/ca/streams")
            .post()
            .body(BodyInserters.fromObject( jsonStreamConfiguration ) )
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .block();

      if ( postResponse == null )
      {
         logger.warn( "ERROR - Stream NOT Created." );
         return null;
      }
      else
      {
         final String streamId = postResponse.bodyToMono( String.class ).block();
         logger.trace( "OK - Stream Created with id {}.", streamId );
         return streamId;
      }
   }

   private String deleteStream( String streamId  )
   {
      logger.trace( "Deleting Stream with id {} ...", streamId );
      final ClientResponse  deleteResponse = WebClient.create( wicaStreamUri )
            .delete()
            .uri("/ca/streams/" + streamId)
            .exchange()
            .block();

      if ( deleteResponse == null )
      {
         logger.warn( "ERROR - Stream NOT Created." );
         return null;
      }
      else
      {
         logger.trace( "OK - Stream with id {} was deleted.", streamId );
         return streamId;
      }
   }

   private Flux<ServerSentEvent<String>> getStreamFlux( String streamId )
   {
      final var parameterizedTypeRef = new ParameterizedTypeReference<ServerSentEvent<String>>() {};
      return WebClient.create( wicaStreamUri)
            .get()
            .uri("/ca/streams/" + streamId)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux( parameterizedTypeRef );
   }


/*- Nested Classes -----------------------------------------------------------*/

}
