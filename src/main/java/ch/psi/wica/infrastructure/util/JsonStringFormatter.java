/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.util;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;

import java.io.IOException;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class JsonStringFormatter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public static String prettyFormat( String jsonInput ) throws RuntimeException
   {
      try
      {
         final ObjectMapper mapper = new ObjectMapper();
         mapper.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS );
         mapper.disable( JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS );
         final JsonNode json = mapper.readTree(jsonInput );
         return mapper.writerWithDefaultPrettyPrinter().writeValueAsString( json );
      }
      catch( IOException ex )
      {
         return "JSON formatting problem !" + ex;
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
