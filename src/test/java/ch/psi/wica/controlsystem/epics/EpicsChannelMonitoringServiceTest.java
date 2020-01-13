/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
   private EventReceiverMock eventReceiverMock;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Package-access methods ---------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      eventReceiverMock.arm();
      assertThat( epicsChannelMonitoringService.getStatistics().getStartRequests(), is( "0") );
      assertThat( epicsChannelMonitoringService.getStatistics().getStopRequests(), is( "0") );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0") );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "0") );
   }

   @AfterEach
   void afterEach()
   {
      epicsChannelMonitoringService.close();
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0") );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "0") );
   }

   @Test
   void testStartMonitoring_ThrowsNullPointerExceptionWhenEpicsChannelNamesIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMonitoringService.startMonitoring(null ) );
   }

   @Test
   void testStartMonitoring_ThrowsIllegalStateExceptionWhenChannelNameNotUnique()
   {
      epicsChannelMonitoringService.startMonitoring( createWicaChannel( "abcd" ));
      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMonitoringService.startMonitoring( createWicaChannel( "abcd" ) ) );
      assertThat( ex.getMessage(), is( "The channel name: 'abcd' is already being monitored." ) );
   }

   // Note: to run this test the EPICS test100.db files needs to be started.
   @Test
   void testStartMonitoring_OneHundredChannelsConnectTime() throws InterruptedException
   {
      final String[] test100ChannelNames = new String[] {
            "wica:test:counter00", "wica:test:counter01", "wica:test:counter02", "wica:test:counter03", "wica:test:counter04",
            "wica:test:counter05", "wica:test:counter06", "wica:test:counter07", "wica:test:counter08", "wica:test:counter09",
            "wica:test:counter10", "wica:test:counter11", "wica:test:counter12", "wica:test:counter13", "wica:test:counter14", 
            "wica:test:counter15", "wica:test:counter16", "wica:test:counter17", "wica:test:counter18", "wica:test:counter19",
            "wica:test:counter20", "wica:test:counter21", "wica:test:counter22", "wica:test:counter23", "wica:test:counter24",
            "wica:test:counter25", "wica:test:counter26", "wica:test:counter27", "wica:test:counter28", "wica:test:counter29",
            "wica:test:counter30", "wica:test:counter31", "wica:test:counter32", "wica:test:counter33", "wica:test:counter34",
            "wica:test:counter35", "wica:test:counter36", "wica:test:counter37", "wica:test:counter38", "wica:test:counter39",
            "wica:test:counter40", "wica:test:counter41", "wica:test:counter42", "wica:test:counter43", "wica:test:counter44",
            "wica:test:counter45", "wica:test:counter46", "wica:test:counter47", "wica:test:counter48", "wica:test:counter49",
            "wica:test:counter50", "wica:test:counter51", "wica:test:counter52", "wica:test:counter53", "wica:test:counter54",
            "wica:test:counter55", "wica:test:counter56", "wica:test:counter57", "wica:test:counter58", "wica:test:counter59",
            "wica:test:counter60", "wica:test:counter61", "wica:test:counter62", "wica:test:counter63", "wica:test:counter64",
            "wica:test:counter65", "wica:test:counter66", "wica:test:counter67", "wica:test:counter68", "wica:test:counter69",
            "wica:test:counter70", "wica:test:counter71", "wica:test:counter72", "wica:test:counter73", "wica:test:counter74",
            "wica:test:counter75", "wica:test:counter76", "wica:test:counter77", "wica:test:counter78", "wica:test:counter79",
            "wica:test:counter80", "wica:test:counter81", "wica:test:counter82", "wica:test:counter83", "wica:test:counter84",
            "wica:test:counter85", "wica:test:counter86", "wica:test:counter87", "wica:test:counter88", "wica:test:counter89",
            "wica:test:counter90", "wica:test:counter91", "wica:test:counter92", "wica:test:counter93", "wica:test:counter94",
            "wica:test:counter95", "wica:test:counter96", "wica:test:counter97", "wica:test:counter98", "wica:test:counter99"
      };

      for ( String channel : test100ChannelNames )
      {
         epicsChannelMonitoringService.startMonitoring( createWicaChannel( channel ));
      }

      Thread.sleep( 500 );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "100" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "100" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "100" ) );
   }

   @Test
   void testStartMonitoring_CheckChannelStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Confirm that initially no channels have been created and that nothing is connected
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );

      // Verify that a call to monitor a channel results in an increase in the channel creation count.
      epicsChannelMonitoringService.startMonitoring( createWicaChannel("offline-channel-1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "1" ) );
   }

   @Test
   void testStopMonitoring_ThrowsIllegalStateExceptionWhenStoppingMonitoringChannelThatWasNeverPreviouslyMonitored()
   {
      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMonitoringService.stopMonitoring( createWicaChannel("unknown-channel" ) ) );
      assertThat( ex.getMessage(), is( "The channel name: 'unknown-channel' was not recognised.") );
   }

   @Test
   void testStopMonitoring_CheckChannelStatisticsAsExpectedWhenDisposingOfflineChannels()
   {
      epicsChannelMonitoringService.startMonitoring( createWicaChannel("offline-channel-1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "1" ) );

      epicsChannelMonitoringService.stopMonitoring( createWicaChannel("offline-channel-1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );
   }

   @Test
   void testStartMonitoring_CheckStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Verify that attempting to monitor a non-existent channel
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "0" ) );

      epicsChannelMonitoringService.startMonitoring( createWicaChannel("non-existent-channel-1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "0" ) );

      epicsChannelMonitoringService.startMonitoring( createWicaChannel("non-existent-channel-2" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "0" ) );

      epicsChannelMonitoringService.close();
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "0" ) );
   }

   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   @Disabled
   @Test
   void testGetConnectedChannelCount() throws InterruptedException
   {
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );

      epicsChannelMonitoringService.startMonitoring(createWicaChannel("wica:test:db_ok" ) );
      Thread.sleep( 1_000 );

      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "1" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );
      epicsChannelMonitoringService.close();

      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );
   }

   @Test
   void testStartMonitoring_verifyInitialConnectBehaviour_HandlersAreNotNotifiedIfChannelOffline() throws InterruptedException
   {
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      epicsChannelMonitoringService.startMonitoring( createWicaChannel("non-existent-channel" ) );
      Thread.sleep( 1_000 );
      assertThat( eventReceiverMock.getMetadataPublishedTimestamp().isEmpty(), is( true ) );
      assertThat( eventReceiverMock.getValuePublishedTimestamp().isEmpty(), is( true ) );
   }

   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   @Disabled
   @Test
   void testStartMonitoring_verifyInitialConnectBehaviour_NotificationSequence() throws InterruptedException
   {
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      epicsChannelMonitoringService.startMonitoring( createWicaChannel("wica:test:db_ok" ) );
      Thread.sleep( 1_000 );
      assertThat( eventReceiverMock.getMetadataPublishedTimestamp().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getValuePublishedTimestamp().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getMetadataPublishedTimestamp().get().isBefore( eventReceiverMock.getValuePublishedTimestamp().get() ), is( true) );
      assertThat( eventReceiverMock.getMetadata().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getMetadata().get().getType(), is(WicaChannelType.REAL ) );
      assertThat( eventReceiverMock.getValue().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getValue().get().isConnected(), is( true ) );
   }


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
