/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.stream.WicaStreamSerializer;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.infrastructure.stream.WicaStreamBuilder;
import ch.psi.wica.model.stream.WicaStreamProperties;
import ch.psi.wica.infrastructure.stream.WicaStreamPropertiesBuilder;
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
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

// By default these tests are suppressed as they would create problems in the automatic
// build system. They test should be enabled as required during pre-production testing.
@Disabled
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
@AutoConfigureMockMvc
class WicaStreamLoadTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamLoadTest.class );

   private final String wicaStreamUri = "http://localhost:8080";
   //private final String wicaStreamUri = "https://gfa-wica.psi.ch";

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
         assertNotNull( sendStreamCreateRequest( wicaStream ) );
      }
      final long createStreamCycleTime = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Stream Create test completed {} iterations in {} us. Throughput = {} requests/second.", iterations, createStreamCycleTime, (1_000_000 * iterations ) / createStreamCycleTime );

      logger.info( "Starting Single-Threaded Stream Delete test..." );
      int firstId = Integer.parseInt( firstResult );
      stopWatch.reset();
      stopWatch.start();

      // Sequentially execute all the DELETE tasks.
      for ( int i = 0; i < iterations; i++ )
      {
         assertNotNull(sendStreamDeleteRequest(String.valueOf(firstId + i ) ) );
      }
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Stream Delete Test completed {} iterations in {} us. Throughput = {} requests/second.", iterations, deleteStreamCycleTime, (1_000_000 * iterations ) / deleteStreamCycleTime );
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
      final String firstResult = sendStreamCreateRequest( wicaStream );
      assertNotNull( firstResult );

      // Submit all the CREATE tasks.
      for ( int i = 1; i < iterations; i++ )
      {
         executor.submit(() -> sendStreamCreateRequest (wicaStream ));
      }

      // Get all the results and check that each CREATE operation succeeded.
      for ( int counter = 1; counter < iterations; counter++ )
      {
         final String result = executor.take().get();
         assertNotNull( result );
      }
      final long createStreamCycleTime = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Multi-Threaded Stream Create test completed {} iterations in {} us. Throughput = {} requests/second.", iterations, createStreamCycleTime, (1_000_000 * iterations ) / createStreamCycleTime );

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
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Multi-Threaded Stream Delete test completed {} iterations in {} us. Throughput = {} requests/second.", iterations, deleteStreamCycleTime, (1_000_000 * iterations ) / deleteStreamCycleTime );
   }

   private static Stream<Arguments> getArgsForTestGetSingleThreadedThroughput()
   {
      return Stream.of( Arguments.of( 1, 10_000, proscanStream ),
                        Arguments.of( 1, 10_000, proscanStream ),
                        Arguments.of( 1, 10_000, proscanStream ),
                        Arguments.of( 10, 10_000, proscanStream ),
                        Arguments.of( 10, 10_000, proscanStream ),
                        Arguments.of( 10, 10_000, proscanStream ) );
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
         assertNotNull( sendStreamCreateRequest( wicaStream ) );
      }
      final long createStreamCycleTime = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Stream Create test peformed POST operate on {} streams in {} us. Throughput = {} requests/second.", numberOfStreams, createStreamCycleTime, (1_000_000 * numberOfStreams ) / createStreamCycleTime );

      logger.info( "Starting Single-Threaded Stream Get test..." );
      int firstId = Integer.parseInt( firstResult );
      stopWatch.reset();
      stopWatch.start();

      List<Disposable> disposables = new ArrayList<>();

      // Sequentially execute all the GET tasks.
      final AtomicInteger eventCounter = new AtomicInteger();
      final AtomicInteger byteCounter = new AtomicInteger();
      for ( int i = 0; i < numberOfStreams; i++ )
      {
         var f = sendStreamSubscribeRequest(String.valueOf(firstId + i ) )
                 .doOnCancel(() -> logger.info( "FLUX CANCELLED !!" ) )
                 .subscribe( (sse) -> {
                    logger.trace( "Received SSE from stream with id: {}, event: {}",sse.id(), sse.event() );
                    eventCounter.incrementAndGet();
                    Optional.ofNullable( sse.data() ).ifPresent( (x) -> byteCounter.accumulateAndGet( x.length(), Integer::sum ) );
                 },
                   (e) -> logger.error( "FLUX ERROR: {} ", e.toString() ) );
         disposables.add( f );
      }

      final long getStreamCycleTime = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Stream Get Test performed GET operation on {} streams in {} us. Throughput = {} requests/second. ", numberOfStreams, getStreamCycleTime, (1_000_000 * numberOfStreams ) / getStreamCycleTime );

      logger.info( "Pausing for {} ms.", applyLoadForDurationInMillis );
      Thread.sleep( applyLoadForDurationInMillis );

      logger.info( "Event counter received {} events on all channels", eventCounter.get() );
      logger.info( "Event data payload was {} unicode characters on all channels", byteCounter.get() );

      logger.info( "Disposing subscribers..." );
      // Sequentially execute all the DISPOSE tasks.
      for ( int i = 0; i < numberOfStreams; i++ )
      {
         disposables.get( i ).dispose();
      }
      logger.info( "OK - disposed subscribers..." );

      // This sleep is an attempt to workaround a problem with the reactor
      // netty library which generates the following exception:
      // reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response
      Thread.sleep( 500 );

      logger.info( "Starting Single-Threaded Stream Delete test..." );
      stopWatch.reset();
      stopWatch.start();

      // Sequentially execute all the DELETE tasks.
      for ( int i = 0; i < numberOfStreams; i++ )
      {
         assertNotNull( sendStreamDeleteRequest( String.valueOf(firstId + i ) ) );
      }
      final long deleteStreamCycleTime = stopWatch.getTime(TimeUnit.MICROSECONDS );
      logger.info( "Stream Delete Test performed DELETE operation on {} streams in {} us. Throughput = {} requests/second.", numberOfStreams, deleteStreamCycleTime, (1_000_000 * numberOfStreams ) / deleteStreamCycleTime );
   }


