/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.epics.EpicsControlSystemMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith( SpringRunner.class)
@SpringBootTest
class WicaStreamDataSupplierTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private WicaStreamService wicaStreamService;

   @Autowired
   private EpicsControlSystemMonitoringService epicsService;

   private WicaStreamDataSupplier supplier;

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
      final WicaStream stream = wicaStreamService.create( testString );
      supplier = new WicaStreamDataSupplier( stream, new WicaChannelMetadataStash(), new WicaChannelValueStash( 16 ) );
   }

   @Test
   void test_getNotifiedValues()
   {
      // Verify that a first call to getNotifiedValues does indeed get everything
      final var valueMap = supplier.getNotifiedValues();
      assertEquals( 3, valueMap.size() );

      assertTrue( valueMap.containsKey( WicaChannelName.of( "CH1##1") ) );
      final var valueList1 = valueMap.get( WicaChannelName.of( "CH1##1") );
      assertTrue( valueList1.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList1.size() );

      assertTrue( valueMap.containsKey( WicaChannelName.of( "CH1##2") ) );
      final var valueList2 = valueMap.get( WicaChannelName.of( "CH1##2" ) );
      assertTrue( valueList2.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList2.size() );

      assertTrue( valueMap.containsKey( WicaChannelName.of( "CH2" ) ) );
      final var valueList3 = valueMap.get( WicaChannelName.of( "CH2" ) );
      assertTrue( valueList3.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList3.size() );
   }

   @Test
   void test_getNotifiedValueChanges()
   {
      // Verify that a first call to getNotifiedValues does indeed get everything
      final var valueMap = supplier.getNotifiedValueChanges();
      assertEquals( 3, valueMap.size() );

      assertTrue( valueMap.containsKey( WicaChannelName.of("CH1##1") ) );
      final var valueList1 = valueMap.get( WicaChannelName.of("CH1##1") );
      assertTrue( valueList1.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList1.size() );

      assertTrue( valueMap.containsKey( WicaChannelName.of("CH1##2") ) );
      final var valueList2 = valueMap.get( WicaChannelName.of("CH1##2" ) );
      assertTrue( valueList2.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList2.size() );

      assertTrue( valueMap.containsKey(WicaChannelName.of("CH2") ) );
      final var valueList3 = valueMap.get(WicaChannelName.of("CH2") );
      assertTrue( valueList3.get( 0 ) instanceof WicaChannelValue.WicaChannelValueDisconnected);
      assertEquals( 1, valueList3.size() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
