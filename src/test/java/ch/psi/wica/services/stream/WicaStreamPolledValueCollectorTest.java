/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.infrastructure.stream.WicaStreamBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import ch.psi.wica.model.channel.value.WicaChannelValueBuilder;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.services.channel.WicaChannelValueFilteringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamPolledValueCollectorTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @MockBean
   private WicaChannelValueFilteringService wicaChannelValueFilteringServiceMock;

   @Captor
   private ArgumentCaptor<WicaChannel> captorChannel;

   @Captor
   private ArgumentCaptor<List<WicaChannelValue>> captorValueList;

   private WicaStreamPolledValueCollectorService serviceUnderTest;
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
      // Note: CH1 DOES publish values to the polled value buffer.
      testChannel1 = WicaChannelBuilder
            .create()
            .withChannelNameAndProperties( "CH1_POLL", WicaChannelPropertiesBuilder
               .create()
               .withDefaultProperties()
               .withDataAcquisitionMode(WicaDataAcquisitionMode.POLL )
               .build() )
            .build();

      // Note: CH2 does NOT publish values to the polled value buffer.
      testChannel2 = WicaChannelBuilder
            .create()
            .withChannelNameAndProperties( "CH2_MONITOR", WicaChannelPropertiesBuilder
               .create()
               .withDefaultProperties()
               .withDataAcquisitionMode(WicaDataAcquisitionMode.MONITOR )
               .build())
            .build();

      // Note: CH3 DOES publish values to the polled value buffer.
      testChannel3 = WicaChannelBuilder
            .create()
            .withChannelNameAndProperties( "CH3_POLL_MONITOR", WicaChannelPropertiesBuilder
               .create()
               .withDefaultProperties()
               .withDataAcquisitionMode(WicaDataAcquisitionMode.POLL_MONITOR )
               .build() )
         .build();

      // Note: CH4 DOES publish values to the polled value buffer.
      testChannel4 = WicaChannelBuilder
            .create()
            .withChannelNameAndProperties( "CH4_POLL_AND_MONITOR", WicaChannelPropertiesBuilder
                  .create()
                  .withDefaultProperties()
                  .withDataAcquisitionMode(WicaDataAcquisitionMode.POLL_AND_MONITOR )
                  .build() )
            .build();

      testStream = WicaStreamBuilder
                           .create()
                           .withChannel( testChannel1 )
                           .withChannel( testChannel2 )
                           .withChannel( testChannel3 )
                           .withChannel( testChannel4 )
                           .build();

      serviceUnderTest = new WicaStreamPolledValueCollectorService( 5, wicaChannelValueFilteringServiceMock );
      given( wicaChannelValueFilteringServiceMock.filterLastValues( captorValueList.capture() ) ).willAnswer(( x) -> captorValueList.getValue() );
      given( wicaChannelValueFilteringServiceMock.filterValues(  captorChannel.capture(), captorValueList.capture() ) ).willAnswer(( x) -> captorValueList.getValue() );
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
      final WicaChannelValue someValue1A = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue1B = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue3A = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue3B = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue4A = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue4B = WicaChannelValueBuilder.createChannelValueDisconnected();

      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel1, someValue1A ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel1, someValue1B ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel3, someValue3A ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel3, someValue3B ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel4, someValue4A ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel4, someValue4B ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap = serviceUnderTest.getLatest( testStream );

      assertThat( resultMap.entrySet().size(), is( 3) );
      assertThat( resultMap.keySet(), not( hasItem( testChannel2 ) ) );
      assertThat( resultMap.keySet(), hasItems( testChannel1, testChannel3, testChannel4 ) );
      assertThat( resultMap.values(), hasItem( List.of( someValue1A, someValue1B) ) );
      assertThat( resultMap.values(), hasItem( List.of( someValue3A, someValue3B) ) );
      assertThat( resultMap.values(), hasItem( List.of( someValue4A, someValue4B) ) );
   }

   @Test
   void test_get()
   {
      final LocalDateTime beginTime = LocalDateTime.now();
      final WicaChannelValue someValue1A = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue1B = WicaChannelValueBuilder.createChannelValueDisconnected();
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel1, someValue1A ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel1, someValue1B ) );

      final LocalDateTime middleTime = LocalDateTime.now();

      final WicaChannelValue someValue3A = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue3B = WicaChannelValueBuilder.createChannelValueDisconnected();
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel3, someValue3A ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel3, someValue3B ) );

      final LocalDateTime endTime = LocalDateTime.now();
      final WicaChannelValue someValue4A = WicaChannelValueBuilder.createChannelValueDisconnected();
      final WicaChannelValue someValue4B = WicaChannelValueBuilder.createChannelValueDisconnected();
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel4, someValue4A ) );
      serviceUnderTest.handleUpdateEvent( new WicaChannelPolledValueUpdateEvent( testChannel4, someValue4B ) );


      final Map<WicaChannel, List<WicaChannelValue>> resultMap1A = serviceUnderTest.get( testStream, LocalDateTime.MIN );
      assertThat( resultMap1A.entrySet().size(), is( 3 ) );
      assertThat( resultMap1A.keySet(), not( hasItem( testChannel2 ) ) );
      assertThat( resultMap1A.keySet(), hasItems( testChannel1, testChannel3, testChannel4 ) );
      assertThat( resultMap1A.values(), hasItem( List.of( someValue1A, someValue1B) ) );
      assertThat( resultMap1A.values(), hasItem( List.of( someValue3A, someValue3B) ) );
      assertThat( resultMap1A.values(), hasItem( List.of( someValue4A, someValue4B) ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap1B = serviceUnderTest.get( testStream, beginTime );
      assertThat( resultMap1B.entrySet().size(), is( 3 ) );
      assertThat( resultMap1B.keySet(), not( hasItem( testChannel2 ) ) );
      assertThat( resultMap1B.keySet(), hasItems( testChannel1, testChannel3, testChannel4 ) );
      assertThat( resultMap1B.values(), hasItem( List.of( someValue1A, someValue1B) ) );
      assertThat( resultMap1B.values(), hasItem( List.of( someValue3A, someValue3B) ) );
      assertThat( resultMap1B.values(), hasItem( List.of( someValue4A, someValue4B) ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap2 = serviceUnderTest.get( testStream, middleTime );
      assertThat( resultMap2.entrySet().size(), is( 2 ) );
      assertThat( resultMap2.keySet(), hasItems( testChannel3, testChannel4 ) );
      assertThat( resultMap2.keySet(), not( hasItem( testChannel1 ) ) );
      assertThat( resultMap2.keySet(), not( hasItem( testChannel2 ) ) );
      assertThat( resultMap2.values(), hasItem( List.of( someValue3A, someValue3B) ) );
      assertThat( resultMap2.values(), hasItem( List.of( someValue4A, someValue4B) ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap3 = serviceUnderTest.get( testStream, endTime );
      assertThat( resultMap3.entrySet().size(), is( 1 ) );
      assertThat( resultMap3.keySet(), hasItems( testChannel4 ) );
      assertThat( resultMap3.keySet(), not( hasItem( testChannel1 ) ) );
      assertThat( resultMap3.keySet(), not( hasItem( testChannel2 ) ) );
      assertThat( resultMap3.keySet(), not( hasItem( testChannel3 ) ) );
      assertThat( resultMap3.values(), hasItem( List.of( someValue4A, someValue4B) ) );

      final Map<WicaChannel, List<WicaChannelValue>> resultMap4 = serviceUnderTest.get( testStream, LocalDateTime.MAX );
      assertThat( resultMap4.entrySet().size(), is( 0 ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
