/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelValueTimestampRewriter;
import ch.psi.wica.infrastructure.stream.WicaStreamBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.services.channel.WicaChannelValueFilteringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamMonitoredValueCollectorTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @MockBean
   private WicaChannelValueFilteringService wicaChannelValueFilteringServiceMock;

   @MockBean
   private ApplicationEventPublisher applicationEventPublisher;

   @MockBean
   private WicaChannelValueTimestampRewriter wicaChannelValueTimestampRewriter;

   @Captor
   private ArgumentCaptor<WicaChannel> captorChannel;

   @Captor
   private ArgumentCaptor<List<WicaChannelValue>> captorValueList;

   private WicaStreamMonitoredValueCollectorService serviceUnderTest;
   private WicaStream testStream;
   private WicaChannel testChannel1;
   private WicaChannel testChannel2;
   private WicaChannel testChannel3;
   private WicaChannel testChannel4;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      // Note: CH1 DOES publish any values to the monitor buffer.
      testChannel1 = WicaChannelBuilder.create().withChannelNameAndProperties( "CH1_MONITOR", WicaChannelPropertiesBuilder
            .create()
            .withDefaultProperties()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.MONITOR )
            .build() )
            .build();

      // Note: CH2 does NOT publish any values to the monitor buffer.
      testChannel2 = WicaChannelBuilder.create().withChannelNameAndProperties( "CH2_POLL", WicaChannelPropertiesBuilder
            .create()
            .withDefaultProperties()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL )
            .build())
            .build();

      // Note: CH3 DOES publish any values to the monitor buffer.
      testChannel3 = WicaChannelBuilder.create().withChannelNameAndProperties( "CH3_POLL_AND_MONITOR", WicaChannelPropertiesBuilder
            .create()
            .withDefaultProperties()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL_AND_MONITOR )
            .build() )
            .build();

      // Note: CH4 does NOT publish any values to the monitor buffer.
      testChannel4 = WicaChannelBuilder.create().withChannelNameAndProperties( "CH4_POLL_MONITOR", WicaChannelPropertiesBuilder
            .create()
            .withDefaultProperties()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL_MONITOR )
            .build() )
            .build();

      testStream = WicaStreamBuilder
            .create()
            .withChannel( testChannel1 )
            .withChannel( testChannel2 )
            .withChannel( testChannel3 )
            .withChannel( testChannel4 )
            .build();

      serviceUnderTest = new WicaStreamMonitoredValueCollectorService( 5,
                                                                       applicationEventPublisher,
                                                                       wicaChannelValueTimestampRewriter,
                                                                       wicaChannelValueFilteringServiceMock );

      given( wicaChannelValueFilteringServiceMock.filterValues( captorChannel.capture(), captorValueList.capture() ) ).willAnswer(( x) -> captorValueList.getValue() );
      given( wicaChannelValueFilteringServiceMock.filterLastValues( captorValueList.capture() ) ).willAnswer(( x) -> captorValueList.getValue() );
   }

   @Test
   void test_initialisation()
   {
      assertThat( serviceUnderTest.getLatest( testStream ).entrySet().isEmpty(), is( true ) );
      assertThat( serviceUnderTest.get( testStream, LocalDateTime.MIN ).entrySet().isEmpty(), is( true ) );
      assertThat( serviceUnderTest.get( testStream, LocalDateTime.MAX ).entrySet().isEmpty(), is( true ) );
  }

   @Test
   void test_getLatest()
   {
      final WicaChannelValue someValue1A = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue someValue1B = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue someValue2A = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue someValue2B = WicaChannelValue.createChannelValueDisconnected();

      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel1, someValue1A ) );
      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel1, someValue1B ) );
      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel3, someValue2A ) );
      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel3, someValue2B ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap = serviceUnderTest.getLatest( testStream );

      assertThat( resultMap.entrySet().size(), is( 2 ) );
      assertThat( resultMap.keySet(), not( hasItems( testChannel2, testChannel4 ) ) );
      assertThat( resultMap.keySet(), hasItems( testChannel1, testChannel3 ) );
      assertThat( resultMap.values(), hasItem( List.of( someValue1A, someValue1B) ) );
      assertThat( resultMap.values(), hasItem( List.of( someValue2A, someValue2B) ) );
   }

   @Test
   void test_get()
   {
      final WicaChannelValue someValue1A = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue someValue1B = WicaChannelValue.createChannelValueDisconnected();
      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel1, someValue1A ) );
      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel1, someValue1B ) );

      final LocalDateTime middleTime = LocalDateTime.now();

      final WicaChannelValue someValue2A = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue someValue2B = WicaChannelValue.createChannelValueDisconnected();
      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel3, someValue2A ) );
      serviceUnderTest.handleWicaChannelMonitoredValueUpdateEvent( new WicaChannelMonitoredValueUpdateEvent( testChannel3, someValue2B ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap1 = serviceUnderTest.get( testStream, LocalDateTime.MIN );
      assertThat( resultMap1.entrySet().size(), is( 2 ) );
      assertThat( resultMap1.keySet(), not( hasItems( testChannel2, testChannel4 ) ) );
      assertThat( resultMap1.keySet(), hasItems( testChannel1, testChannel3 ) );
      assertThat( resultMap1.values(), hasItem( List.of( someValue1A, someValue1B) ) );
      assertThat( resultMap1.values(), hasItem( List.of( someValue2A, someValue2B) ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap2 = serviceUnderTest.get( testStream, middleTime );
      assertThat( resultMap2.entrySet().size(), is( 1 ) );
      assertThat( resultMap2.keySet(), hasItem( testChannel3 ) );
      assertThat( resultMap2.keySet(), not( hasItems( testChannel1, testChannel2 ) ) );
      assertThat( resultMap2.values(), hasItem( List.of( someValue2A, someValue2B) ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap3 = serviceUnderTest.get( testStream, LocalDateTime.MAX );
      assertThat( resultMap3.entrySet().size(), is( 0 ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
