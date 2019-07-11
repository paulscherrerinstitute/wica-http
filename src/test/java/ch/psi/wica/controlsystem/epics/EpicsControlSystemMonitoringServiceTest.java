/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.channel.WicaChannelMetadataBufferService;
import ch.psi.wica.services.channel.WicaChannelValueBufferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class EpicsControlSystemMonitoringServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private WicaChannelMetadataBufferService wicaChannelMetadataBufferServiceMock;
   private WicaChannelValueBufferService wicaChannelValueBufferServiceMock;

   private EpicsControlSystemMonitoringService epicsControlSystemMonitoringService;

   @Captor
   private ArgumentCaptor<EpicsChannelName> channelNameCaptor;

   @Captor
   private ArgumentCaptor<Consumer<Boolean>> stateChangedCaptor;

   @Captor
   private ArgumentCaptor<Consumer<WicaChannelMetadata>> metadataChangedCaptor;

   @Captor
   ArgumentCaptor<Consumer<WicaChannelValue>> valueChangedCaptor;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach()
   void setup()
   {
      MockitoAnnotations.initMocks(this );
      final EpicsChannelMonitorService monitorServiceMock = Mockito.mock( EpicsChannelMonitorService.class );
      wicaChannelMetadataBufferServiceMock = Mockito.mock(WicaChannelMetadataBufferService.class );
      wicaChannelValueBufferServiceMock = Mockito.mock(WicaChannelValueBufferService.class );

      epicsControlSystemMonitoringService = new EpicsControlSystemMonitoringService( monitorServiceMock,
                                                                                     wicaChannelMetadataBufferServiceMock,
                                                                                     wicaChannelValueBufferServiceMock);
   }

   @Test
   void testConstructor_ThrowsNullPointerExceptionWhenArgumentNull()
   {
      assertThrows( NullPointerException.class, () -> new EpicsControlSystemMonitoringService(null,
                                                                                              wicaChannelMetadataBufferServiceMock,
                                                                                              wicaChannelValueBufferServiceMock) );
   }

   @Test
   void testStartMonitoring()
   {
      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
      assertEquals(0, epicsControlSystemMonitoringService.getInterestCountForChannel(myWicaChannelName ) );
      epicsControlSystemMonitoringService.startMonitoring(myWicaChannelName );
      assertEquals(1, epicsControlSystemMonitoringService.getInterestCountForChannel(myWicaChannelName ) );
      epicsControlSystemMonitoringService.startMonitoring(myWicaChannelName );
      assertEquals(2, epicsControlSystemMonitoringService.getInterestCountForChannel(myWicaChannelName ) );
   }

   @Test
   void testStopMonitoring()
   {
      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
      epicsControlSystemMonitoringService.startMonitoring(myWicaChannelName );
      epicsControlSystemMonitoringService.startMonitoring(myWicaChannelName );
      epicsControlSystemMonitoringService.startMonitoring(myWicaChannelName );
      epicsControlSystemMonitoringService.stopMonitoring(myWicaChannelName );
      assertEquals(2, epicsControlSystemMonitoringService.getInterestCountForChannel(myWicaChannelName ) );
      epicsControlSystemMonitoringService.stopMonitoring(myWicaChannelName );
      assertEquals(1, epicsControlSystemMonitoringService.getInterestCountForChannel(myWicaChannelName ) );
      epicsControlSystemMonitoringService.stopMonitoring(myWicaChannelName );
      assertEquals(0, epicsControlSystemMonitoringService.getInterestCountForChannel(myWicaChannelName ) );
   }

   // TODO Restore the tests below or eliminate them
