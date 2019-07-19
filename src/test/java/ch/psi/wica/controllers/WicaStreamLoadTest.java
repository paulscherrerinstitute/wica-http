/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.apache.commons.lang3.time.StopWatch;

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

@SpringBootTest
@AutoConfigureMockMvc
class WicaStreamLoadTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamLoadTest.class );

   private final String wicaStreamUri = "https://gfa-wica.psi.ch";

   private static WicaStream lightStream = makeWicaStream( 1 );
   private static WicaStream heavyStream = makeWicaStream( 1000 );
   private static WicaStream proscanStream = makeProscanStream();

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   private static Stream<Arguments> getArgsForTestStreamCreateAndDeleteSingleThreadedThroughput()
   {
      return Stream.of( Arguments.of(   1, lightStream ),
                        Arguments.of(   1, lightStream ),
                        Arguments.of(   1, lightStream ),
                        Arguments.of(   2, lightStream ),
                        Arguments.of(   2, lightStream ),
                        Arguments.of(   2, lightStream ),
                        Arguments.of(   5, lightStream ),
                        Arguments.of(   5, lightStream ),
                        Arguments.of(   5, lightStream ),
                        Arguments.of(  10, lightStream ),
                        Arguments.of(  10, lightStream ),
                        Arguments.of(  10, lightStream ),
                        Arguments.of(  20, lightStream ),
                        Arguments.of(  20, lightStream ),
                        Arguments.of(  50, lightStream ),
                        Arguments.of(  50, lightStream ),
                        Arguments.of(  50, lightStream ),
                        Arguments.of( 100, lightStream ),
                        Arguments.of( 100, lightStream ),
                        Arguments.of( 100, lightStream ),
                        Arguments.of(   1, heavyStream ),
                        Arguments.of(   1, heavyStream ),
                        Arguments.of(   1, heavyStream ),
                        Arguments.of(   2, heavyStream ),
                        Arguments.of(   2, heavyStream ),
                        Arguments.of(   2, heavyStream ),
                        Arguments.of(   5, heavyStream ),
                        Arguments.of(   5, heavyStream ),
                        Arguments.of(   5, heavyStream ),
                        Arguments.of(  10, heavyStream ),
                        Arguments.of(  10, heavyStream ),
                        Arguments.of(  10, heavyStream ),
                        Arguments.of(  20, heavyStream ),
                        Arguments.of(  20, heavyStream ),
                        Arguments.of(  20, heavyStream ),
                        Arguments.of(  50, heavyStream ),
                        Arguments.of(  50, heavyStream ),
                        Arguments.of(  50, heavyStream ),
                        Arguments.of( 100, heavyStream ),
                        Arguments.of( 100, heavyStream ),
                        Arguments.of( 100, heavyStream ),
                        Arguments.of( 10, proscanStream ),
                        Arguments.of( 20, proscanStream ),
                        Arguments.of( 50, proscanStream ) );
   }

   @MethodSource( "getArgsForTestStreamCreateAndDeleteSingleThreadedThroughput" )
   @ParameterizedTest
   void testStreamCreateAndDeleteSingleThreadedThroughput( int iterations, WicaStream wicaStream )
   {
      logger.info( "Starting Single-Threaded Stream Create test..." );
      final StopWatch stopWatch = StopWatch.createStarted();
      final String firstResult = sendStreamCreateRequest( wicaStream );
      assertNotNull( firstResult );

      // Sequentially execute all the CREATE tasks.
      for ( int i = 1; i < iterations; i++ )
      {
         assertNotNull(sendStreamCreateRequest(wicaStream ) );
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
         assertNotNull(sendStreamDeleteRequest(String.valueOf(firstId + i ) ) );
      }
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Stream Delete Test completed {} iterations in {} ms. Throughput = {} requests/second.", iterations, deleteStreamCycleTime, (1000 * iterations ) / deleteStreamCycleTime );
   }


   private static Stream<Arguments> getArgsForTestStreamCreateAndDeleteMultiThreadedThroughput()
   {
      return Stream.of( Arguments.of(   1, lightStream  ),
                        Arguments.of(   2, lightStream  ),
                        Arguments.of(   5, lightStream  ),
                        Arguments.of(  10, lightStream  ),
                        Arguments.of(  20, lightStream  ),
                        Arguments.of(  50, lightStream  ),
                        Arguments.of( 100, lightStream  ),
                        Arguments.of(   1, heavyStream  ),
                        Arguments.of(   2, heavyStream  ),
                        Arguments.of(   5, heavyStream  ),
                        Arguments.of(  10, heavyStream  ),
                        Arguments.of(  20, heavyStream  ),
                        Arguments.of(  50, heavyStream  ),
                        Arguments.of( 100, heavyStream  ) );
   }

   @MethodSource( "getArgsForTestStreamCreateAndDeleteMultiThreadedThroughput" )
   @ParameterizedTest
   void testStreamCreateAndDeleteMultiThreadedThroughput( int iterations, WicaStream wicaStream ) throws InterruptedException, ExecutionException
   {
      logger.info( "Starting Multi-Threaded Stream Create test..." );
      final CompletionService<String> executor = new ExecutorCompletionService<>( Executors.newFixedThreadPool( 100 ) );
      final StopWatch stopWatch = StopWatch.createStarted();
      final String firstResult = sendStreamCreateRequest(wicaStream );
      assertNotNull( firstResult );

      // Submit all the CREATE tasks.
      for ( int i = 1; i < iterations; i++ )
      {
         executor.submit(() -> sendStreamCreateRequest(wicaStream ));
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
         executor.submit( () -> sendStreamDeleteRequest(id ) );
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
      return Stream.of( Arguments.of( 30, 5000, lightStream ) );
   }

   @MethodSource( "getArgsForTestGetSingleThreadedThroughput" )
   @ParameterizedTest
   void testGetSingleThreadedThroughput( int numberOfStreams, int applyLoadForDurationInMillis, WicaStream wicaStream ) throws InterruptedException
   {
      logger.info( "Starting Single-Threaded Stream Create test..." );
      final StopWatch stopWatch = StopWatch.createStarted();
      final String firstResult = sendStreamCreateRequest( wicaStream );
      assertNotNull( firstResult );

      // Sequentially execute all the CREATE tasks.
      for ( int i = 1; i < numberOfStreams; i++ )
      {
         assertNotNull( sendStreamCreateRequest(wicaStream ) );
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
         var f = sendStreamSubscribeRequest(String.valueOf(firstId + i ) )
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
         assertNotNull(sendStreamDeleteRequest(String.valueOf(firstId + i ) ) );
      }
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MILLISECONDS );
      logger.info( "Stream Delete Test completed {} iterations in {} ms. Throughput = {} requests/second.", numberOfStreams, deleteStreamCycleTime, (1000 * numberOfStreams ) / deleteStreamCycleTime );
   }


