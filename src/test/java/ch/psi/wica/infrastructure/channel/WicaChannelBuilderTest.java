/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelBuilderTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testCreateFromNameAndEmptyProperties()
   {
      final String testName = "simon";
      final WicaChannelProperties emptyProps = WicaChannelPropertiesBuilder.create().build();

      final WicaChannel objectUnderTest = WicaChannelBuilder.create().withChannelNameAndEmptyProperties(testName ).build();

      assertThat( objectUnderTest.getName().toString(), is( testName ) );
      assertThat( objectUnderTest.getProperties(), is( emptyProps) );
   }

   @Test
   void testCreateFromNameAndDefaultProperties()
   {
      final String testName = "simon";
      final WicaChannelProperties defaultProps = WicaChannelPropertiesBuilder.create().withDefaultProperties().build();

      final WicaChannel objectUnderTest = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( testName ).build();

      assertThat( objectUnderTest.getName().toString(), is( testName ) );
      assertThat( objectUnderTest.getProperties(), is( defaultProps ) );
   }

   @Test
   void testCreateFromNameAndProperties()
   {
      final String testName = "simon";
      final WicaChannelProperties testProps = WicaChannelPropertiesBuilder.create().withNumericPrecision( 55 ).build();

      final WicaChannel objectUnderTest = WicaChannelBuilder.create().withChannelNameAndProperties( testName, testProps ).build();

      assertThat( objectUnderTest.getName().toString(), is( testName ) );
      assertThat( objectUnderTest.getProperties(), is( testProps ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
