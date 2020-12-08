/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelNameTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelNameTest.class );


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testProtocol()
   {
      assertEquals(WicaChannelName.Protocol.CA, WicaChannelName.Protocol.of("ca://") );
      assertEquals( WicaChannelName.Protocol.PV, WicaChannelName.Protocol.of( "pv://") );
      assertThrows( IllegalArgumentException.class, () -> WicaChannelName.Protocol.of( "ca:" ) );
      assertThrows( IllegalArgumentException.class, () -> WicaChannelName.Protocol.of( "cA://" ) );
      assertThrows( IllegalArgumentException.class, () -> WicaChannelName.Protocol.of( "Ca://" ) );
      assertThrows( IllegalArgumentException.class, () -> WicaChannelName.Protocol.of( "CA://" ) );
      assertThrows( IllegalArgumentException.class, () -> WicaChannelName.Protocol.of( "PV://" ) );
   }

   @Test
   void test1()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "abc" );
      assertEquals(ControlSystemName.of("abc" ), wicaChannelName.getControlSystemName() );
      assertFalse( wicaChannelName.getProtocol().isPresent() );
      assertFalse( wicaChannelName.getInstance().isPresent() );
      assertEquals( "abc", wicaChannelName.asString() );
      logger.info( "WicaChannelName looks like this '{}' ", wicaChannelName.asString() );
   }

   @Test
   void test2()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "abc##4" );
      assertEquals( ControlSystemName.of( "abc" ), wicaChannelName.getControlSystemName() );
      assertFalse( wicaChannelName.getProtocol().isPresent() );
      assertTrue( wicaChannelName.getInstance().isPresent() );
      assertEquals( 4, wicaChannelName.getInstance().get().intValue() );
      assertEquals( "abc##4", wicaChannelName.asString() );
      logger.info( "WicaChannelName looks like this '{}' ", wicaChannelName.asString() );
   }

   @Test
   void test3()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "ca://abc##00006" );
      assertEquals( ControlSystemName.of( "abc" ), wicaChannelName.getControlSystemName() );
      assertTrue( wicaChannelName.getProtocol().isPresent() );
      assertEquals( WicaChannelName.Protocol.CA, wicaChannelName.getProtocol().get() );
      assertTrue( wicaChannelName.getInstance().isPresent() );
      assertEquals( 6, wicaChannelName.getInstance().get().intValue() );
      assertEquals( "ca://abc##00006", wicaChannelName.asString() );
      logger.info( "WicaChannelName looks like this '{}' ", wicaChannelName.asString() );
   }

   @Test
   void test4()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "pv://abc##00006" );
      assertEquals( ControlSystemName.of( "abc" ), wicaChannelName.getControlSystemName() );
      assertTrue( wicaChannelName.getProtocol().isPresent() );
      assertEquals( WicaChannelName.Protocol.PV, wicaChannelName.getProtocol().get() );
      assertTrue( wicaChannelName.getInstance().isPresent() );
      assertEquals( 6, wicaChannelName.getInstance().get().intValue() );
      assertEquals( "pv://abc##00006", wicaChannelName.asString() );
      logger.info( "WicaChannelName looks like this '{}' ", wicaChannelName.asString() );
   }

   @Test
   void test5()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "abc##0" );
      assertEquals( ControlSystemName.of( "abc" ), wicaChannelName.getControlSystemName() );
      assertFalse( wicaChannelName.getProtocol().isPresent() );
      assertTrue( wicaChannelName.getInstance().isPresent() );
      assertEquals( 0, wicaChannelName.getInstance().get().intValue()  );
      assertEquals( "abc##0", wicaChannelName.asString() );
      logger.info( "WicaChannelName looks like this '{}' ", wicaChannelName.asString() );
   }

   @Test
   void test6()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "abc##1" );
      assertEquals( ControlSystemName.of( "abc" ), wicaChannelName.getControlSystemName() );
      assertFalse( wicaChannelName.getProtocol().isPresent() );
      assertTrue( wicaChannelName.getInstance().isPresent() );
      assertEquals( 1, wicaChannelName.getInstance().get().intValue()  );
      assertEquals( "abc##1", wicaChannelName.asString() );
      logger.info( "WicaChannelName looks like this '{}' ", wicaChannelName.asString() );
   }

   @Test
   void testCsNameAllowedCharacters()
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( ".[]<>-_:" );
      assertEquals( ControlSystemName.of( ".[]<>-_:" ), wicaChannelName.getControlSystemName() );
      assertFalse( wicaChannelName.getProtocol().isPresent() );
      assertFalse( wicaChannelName.getInstance().isPresent() );
      assertEquals( ".[]<>-_:", wicaChannelName.asString() );
      logger.info( "WicaChannelName looks like this '{}' ", wicaChannelName.asString() );
   }

   @Test
   void testCsNameDisallowedCharacters()
   {
      assertThrows( IllegalArgumentException.class, () -> WicaChannelName.of( "#" ) );
   }

   @Test
   void testJsonSerialization() throws JsonProcessingException
   {
      final WicaChannelName wicaChannelName = WicaChannelName.of( "abc##1" );
      final ObjectMapper objectMapper = new ObjectMapper();
      final String serializedValue = objectMapper.writeValueAsString( wicaChannelName );

      logger.info( "Serialized form of WicaChannelName looks like this '{}' ", serializedValue );
   }

   @Test
   void testIsValueObject()
   {
      WicaChannelName name1 = WicaChannelName.of( "N");
      WicaChannelName name2 = WicaChannelName.of( "N");
      WicaChannelName name3 = WicaChannelName.of( "N##1");
      WicaChannelName name4 = WicaChannelName.of( "N##1");
      WicaChannelName name5 = WicaChannelName.of( "ca://N");
      WicaChannelName name6 = WicaChannelName.of( "ca://N");
      WicaChannelName name7 = WicaChannelName.of( "ca://N");
      assertThat( name1, is( name2 ) );
      assertThat( name3, is( name4 ) );
      assertThat( name5, is( name6 ) );
      assertThat( name1.equals( name7 ), is( false ) );
   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
