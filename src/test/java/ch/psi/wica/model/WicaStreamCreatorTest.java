/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.services.stream.WicaStreamCreator;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith( SpringRunner.class)
@SpringBootTest
class WicaStreamCreatorTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamCreatorTest.class );
   private WicaChannelValueStash stash;

   @Autowired
   public WicaStreamCreator creator;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   public void testGoodDecodeSequence()
   {
      String testString = "{ \"props\" : {}, \"channels\":  [ { \"name\": \"MHC1:IST:2\" } ] }";
      final WicaStream stream = creator.create( testString );

      assertEquals( 10000, stream.getHeartbeatFluxInterval() );
      assertEquals( 100, stream.getChannelValueUpdateFluxInterval() );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
