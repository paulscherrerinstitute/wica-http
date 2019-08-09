/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

// Need to rewire the service after each test since it will previously
// have been closed down at the end of the previous test.
@DirtiesContext( classMode= DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD )
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class EpicsChannelMonitoringServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private EpicsChannelMonitoringService epicsChannelMonitoringService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      assertEquals(0, epicsChannelMonitoringService.getChannelsCreatedCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsDeletedCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
   }

   @AfterEach
   void afterEach()
   {
      epicsChannelMonitoringService.close();
      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenEpicsChannelNamesIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitoringService.startMonitoring(null, ( b)->{}, ( m)->{}, ( v)->{} ) );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenStateChangeHandlerIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("abcd" ), null, ( m)->{}, ( v)->{} ) );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenMetadataChangeHandlerIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("abcd" ), ( b)->{}, null, ( v)->{} ) );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenValueChangeHandlerIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("abcd" ), ( b)->{}, ( m)->{}, null ) );
   }

   @Test
   void testStartMonitoring_ThrowsIllegalStatExceptionWhenChannelNameNotUnique()
   {
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("abcd" ), ( b)->{}, ( m)->{}, ( v)->{} );
      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("abcd" ), ( b)->{}, ( m)->{}, ( v)->{} ) );
      assertThat( ex.getMessage(), is( "The channel name: 'abcd' is already being monitored." ) );
   }

   @Test
   void testStartMonitoring_CheckChannelStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Confirm that initially no channels have been created and that nothing is connected
      assertEquals(0, epicsChannelMonitoringService.getChannelsDeletedCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsCreatedCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );

      // Verify that a call to monitor a channel results in an increase in the channel creation count.
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("offline-channel-1" ), ( b)->{}, ( m)->{}, ( v)->{} );
      assertEquals(0, epicsChannelMonitoringService.getChannelsDeletedCount() );
      assertEquals(1, epicsChannelMonitoringService.getChannelsCreatedCount() );
      assertEquals(1, epicsChannelMonitoringService.getChannelsActiveCount() );

      // Verify that the channel connection count is still zero.
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );

      // Verify that monitoring a channel with the same name increases the connection count but not the connection count.
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("offline-channel-2" ), ( b)->{}, ( m)->{}, ( v)->{} );
      assertEquals(2, epicsChannelMonitoringService.getChannelsActiveCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
   }

   @Test
   void testStopMonitoring_ThrowsIllegalStateExceptionWhenStoppingMonitoringChannelThatWasNeverPreviouslyMonitored()
   {
      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMonitoringService.stopMonitoring(new EpicsChannelName("unknown-channel" ) ) );
      assertThat( ex.getMessage(), is( "The channel name: 'unknown-channel' was not recognised.") );
   }

   @Test
   void testStopMonitoring_CheckChannelStatisticsAsExpectedWhenDisposingOfflineChannels()
   {
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("offline-channel-1" ), ( b)->{}, ( m)->{}, ( v)->{} );
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("offline-channel-2" ), ( b)->{}, ( m)->{}, ( v)->{} );
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("offline-channel-3" ), ( b)->{}, ( m)->{}, ( v)->{} );
      assertEquals(0, epicsChannelMonitoringService.getChannelsDeletedCount() );
      assertEquals(3, epicsChannelMonitoringService.getChannelsCreatedCount() );
      assertEquals(3, epicsChannelMonitoringService.getChannelsActiveCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );

      epicsChannelMonitoringService.stopMonitoring(new EpicsChannelName("offline-channel-1" ) );
      assertEquals(1, epicsChannelMonitoringService.getChannelsDeletedCount() );
      assertEquals(3, epicsChannelMonitoringService.getChannelsCreatedCount() );
      assertEquals(2, epicsChannelMonitoringService.getChannelsActiveCount() );
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
   }

   @Test
   void testStartMonitoring_CheckMonitorStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Verify that attempting to monitor a non-existent channel
      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount() );
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("non-existent-channel 1" ), ( b)->{}, ( m)->{}, ( v)->{} );
      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount() );
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName("non-existent-channel 2" ), ( b)->{}, ( m)->{}, ( v)->{} );
      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount() );

      epicsChannelMonitoringService.close();
      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount());
   }

   @Test
   void testGetChannelConnectionCount() throws InterruptedException
   {
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount());
      epicsChannelMonitoringService.startMonitoring( new EpicsChannelName("test:db_ok" ), ( b)->{}, ( m)->{}, ( v)->{} );
      Thread.sleep( 1_000 );
      assertEquals(1, epicsChannelMonitoringService.getChannelsConnectedCount());
      epicsChannelMonitoringService.close();
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount());
   }

   @Test
   void testStartMonitoring_verifyInitialConnectBehaviour_HandlersAreNotNotifiedIfChannelOffline() throws InterruptedException
   {
      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
      epicsChannelMonitoringService.startMonitoring( new EpicsChannelName("non-existent-channel" ), stateChangeHandlerMock, ( m)->{}, valueChangeHandlerMock );
      Thread.sleep( 1_000 );
      Mockito.verify( stateChangeHandlerMock, never() ).accept( anyBoolean() );
      Mockito.verify( valueChangeHandlerMock, never() ).accept( any() );
   }

   @Test
   void testStartMonitoring_verifyInitialConnectBehaviour_NotificationSequence() throws InterruptedException
   {
      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
      epicsChannelMonitoringService.startMonitoring( new EpicsChannelName("test:db_ok" ), stateChangeHandlerMock, ( m)->{}, valueChangeHandlerMock );
      Thread.sleep( 1_000 );
      final InOrder inOrder = inOrder( stateChangeHandlerMock, valueChangeHandlerMock );
      inOrder.verify( stateChangeHandlerMock ).accept(true );
   }

   @Test
   void testStartMonitoring_verifyMonitorDisconnectBehaviour()
   {
      // TODO: write this
   }

   @Test
   void testStartMonitoring_verifyBehaviourWithDifferentChannelTypes()
   {
      // TODO: write this
   }

   @Test
   void testStopMonitoring_verifyConnectionCountChanges() throws InterruptedException
   {
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
      epicsChannelMonitoringService.startMonitoring(new EpicsChannelName( "test:db_ok" ), ( b)->{}, ( m)->{}, ( v)->{} );
      Thread.sleep( 3_000 );
      assertEquals(1, epicsChannelMonitoringService.getChannelsConnectedCount() );
      epicsChannelMonitoringService.stopMonitoring(new EpicsChannelName( "test:db_ok" ) );
      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
   }

/*- Nested Classes -----------------------------------------------------------*/

   // Note: these interfaces exist to avoid the need for an unchecked cast in
   // some of the tests above
   private interface BooleanConsumer extends Consumer<Boolean> {}
   private interface EpicsChannelValueConsumer extends Consumer<WicaChannelValue> {}

}
