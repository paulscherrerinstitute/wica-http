/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.util;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class JsonStringFormatterTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testPrettyPrint()
   {
      final String result = JsonStringFormatter.prettyFormat( "{\"value1\":\"NaN\",\"value2\":\"Infinity\"  }" );
      assertThat( result, is( "{\n  \"value1\" : \"NaN\",\n  \"value2\" : \"Infinity\"\n}" ) );

   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
