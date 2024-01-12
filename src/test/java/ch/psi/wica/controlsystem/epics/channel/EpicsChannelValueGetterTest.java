/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.value.WicaChannelValueConnectedInteger;
import ch.psi.wica.model.channel.value.WicaChannelValueDisconnected;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.data.AlarmSeverity;
import org.epics.ca.data.AlarmStatus;
import org.epics.ca.data.Metadata;
import org.epics.ca.data.Timestamped;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class EpicsChannelValueGetterTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private EpicsChannelValueGetter epicsChannelValueGetter;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Package-access methods ---------------------------------------------------*/

   @Test
   void testGetValueWithoutProblem()
   {
      // Setup a CompletableFuture to return a happy, timestamped test value.
      final Integer testValue = 1234;
      final Timestamped<Object> timestamped = new Timestamped<>();
      timestamped.setAlarmSeverity( AlarmSeverity.NO_ALARM );
      timestamped.setAlarmStatus( AlarmStatus.NO_ALARM );
      timestamped.setNanos( 0 );
      timestamped.setSeconds( 0L );
      timestamped.setValue( testValue );
      final CompletableFuture<Metadata<Object>> completedFuture = CompletableFuture.completedFuture( timestamped );

      // Setup the caChannel mock to answer questions about the channel name and its current connection state
      // and then to return the configured CompletableFuture.
      @SuppressWarnings( "unchecked" )
      final Channel<Object> caChannel =  Mockito.mock( Channel.class );
      when( caChannel.getName() ).thenReturn( "some-channel-name" );
      when( caChannel.getConnectionState() ).thenReturn( ConnectionState.CONNECTED );
      when( caChannel.getAsync( any() ) ).thenReturn( completedFuture );

      // Finally invoke the method-under-test and verify that it returns the test value.
      final var wicaChannelValue = (WicaChannelValueConnectedInteger) assertDoesNotThrow ( () -> epicsChannelValueGetter.get( caChannel, 1, TimeUnit.SECONDS ) );
      assertThat( wicaChannelValue.getValue(), is( testValue ) );
   }

   @Test
   void testGetValueChannelOfflineAtInvocationTime()
   {
      @SuppressWarnings( "unchecked" )
      final Channel<Object> caChannel =  Mockito.mock( Channel.class );

      // Setup the caChannel mock to answer questions about the channel name and then report the channel is disconnected.
      when( caChannel.getName() ).thenReturn( "some-channel-name" );
      when( caChannel.getConnectionState() ).thenReturn( ConnectionState.DISCONNECTED );

      // Finally invoke the method-under-test and verify that it returns a value indication the channel is disconnected.
      final var wicaChannelValue = (WicaChannelValueDisconnected) assertDoesNotThrow ( () -> epicsChannelValueGetter.get( caChannel, 1, TimeUnit.SECONDS ) );
      assertThat( wicaChannelValue.isConnected(), is( false ) );
   }


   @Test
   void testGetValueChannelOfflineAtNetworkFetchTime()
   {
       // Setup a CompletableFuture to hang for 5 seconds.
      final CompletableFuture<Metadata<Object>> hangingCompletableFuture = CompletableFuture.supplyAsync( () ->  {
         try
         {
            Thread.sleep(5000 );
         }
         catch ( InterruptedException e )
         {
            Thread.currentThread().interrupt();
         }
         return null; }  );

      // Setup the caChannel mock to answer questions about the channel name and its current connection state
      // and then to return the configured CompletableFuture which will hang without completion for longer than
      // the timeout interval.
      @SuppressWarnings( "unchecked" )
      final Channel<Object> caChannel =  Mockito.mock( Channel.class );
      when( caChannel.getName() ).thenReturn( "some-channel-name" );
      when( caChannel.getConnectionState() ).thenReturn( ConnectionState.CONNECTED );
      when( caChannel.getAsync( any() ) ).thenReturn( hangingCompletableFuture );

      // Finally invoke the method-under-test and verify that it throws a TimeoutException.
      assertThrows ( TimeoutException.class, () -> epicsChannelValueGetter.get( caChannel, 1, TimeUnit.MILLISECONDS ) );
   }

   @Test
   void testGetValueChannelAccessGetFails()
   {
      // Setup a CompletableFuture whose execution throws some Exception.
      final CompletableFuture<Metadata<Object>> throwingCompletableFuture = CompletableFuture.failedFuture( new UnsupportedOperationException() );

      @SuppressWarnings( "unchecked" )
      final Channel<Object> caChannel =  Mockito.mock( Channel.class );
      when( caChannel.getName() ).thenReturn( "some-channel-name" );
      when( caChannel.getConnectionState() ).thenReturn( ConnectionState.CONNECTED );
      when( caChannel.getAsync( any() ) ).thenReturn( throwingCompletableFuture );

      // Finally invoke the method-under-test and check it throws an ExecutionException.
      assertThrows ( ExecutionException.class, () -> epicsChannelValueGetter.get( caChannel, 1, TimeUnit.MILLISECONDS ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
