/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


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
                                                      16,
                                                      17.0 );


      final WicaChannel objectUnderTest =  new WicaChannel( testName, testProps );

      assertThat( objectUnderTest.getName(), is( testName ) );
      assertThat( objectUnderTest.getProperties(), is( testProps ) );
   }

   @Test
   void testIsValueObject()
   {
      final WicaChannelName nameN1 = WicaChannelName.of(  "N" );
      final WicaChannelName nameN2 = WicaChannelName.of(  "N" );
      final WicaChannelProperties propsP1 = WicaChannelPropertiesBuilder.create().withFieldsOfInterest( "P" ).build();
      final WicaChannelProperties propsP2 = WicaChannelPropertiesBuilder.create().withFieldsOfInterest( "P" ).build();
      final WicaChannel wicaChannelN1P1 = new WicaChannel( nameN1, propsP1 );
      final WicaChannel wicaChannelN1P2 = new WicaChannel( nameN1, propsP2 );
      final WicaChannel wicaChannelN2P1 = new WicaChannel( nameN2, propsP1 );
      final WicaChannel wicaChannelN2P2 = new WicaChannel( nameN2, propsP2 );
      assertThat( wicaChannelN1P1, is( wicaChannelN1P2 ) );
      assertThat( wicaChannelN2P1, is( wicaChannelN2P2 ) );
      assertThat( wicaChannelN1P1, is( wicaChannelN2P2 ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
