/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.EventReceiverMock;
import ch.psi.wica.controlsystem.epics.channel.EpicsChannelManager;
import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import ch.psi.wica.model.channel.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

// Need to rewire the service after each test since it will previously
// have been closed down at the end of the previous test.
@DirtiesContext( classMode= DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD )
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class EpicsChannelMetadataServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private EpicsChannelManager.EpicsPolledChannelManagerService epicsChannelManager;

   @Autowired
   private EpicsChannelMetadataService epicsChannelMetadataService;

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
      assertThat( epicsChannelMetadataService.getStatistics().getStartRequests(), is( "0") );
      assertThat( epicsChannelMetadataService.getStatistics().getStopRequests(), is( "0") );
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0") );
      assertThat( epicsChannelMetadataService.getStatistics().getChannelConnectCount(), is( "0") );
   }

   @AfterEach
   void afterEach()
   {
      epicsChannelMetadataService.close();
      assertThat( epicsChannelMetadataService.getStatistics().getStartRequests(), is( "0") );
      assertThat( epicsChannelMetadataService.getStatistics().getStopRequests(), is( "0") );
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0") );
      assertThat( epicsChannelMetadataService.getStatistics().getChannelConnectCount(), is( "0") );
   }

   @Test
   void testStartDataAcquisition_ThrowsNullPointerExceptionWhenEpicsChannelNamesIsNull()
   {
      assertThrows( NullPointerException.class, () -> epicsChannelMetadataService.startDataAcquisition( null ) );
   }

   @Test
   void testStartDataAcquisition_ThrowsIllegalStateExceptionWhenChannelNameNotUnique()
   {
      epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "abcd" ));
      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "abcd" ) ) );
      assertThat( ex.getMessage(), is( "The metadata request object is already active." ) );
   }

   @Test
   void testStartDataAcquisition_CheckChannelStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Confirm that initially no channels have been created and that nothing is connected
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0" ) );

      // Verify that a call to start data acquisition results in an increase in the start request count
      epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "offline-channel-1" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getStartRequests(), is( "1" ) );
   }

   @Test
   void testStopDataAcquisition_ThrowsIllegalStateExceptionWhenStoppingMonitoringChannelThatWasNeverPreviouslyMonitored()
   {
      final var ex = assertThrows( IllegalStateException.class, () -> epicsChannelMetadataService.stopDataAcquisition( createMetadataRequest( "unknown-channel" ) ) );
      assertThat( ex.getMessage(), is( "The metadata request object was not recognised.") );
   }

   @Test
   void testStopDataAcquisition_CheckChannelStatisticsAsExpectedWhenDisposingOfflineChannels()
   {
      epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "offline-channel-1" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "1" ) );

      epicsChannelMetadataService.stopDataAcquisition( createMetadataRequest( "offline-channel-1" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0" ) );
   }

   @Test
   void testStartDataAcquisition_CheckStatisticsAsExpectedWhenDealingWithOfflineChannels()
   {
      // Verify that attempting to monitor a non-existent channel does not result in the
      // monitor count increasing.
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0" ) );

      epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "non-existent-channel-1" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "1" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getChannelConnectCount(), is( "0" ) );

      epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "non-existent-channel-2" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "2" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getChannelConnectCount(), is( "0" ) );

      epicsChannelMetadataService.close();
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0" ) );
   }

   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   // Please run the EPICS IOC 'epics_tests.db' defined in 'src/test/resources/epics'.
   // Also, you may need to ensure that your local VPN is not active.
   @Disabled
   @Test
   void testGetChannelConnectCount() throws InterruptedException
   {
      epicsChannelManager.createChannel( EpicsChannelName.of(  "wica:test:db_ok" ) );
      epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "wica:test:db_ok" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getStartRequests(), is( "1" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "1" ) );
      Thread.sleep( 1_000 );
      assertThat( epicsChannelMetadataService.getStatistics().getChannelConnectCount(), is( "1" ) );

      epicsChannelMetadataService.close();
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0" ) );
      assertThat( epicsChannelMetadataService.getStatistics().getChannelConnectCount(), is( "0" ) );
   }

   @Test
   void testStartDataAcquisition_verifyInitialConnectBehaviour_HandlersAreNotNotifiedIfChannelOffline() throws InterruptedException
   {
      assertThat( epicsChannelMetadataService.getStatistics().getActiveChannels(), is( "0" ) );
      epicsChannelMetadataService.startDataAcquisition( createMetadataRequest( "non-existent-channel" ) );
      Thread.sleep( 1_000 );
      assertThat( eventReceiverMock.getMetadataPublishedTimestamp().isEmpty(), is( true ) );
      assertThat( eventReceiverMock.getValuePublishedTimestamp().isEmpty(), is( true ) );
   }

/*- Private methods ----------------------------------------------------------*/

   private EpicsChannelMetadataRequest createMetadataRequest( String name )
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( name );
      final WicaChannelProperties wicaChannelProperties = new WicaChannelProperties();
      final WicaChannel wicaChannel = new WicaChannel( wicaChannelName, wicaChannelProperties );
      return new EpicsChannelMetadataRequest( wicaChannel );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