/*- Private methods ----------------------------------------------------------*/

   private static WicaStream makeProscanStream()
   {
      WicaStreamProperties wicaStreamProperties = WicaStreamProperties.createBuilder()
            .withHeartbeatFluxInterval( 15000 )
            .withChangedValueFluxInterval( 100 )
            .withPolledValueFluxInterval( 1000 )
            .withDataAcquisitionMode( WicaChannelProperties.DataAcquisitionMode.MONITOR )
            .withPolledValueSamplingRatio( 1 )
            .withNumericPrecision(6)
            .withFieldsOfInterest("val;sevr").build();

      return WicaStream.createBuilder()
            .withStreamProperties( wicaStreamProperties )
            .withChannelName("XPROSCAN:TIME:2" )
            .withChannelName("XPROREG:STAB:1" )
            .withChannelName("EMJCYV:STA3:2" )
            .withChannelName("EMJCYV:CTRL:1" )
            .withChannelName("MMAV6:IST:2" )
            .withChannelName("MMAC3:STR:2" )
            .withChannelNameAndProperties("EMJCYV:STAW:1", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).build() )
            .withChannelNameAndProperties("EMJCYV:IST:2", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).build() )
            .withChannelName("MMAC:SOL:2" )
            .withChannelName("DMAD1:IST:2" )
            .withChannelNameAndProperties("PRO:REG2D:Y:2", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelNameAndProperties("PRO:REG2D:X:2", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelNameAndProperties("PRO:REG2D:X", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelNameAndProperties("PRO:REG2D:Y", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelName("AMAKI1:IST:2" )
            .withChannelName("CMJSEV:PWRF:2" )
            .withChannelName("CMJLL:SOLA:2" )
            .withChannelName("EMJEC1V:IST:2" )
            .withChannelName("EMJEC2V:IST:2" )
            .withChannelName("AMJHS-I:IADC:2" )
            .withChannelName("MMJF:IST:2" )
            .withChannelName("IMJV:IST:2" )
            .withChannelName("IMJI:IST:2" )
            .withChannelName("IMJGF:IST:2" )
            .withChannelName("XPROIONS:IST1:2" )
            .withChannelName("QMA1:IST:2" )
            .withChannelName("QMA2:IST:2" )
            .withChannelName("QMA3:IST:2" )
            .withChannelName("SMJ1X:IST:2" )
            .withChannelName("SMJ2Y:IST:2" )
            .withChannelName("SMA1X:IST:2" )
            .withChannelName("SMA1Y:IST:2" )
            .withChannelName("FMJEP:IST:2" )
            .withChannelName("MMJP2:IST1:2" )
            .withChannelName("FMJEPI:POS:2" )
            .withChannelName("FMJIP:IST:2" )
            .withChannelName("MMJP2:IST2:2" )
            .withChannelName("FMJIPI:POS:2" )
            .withChannelName("FMJIPI:BREI:2" )
            .withChannelName("PRO:CURRENTALARM:1" )
            .withChannelNameAndProperties("MMAC3:STR:2##2", WicaChannelProperties.createBuilder()
                  .withDataAcquisitionMode( WicaChannelProperties.DataAcquisitionMode.POLL_AND_MONITOR )
                  .withFieldsOfInterest( "val;ts" )
                  .withFilterType(WicaChannelProperties.FilterType.CHANGE_FILTERER )
                  .withFilterDeadband( 5 ).build() )
            .withChannelNameAndProperties("EMJCYV:IST:2##2", WicaChannelProperties.createBuilder()
                                        .withDataAcquisitionMode( WicaChannelProperties.DataAcquisitionMode.POLL_AND_MONITOR )
                                        .withFieldsOfInterest( "val;ts" )
                                        .withFilterType(WicaChannelProperties.FilterType.ONE_IN_M )
                                        .withFilterCycleLength( 5 ).build() )
            .withChannelNameAndProperties("CMJSEV:PWRF:2##2", WicaChannelProperties.createBuilder()
                                        .withDataAcquisitionMode( WicaChannelProperties.DataAcquisitionMode.POLL_AND_MONITOR )
                                        .withFieldsOfInterest( "val;ts" )
                                        .withFilterType(WicaChannelProperties.FilterType.ONE_IN_M )
                                        .withFilterCycleLength( 5 ).build() )
            .withChannelName("BMA1:STA:2" )
            .withChannelName("BMA1:STAR:2##1" )
            .withChannelName("BMA1:STAR:2##2" )
            .withChannelName("BMA1:STAP:2##1" )
            .withChannelName("BMA1:STAP:2##2" )
            .withChannelName("BME1:STA:2" )
            .withChannelName("BME1:STAR:2##1" )
            .withChannelName("BME1:STAR:2##2" )
            .withChannelName("BME1:STAP:2##1" )
            .withChannelName("BME1:STAP:2##2" )
            .withChannelName("BMB1:STA:2" )
            .withChannelName("BMB1:STAR:2##1" )
            .withChannelName("BMB1:STAR:2##2" )
            .withChannelName("BMB1:STAP:2##1" )
            .withChannelName("BMB1:STAP:2##2" )
            .withChannelName("BMC1:STA:2" )
            .withChannelName("BMC1:STAR:2##1" )
            .withChannelName("BMC1:STAR:2##2" )
            .withChannelName("BMC1:STAP:2##1" )
            .withChannelName("BMC1:STAP:2##2" )
            .withChannelName("BMD1:STA:2" )
            .withChannelName("BMD1:STAR:2##1" )
            .withChannelName("BMD1:STAR:2##2" )
            .withChannelName("BMD2:STA:2" )
            .withChannelName("BMD2:STAR:2##1" )
            .withChannelName("BMD2:STAR:2##2" )
            .withChannelName("BMD2:STAP:2##1" )
            .withChannelName("BMD2:STAP:2##2" )
            .withChannelNameAndProperties("MMAP5X:PROF:2:P", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelNameAndProperties("MMAP5X:PROF:2", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelName("MMAP5X:SPB:2" )
            .withChannelNameAndProperties("MMAP6Y:PROF:2:P", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelNameAndProperties("MMAP6Y:PROF:2", WicaChannelProperties.createBuilder().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelName("MMAP6Y:SPB:2" )
            .withChannelName("YMJCS1K:IST:2" )
            .withChannelName("YMJCS2K:IST:2" )
            .withChannelName("YMJHH:SPARB:2" )
            .withChannelName("YMJHH:STR:2" )
            .withChannelName("YMJHL:IST:2" )
            .withChannelName("YMJHG:IST:2" )
            .withChannelName("YMJKKRT:IST:2" )
            .withChannelName("RPS-IQ:STA:1" )
            .withChannelName("UMJSSB:BIQX:1" )
            .withChannelName("RPS-HFRD:STA:1" )
            .withChannelName("UMJSSB:BHRX:1" )
            .withChannelName("RPS-HF:STA:1" )
            .withChannelName("UMJSSB:BHFX:1" )
            .withChannelName("MJSSB:BDEX:1" )
            .withChannelName("XPROSCAN:STAB:2" )
            .build();
   }


   private static WicaStream makeWicaStream( int numberOfChannels )
   {
      WicaStreamProperties wicaStreamProperties = WicaStreamProperties.createBuilder()
            .withHeartbeatFluxInterval( 15000 )
            .withChangedValueFluxInterval( 100 )
            .withPolledValueFluxInterval( 1000 )
            .withDataAcquisitionMode(WicaChannelProperties.DataAcquisitionMode.MONITOR)
            .withPolledValueSamplingRatio(1)
            .withNumericPrecision(6)
            .withFieldsOfInterest("val;sevr").build();

      WicaStream.Builder wicaStreamBuilder = WicaStream.createBuilder()
            .withStreamProperties( wicaStreamProperties );

      for ( int i =0; i < numberOfChannels; i++ )
      {
         wicaStreamBuilder = wicaStreamBuilder.withChannelName("CHAN-" + i  );
      }

      return wicaStreamBuilder.build();
   }

   private String sendStreamCreateRequest( WicaStream wicaStream )
   {
      logger.trace( "Creating new Stream..." );

      final String jsonStreamConfiguration = wicaStream.toJsonString();
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

   private String sendStreamDeleteRequest( String streamId  )
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

   private Flux<ServerSentEvent<String>> sendStreamSubscribeRequest( String streamId )
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
