/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaStreamTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testEmptyConstructor()
   {
      final var objectUnderTest = new WicaStream();
      assertThat( objectUnderTest.getWicaChannels(),         nullValue() );
      assertThat( objectUnderTest.getWicaStreamId(),         nullValue() );
      assertThat( objectUnderTest.getWicaStreamProperties(), nullValue() );
   }

   @Test
   void testFullConstructor()
   {
      final var testId = WicaStreamId.createNext();
      final var testProps = new WicaStreamProperties(20,
                                                     21,
                                                     22,
                                                     23,
                                                      WicaDataAcquisitionMode.MONITOR,
                                                     11,
                                                     "fields",
                                                     12,
                                                      WicaFilterType.LAST_N,
                                                     13,
                                                     14,
                                                     15,
                                                     16.0 );

      final WicaStream objectUnderTest =  new WicaStream( testId, testProps, Set.of() );
      assertThat( objectUnderTest.getWicaStreamId(), is( testId ) );
      assertThat( objectUnderTest.getWicaStreamProperties(), is( testProps ) );
      assertThat( objectUnderTest.getWicaChannels(), is( Set.of() ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
