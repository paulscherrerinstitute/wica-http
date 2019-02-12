/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


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
   void test()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "simon" );
      final WicaChannel wicaChannel = new WicaChannel(wicaChannelName );
      assertEquals( wicaChannelName, wicaChannel.getName() );
      assertFalse( wicaChannel.getProperties().getFieldsOfInterest().isPresent() );
      assertFalse( wicaChannel.getProperties().getNumericPrecision().isPresent() );
      final var wicaChannelValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final var wicaChannelValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final var myValueList = List.of( wicaChannelValue1, wicaChannelValue2 );
      final var res1 = wicaChannel.apply(myValueList );
      assertEquals( 1, res1.size() );
      final var res2 = wicaChannel.apply(List.of() );
      assertEquals( 0, res2.size() );
   }



/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
