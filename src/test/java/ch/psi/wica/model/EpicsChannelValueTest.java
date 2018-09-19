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
class EpicsChannelValueTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelValueTest.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testValueSerialisation()
   {
      final Map<EpicsChannelName,EpicsChannelValue> map = new HashMap<>();
      map.put( new EpicsChannelName( "chan1" ), new EpicsChannelValue( "abc", EpicsChannelAlarmSeverity.MINOR_ALARM, 0, LocalDateTime.now(), LocalDateTime.now() ) );
      map.put( new EpicsChannelName( "chan2" ), new EpicsChannelValue( "123", EpicsChannelAlarmSeverity.MAJOR_ALARM, 0, LocalDateTime.now(), LocalDateTime.now() ) );

      final String valueString = EpicsChannelValue.convertMapToJsonRepresentation( map );
      logger.info( "JSON Value String looks like this: '{}'", valueString );
   }

   @Test
   void testMetadataSerialisation()
   {
      final Map<EpicsChannelName,EpicsChannelMetadata> map = new HashMap<>();
      map.put( new EpicsChannelName( "chan1" ), EpicsChannelMetadata.createStringInstance() );
      map.put( new EpicsChannelName( "chan2" ), EpicsChannelMetadata.createRealInstance( "abc",0,0,0,0,0,0,0,0,0 ) );
      final String metadataString = EpicsChannelMetadata.convertMapToJsonRepresentation( map );
      logger.info( "JSON Metadata String looks like this: '{}'", metadataString );
   }

   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
