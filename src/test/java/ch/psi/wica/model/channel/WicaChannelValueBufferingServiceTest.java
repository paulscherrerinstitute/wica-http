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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
      stream = new WicaStream( WicaStreamId.createNext(), Set.of ( WicaChannel.createFromName("abc" ),
                                                                   WicaChannel.createFromName("def" ),
                                                                   WicaChannel.createFromName("ghi" ) ) );
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
   void testAddGetLaterThanChannel()
   {
      // Create 5 channel values after the begin time and assign them to channel abc
      // Since the serviceUnderTest is only 3 values in size only 3 of them should be accepted.
      final LocalDateTime beginTime = LocalDateTime.MIN;
      final ControlSystemName abc = ControlSystemName.of( "abc" );
      final var evUpdateABC =  new WicaChannelValueUpdateEvent( abc, WicaChannelValue.createChannelValueDisconnected() );
      serviceUnderTest.handleValueUpdate( evUpdateABC );
      serviceUnderTest.handleValueUpdate( evUpdateABC );
      serviceUnderTest.handleValueUpdate( evUpdateABC );
      serviceUnderTest.handleValueUpdate(evUpdateABC );
      serviceUnderTest.handleValueUpdate(evUpdateABC );

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

      final List<WicaChannelValue> laterThanBeginTimeListAbc =  serviceUnderTest.getLaterThan(abc, beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListAbc = serviceUnderTest.getLaterThan(abc, middleTime );
      final List<WicaChannelValue> laterThanEndTimeListAbc =    serviceUnderTest.getLaterThan(abc, endTime    );
      final List<WicaChannelValue> laterThanBeginTimeListDef =  serviceUnderTest.getLaterThan(def, beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListDef = serviceUnderTest.getLaterThan(def, middleTime );
      final List<WicaChannelValue> laterThanEndTimeListDef =    serviceUnderTest.getLaterThan(def, endTime    );
      final List<WicaChannelValue> laterThanBeginTimeListGhi =  serviceUnderTest.getLaterThan(ghi, beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListGhi = serviceUnderTest.getLaterThan(ghi, middleTime );
      final List<WicaChannelValue> laterThanEndTimeListGhi =    serviceUnderTest.getLaterThan(ghi, endTime    );

      Assert.assertEquals( 3, laterThanBeginTimeListAbc.size() );
      Assert.assertEquals( 0, laterThanMiddleTimeListAbc.size() );
      Assert.assertEquals( 0, laterThanEndTimeListAbc.size() );

      Assert.assertEquals( 2, laterThanBeginTimeListDef.size() );
      Assert.assertEquals( 2, laterThanMiddleTimeListDef.size() );
      Assert.assertEquals( 0, laterThanEndTimeListDef.size() );

      Assert.assertEquals( 1, laterThanBeginTimeListGhi.size() );
      Assert.assertEquals( 1, laterThanMiddleTimeListGhi.size() );
      Assert.assertEquals( 1, laterThanEndTimeListGhi.size() );
   }

   @Test
   void testAddGetLaterThanMultipleValues() throws InterruptedException
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

      final List<WicaChannelValue> laterThanBeginTimeList = serviceUnderTest.getLaterThan(abc, beginTime );
      Assert.assertEquals( 3, laterThanBeginTimeList.size() );
      Assert.assertEquals( testValue1, laterThanBeginTimeList.get( 0 ) );
   }

//   @Test
//   void testAddGetLaterThanStream()
//   {
//      // Create 5 channel values after the begin time and assign them to channel abc
//      // Since the serviceUnderTest is only 3 values in size only 3 of them should be accepted
//      final LocalDateTime beginTime = LocalDateTime.now();
//      final ControlSystemName abc = ControlSystemName.of( "abc" );
//      serviceUnderTest.add( abc, WicaChannelValue.createChannelValueDisconnected() );
//      serviceUnderTest.add( abc, WicaChannelValue.createChannelValueDisconnected() );
//      serviceUnderTest.add( abc, WicaChannelValue.createChannelValueDisconnected() );
//      serviceUnderTest.add( abc, WicaChannelValue.createChannelValueDisconnected() );
//      serviceUnderTest.add( abc, WicaChannelValue.createChannelValueDisconnected() );
//
//      // Create 2 channel values after the middle time and assign them to channel def
//      final LocalDateTime middleTime = LocalDateTime.now();
//      final ControlSystemName def = ControlSystemName.of( "def" );
//      serviceUnderTest.add( def, WicaChannelValue.createChannelValueDisconnected() );
//      serviceUnderTest.add( def, WicaChannelValue.createChannelValueDisconnected() );
//
//      // Create 1 channel value after the end time and assign them to channel ghi
//      final LocalDateTime endTime = LocalDateTime.now();
//      final ControlSystemName ghi = ControlSystemName.of( "ghi" );
//      serviceUnderTest.add( ghi, WicaChannelValue.createChannelValueDisconnected() );
//
//      final Map<WicaChannelName, List<WicaChannelValue>> laterThanBeginTimeMap = serviceUnderTest.getLaterThan( stream.getWicaChannels(), beginTime );
//      Assert.assertEquals( 3, laterThanBeginTimeMap.size() );
//      Assert.assertEquals( 3, laterThanBeginTimeMap.get( abc ).size() );
//      Assert.assertEquals( 2, laterThanBeginTimeMap.get( def ).size() );
//      Assert.assertEquals( 1, laterThanBeginTimeMap.get( ghi ).size() );
//
//      final Map<WicaChannelName, List<WicaChannelValue>> laterThanMiddleTimeMap = serviceUnderTest.getLaterThan( stream.getWicaChannels(), middleTime );
//      Assert.assertEquals( 2, laterThanMiddleTimeMap.size() );
//      Assert.assertEquals( 2, laterThanMiddleTimeMap.get( def ).size() );
//      Assert.assertEquals( 1, laterThanMiddleTimeMap.get( ghi ).size() );
//
//      final Map<WicaChannelName, List<WicaChannelValue>> laterThanEndTimeMap = serviceUnderTest.getLaterThan( stream.getWicaChannels(), endTime );
//      Assert.assertEquals( 1, laterThanEndTimeMap.size() );
//      Assert.assertEquals( 1, laterThanEndTimeMap.get( ghi ).size() );
//   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
