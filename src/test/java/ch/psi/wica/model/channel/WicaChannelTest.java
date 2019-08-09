/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelTest
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
      final var objectUnderTest = new WicaChannel();
      assertThat( objectUnderTest.getName(),         nullValue() );
      assertThat( objectUnderTest.getProperties(),   nullValue() );
      assertThat( objectUnderTest.getNameAsString(), is ("" ) );
   }

   @Test
   void testFullConstructor()
   {
      final var testName = WicaChannelName.of( "simon" );
      final var testProps = new WicaChannelProperties( WicaDataAcquisitionMode.MONITOR,
                                                      11,
                                                      "fields",
                                                      12,
                                                       WicaFilterType.LAST_N,
                                                      13,
                                                      14,
                                                      15,
                                                      16.0 );


      final WicaChannel objectUnderTest =  new WicaChannel( testName, testProps );

      assertThat( objectUnderTest.getName(), is( testName ) );
      assertThat( objectUnderTest.getProperties(), is( testProps ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
