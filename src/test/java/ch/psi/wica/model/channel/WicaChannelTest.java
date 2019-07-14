/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
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
   void testCreateFromWicaChannelNameObject()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "simon" );
      final WicaChannel objectUnderTest = WicaChannel.createFromName( wicaChannelName );
      final WicaChannelProperties props = objectUnderTest.getProperties();
      assertThat( props, is( WicaChannelProperties.createDefaultInstance() ) );
   }

   @Test
   void testCreateFromWicaChannelStringName()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "simon" );
      final WicaChannel objectUnderTest = WicaChannel.createFromName( "simon" );
      final WicaChannelProperties props = objectUnderTest.getProperties();
      assertThat( props, is( WicaChannelProperties.createDefaultInstance() ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