//   @Test
//   void testStartMonitoringChannel()
//   {
//      // Create a Wica Channel and start monitoring it
//      final var epicsControlSystemMonitoringService = new EpicsWicaDataMonitoringService(monitorServiceMock );
//      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
//
//      epicsControlSystemMonitoringService.startMonitoring( myWicaChannelName );
//
//      // Check that this service registers with the EpicsMonitorService and provides it with the
//      // hooks to call it back when the channel connects/disconnects or receives a new value from
//      // the remote data source
//      Mockito.verify( monitorServiceMock, times(1)).startMonitoring( channelNameCaptor.capture(),
//                                                                                             stateChangedCaptor.capture(),
//                                                                                             metadataChangedCaptor.capture(),
//                                                                                             valueChangedCaptor.capture() );
//
//      // Verify that the channel name is passed correctly to the EpicsMonitorService
//      final EpicsChannelName channelName = channelNameCaptor.getValue();
//      assertEquals( myWicaChannelName.getControlSystemName(), channelName.toString() );
//
//
//      // Get the supplied callback handlers from the captors. This gives us the means
//      // of injecting various conditions back into the service under test.
//      final Consumer<Boolean> stateChangedHandler = stateChangedCaptor.getValue();
//      final Consumer<WicaChannelValue> valueChangedHandler = valueChangedCaptor.getValue();
//      final Consumer<WicaChannelMetadata> metadataChangedHandler = metadataChangedCaptor.getValue();
//
//      // First up: Tell the service-under-test that the channel is disconnected and use the
//      // observer getValue method to get the channel's current value.
//      stateChangedHandler.accept( false );
//      final WicaChannelValue observedChannelDisconnectedValue = epicsControlSystemMonitoringService.getChannelValue( myWicaChannelName );
//
//
//      // Probe the channel's various fields to check that they are as expected.
//      assertFalse( observedChannelDisconnectedValue.isConnected() );
//      final Duration delay1 = Duration.between( LocalDateTime.now(), observedChannelDisconnectedValue.getWicaServerTimestamp() );
//      assertTrue(delay1.toSecondsPart() < 1 );
//
//      // Now tell the service-under test that the channel has connected and provide
//      // initial objects for the channel's metadata and value
//      // observer getValue method to get the channel's current value.
//      stateChangedHandler.accept( true );
//      metadataChangedHandler.accept( WicaChannelMetadata.createStringInstance() );
//      valueChangedHandler.accept( WicaChannelValue.createChannelValueConnected( WicaChannelAlarmSeverity.NO_ALARM,
//                                                                                WicaChannelAlarmStatus.ofNoError(),
//                                                                                LocalDateTime.now(),
//                                                                                "MyFirstValue" ) );
//
//
//      final WicaChannelValue observedChannelConnectedValue = epicsControlSystemMonitoringService.getChannelValue( myWicaChannelName );
//
//      // Probe the channel's various fields to check that they are as expected.
//      assertTrue( observedChannelConnectedValue.isConnected() );
//      assertEquals( WicaChannelValue.WicaChannelValueConnectedString.class, observedChannelConnectedValue.getClass() );
//      assertEquals( "MyFirstValue" ,((WicaChannelValue.WicaChannelValueConnectedString) observedChannelConnectedValue).getValue() );
//      final Duration delay2 = Duration.between( LocalDateTime.now(), observedChannelConnectedValue.getWicaServerTimestamp() );
//      assertTrue(delay2.toSecondsPart() < 1 );
//      assertEquals( 0, ((WicaChannelValue.WicaChannelValueConnected) observedChannelConnectedValue).getWicaChannelAlarmStatus().getStatusCode() );
//      assertEquals( WicaChannelAlarmSeverity.NO_ALARM, ((WicaChannelValue.WicaChannelValueConnected) observedChannelConnectedValue).getWicaAlarmSeverity() );
//    }
//
//   @Test
//   void testStartMonitoringStream()
//   {
//      // Create a Wica Channel and start monitoring it
//      epicsControlSystemMonitoringService = new EpicsWicaDataMonitoringService(monitorServiceMock );
//
//      final WicaChannelValue wicaChannelValueConnected  = WicaChannelValue.createChannelValueConnected( 123 );
//      final WicaChannelName wicaChannelName1 = WicaChannelName.of( "abc" );
//      final WicaChannelName wicaChannelName2 = WicaChannelName.of( "def" );
//      final Set<WicaChannel> wicaChannelSet = Set.of( WicaChannel.of( wicaChannelName1 ), WicaChannel.of( wicaChannelName2 ) );
//      epicsControlSystemMonitoringService.startMonitoring( wicaChannelName1 );
//      epicsControlSystemMonitoringService.startMonitoring( wicaChannelName2 );
//
//      // Check that this service registers with the EpicsMonitorService and provides it with the
//      // hooks to call it back when the channel connects/disconnects or receives a new value from
//      // the remote data source
//      Mockito.verify( monitorServiceMock, times(2) ).startMonitoring( channelNameCaptor.capture(),
//                                                                                              stateChangedCaptor.capture(),
//                                                                                              metadataChangedCaptor.capture(),
//                                                                                              valueChangedCaptor.capture() );
//
//      // Verify that the channel names are passed to the EpicsMonitorService
//      // ...
//
//      // Check that the DataService makes a call on the MonitorService to ask it to start
//      // monitoring the channels that were specified in the stream.
//      final var channelNameList = channelNameCaptor.getAllValues();
//      Assert.assertThat( channelNameList.get( 0 ).toString(), isOneOf( wicaChannelName1.getControlSystemName(), wicaChannelName1.getControlSystemName() )  );
//      Assert.assertThat( channelNameList.get( 1 ).toString(), isOneOf( wicaChannelName1.getControlSystemName(), wicaChannelName2.getControlSystemName() )  );
//
//      // Check that the DataService initially indicates that the channels in the stream are not connected.
//      final Map<WicaChannelName,List<WicaChannelValue>> map1 = epicsControlSystemMonitoringService.getLaterThan( wicaChannelSet, LocalDateTime.MIN );
//      assertEquals(2, map1.size() );
//      assertEquals( 1, map1.get( wicaChannelName1 ).size() );
//      assertEquals( 1, map1.get( wicaChannelName1 ).size() );
//      assertFalse( map1.get( wicaChannelName1 ).get( 0 ).isConnected() );
//      assertFalse( map1.get( wicaChannelName2 ).get( 0 ).isConnected() );
//
//      // Verify that the channels remain in the not connected state even after the connection
//      // state change handler has reconnected.
//      final List<Consumer<Boolean>> stateChangedHandlers = stateChangedCaptor.getAllValues();
//      stateChangedHandlers.get( 0 ).accept( true );
//      stateChangedHandlers.get( 1 ).accept( true );
//      final Map<WicaChannelName,List<WicaChannelValue>> map2 = epicsControlSystemMonitoringService.getLaterThan( wicaChannelSet, LocalDateTime.MIN );
//      assertEquals(2, map2.size() );
//      assertEquals( 1, map2.get( wicaChannelName1 ).size() );
//      assertEquals( 1, map2.get( wicaChannelName1 ).size() );
//      assertFalse( map2.get( wicaChannelName1 ).get( 0 ).isConnected() );
//      assertFalse( map2.get( wicaChannelName2 ).get( 0 ).isConnected() );
//
//      // Verify that if we inject a couple of values the connection state is now shown as connected.
//      final List<Consumer<WicaChannelValue>> valueChangedHandlers = valueChangedCaptor.getAllValues();
//      valueChangedHandlers.get( 0 ).accept( wicaChannelValueConnected );
//      valueChangedHandlers.get( 1 ).accept( wicaChannelValueConnected );
//      final Map<WicaChannelName,List<WicaChannelValue>> map3 = epicsControlSystemMonitoringService.getLaterThan( wicaChannelSet, LocalDateTime.MIN );
//      assertEquals(2, map3.size() );
//      assertEquals( 2, map3.get( wicaChannelName1 ).size() );
//      assertEquals( 2, map3.get( wicaChannelName1 ).size() );
//      assertTrue( map3.get( wicaChannelName1 ).get( 1 ).isConnected() );
//      assertTrue( map3.get( wicaChannelName2 ).get( 1 ).isConnected() );
//
//      // Verify the channel values
//      assertEquals( ((WicaChannelValue.WicaChannelValueConnectedInteger) wicaChannelValueConnected).getValue(), ((WicaChannelValue.WicaChannelValueConnectedInteger) map3.get(wicaChannelName1 ).get( 1 )).getValue() );
//      assertEquals( ((WicaChannelValue.WicaChannelValueConnectedInteger) wicaChannelValueConnected).getWicaAlarmSeverity(), ((WicaChannelValue.WicaChannelValueConnectedInteger) map3.get(wicaChannelName1 ).get( 1 )).getWicaAlarmSeverity() );
//      assertEquals( ((WicaChannelValue.WicaChannelValueConnectedInteger) wicaChannelValueConnected).getWicaChannelAlarmStatus(), ((WicaChannelValue.WicaChannelValueConnectedInteger) map3.get(wicaChannelName1 ).get( 1 )).getWicaChannelAlarmStatus() );
//      assertEquals( wicaChannelValueConnected.getWicaServerTimestamp(), map3.get(wicaChannelName1 ).get( 1 ).getWicaServerTimestamp() );
//
//      // Verify that the injected metadata is correctly captured
//      final WicaChannelMetadata wicaChannelMetadata  = WicaChannelMetadata.createStringInstance();
//      final List<Consumer<WicaChannelMetadata>> metadataChangedHandlers = metadataChangedCaptor.getAllValues();
//      metadataChangedHandlers.get( 0 ).accept( wicaChannelMetadata );
//      metadataChangedHandlers.get( 1 ).accept( wicaChannelMetadata );
//      final WicaChannelMetadata wicaChannelMetadataRcvd1 = epicsControlSystemMonitoringService.get( wicaChannelName1 );
//      final WicaChannelMetadata wicaChannelMetadataRcvd2 = epicsControlSystemMonitoringService.get( wicaChannelName1 );
//      assertEquals( wicaChannelMetadata.getType(), wicaChannelMetadataRcvd1.getType() );
//      assertEquals( wicaChannelMetadata.getType(), wicaChannelMetadataRcvd2.getType() );
//   }
//
//   @Test
//   void testGetLaterThan()
//   {
//      final WicaChannelName wicaChannelName1 = WicaChannelName.of("abc" );
//      final WicaChannelName wicaChannelName2 = WicaChannelName.of("def" );
//      final Set<WicaChannel> channelSet = Set.of( WicaChannel.of( wicaChannelName1 ), WicaChannel.of( wicaChannelName2 ) );
//
//      final WicaStream myStream = new WicaStream( WicaStreamId.of( "0" ),channelSet );
//      epicsControlSystemMonitoringService.startMonitoring( wicaChannelName1 );
//      epicsControlSystemMonitoringService.startMonitoring( wicaChannelName2 );
//
//      var result1 = epicsControlSystemMonitoringService.getLaterThan(channelSet, LocalDateTime.now() );
//      assertEquals( 0, result1.size() );
//      var result2 = epicsControlSystemMonitoringService.getLaterThan(channelSet, LONG_AGO );
//      assertEquals( 2, result2.size() );
//      var result3 = epicsControlSystemMonitoringService.getLaterThan(channelSet, LocalDateTime.now() );
//      assertEquals( 0, result3.size() );
//   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
