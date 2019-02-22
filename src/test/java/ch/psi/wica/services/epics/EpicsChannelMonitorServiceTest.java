/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Ignore
@SpringBootTest
class EpicsChannelMonitorServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private EpicsChannelMonitorService epicsChannelMonitorService = new EpicsChannelMonitorService( );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      epicsChannelMonitorService = new EpicsChannelMonitorService();
   }

   @AfterEach
   void afterEach()
   {
      epicsChannelMonitorService.close();
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenEpicsChannelNamesIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitorService.startMonitoring(null, (b)->{}, (m)->{}, (v)->{} ) );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenStateChangeHandlerIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitorService.startMonitoring( new EpicsChannelName("abcd" ), null, (m)->{}, (v)->{} ) );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenMetadataChangeHandlerIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitorService.startMonitoring( new EpicsChannelName("abcd" ), (b)->{}, null, (v)->{} ) );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenValueChangeHandlerIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitorService.startMonitoring( new EpicsChannelName("abcd" ), (b)->{}, (m)->{}, null ) );
   }

   @Test
   void testStartMonitoring_ThrowsIllegalArgumentExceptionWhenChannelNameNotUnique()
   {
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName("abcd" ), (b)->{}, (m)->{}, (v)->{} );
      assertThrows( IllegalArgumentException.class, () -> epicsChannelMonitorService.startMonitoring( new EpicsChannelName("abcd" ), (b)->{}, (m)->{}, (v)->{} ) );
   }

   @Test
   void testStartMonitoring_CheckChannelStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Confirm that initially no channels have been created and that nothing is connected
      assertEquals( 0, EpicsChannelMonitorService.getChannelsCreatedCount() );
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );

      // Verify that a call to monitor a channel results in an increase in the channel creation count.
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "offline-channel-1" ), (b)->{}, (m)->{}, (v)->{} );
      assertEquals( 1, EpicsChannelMonitorService.getChannelsCreatedCount() );

      // Verify that the channel connection count is still zero.
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );

      // Verify that monitoring a channel with the same name increases the connection count but not the connection count.
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "offline-channel-2" ), (b)->{}, (m)->{}, (v)->{} );
      assertEquals( 2, EpicsChannelMonitorService.getChannelsCreatedCount() );
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );
   }

   @Test
   void testStopMonitoring_ThrowsIllegalArgumentExceptionWhenChannelNameNotRecognised()
   {
      assertThrows( IllegalArgumentException.class, () -> epicsChannelMonitorService.stopMonitoring( new EpicsChannelName("unknown-channel" ) ) );
   }

   @Test
   void testStopMonitoring_CheckChannelStatisticsAsExpectedWhenDisposingOfflineChannels()
   {
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "offline-channel-1" ), (b)->{}, (m)->{}, (v)->{} );
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "offline-channel-2" ), (b)->{}, (m)->{}, (v)->{} );
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "offline-channel-3" ), (b)->{}, (m)->{}, (v)->{} );
      assertEquals( 3, EpicsChannelMonitorService.getChannelsCreatedCount() );
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );

      epicsChannelMonitorService.stopMonitoring( new EpicsChannelName( "offline-channel-1" ) );
      assertEquals( 3, EpicsChannelMonitorService.getChannelsCreatedCount() );
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );
   }


   @Test
   void testStartMonitoring_CheckMonitorStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Verify that attempting to monitor a non-existent channel
      assertEquals( 0, EpicsChannelMonitorService.getMonitorsConnectedCount() );
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "non-existent-channel 1" ), (b)->{}, (m)->{}, (v)->{} );
      assertEquals( 0, EpicsChannelMonitorService.getMonitorsConnectedCount() );
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "non-existent-channel 2" ), (b)->{}, (m)->{}, (v)->{} );
      assertEquals( 0, EpicsChannelMonitorService.getMonitorsConnectedCount() );

      epicsChannelMonitorService.close();
      assertEquals( 0, EpicsChannelMonitorService.getMonitorsConnectedCount());
   }

   @Test
   void testGetChannelConnectionCount() throws InterruptedException
   {
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount());
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName("test:db_ok" ), (b)->{}, (m)->{}, (v)->{} );
      Thread.sleep( 1_000 );
      assertEquals( 1, EpicsChannelMonitorService.getChannelsConnectedCount());
      epicsChannelMonitorService.close();
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount());
   }


   @Test
   void testStartMonitoring_verifyInitialConnectBehaviour_HandlersAreNotNotifiedIfChannelOffline() throws InterruptedException
   {
      assertEquals( 0, EpicsChannelMonitorService.getChannelsCreatedCount() );
      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName("non-existent-channel" ), stateChangeHandlerMock, (m)->{}, valueChangeHandlerMock );
      Thread.sleep( 1_000 );
      Mockito.verify( stateChangeHandlerMock, never() ).accept( anyBoolean() );
      Mockito.verify( valueChangeHandlerMock, never() ).accept( any() );
   }

   @Test
   void testStartMonitoring_verifyInitialConnectBehaviour_NotificationSequence() throws InterruptedException
   {
      assertEquals( 0, EpicsChannelMonitorService.getChannelsCreatedCount() );
      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
      epicsChannelMonitorService.startMonitoring(new EpicsChannelName("test:db_ok" ), stateChangeHandlerMock, (m)->{}, valueChangeHandlerMock );
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
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );
      epicsChannelMonitorService.startMonitoring( new EpicsChannelName( "test:db_ok" ), (b)->{}, (m)->{}, (v)->{} );
      Thread.sleep( 1_000 );
      assertEquals( 1, EpicsChannelMonitorService.getChannelsConnectedCount() );
      epicsChannelMonitorService.stopMonitoring( new EpicsChannelName( "test:db_ok" ) );
      assertEquals( 0, EpicsChannelMonitorService.getChannelsConnectedCount() );
   }

   @Test
   void testStopMonitoring_verifyIllegalArgumentExceptionWhenChannelNotRecognised()
   {
      assertThrows( IllegalArgumentException.class, () ->  epicsChannelMonitorService.stopMonitoring( new EpicsChannelName( "XXXXX" ) ) );
   }


/*- Nested Classes -----------------------------------------------------------*/

   // Note: these interfaces exist to avoid the need for an unchecked cast in
   // some of the tests above
   private interface BooleanConsumer extends Consumer<Boolean> {}
   private interface EpicsChannelValueConsumer extends Consumer<WicaChannelValue> {}

}
