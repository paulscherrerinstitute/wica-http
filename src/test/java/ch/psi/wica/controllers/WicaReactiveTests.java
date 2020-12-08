package ch.psi.wica.controllers;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WicaReactiveTests
{

   private final Logger logger = LoggerFactory.getLogger( WicaReactiveTests.class );

   // This test verifies that cancel requests are back-propagated up the operator chain to
   // the root publisher, in this case flux1 and flux2. Single-threaded case.
   // TODO: Need to deal with ReplayProcessor deprecation warning
   @SuppressWarnings( "deprecation" )
   @Test
   void reactorTest_check_cancel_back_propagation()
   {
      final AtomicBoolean flux1Cancelled= new AtomicBoolean( false );
      final AtomicBoolean flux2Cancelled= new AtomicBoolean( false );
      final AtomicBoolean flux3Cancelled= new AtomicBoolean( false );

      // These sources allow us to inject signals into the flux under test
      final ReplayProcessor<Long> src1 = ReplayProcessor.create();
      final ReplayProcessor<Long> src2 = ReplayProcessor.create();

      final Flux<Long> flux1 = src1.doOnCancel( () -> { logger.info( "flux1 received cancel !");  flux1Cancelled.set( true ); } );
      final Flux<Long> flux2 = src2.doOnCancel( () -> { logger.info( "flux2 received cancel !");  flux2Cancelled.set( true ); } );
      final Flux<Long> flux3 = flux1.mergeWith( flux2 ).log().doOnCancel( () -> flux3Cancelled.set( true ) );

      StepVerifier.create( flux3 ).expectSubscription()
                                  .then( () -> LongStream.range( 1,  11 ).forEach(src1::onNext ) )
                                  .then( () -> LongStream.range( 11, 21 ).forEach(src2::onNext ) )
                                  .then( () -> LongStream.range( 21, 31 ).forEach(src1::onNext ) )
                                  .then( () -> LongStream.range( 31, 41 ).forEach(src2::onNext ) )
                                  .expectNextCount( 40 )
                                  .thenCancel()
                                  .verify();
      assertTrue( flux1Cancelled.get() );
      assertTrue( flux2Cancelled.get() );
      assertTrue( flux3Cancelled.get() );
   }

   // This test verifies that cancel requests are back-propagated up the operator chain to
   // the root publisher, in this case flux1 and flux2. Multi-threaded case.
   // TODO: Need to deal with ReplayProcessor deprecation warning
   @SuppressWarnings( "deprecation" )
   @Test
   void reactorTest_check_cancel_back_propagation_threaded() throws InterruptedException
   {
      final AtomicBoolean flux1Cancelled= new AtomicBoolean( false );
      final AtomicBoolean flux2Cancelled= new AtomicBoolean( false );
      final AtomicBoolean flux3Cancelled= new AtomicBoolean( false );

      // These sources allow us to inject signals into the flux under test
      final ReplayProcessor<Long> src1 = ReplayProcessor.create();
      final ReplayProcessor<Long> src2 = ReplayProcessor.create();

      final Flux<Long> flux1 = src1.publishOn( Schedulers.newSingle( "flux1-pub-thread1" ) ).doOnCancel( () ->  { logger.info( "flux1 received cancel !");  flux1Cancelled.set( true ); } );
      final Flux<Long> flux2 = src2.publishOn( Schedulers.newSingle( "flux2-pub-thread2" ) ).doOnCancel( () ->  { logger.info( "flux2 received cancel !");  flux2Cancelled.set( true ); } );
      final Flux<Long> flux3 = flux1.mergeWith( flux2 ).log().doOnCancel( () -> flux3Cancelled.set( true ) );

      StepVerifier.create( flux3 ).expectSubscription()
            .then( () -> LongStream.range( 1,  11 ).forEach( src1::onNext ) )
            .then( () -> LongStream.range( 11, 21 ).forEach( src2::onNext ) )
            .then( () -> LongStream.range( 21, 31 ).forEach( src1::onNext ) )
            .then( () -> LongStream.range( 31, 41 ).forEach( src2::onNext ) )
            .expectNextCount( 40 )
            .thenCancel()
            .verify();

      // Since publication is on a different thread we need to allow time for the
      // changes to propagate before testing immediately.
      Thread.sleep( 1000L );

      assertTrue( flux1Cancelled.get() );
      assertTrue( flux2Cancelled.get() );
      assertTrue( flux3Cancelled.get() );
   }


   // "BROKEN/UNEXPECTED" EXCEPTION BEHAVIOUR VERIFICATION
   //
   // This test verifies how and/or whether exceptions thrown by the subscriber are
   // back-propagated up the operator chain to the root publisher, in this case flux1 and flux2.
   //
   // The scenario below illustrates the situation where the subscriber does NOT provide its
   // own error handler. The behaviour with respect to subscriber-thrown exceptions is then
   // expected to be as follows:
   //
   //   (a) the subscriber exception IS back-propagated to the root publisher.
   //   (b) Only the merged flux subscriber gets its subscription cancelled.
   //   (c) the two upstream subscribers (flux1 and flux2) are NOT cancelled.
   //
   // Currently (2018-05-22) there is very little about this in the discussion forums. But I did
   // find the following (which relates to the Rx library on Windows0 but which seems to describe
   // the behaviour that I have seen.
   // https://stackoverflow.com/questions/19309417/what-should-happen-if-i-throw-an-exception-in-my-subscribe-callback-for-an-obser
   // https://social.msdn.microsoft.com/Forums/en-US/6ea06e08-6d26-49b4-a3fa-276d1efef780/what-if-an-exception-is-thrown-by-the-observer-in-onnext?forum=rx
   // TODO: Need to deal with ReplayProcessor deprecation warning
   @SuppressWarnings( "deprecation" )
   @Test
   void reactorTest_check_exception_back_propagation_no_subscriber_error_handler()
   {
      final AtomicReference<Throwable> dataSourceThrowCatcher = new AtomicReference<>();
      final AtomicBoolean flux1Cancelled= new AtomicBoolean( false);
      final AtomicBoolean flux2Cancelled= new AtomicBoolean( false);
      final AtomicBoolean flux3Cancelled= new AtomicBoolean( false);

      final RuntimeException myFunkyException = new RuntimeException( "myFunkyRuntimeException" );

      // These sources allow us to inject signals into the flux under test
      final ReplayProcessor<Long> src1 = ReplayProcessor.create();
      final ReplayProcessor<Long> src2 = ReplayProcessor.create();

      // Verify there are no downstream subscribers at the beginning
      assertEquals( 0L, src1.downstreamCount() );
      assertEquals( 0L, src2.downstreamCount() );

      final Flux<Long> flux1 = src1.log().doOnCancel( () -> { logger.info( "flux1 publisher received onCancel !");  flux1Cancelled.set( true ); } );
      final Flux<Long> flux2 = src2.log().doOnCancel( () -> { logger.info( "flux2 publisher received onCancel !");  flux2Cancelled.set( true ); } );
      final Flux<Long> flux3 = flux1.mergeWith( flux2 ).log().doOnCancel( () -> { logger.info( "flux3 publisher received onCancel !");  flux3Cancelled.set( true ); } );

      // Verify there are no downstream subscribers even after we have created the merged flux. This
      // is presumably because noone has subscribed to it.
      assertEquals( 0L, src1.downstreamCount() );
      assertEquals( 0L, src2.downstreamCount() );

      // Now subscribe, but WITHOUT an error handler
      final Disposable disposable = flux3.subscribe( l -> {
         logger.info( "flux3 consumer received data: '{}'", l );
         if ( l == -1L ) {
            logger.info( "flux3 consumer is about to throw: '{}'...", myFunkyException.getMessage() );
            throw myFunkyException;
         }
      } );

      // Following the subscribe operation above the downstream count here should now
      // indicate that the fluxes are now connected.
      assertEquals( 1L, src1.downstreamCount() );
      assertEquals( 1L, src2.downstreamCount() );

      // Send some data on src1
      logger.info( "Sending data on src1...");
      src1.onNext(15L);
      src1.onNext(1000L);
      src1.onNext(1L);

      // Send some data on src2
      logger.info( "Sending data on src2...");
      src2.onNext(17L);
      src2.onNext(2000L);
      src2.onNext(1L);

      // Now send the magic explode token causing the flux3 subscriber to trigger an exception
      // when the onNext signal is delivered
      logger.info( "Sending magic explode token...");
      try {
         src1.onNext(-1L);
      }
      catch (Throwable t ) {
         dataSourceThrowCatcher.set( t );
         logger.info("dataSourceThrowCatcher caught throwable: '{}'", t.getMessage()  );
      }

      // Verify that flux3 got its subscription cancelled
      assertTrue( flux3Cancelled.get() );

      // Previously: The data sources do not know that the downwind subscriber flux3 has had its subscription
      // cancelled - because they were not informed !
      // Since 2.4.0 the downwind subscribers DO get informed !
      assertTrue( flux1Cancelled.get() ); // behaviour change 2.4.0
      assertTrue( flux2Cancelled.get() ); // behaviour change 2.4.0

      // Previously: even triggering the cancel signal directly will NOT cause the upstream publishers
      // to get cancelled. (Presumably it's too late as the merged publisher has already
      // cancelled the subscription.
      // Since 2.4.0 the downwind subscribers DO get informed !
      disposable.dispose();
      assertTrue( flux1Cancelled.get() ); // behaviour change 2.4.0
      assertTrue( flux2Cancelled.get() ); // behaviour change 2.4.0

      // Previously: Therefore the downwind counters (correctly) show there is something
      // subscribed.
      // Since 2.4.0 the downwind count is zero !
      assertEquals( 0L, src1.downstreamCount() );
      assertEquals( 0L, src2.downstreamCount() );

      // Previously: verify that the exception WAS back-propagated to the data source
      // Since 2.4.0 the exception is NOT propagated since the data source is meant to be completely isolated.
      assertFalse( Exceptions.isErrorCallbackNotImplemented( dataSourceThrowCatcher.get() ) );

      // Try to send something else down the pipe and verify this does not cause exceptions
      // even though the publication mechanism is now effectively broken.
      dataSourceThrowCatcher.set( null );
      try {
         logger.info( "Sending more data on src1...");
         src1.onNext(100L );
      }
      catch (Throwable t ) {
         dataSourceThrowCatcher.set( t );
         logger.info("dataSourceThrowCatcher caught throwable: '{}'", t.getMessage()  );
      }
      assertNull( dataSourceThrowCatcher.get() );
   }

   // "WORKABLE" EXCEPTION BEHAVIOUR VERIFICATION
   //
   // This test verifies how and/or whether exceptions thrown by the subscriber are
   // back-propagated up the operator chain to the root publisher, in this case flux1 and
   // flux2.
   //
   // The scenario below illustrates the situation where the subscriber provides their own
   // error handler. The behaviour with respect to subscriber-thrown exceptions is then
   // expected to be as follows:
   //
   //   (a) the subscriber's error handler is called (with the throwable passed to it).
   //   (b) the subscriber exception is NOT back-propagated to the upstream publisher.
   //   (c) all subscriptions in the upstream chain are automatically cancelled.
   //
   // TODO: Need to deal with ReplayProcessor deprecation warning
   @SuppressWarnings( "deprecation" )
   @Test
   void reactorTest_check_exception_back_propagation_with_subscriber_error_handler()
   {
      final AtomicReference<Throwable> dataSourceThrowCatcher = new AtomicReference<>();
      final AtomicReference<Throwable> flux3ConsumerErrorCatcher = new AtomicReference<>();

      final AtomicBoolean flux1Cancelled= new AtomicBoolean( false);
      final AtomicBoolean flux2Cancelled= new AtomicBoolean( false);
      final AtomicBoolean flux3Cancelled= new AtomicBoolean( false);

      final RuntimeException myFunkyException = new RuntimeException( "myFunkyRuntimeException" );

      // These sources allow us to inject signals into the flux under test
      final ReplayProcessor<Long> src1 = ReplayProcessor.create();
      final ReplayProcessor<Long> src2 = ReplayProcessor.create();

      // Verify there are no downstream subscribers at the beginning
      assertEquals( 0L, src1.downstreamCount() );
      assertEquals( 0L, src2.downstreamCount() );

      final Flux<Long> flux1 = src1.log().doOnCancel( () -> { logger.info( "flux1 publisher received onCancel !");  flux1Cancelled.set( true ); } );
      final Flux<Long> flux2 = src2.log().doOnCancel( () -> { logger.info( "flux2 publisher received onCancel !");  flux2Cancelled.set( true ); } );
      final Flux<Long> flux3 = flux1.mergeWith( flux2 ).log().doOnCancel( () -> { logger.info( "flux3 publisher received onCancel !");  flux3Cancelled.set( true ); } );

      // Verify there are no downstream subscribers even after we have created the merged flux. This
      // is presumably because noone has subscribed to it.
      assertEquals( 0L, src1.downstreamCount() );
      assertEquals( 0L, src2.downstreamCount() );

      // Now subscribe, supplying a user-defined error handler.
      flux3.subscribe( l -> {
         logger.info( "flux3 consumer received data: '{}'", l );
         if ( l == -1L ) {
            logger.info( "flux3 consumer is about to throw: '{}'...", myFunkyException.getMessage() );
            throw myFunkyException;
         }
      }, t -> {
         logger.info( "flux3 consumer error handler got advice of an error via throwable: '{}'", t.getMessage() );
         flux3ConsumerErrorCatcher.set( t );
      } );

      // Following the subscribe operation above the downstream count here should now
      // indicate that the fluxes are now connected.
      assertEquals( 1L, src1.downstreamCount() );
      assertEquals( 1L, src2.downstreamCount() );

      // Send some data on src1
      logger.info( "Sending data on src1...");
      src1.onNext(15L);
      src1.onNext(1000L);
      src1.onNext(1L);

      // Send some data on src2
      logger.info( "Sending data on src2...");
      src2.onNext(17L);
      src2.onNext(2000L);
      src2.onNext(1L);

      // Now send the magic explode token causing the mergedFlux subscriber to trigger an execption
      // when the onNext signal is delivered. Catch any execptions that get bubbled up.
      try {
         logger.info( "Sending magic explode token...");
         src1.onNext(-1L);
      }
      catch (Throwable t ) {
         dataSourceThrowCatcher.set( t );
         logger.info("dataSourceThrowCatcher caught throwable: '{}'", t.getMessage()  );
      }
      assertEquals( 0L, src1.downstreamCount() );
      assertEquals( 0L, src2.downstreamCount() );

      // Verify that all publishers had their subscriptions cancelled
      assertTrue( flux1Cancelled.get() );
      assertTrue( flux2Cancelled.get() );
      assertTrue( flux3Cancelled.get() );

      // Verify that the exception was NOT back propagated to the source
      assertNull( dataSourceThrowCatcher.get() );

      // Verify that the exception was offered to the local error handler
      // (which in this case swallowed it).
      assertEquals( myFunkyException, flux3ConsumerErrorCatcher.get() );

      // Try to send something else down the pipe and verify this does not cause exceptions
      // even though the publication mechanism is now effectively broken
      dataSourceThrowCatcher.set( null );
      try {
         logger.info( "Sending more data on src1...");
         src1.onNext(100L );
      }
      catch (Throwable t ) {
         dataSourceThrowCatcher.set( t );
         logger.info("dataSourceThrowCatcher caught throwable: '{}'", t.getMessage()  );
      }
      assertNull( dataSourceThrowCatcher.get() );
   }
}