/*- Private methods ----------------------------------------------------------*/

   private static WicaStream makeProscanStream()
   {
      WicaStreamProperties wicaStreamProperties = WicaStreamPropertiesBuilder.create()
            .withHeartbeatFluxInterval( 15000 )
            .withMetadataFluxInterval( 100 )
            .withMonitoredValueFluxInterval(100 )
            .withPolledValueFluxInterval( 1000 )
            .withDataAcquisitionMode( WicaDataAcquisitionMode.MONITOR )
            .withPollingIntervalInMillis( 1000 )
            .withNumericPrecision(6)
            .withFieldsOfInterest( "val,sevr" )
            .withFilterType(WicaFilterType.LAST_N )
            .withFilterNumSamples( 1 )
            .build();

      return WicaStreamBuilder.create()
            .withStreamProperties( wicaStreamProperties )
            .withChannelNameAndStreamProperties("XPROSCAN:TIME:2" )
            .withChannelNameAndStreamProperties("XPROREG:STAB:1" )
            .withChannelNameAndStreamProperties("EMJCYV:STA3:2" )
            .withChannelNameAndStreamProperties("EMJCYV:CTRL:1" )
            .withChannelNameAndStreamProperties("MMAV6:IST:2" )
            .withChannelNameAndStreamProperties("MMAC3:STR:2" )
            .withChannelNameAndCombinedProperties("EMJCYV:STAW:1", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).build() )
            .withChannelNameAndCombinedProperties("EMJCYV:IST:2", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).build() )
            .withChannelNameAndStreamProperties("MMAC:SOL:2" )
            .withChannelNameAndStreamProperties("DMAD1:IST:2" )
            .withChannelNameAndCombinedProperties("PRO:REG2D:Y:2", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelNameAndCombinedProperties("PRO:REG2D:X:2", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelNameAndCombinedProperties("PRO:REG2D:X", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelNameAndCombinedProperties("PRO:REG2D:Y", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2 ).build() )
            .withChannelNameAndStreamProperties("AMAKI1:IST:2" )
            .withChannelNameAndStreamProperties("CMJSEV:PWRF:2" )
            .withChannelNameAndStreamProperties("CMJLL:SOLA:2" )
            .withChannelNameAndStreamProperties("EMJEC1V:IST:2" )
            .withChannelNameAndStreamProperties("EMJEC2V:IST:2" )
            .withChannelNameAndStreamProperties("AMJHS-I:IADC:2" )
            .withChannelNameAndStreamProperties("MMJF:IST:2" )
            .withChannelNameAndStreamProperties("IMJV:IST:2" )
            .withChannelNameAndStreamProperties("IMJI:IST:2" )
            .withChannelNameAndStreamProperties("IMJGF:IST:2" )
            .withChannelNameAndStreamProperties("XPROIONS:IST1:2" )
            .withChannelNameAndStreamProperties("QMA1:IST:2" )
            .withChannelNameAndStreamProperties("QMA2:IST:2" )
            .withChannelNameAndStreamProperties("QMA3:IST:2" )
            .withChannelNameAndStreamProperties("SMJ1X:IST:2" )
            .withChannelNameAndStreamProperties("SMJ2Y:IST:2" )
            .withChannelNameAndStreamProperties("SMA1X:IST:2" )
            .withChannelNameAndStreamProperties("SMA1Y:IST:2" )
            .withChannelNameAndStreamProperties("FMJEP:IST:2" )
            .withChannelNameAndStreamProperties("MMJP2:IST1:2" )
            .withChannelNameAndStreamProperties("FMJEPI:POS:2" )
            .withChannelNameAndStreamProperties("FMJIP:IST:2" )
            .withChannelNameAndStreamProperties("MMJP2:IST2:2" )
            .withChannelNameAndStreamProperties("FMJIPI:POS:2" )
            .withChannelNameAndStreamProperties("FMJIPI:BREI:2" )
            .withChannelNameAndStreamProperties("PRO:CURRENTALARM:1" )
            .withChannelNameAndCombinedProperties("MMAC3:STR:2##2", WicaChannelPropertiesBuilder.create()
               .withDataAcquisitionMode(WicaDataAcquisitionMode.POLL_MONITOR)
               .withFieldsOfInterest( "val;ts" )
               .withFilterType( WicaFilterType.CHANGE_FILTERER )
               .withFilterDeadband( 5 )
               .build() )
            .withChannelNameAndCombinedProperties("EMJCYV:IST:2##2", WicaChannelPropertiesBuilder.create()
               .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL_MONITOR)
               .withFieldsOfInterest( "val;ts" )
               .withFilterType( WicaFilterType.ONE_IN_M )
               .withFilterCycleLength( 1 )
               .build() )
            .withChannelNameAndCombinedProperties("CMJSEV:PWRF:2##2", WicaChannelPropertiesBuilder.create()
               .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL_MONITOR)
               .withFieldsOfInterest( "val;ts" )
               .withFilterType( WicaFilterType.ONE_IN_M )
               .withFilterCycleLength( 1 )
               .build() )
            .withChannelNameAndStreamProperties("BMA1:STA:2" )
            .withChannelNameAndStreamProperties("BMA1:STAR:2##1" )
            .withChannelNameAndStreamProperties("BMA1:STAR:2##2" )
            .withChannelNameAndStreamProperties("BMA1:STAP:2##1" )
            .withChannelNameAndStreamProperties("BMA1:STAP:2##2" )
            .withChannelNameAndStreamProperties("BME1:STA:2" )
            .withChannelNameAndStreamProperties("BME1:STAR:2##1" )
            .withChannelNameAndStreamProperties("BME1:STAR:2##2" )
            .withChannelNameAndStreamProperties("BME1:STAP:2##1" )
            .withChannelNameAndStreamProperties("BME1:STAP:2##2" )
            .withChannelNameAndStreamProperties("BMB1:STA:2" )
            .withChannelNameAndStreamProperties("BMB1:STAR:2##1" )
            .withChannelNameAndStreamProperties("BMB1:STAR:2##2" )
            .withChannelNameAndStreamProperties("BMB1:STAP:2##1" )
            .withChannelNameAndStreamProperties("BMB1:STAP:2##2" )
            .withChannelNameAndStreamProperties("BMC1:STA:2" )
            .withChannelNameAndStreamProperties("BMC1:STAR:2##1" )
            .withChannelNameAndStreamProperties("BMC1:STAR:2##2" )
            .withChannelNameAndStreamProperties("BMC1:STAP:2##1" )
            .withChannelNameAndStreamProperties("BMC1:STAP:2##2" )
            .withChannelNameAndStreamProperties("BMD1:STA:2" )
            .withChannelNameAndStreamProperties("BMD1:STAR:2##1" )
            .withChannelNameAndStreamProperties("BMD1:STAR:2##2" )
            .withChannelNameAndStreamProperties("BMD2:STA:2" )
            .withChannelNameAndStreamProperties("BMD2:STAR:2##1" )
            .withChannelNameAndStreamProperties("BMD2:STAR:2##2" )
            .withChannelNameAndStreamProperties("BMD2:STAP:2##1" )
            .withChannelNameAndStreamProperties("BMD2:STAP:2##2" )
            .withChannelNameAndCombinedProperties("MMAP5X:PROF:2:P", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelNameAndCombinedProperties("MMAP5X:PROF:2", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelNameAndStreamProperties("MMAP5X:SPB:2" )
            .withChannelNameAndCombinedProperties("MMAP6Y:PROF:2:P", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelNameAndCombinedProperties("MMAP6Y:PROF:2", WicaChannelPropertiesBuilder.create().withFieldsOfInterest("val" ).withNumericPrecision(2).build() )
            .withChannelNameAndStreamProperties("MMAP6Y:SPB:2" )
            .withChannelNameAndStreamProperties("YMJCS1K:IST:2" )
            .withChannelNameAndStreamProperties("YMJCS2K:IST:2" )
            .withChannelNameAndStreamProperties("YMJHH:SPARB:2" )
            .withChannelNameAndStreamProperties("YMJHH:STR:2" )
            .withChannelNameAndStreamProperties("YMJHL:IST:2" )
            .withChannelNameAndStreamProperties("YMJHG:IST:2" )
            .withChannelNameAndStreamProperties("YMJKKRT:IST:2" )
            .withChannelNameAndStreamProperties("RPS-IQ:STA:1" )
            .withChannelNameAndStreamProperties("UMJSSB:BIQX:1" )
            .withChannelNameAndStreamProperties("RPS-HFRD:STA:1" )
            .withChannelNameAndStreamProperties("UMJSSB:BHRX:1" )
            .withChannelNameAndStreamProperties("RPS-HF:STA:1" )
            .withChannelNameAndStreamProperties("UMJSSB:BHFX:1" )
            .withChannelNameAndStreamProperties("MJSSB:BDEX:1" )
            .withChannelNameAndStreamProperties("XPROSCAN:STAB:2" )
            .build();
   }


   private static WicaStream makeWicaStream( int numberOfChannels )
   {
      WicaStreamProperties wicaStreamProperties = WicaStreamPropertiesBuilder.create()
            .withDefaultProperties()
            .build();

      WicaStreamBuilder wicaStreamBuilder = WicaStreamBuilder.create()
            .withStreamProperties( wicaStreamProperties );

      for ( int i =0; i < numberOfChannels; i++ )
      {
         wicaStreamBuilder = wicaStreamBuilder.withChannelNameAndDefaultProperties( "CHAN-" + i  );
      }

      return wicaStreamBuilder.build();
   }

   private String sendStreamCreateRequest( WicaStream wicaStream )
   {
      logger.trace( "Creating new Stream..." );

      final String jsonStreamConfiguration = WicaStreamSerializer.writeToJson( wicaStream );

      final ClientResponse postResponse = WebClient.create( wicaStreamUri + "/ca/streams")
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
