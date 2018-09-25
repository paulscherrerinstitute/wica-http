/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelValueTest.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testValueSerialisation()
   {
      final Map<WicaChannelName, WicaChannelValue> map = new HashMap<>();
      map.put(new WicaChannelName("chan1" ), WicaChannelValue.createChannelConnectedValue("abc", WicaChannelAlarmSeverity.MINOR_ALARM, 0, LocalDateTime.now(), LocalDateTime.now() ) );
      map.put(new WicaChannelName("chan2" ), WicaChannelValue.createChannelConnectedValue("123", WicaChannelAlarmSeverity.MAJOR_ALARM, 0, LocalDateTime.now(), LocalDateTime.now() ) );

      final String valueString = WicaChannelValue.convertMapToJsonRepresentation(map );
      logger.info( "JSON Value String looks like this: '{}'", valueString );
   }

   @Test
   void testMetadataSerialisation()
   {
      final Map<WicaChannelName, WicaChannelMetadata> map = new HashMap<>();
      map.put(new WicaChannelName("chan1" ), WicaChannelMetadata.createStringInstance() );
      map.put(new WicaChannelName("chan2" ), WicaChannelMetadata.createRealInstance("abc", 0, 0, 0, 0, 0, 0, 0, 0, 0 ) );
      final String metadataString = WicaChannelMetadata.convertMapToJsonRepresentation(map );
      logger.info( "JSON Metadata String looks like this: '{}'", metadataString );
   }

   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
