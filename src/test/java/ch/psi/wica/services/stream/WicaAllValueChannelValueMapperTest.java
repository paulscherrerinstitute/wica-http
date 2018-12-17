/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelAlarmSeverity;
import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaAllValueChannelValueMapperTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaAllValueChannelValueMapperTest.class );


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testMap()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected( "jkl" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4 );

      final WicaChannelValueMapper mapper = new WicaAllValueChannelValueMapper();
      final List<WicaChannelValue> outputList  = mapper.map( inputList );
      assertEquals( inputList.size(), outputList.size() );
      assertEquals( inputList.get( 3 ), outputList.get( 3 ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
