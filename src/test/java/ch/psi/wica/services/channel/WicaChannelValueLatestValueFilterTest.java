/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueLatestValueFilterTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testMapZeroValues()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected( "jkl" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4 );

      final WicaChannelValueFilter mapper = new WicaChannelValueLatestValueFilter(0 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );
      assertThat( outputList.size(), is( 0 ) );
   }

   @Test
   void testMapOneValue()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected( "jkl" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4 );

      final WicaChannelValueFilter mapper = new WicaChannelValueLatestValueFilter(1 );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );
      assertThat( outputList.size(), is( 1 ) );
      assertThat( outputList.get( 0 ), is( inputList.get( 3 ) ) );
   }

   @Test
   void testMapAllValues()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( "ghi" );
      final WicaChannelValue strValue4 = WicaChannelValue.createChannelValueConnected( "jkl" );

      final List<WicaChannelValue> inputList = List.of( strValue1, strValue2, strValue3, strValue4 );

      final WicaChannelValueFilter mapper = new WicaChannelValueLatestValueFilter(Integer.MAX_VALUE );
      final List<WicaChannelValue> outputList  = mapper.apply(inputList );
      assertThat( outputList.size(), is( 4 ) );
      assertThat( outputList.get( 0 ), is( inputList.get( 0 ) ) );
      assertThat( outputList.get( 1 ), is( inputList.get( 1 ) ) );
      assertThat( outputList.get( 2 ), is( inputList.get( 2 ) ) );
      assertThat( outputList.get( 3 ), is( inputList.get( 3 ) ) );
   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
