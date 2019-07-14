/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      WicaChannelName wicaChannelName = WicaChannelName.of( "abc##1" );
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


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
