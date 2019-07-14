/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.channel.WicaChannelValueTimestampRewriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelValueTimestampRewriterTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaChannelValueTimestampRewriter rewriter;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setUp()
   {
      rewriter = new WicaChannelValueTimestampRewriter();
   }

   @Test
   void test()
   {
      final var strValue = WicaChannelValue.createChannelValueConnected("abc" );
      final LocalDateTime newTimestamp = LocalDateTime.now();
      final WicaChannelValue.WicaChannelValueConnectedString newValue = ( WicaChannelValue.WicaChannelValueConnectedString) rewriter.rewrite( strValue, newTimestamp );
      assertEquals( newTimestamp, newValue.getDataSourceTimestamp() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
