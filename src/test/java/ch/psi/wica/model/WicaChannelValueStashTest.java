/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelValueStashTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaChannelValueStash stash;
   private WicaStream stream;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/


   @BeforeEach
   void setup()
   {
      stash = new WicaChannelValueStash(3 );
      stream = new WicaStream( WicaStreamId.createNext(),
                               Set.of( WicaChannel.of( "abc" ), WicaChannel.of("def" ), WicaChannel.of("ghi" ) ) );
   }

   @Test
   void testParameterValidation()
   {
      assertThrows( NullPointerException.class, () -> stash.add( null, WicaChannelValue.createChannelValueDisconnected() ) );
      assertThrows( NullPointerException.class, () -> stash.add( WicaChannelName.of( "toffee" ),null ) );
      assertThrows( NullPointerException.class, () -> stash.getLaterThan( stream.getWicaChannels(), null ) );
      assertThrows( NullPointerException.class, () -> stash.getLatest( null ) );
      assertThrows( IllegalStateException.class, () -> stash.getLatest( WicaChannelName.of( "rhubarb" ) ) );
      assertThrows( IllegalStateException.class, () -> stash.getLatest( WicaChannelName.of( "toffee" ) ) );
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
      // Since the stash is only 3 values in size only 3 of them should be accepted
      final LocalDateTime beginTime = LocalDateTime.MIN;
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );

      // Create 2 channel values after the middle time and assign them to channel def
      final LocalDateTime middleTime = LocalDateTime.now();
      stash.add( WicaChannelName.of( "def" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "def" ), WicaChannelValue.createChannelValueDisconnected() );

      // Create 1 channel value after the end time and assign them to channel ghi
      final LocalDateTime endTime = LocalDateTime.now();
      stash.add( WicaChannelName.of( "ghi" ), WicaChannelValue.createChannelValueDisconnected() );

      final List<WicaChannelValue> laterThanBeginTimeListAbc =  stash.getLaterThan( WicaChannelName.of( "abc" ), beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListAbc = stash.getLaterThan( WicaChannelName.of( "abc" ), middleTime );
      final List<WicaChannelValue> laterThanEndTimeListAbc =    stash.getLaterThan( WicaChannelName.of( "abc" ), endTime    );
      final List<WicaChannelValue> laterThanBeginTimeListDef =  stash.getLaterThan( WicaChannelName.of( "def" ), beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListDef = stash.getLaterThan( WicaChannelName.of( "def" ), middleTime );
      final List<WicaChannelValue> laterThanEndTimeListDef =    stash.getLaterThan( WicaChannelName.of( "def" ), endTime    );
      final List<WicaChannelValue> laterThanBeginTimeListGhi =  stash.getLaterThan( WicaChannelName.of( "ghi" ), beginTime  );
      final List<WicaChannelValue> laterThanMiddleTimeListGhi = stash.getLaterThan( WicaChannelName.of( "ghi" ), middleTime );
      final List<WicaChannelValue> laterThanEndTimeListGhi =    stash.getLaterThan( WicaChannelName.of( "ghi" ), endTime    );

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
      final LocalDateTime beginTime = LocalDateTime.MIN;
      final WicaChannelValue testValue1 = WicaChannelValue.createChannelValueDisconnected();
      final ExecutorService executorService = Executors.newFixedThreadPool(100);

      for ( int i = 0; i < 200; i++ )
      {
         Runnable r = () -> stash.add(WicaChannelName.of("abc"), testValue1 );
         executorService.submit( r  );
      }
      executorService.awaitTermination(1, TimeUnit.SECONDS );

      final List<WicaChannelValue> laterThanBeginTimeList = stash.getLaterThan( WicaChannelName.of( "abc"), beginTime );
      Assert.assertEquals( 3, laterThanBeginTimeList.size() );
      Assert.assertEquals( testValue1, laterThanBeginTimeList.get( 0 ) );
   }

   @Test
   void testAddGetLaterThanStream()
   {
      // Create 5 channel values after the begin time and assign them to channel abc
      // Since the stash is only 3 values in size only 3 of them should be accepted
      final LocalDateTime beginTime = LocalDateTime.now();
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "abc" ), WicaChannelValue.createChannelValueDisconnected() );

      // Create 2 channel values after the middle time and assign them to channel def
      final LocalDateTime middleTime = LocalDateTime.now();
      stash.add( WicaChannelName.of( "def" ), WicaChannelValue.createChannelValueDisconnected() );
      stash.add( WicaChannelName.of( "def" ), WicaChannelValue.createChannelValueDisconnected() );

      // Create 1 channel value after the end time and assign them to channel ghi
      final LocalDateTime endTime = LocalDateTime.now();
      stash.add( WicaChannelName.of( "ghi" ), WicaChannelValue.createChannelValueDisconnected() );

      final Map<WicaChannelName, List<WicaChannelValue>> laterThanBeginTimeMap = stash.getLaterThan( stream.getWicaChannels(), beginTime );
      Assert.assertEquals( 3, laterThanBeginTimeMap.size() );
      Assert.assertEquals( 3, laterThanBeginTimeMap.get( WicaChannelName.of( "abc" ) ).size() );
      Assert.assertEquals( 2, laterThanBeginTimeMap.get( WicaChannelName.of( "def" ) ).size() );
      Assert.assertEquals( 1, laterThanBeginTimeMap.get( WicaChannelName.of( "ghi" ) ).size() );

      final Map<WicaChannelName, List<WicaChannelValue>> laterThanMiddleTimeMap = stash.getLaterThan( stream.getWicaChannels(), middleTime );
      Assert.assertEquals( 2, laterThanMiddleTimeMap.size() );
      Assert.assertEquals( 2, laterThanMiddleTimeMap.get( WicaChannelName.of( "def" ) ).size() );
      Assert.assertEquals( 1, laterThanMiddleTimeMap.get( WicaChannelName.of( "ghi" ) ).size() );

      final Map<WicaChannelName, List<WicaChannelValue>> laterThanEndTimeMap = stash.getLaterThan( stream.getWicaChannels(), endTime );
      Assert.assertEquals( 1, laterThanEndTimeMap.size() );
      Assert.assertEquals( 1, laterThanEndTimeMap.get( WicaChannelName.of( "ghi" ) ).size() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
