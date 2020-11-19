/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelType;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

// Need to rewire the service after each test since it will previously
// have been closed down at the end of the previous test.
@DirtiesContext( classMode= DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD )
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class EpicsChannelPollingServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private EpicsChannelPollingService epicsChannelPollingService;

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
      assertThat( epicsChannelPollingService.getStatistics().getStartRequests(), is( "0") );
      assertThat( epicsChannelPollingService.getStatistics().getStopRequests(), is( "0") );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollCycleCount(), is( "0") );
      assertThat( epicsChannelPollingService.getStatistics().getPollSuccessCount(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollFailureCount(), is( "0" ) );
   }

   @AfterEach
   void afterEach()
   {
      epicsChannelPollingService.close();
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "0" ) );
   }

   @Test
   void testStartPolling_ThrowsNullPointerExceptionWhenEpicsChannelNamesIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelPollingService.startPolling( null ) );
   }

   @Test
   void testStartPolling_ThrowsIllegalStateExceptionWhenWicaChannelNotUnique()
   {
      final EpicsChannelPollingRequest pollingRequest1 = createPollRequest("abcd", 100 );
      final EpicsChannelPollingRequest pollingRequest2 = createPollRequest("abcd", 100 );
      epicsChannelPollingService.startPolling( pollingRequest1 );

      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelPollingService.startPolling( pollingRequest2 ) );
      assertThat( ex.getMessage(), is( "The request object is already active." ) );
   }

   @Test
   void testStartPolling_DoesntThrowsIllegalStateExceptionWhenChannelNameNotUniqueAndPollingIntervalsAreDifferent()
   {
      final EpicsChannelPollingRequest pollingRequest1 = createPollRequest("abcd", 100 );
      final EpicsChannelPollingRequest pollingRequest2 = createPollRequest("abcd", 101 );

      epicsChannelPollingService.startPolling( pollingRequest1 );
      assertDoesNotThrow( () -> epicsChannelPollingService.startPolling( pollingRequest2 ) );
   }

   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   // Please run the EPICS IOC 'epics_tests.db' defined in 'src/test/resources/epics'.
   // Also, you may need to ensure that your local VPN is not active.
   @Disabled
   @Test
   void testStartPolling_OneHundredOnlineChannelsConnectTime()
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

      final StopWatch stopWatch = StopWatch.createStarted();
      for ( String channel : test100ChannelNames )
      {
         epicsChannelPollingService.startPolling( createPollRequest( channel, 1000 ) );
      }

      // Verify that the connection time for 100 channels is less than 500ms.
      assertThat( stopWatch.getTime( TimeUnit.MILLISECONDS), lessThan( 500L ) );
   }

   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   // Please run the EPICS IOC 'epics_tests.db' defined in 'src/test/resources/epics'.
   // Also, you may need to ensure that your local VPN is not active.
   @Disabled
   @Test
   void testStartPolling_OneHundredOnlineChannelsStatisticsCollection() throws InterruptedException
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
         epicsChannelPollingService.startPolling( createPollRequest( channel, 1000 ) );
      }

      Thread.sleep( 2500 );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "100" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollCycleCount(), is( "200" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollFailureCount(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollSuccessCount(), is( "200" ) );
      assertThat( epicsChannelPollingService.getStatistics().getStartRequests(), is( "100" ) );
      assertThat( epicsChannelPollingService.getStatistics().getStopRequests(), is( "0" ) );
   }

   @Test
   void testStopPolling_ThrowsIllegalStateExceptionWhenStoppingPollingChannelThatWasNeverPreviouslyPolled()
   {
      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelPollingService.stopPolling( createPollRequest("unknown-channel" ) ) );
      assertThat( ex.getMessage(), is( "The request object was not recognised." ) );
   }

   @Test
   void testStartPolling_CheckChannelStatisticsAsExpectedForOfflineChannels() throws InterruptedException
   {
      final EpicsChannelPollingRequest pollRequest = createPollRequest("offline-channel", 100 );

      epicsChannelPollingService.startPolling( pollRequest );
      assertThat( epicsChannelPollingService.getStatistics().getStartRequests(), is( "1" ) );
      assertThat( epicsChannelPollingService.getStatistics().getStopRequests(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "1" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollCycleCount(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollSuccessCount(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollFailureCount(), is( "0" ) );

      Thread.sleep( 1000 );
      assertThat( epicsChannelPollingService.getStatistics().getStartRequests(), is( "1" ) );
      assertThat( epicsChannelPollingService.getStatistics().getStopRequests(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "1" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollCycleCount(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollSuccessCount(), is( "0" ) );
      assertThat( epicsChannelPollingService.getStatistics().getPollFailureCount(), is( "0" ) );

      epicsChannelPollingService.stopPolling( pollRequest );
   }

   @Test
   void testStopMonitoring_CheckChannelStatisticsAsExpectedWhenDisposingOfflineChannels() throws InterruptedException
   {
      final EpicsChannelPollingRequest pollRequest = createPollRequest("offline-channel", 500 );

      epicsChannelPollingService.startPolling( pollRequest );
      assertThat( epicsChannelPollingService.getStatistics().getStartRequests(), is( "1") );
      assertThat( epicsChannelPollingService.getStatistics().getStopRequests(), is( "0") );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "1") );
      assertThat( epicsChannelPollingService.getStatistics().getPollSuccessCount(), is( "0") );
      assertThat( epicsChannelPollingService.getStatistics().getPollFailureCount(), is( "0") );

      Thread.sleep( 800 );

      epicsChannelPollingService.stopPolling( pollRequest );
      assertThat( epicsChannelPollingService.getStatistics().getStartRequests(), is( "1") );
      assertThat( epicsChannelPollingService.getStatistics().getStopRequests(), is( "1") );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "0") );
      assertThat( epicsChannelPollingService.getStatistics().getPollSuccessCount(), is( "0") );
      assertThat( epicsChannelPollingService.getStatistics().getPollFailureCount(), is( "0") );
   }

   @Test
   void testStartPolling_CheckStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Verify that attempting to poll a non-existent channel does result in the
      // active poller count increasing.
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "0" ) );

      epicsChannelPollingService.startPolling( createPollRequest("non-existent-channel-1", 100 ) );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "1" ) );

      epicsChannelPollingService.startPolling( createPollRequest("non-existent-channel-2", 101 ) );
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "2" ) );

      epicsChannelPollingService.close();
      assertThat( epicsChannelPollingService.getStatistics().getTotalChannelCount(), is( "0" ) );
   }

   @Test
   void testStartPolling_verifyInitialConnectBehaviour_HandlersAreNotNotifiedIfChannelOffline() throws InterruptedException
   {
      epicsChannelPollingService.startPolling( createPollRequest("non-existent-channel" ) );
      Thread.sleep( 1000 );
      assertThat( eventReceiverMock.getMetadataPublishedTimestamp().isEmpty(), is( true ) );
      assertThat( eventReceiverMock.getValuePublishedTimestamp().isEmpty(), is( true ) );
   }

   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   // Please run the EPICS IOC 'epics_tests.db' defined in 'src/test/resources/epics'.
   // Also, you may need to ensure that your local VPN is not active.
   @Disabled
   @Test
   void testStartPolling_verifyInitialConnectBehaviour_NotificationSequence() throws InterruptedException
   {
      epicsChannelPollingService.startPolling( createPollRequest("wica:test:db_ok" ) );
      Thread.sleep( 2500 );
      assertThat( eventReceiverMock.getMetadataPublishedTimestamp().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getValuePublishedTimestamp().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getMetadataPublishedTimestamp().get().isBefore( eventReceiverMock.getValuePublishedTimestamp().get() ), is( true) );
      assertThat( eventReceiverMock.getMetadata().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getMetadata().get().getType(), is(WicaChannelType.REAL ) );
      assertThat( eventReceiverMock.getValue().isPresent(), is( true ) );
      assertThat( eventReceiverMock.getValue().get().isConnected(), is( true ) );
   }


/*- Private methods ----------------------------------------------------------*/

   private EpicsChannelPollingRequest createPollRequest( String name )
   {
      return createPollRequest( name, 4000 );
   }

   private EpicsChannelPollingRequest createPollRequest( String name, int pollingInterval )
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( name );
      final WicaChannelProperties wicaChannelProperties = WicaChannelPropertiesBuilder
            .create()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL )
            .withPollingIntervalInMillis( pollingInterval )
            .build();

      final WicaChannel wicaChannel = new WicaChannel( wicaChannelName, wicaChannelProperties );
      return new EpicsChannelPollingRequest( wicaChannel );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
