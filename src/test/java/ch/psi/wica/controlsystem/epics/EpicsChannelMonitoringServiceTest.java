/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.BeforeClass;
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


   private static String[] proscanChannelNames = new String[] { "IMJI:IST:2", "XPROSCAN:STAB:2", "YMJHG:IST:2", "EMJCYV:STA3:2", "EMJCYV:IST:2", "MMAP6Y:PROF:2", "MMAV6:IST:2", "SMJ1X:IST:2", "YMJHH:SPARB:2", "EMJCYV:STAW:1", "FMJIP:IST:2", "RPS-HFRD:STA:1", "FMJEP:IST:2", "IMJGF:IST:2", "BMB1:STAP:2", "FMJIPI:BREI:2", "BMD1:STAR:2", "YMJHL:IST:2", "BMC1:STA:2", "UMJSSB:BHFX:1", "UMJSSB:BIQX:1", "BMD1:STA:2", "AMJHS-I:IADC:2", "QMA1:IST:2", "MMAP5X:SPB:2", "PRO:REG2D:X", "BME1:STAP:2", "PRO:REG2D:Y", "XPROREG:STAB:1", "SMA1Y:IST:2", "MMAP5X:PROF:2:P", "BMD2:STA:2", "EMJEC2V:IST:2", "MMAP6Y:SPB:2", "CMJSEV:PWRF:2", "BMC1:STAR:2", "BMD2:STAR:2", "BMA1:STAP:2", "MMJP2:IST1:2", "RPS-IQ:STA:1", "SMA1X:IST:2", "XPROIONS:IST1:2", "PRO:CURRENTALARM:1", "UMJSSB:BDEX:1", "YMJCS2K:IST:2", "YMJCS1K:IST:2", "FMJEPI:POS:2", "MMAC3:STR:2", "XPROSCAN:TIME:2", "AMAKI1:IST:2", "EMJEC1V:IST:2", "MMAP6Y:PROF:2:P", "IMJV:IST:2", "QMA2:IST:2", "SMJ2Y:IST:2", "FMJIPI:POS:2", "PRO:REG2D:Y:2", "BMB1:STAR:2", "BMB1:STA:2", "FMJEPI:BREI:2", "MMAC:SOL:2", "MMJP2:IST2:2", "YMJHH:STR:2", "BME1:STA:2", "BMA1:STA:2", "BME1:STAR:2", "UMJSSB:BHRX:1", "MMAP5X:PROF:2", "RPS-HF:STA:1", "YMJKKRT:IST:2", "DMAD1:IST:2", "BMC1:STAP:2", "QMA3:IST:2", "BMD2:STAP:2", "CMJLL:SOLA:2", "BMA1:STAR:2", "EMJCYV:CTRL:1", "PRO:REG2D:X:2", "MMJF:IST:2" };

   private static String[] test100ChannelNames = new String[] { "wica:test:counter00", "wica:test:counter01", "wica:test:counter02", "wica:test:counter03", "wica:test:counter04",
                                                                "wica:test:counter05", "wica:test:counter06", "wica:test:counter07", "wica:test:counter08", "wica:test:counter09",
                                                                "wica:test:counter10", "wica:test:counter11", "wica:test:counter12", "wica:test:counter13", "wica:test:counter14",
                                                                "wica:test:counter15", "wica:test:counter16", "wica:test:counter17", "wica:test:counter18", "wica:test:counter19"  };

   /*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Package-access methods ---------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
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

   @Test
   void testStartMonitoring_OneHundredChannelsConnectTime() throws InterruptedException
   {
      for ( String channel : proscanChannelNames )
      {
         epicsChannelMonitoringService.startMonitoring( createWicaChannel( channel ));
      }

      Thread.sleep( 1000 );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "79" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "79" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalMonitorCount(), is( "79" ) );
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
   //@Disabled
   @Test
   void testGetConnectedChannelCount() throws InterruptedException
   {
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getClosedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getDisconnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNeverConnectedChannelCount(), is( "0" ) );
      assertThat( epicsChannelMonitoringService.getStatistics().getNotConnectedChannelCount(), is( "0" ) );

      epicsChannelMonitoringService.startMonitoring(createWicaChannel("test:db_ok" ) );
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
      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
      epicsChannelMonitoringService.startMonitoring( createWicaChannel("non-existent-channel" ) );
      Thread.sleep( 1_000 );
      Mockito.verify( epicsEventPublisherMock, never() ).publishConnectionStateChanged( null, anyBoolean() );
      Mockito.verify( valueChangeHandlerMock, never() ).accept( any() );
   }

   // TODO - Test disabled for now. Need way of starting EPICS server when performing tests as part of automatic build.
   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   @Disabled
   @Test
   void testStartMonitoring_verifyInitialConnectBehaviour_NotificationSequence() throws InterruptedException
   {
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0" ) );
      final Consumer<Boolean> stateChangeHandlerMock = Mockito.mock( BooleanConsumer.class );
      final Consumer<WicaChannelValue> valueChangeHandlerMock = Mockito.mock(EpicsChannelValueConsumer.class );
      epicsChannelMonitoringService.startMonitoring( createWicaChannel("test:db_ok" ) );
      Thread.sleep( 1_000 );
      final InOrder inOrder = inOrder( stateChangeHandlerMock, valueChangeHandlerMock );
      inOrder.verify( stateChangeHandlerMock ).accept(true );
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
