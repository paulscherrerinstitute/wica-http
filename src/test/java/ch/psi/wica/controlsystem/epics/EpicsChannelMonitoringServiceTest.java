/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

   @Autowired
   private EpicsEventPublisher epicsEventPublisherMock = Mockito.mock( EpicsEventPublisher.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Package-access methods ---------------------------------------------------*/

//   @BeforeEach
//   void beforeEach()
//   {
//      assertThat( epicsChannelMonitoringService.getStatistics().getStartRequests(), is( "0") );
//      assertThat( epicsChannelMonitoringService.get(), is( "0") );
//      assertThat( epicsChannelMonitoringService.getChannelsCreatedCount(), is( "0") );
//      assertThat( epicsChannelMonitoringService.getChannelsCreatedCount(), is( "0") );
//   }
//
//   @AfterEach
//   void afterEach()
//   {
//      epicsChannelMonitoringService.close();
//      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//   }
//
//   @Test
//   void testStartMonitoring_ThrowsNullPointerExceptionWhenEpicsChannelNamesIsNull()
//   {
//      assertThrows( NullPointerException.class, () -> epicsChannelMonitoringService.startMonitoring(null ) );
//   }
//
//
//   @Test
//   void testStartMonitoring_ThrowsIllegalStatExceptionWhenChannelNameNotUnique()
//   {
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel( "abcd" ));
//      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMonitoringService.startMonitoring( createWicaChannel( "abcd" ) ) );
//      assertThat( ex.getMessage(), is( "The channel name: 'abcd' is already being monitored." ) );
//   }
//
//   @Test
//   void testStartMonitoring_CheckChannelStatisticsAsExpectedWhenDealingWithOfflineChannels()
//   {
//      // Confirm that initially no channels have been created and that nothing is connected
//      assertEquals(0, epicsChannelMonitoringService.getChannelsDeletedCount() );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsCreatedCount() );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//
//      // Verify that a call to monitor a channel results in an increase in the channel creation count.
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel("offline-channel-1" ) );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsDeletedCount() );
//      assertEquals(1, epicsChannelMonitoringService.getChannelsCreatedCount() );
//      assertEquals(1, epicsChannelMonitoringService.getChannelsActiveCount() );
//
//      // Verify that the channel connection count is still zero.
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//
//      // Verify that monitoring a channel with the same name increases the connection count but not the connection count.
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel("offline-channel-2" ) );
//      assertEquals(2, epicsChannelMonitoringService.getChannelsActiveCount() );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//   }
//
//   @Test
//   void testStopMonitoring_ThrowsIllegalStateExceptionWhenStoppingMonitoringChannelThatWasNeverPreviouslyMonitored()
//   {
//      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMonitoringService.stopMonitoring( createWicaChannel("unknown-channel" ) ) );
//      assertThat( ex.getMessage(), is( "The channel name: 'unknown-channel' was not recognised.") );
//   }
//
//   @Test
//   void testStopMonitoring_CheckChannelStatisticsAsExpectedWhenDisposingOfflineChannels()
//   {
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel("offline-channel-1" ) );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsDeletedCount() );
//      assertEquals(3, epicsChannelMonitoringService.getChannelsCreatedCount() );
//      assertEquals(3, epicsChannelMonitoringService.getChannelsActiveCount() );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//
//      epicsChannelMonitoringService.stopMonitoring( createWicaChannel("offline-channel-1" ) );
//      assertEquals(1, epicsChannelMonitoringService.getChannelsDeletedCount() );
//      assertEquals(3, epicsChannelMonitoringService.getChannelsCreatedCount() );
//      assertEquals(2, epicsChannelMonitoringService.getChannelsActiveCount() );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//   }
//
//   @Test
//   void testStartMonitoring_CheckMonitorStatisticsAsExpectedWhenDealingWithOfflineChannels()
//   {
//      // Verify that attempting to monitor a non-existent channel
//      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount() );
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel("non-existent-channel-1" ) );
//      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount() );
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel("non-existent-channel-2" ) );
//      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount() );
//
//      epicsChannelMonitoringService.close();
//      assertEquals(0, epicsChannelMonitoringService.getMonitorsConnectedCount());
//   }
//
//   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
//   // By default this test is suppressed as it would create problems in the automatic
//   // build system. The test should be enabled as required during pre-production testing.
//   @Disabled
//   @Test
//   void testGetChannelConnectionCount() throws InterruptedException
//   {
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount());
//      epicsChannelMonitoringService.startMonitoring(createWicaChannel("test:db_ok" ) );
//      Thread.sleep( 1_000 );
//      assertEquals(1, epicsChannelMonitoringService.getChannelsConnectedCount());
//      epicsChannelMonitoringService.close();
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount());
//   }

//   @Test
//   void testStartMonitoring_verifyInitialConnectBehaviour_HandlersAreNotNotifiedIfChannelOffline() throws InterruptedException
//   {
//      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
//      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
//      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel("non-existent-channel" ) );
//      Thread.sleep( 1_000 );
//      Mockito.verify( epicsEventPublisherMock, never() ).accept( anyBoolean() );
//      Mockito.verify( valueChangeHandlerMock, never() ).accept( any() );
//   }
//
//   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
//   // By default this test is suppressed as it would create problems in the automatic
//   // build system. The test should be enabled as required during pre-production testing.
//   @Disabled
//   @Test
//   void testStartMonitoring_verifyInitialConnectBehaviour_NotificationSequence() throws InterruptedException
//   {
//      assertEquals(0, epicsChannelMonitoringService.getChannelsActiveCount() );
//      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
//      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel("test:db_ok" ), stateChangeHandlerMock, ( m)->{}, valueChangeHandlerMock );
//      Thread.sleep( 1_000 );
//      final InOrder inOrder = inOrder( stateChangeHandlerMock, valueChangeHandlerMock );
//      inOrder.verify( stateChangeHandlerMock ).accept(true );
//   }

//   @Test
//   void testStartMonitoring_verifyMonitorDisconnectBehaviour()
//   {
//      // TODO: write this
//   }
//
//   @Test
//   void testStartMonitoring_verifyBehaviourWithDifferentChannelTypes()
//   {
//      // TODO: write this
//   }
//
//   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
//   @Disabled
//   @Test
//   void testStopMonitoring_verifyConnectionCountChanges() throws InterruptedException
//   {
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//      epicsChannelMonitoringService.startMonitoring( createWicaChannel( "test:db_ok" ) );
//      Thread.sleep( 3_000 );
//      assertEquals(1, epicsChannelMonitoringService.getChannelsConnectedCount() );
//      epicsChannelMonitoringService.stopMonitoring( createWicaChannel( "test:db_ok" ) );
//      assertEquals(0, epicsChannelMonitoringService.getChannelsConnectedCount() );
//   }

/*- Private methods ----------------------------------------------------------*/

   private WicaChannel createWicaChannel( String name )
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( name );
      final WicaChannelProperties wicaChannelProperties = new WicaChannelProperties();
      return new WicaChannel( wicaChannelName, wicaChannelProperties );
   }

   /*- Nested Classes -----------------------------------------------------------*/

   // Note: these interfaces exist to avoid the need for an unchecked cast in
   // some of the tests above
   private interface BooleanConsumer extends Consumer<Boolean> {}
   private interface EpicsChannelValueConsumer extends Consumer<WicaChannelValue> {}

}
