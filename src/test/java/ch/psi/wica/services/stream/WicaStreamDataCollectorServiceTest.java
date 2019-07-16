/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE )
class WicaStreamDataCollectorServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private WicaStreamLifecycleService wicaStreamLifecycleService;

   @Autowired
   private WicaStreamDataCollectorService wicaStreamDataCollectorService;

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

      wicaStream = wicaStreamLifecycleService.create(testString );
   }

   @Test
   void test_getInitialValueMap()
   {
      // Verify that a first call to getFilteredMonitoredValueMap does indeed get everything
      final var valueMap = wicaStreamDataCollectorService.getInitialValueMap( wicaStream );
      assertEquals( 3, valueMap.size() );

      assertTrue( valueMap.containsKey( WicaChannel.createFromName( "CH1##1") ) );
      final var valueList1 = valueMap.get( WicaChannel.createFromName( "CH1##1") );
      assertTrue( valueList1.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList1.size() );

      assertTrue( valueMap.containsKey( WicaChannel.createFromName( "CH1##2") ) );
      final var valueList2 = valueMap.get( WicaChannel.createFromName( "CH1##2" ) );
      assertTrue( valueList2.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList2.size() );

      assertTrue( valueMap.containsKey( WicaChannel.createFromName( "CH2" ) ) );
      final var valueList3 = valueMap.get( WicaChannel.createFromName( "CH2" ) );
      assertTrue( valueList3.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList3.size() );
   }

   @Test
   void test_getFilteredChangedValueMap()
   {
      // Verify that a first call to getFilteredMonitoredValueMap does indeed get everything
      final var valueMap = wicaStreamDataCollectorService.getChangedValueMap(wicaStream );
      assertEquals( 3, valueMap.size() );

      assertTrue( valueMap.containsKey( WicaChannel.createFromName("CH1##1") ) );
      final var valueList1 = valueMap.get( WicaChannel.createFromName("CH1##1") );
      assertTrue( valueList1.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList1.size() );

      assertTrue( valueMap.containsKey( WicaChannel.createFromName("CH1##2") ) );
      final var valueList2 = valueMap.get( WicaChannel.createFromName("CH1##2" ) );
      assertTrue( valueList2.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList2.size() );

      assertTrue( valueMap.containsKey( WicaChannel.createFromName("CH2") ) );
      final var valueList3 = valueMap.get(WicaChannel.createFromName("CH2") );
      assertTrue( valueList3.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList3.size() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
