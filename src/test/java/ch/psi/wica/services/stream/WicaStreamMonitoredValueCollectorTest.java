/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamMonitoredValueCollectorTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of(1961, 8, 25, 0, 0 );

   @Autowired
   private WicaStreamLifecycleService wicaStreamLifecycleService;

   @Autowired
   private WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService;

   @Autowired
   private WicaStreamMonitoredValueCollectorService wicaStreamPolledValueCollectorService;

   private WicaStream wicaStream;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setUp()
   {
      final String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"CH1##1\" }, " +
                                                                   "{ \"name\": \"CH1##2\" }, " +
                                                                   "{ \"name\": \"CH2\" }  ] }";

      wicaStream = wicaStreamLifecycleService.create( testString );
   }

   @Test
   void test_getMonitoredValueCollector()
   {
      final var valueMap = wicaStreamMonitoredValueCollectorService.get( wicaStream, LONG_AGO );
      final WicaChannel testChannel1 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CH1##1").build();
      final WicaChannel testChannel2 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CH1##2").build();
      final WicaChannel testChannel3 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CH2").build();

      assertThat( valueMap.size(), is( 3) );

      assertThat( valueMap.containsKey( testChannel1 ), is(true )  );
      final var valueList1 = valueMap.get( testChannel1 );
      assertThat( valueList1.size(), is( 1 ) );
      assertThat( valueList1.get( 0 ), instanceOf( WicaChannelValue.WicaChannelValueDisconnected.class ) );

      assertThat(valueMap.containsKey( testChannel2 ), is(true) );
      final var valueList2 = valueMap.get( testChannel2 );
      assertThat( valueList2.get( 0 ), instanceOf( WicaChannelValue.WicaChannelValueDisconnected.class ) );
      assertThat( valueList2.size(), is( 1 ) );

      assertThat( valueMap.containsKey( testChannel3 ), is(true ) );
      final var valueList3 = valueMap.get( testChannel3 );
      assertThat( valueList3.get( 0 ), instanceOf( WicaChannelValue.WicaChannelValueDisconnected.class ) );
      assertThat( valueList3.size(), is( 1 ) );
   }

   @Test
   void test_getPolledValueCollector()
   {
      final var valueMap = wicaStreamPolledValueCollectorService.get( wicaStream, LONG_AGO );
      final WicaChannel testChannel1 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CH1##1").build();
      final WicaChannel testChannel2 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CH1##2").build();
      final WicaChannel testChannel3 = WicaChannelBuilder.create().withChannelNameAndDefaultProperties("CH2").build();

      assertThat( valueMap.size(), is( 3) );

      assertThat( valueMap.containsKey( testChannel1 ), is(true )  );
      final var valueList1 = valueMap.get( testChannel1 );
      assertThat( valueList1.size(), is( 1 ) );
      assertThat( valueList1.get( 0 ), instanceOf( WicaChannelValue.WicaChannelValueDisconnected.class ) );

      assertThat(valueMap.containsKey( testChannel2 ), is(true) );
      final var valueList2 = valueMap.get( testChannel2 );
      assertThat( valueList2.get( 0 ), instanceOf( WicaChannelValue.WicaChannelValueDisconnected.class ) );
      assertThat( valueList2.size(), is( 1 ) );

      assertThat( valueMap.containsKey( testChannel3 ), is(true ) );
      final var valueList3 = valueMap.get( testChannel3 );
      assertThat( valueList3.get( 0 ), instanceOf( WicaChannelValue.WicaChannelValueDisconnected.class ) );
      assertThat( valueList3.size(), is( 1 ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
