/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaChannelNameTest.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testCreateFromWicaChannelNameObject()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "simon" );
      final WicaChannel objectUnderTest = WicaChannel.createFromName( wicaChannelName );
      final WicaChannelProperties props = objectUnderTest.getProperties();
      assertThat( props, is( WicaChannelProperties.createDefaultInstance() ) );
   }

   @Test
   void testCreateFromWicaChannelStringName()
   {
      final WicaChannel objectUnderTest = WicaChannel.createFromName( "simon" );
      final WicaChannelProperties props = objectUnderTest.getProperties();
      assertThat( props, is( WicaChannelProperties.createDefaultInstance() ) );
   }

   @Test
   void testJsonSerialization() throws JsonProcessingException
   {
      final WicaChannel wicaChannel = WicaChannel.createFromName( "peter" );
      final ObjectMapper objectMapper = new ObjectMapper();
      final String serializedValue = objectMapper.writeValueAsString( wicaChannel );
      logger.info( "Serialized form of WicaChannel looks like this '{}' ", serializedValue );
   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
