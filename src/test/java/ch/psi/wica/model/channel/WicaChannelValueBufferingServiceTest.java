/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelValueUpdateEvent;
import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.controlsystem.event.WicaChannelValueBufferingService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelValueBufferingServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaChannelValueBufferingService serviceUnderTest;
   private WicaStream stream;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setup()
   {
      serviceUnderTest = new WicaChannelValueBufferingService(3 );
      stream = WicaStream.createBuilder().withId( WicaStreamId.createNext() )
         .withChannelName("abc" )
         .withChannelName("def" )
         .withChannelName("ghi" )
         .build();
   }

   @Test
   void testParameterValidation()
   {
      final ControlSystemName rhubarb = ControlSystemName.of( "rhubarb" );
      final ControlSystemName toffee = ControlSystemName.of( "toffee" );

      //noinspection ConstantConditions
      assertThrows( NullPointerException.class, () -> serviceUnderTest.handleValueUpdate(null ) );
      assertThrows( NullPointerException.class, () -> serviceUnderTest.getLaterThan(stream.getWicaChannels(), null ) );
      assertThrows( NullPointerException.class, () -> serviceUnderTest.getLatest(null ) );
      assertThrows( IllegalStateException.class, () -> serviceUnderTest.getLatest( rhubarb) );
      assertThrows( IllegalStateException.class, () -> serviceUnderTest.getLatest( toffee ) );
   }

   @Test
   void testLocalDateTimeMin()
   {
      WicaChannelValue.createChannelValueDisconnected();
   }

   @Test
   void testHandleValueUpdate_GetLaterThanChannel()
   {
      // Create 5 channel values after the begin time and assign them to channel abc
      // Since the serviceUnderTest is only 3 values in size only 3 of them should be accepted.
      final LocalDateTime beginTime = LocalDateTime.MIN;
      final ControlSystemName abc = ControlSystemName.of( "abc" );
      final var evUpdateABC =  new WicaChannelValueUpdateEvent( abc, WicaChannelValue.createChannelValueDisconnected() );
      serviceUnderTest.handleValueUpdate( evUpdateABC );
      serviceUnderTest.handleValueUpdate( evUpdateABC );
      serviceUnderTest.handleValueUpdate( evUpdateABC );
      serviceUnderTest.handleValueUpdate( evUpdateABC );
      serviceUnderTest.handleValueUpdate( evUpdateABC );

      // Create 2 channel values after the middle time and assign them to channel def
      final LocalDateTime middleTime = LocalDateTime.now();
      final ControlSystemName def = ControlSystemName.of( "def" );
      final var evUpdateDEF =  new WicaChannelValueUpdateEvent( def, WicaChannelValue.createChannelValueDisconnected() );
      serviceUnderTest.handleValueUpdate(evUpdateDEF );
      serviceUnderTest.handleValueUpdate(evUpdateDEF );

      // Create 1 channel value after the end time and assign them to channel ghi
      final LocalDateTime endTime = LocalDateTime.now();
      final ControlSystemName ghi = ControlSystemName.of( "ghi" );
      final var evUpdateGHI =  new WicaChannelValueUpdateEvent( ghi, WicaChannelValue.createChannelValueDisconnected() );
      serviceUnderTest.handleValueUpdate(evUpdateGHI );

      final List<WicaChannelValue> laterThanBeginTimeListAbc =  serviceUnderTest.getLaterThan( abc, beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListAbc = serviceUnderTest.getLaterThan( abc, middleTime );
      final List<WicaChannelValue> laterThanEndTimeListAbc =    serviceUnderTest.getLaterThan( abc, endTime    );
      final List<WicaChannelValue> laterThanBeginTimeListDef =  serviceUnderTest.getLaterThan( def, beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListDef = serviceUnderTest.getLaterThan( def, middleTime );
      final List<WicaChannelValue> laterThanEndTimeListDef =    serviceUnderTest.getLaterThan( def, endTime    );
      final List<WicaChannelValue> laterThanBeginTimeListGhi =  serviceUnderTest.getLaterThan( ghi, beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListGhi = serviceUnderTest.getLaterThan( ghi, middleTime );
      final List<WicaChannelValue> laterThanEndTimeListGhi =    serviceUnderTest.getLaterThan( ghi, endTime    );

      assertThat( laterThanBeginTimeListAbc.size(), is( 3 ) );
      assertThat( laterThanMiddleTimeListAbc.size(), is( 0 ) );
      assertThat( laterThanEndTimeListAbc.size(), is( 0 ) );

      assertThat( laterThanBeginTimeListDef.size(), is( 2) );
      assertThat( laterThanMiddleTimeListDef.size(), is( 2 ) );
      assertThat( laterThanEndTimeListDef.size(), is( 0 ) );

      assertThat( laterThanBeginTimeListGhi.size(), is( 1 ) );
      assertThat( laterThanMiddleTimeListGhi.size(), is( 1 ) );
      assertThat( laterThanEndTimeListGhi.size(), is( 1) );
   }

   @Test
   void testHandleValueUpdate_GetLaterThanMultipleValues() throws InterruptedException
   {
      final ControlSystemName abc = ControlSystemName.of( "abc" );
      final LocalDateTime beginTime = LocalDateTime.MIN;
      final WicaChannelValue testValue1 = WicaChannelValue.createChannelValueDisconnected();
      final ExecutorService executorService = Executors.newFixedThreadPool(100);
      final var evUpdateABC =  new WicaChannelValueUpdateEvent( abc, testValue1 );

      for ( int i = 0; i < 200; i++ )
      {
         Runnable r = () -> serviceUnderTest.handleValueUpdate( evUpdateABC );
         executorService.submit( r  );
      }
      executorService.awaitTermination(1, TimeUnit.SECONDS );

      final List<WicaChannelValue> laterThanBeginTimeList = serviceUnderTest.getLaterThan( abc, beginTime );
      Assert.assertThat( laterThanBeginTimeList.size(), is (3) );
      Assert.assertThat( laterThanBeginTimeList.get( 0 ), is( testValue1 ) );
   }

   @Test
   void testHandleValueUpdate_GetLaterThanStream()
   {
      // Create 5 channel values after the begin time and assign them to channel abc
      // Since the serviceUnderTest is only 3 values in size only 3 of them should be accepted
      final LocalDateTime beginTime = LocalDateTime.now();
      final WicaChannel abc = WicaChannel.createFromName( "abc" );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( abc.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( abc.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( abc.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( abc.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( abc.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );

      // Create 2 channel values after the middle time and assign them to channel def
      final LocalDateTime middleTime = LocalDateTime.now();
      final WicaChannel def = WicaChannel.createFromName( "def" );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( def.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( def.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );

      // Create 1 channel value after the end time and assign them to channel ghi
      final LocalDateTime endTime = LocalDateTime.now();
      final WicaChannel ghi = WicaChannel.createFromName( "ghi" );
      serviceUnderTest.handleValueUpdate( new WicaChannelValueUpdateEvent( ghi.getName().getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ) );

      final var laterThanBeginTimeMap = serviceUnderTest.getLaterThan( stream.getWicaChannels(), beginTime );
      assertThat( laterThanBeginTimeMap.size(), is( 3 )  );
      assertThat( laterThanBeginTimeMap.get( abc ).size(), is( 3 )  );
      assertThat( laterThanBeginTimeMap.get( def ).size(), is( 2 )  );
      assertThat( laterThanBeginTimeMap.get( ghi ).size(), is( 1 )  );

      final var laterThanMiddleTimeMap = serviceUnderTest.getLaterThan( stream.getWicaChannels(), middleTime );
      assertThat( laterThanMiddleTimeMap.size(), is( 2) );
      assertThat( laterThanMiddleTimeMap.get( def ).size(), is( 2 ) );
      assertThat( laterThanMiddleTimeMap.get( ghi ).size(), is( 1 ) );

      final var laterThanEndTimeMap = serviceUnderTest.getLaterThan( stream.getWicaChannels(), endTime );
      assertThat( laterThanEndTimeMap.size(), is( 1) );
      assertThat( laterThanEndTimeMap.get( ghi ).size(), is( 1) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
