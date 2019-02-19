/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

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
   void testDefaultFieldValues() throws Exception
   {
      final ObjectMapper mapper = new ObjectMapper();
      final String inputString = "{}";
      final WicaChannelProperties props = mapper.readValue( inputString, WicaChannelProperties.class );
      assertEquals( WicaChannelProperties.DEFAULT_FILTER_TYPE, props.getFilterType() );
      assertEquals( WicaChannelProperties.DEFAULT_FILTER_NUM_SAMPLES, props.getFilterNumSamples() );
      assertEquals( WicaChannelProperties.DEFAULT_FILTER_CYCLE_LENGTH, props.getFilterCycleLength() );
      assertEquals(WicaChannelProperties.DEFAULT_FILTER_SAMPLING_INTERVAL, props.getFilterSamplingIntervalInMillis() );
      assertEquals( WicaChannelProperties.DEFAULT_FILTER_DEADBAND, props.getFilterDeadband() );
      assertEquals( WicaChannelProperties.DEFAULT_POLLING_INTERVAL, props.getPollingIntervalInMillis() );
      assertFalse( props.getFieldsOfInterest().isPresent() );
      assertFalse( props.getNumericPrecision().isPresent() );
      assertFalse( props.getDataAcquisitionMode().isPresent() );
   }

   @Test
   void testAllFieldsPresentDecodedOk() throws Exception
   {
      final ObjectMapper mapper = new ObjectMapper();

      final String inputString = "{" + "\"filter\"" + ":" + "\"last-n\"" + "," +
                                       "\"nsamples\"" + ":" + 99 + "," +
                                       "\"fields\"" + ":" + "\"abc;def\"" + "," +
                                       "\"prec\"" + ":" + 9 + "}";

      final WicaChannelProperties props = mapper.readValue( inputString, WicaChannelProperties.class );
      assertEquals( WicaChannelProperties.FilterType.LAST_N, props.getFilterType() );

   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
