/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
import ch.psi.wica.model.WicaStreamProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith( SpringRunner.class)
@SpringBootTest
class WicaChannelPropertiesTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testUnrecognisedFieldThrowsException() throws Exception
   {
      final ObjectMapper mapper = new ObjectMapper();
      final String inputString = "{" + "\"XXX\"" + ":" + 12345 + "}";
      assertThrows( UnrecognizedPropertyException.class, () -> {
         mapper.readValue( inputString, WicaChannelProperties.class);
      } );
   }

   @Test
   void testAllFieldsPresentDecodedOk() throws Exception
   {
      final ObjectMapper mapper = new ObjectMapper();

      final String inputString = "{" + "\"filter\"" + ":" + "\"last-n\"" + "," +
                                       "\"fparam\"" + ":" + 99 + "," +
                                       "\"fields\"" + ":" + "\"abc;def\"" + "," +
                                       "\"prec\"" + ":" + 9 + "}";

      final WicaChannelProperties props = mapper.readValue( inputString, WicaChannelProperties.class );
      assertEquals( WicaChannelProperties.FilterType.LAST_N, props.getFilterType() );

   }

   @Test
   void testDefaultFieldValues() throws Exception
   {
      final ObjectMapper mapper = new ObjectMapper();
      final String inputString = "{}";
      final WicaChannelProperties props = mapper.readValue( inputString, WicaChannelProperties.class );
//      assertEquals( WicaStreamProperties.DEFAULT_HEARTBEAT_FLUX_INTERVAL, props.getHeartbeatFluxInterval() );
//      assertEquals( WicaStreamProperties.DEFAULT_VALUE_UPDATE_FLUX_INTERVAL, props.getChannelValueUpdateFluxInterval() );
//      assertEquals( WicaStreamProperties.DEFAULT_NUMERIC_PRECISION, props.getNumericPrecision() );
//      assertEquals( Set.of( WicaStreamProperties.DEFAULT_FIELDS_OF_INTEREST.split(";") ), props.getFieldsOfInterest() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
