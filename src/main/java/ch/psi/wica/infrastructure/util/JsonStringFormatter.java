/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.util;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
         final ObjectMapper mapper = JsonMapper.builder()
               .enable( JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS )
               .build();

         final JsonNode json = mapper.readTree( jsonInput );
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
