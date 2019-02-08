/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.epics.EpicsChannelDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
   public WicaStreamService wicaStreamService;

   @Autowired
   public EpicsChannelDataService epicsService;

   @Value( "${wica.default_heartbeat_flux_interval}" )
   private int defaultHeartBeatFluxInterval;

   @Value( "${wica.default_channel_value_update_flux_interval}" )
   private int defaultChannelValueUpdateFluxInterval;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setUp()
   {
      String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"MHC1:IST:2\" }, { \"name\": \"MHC2:IST:2\" }  ] }";
      final WicaStream stream = wicaStreamService.create( testString );

      WicaStreamDataSupplier supplier = new WicaStreamDataSupplier( stream,epicsService );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
