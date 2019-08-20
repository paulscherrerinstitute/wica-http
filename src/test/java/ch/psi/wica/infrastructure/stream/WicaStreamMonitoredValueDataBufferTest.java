/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaStreamMonitoredValueDataBufferTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
   
   private WicaStreamMonitoredValueDataBuffer testObject;
   private WicaStream stream;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setup()
   {
      testObject = new WicaStreamMonitoredValueDataBuffer(3 );
      stream = WicaStreamBuilder.create().withId( WicaStreamId.createNext() )
         .withChannelNameAndDefaultProperties( "abc" )
         .withChannelNameAndDefaultProperties( "def" )
         .withChannelNameAndDefaultProperties( "ghi" )
         .build();
   }

   @Test
   void testLocalDateTimeMin()
   {
      WicaChannelValue.createChannelValueDisconnected();
   }

   @Test
   void testSimpleUpdate()
   {
      final WicaChannel abc = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "abc" ).build();
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      final Map<WicaChannel, List<WicaChannelValue>> map = testObject.getLaterThan( Set.of( abc ), LocalDateTime.MIN );
      assertThat( map.size(), is( 1 ) );
      assertThat( map.get( abc ).size(),is( 1 ) );
   }

   @Test
   void testHandleValueUpdate_GetLaterThanChannel()
   {
      // Create 5 channel values after the begin time and assign them to channel abc
      // Since the testObject is only 3 values in size only 3 of them should be accepted.
      final LocalDateTime beginTime = LocalDateTime.MIN;
      final WicaChannel abc = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "abc" ).build();
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );

      // Create 2 channel values after the middle time and assign them to channel def
      final LocalDateTime middleTime = LocalDateTime.now();
      final WicaChannel def = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "def" ).build();
      injectValueUpdate( def, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( def, WicaChannelValue.createChannelValueDisconnected() );

      // Create 1 channel value after the end time and assign them to channel ghi
      final LocalDateTime endTime = LocalDateTime.now();
      final WicaChannel ghi = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "ghi" ).build();
      injectValueUpdate( ghi, WicaChannelValue.createChannelValueDisconnected() );

      final var beginTimeResultMap = testObject.getLaterThan( Set.of( abc, def, ghi ), beginTime );
      final var middleTimeResultMap = testObject.getLaterThan( Set.of( abc, def, ghi ), middleTime );
      final var endTimeResultMap = testObject.getLaterThan( Set.of( abc, def, ghi ), endTime );

      assertThat( beginTimeResultMap.size(), is( 3 ) );
      assertThat( beginTimeResultMap.get( abc ).size(), is( 3 ) );
      assertThat( middleTimeResultMap.size(), is( 3 ) );
      assertThat( middleTimeResultMap.get( def ).size(), is( 2 ) );
      assertThat( endTimeResultMap.size(), is( 3 ) );
      assertThat( endTimeResultMap.get( ghi ).size(), is( 1 ) );
   }

   @Test
   void testHandleValueUpdate_GetLaterThanMultipleValues() throws InterruptedException
   {
      final WicaChannel abc = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "abc" ).build();
      final LocalDateTime beginTime = LocalDateTime.MIN;
      final WicaChannelValue testValue1 = WicaChannelValue.createChannelValueDisconnected();
      final ExecutorService executorService = Executors.newFixedThreadPool(100);

      for ( int i = 0; i < 200; i++ )
      {
         Runnable r = () -> injectValueUpdate(  abc, testValue1 );
         executorService.submit( r  );
      }
      executorService.awaitTermination(1, TimeUnit.SECONDS );

      final var laterThanBeginTimeMap = testObject.getLaterThan( Set.of( abc ), beginTime );
      assertThat( laterThanBeginTimeMap.size(), is (1) );
      assertThat( laterThanBeginTimeMap.get( abc ).size(), is (3) );
      assertThat( laterThanBeginTimeMap.get( abc ).get( 0 ), is( testValue1 ) );
   }

   @Test
   void testHandleValueUpdate_GetLaterThanStream()
   {
      // Create 5 channel values after the begin time and assign them to channel abc
      // Since the testObject is only 3 values in size only 3 of them should be accepted
      final LocalDateTime beginTime = LocalDateTime.now();
      final WicaChannel abc = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "abc" ).build();
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( abc, WicaChannelValue.createChannelValueDisconnected() );

      // Create 2 channel values after the middle time and assign them to channel def
      final LocalDateTime middleTime = LocalDateTime.now();
      final WicaChannel def = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "def" ).build();
      injectValueUpdate( def, WicaChannelValue.createChannelValueDisconnected() );
      injectValueUpdate( def, WicaChannelValue.createChannelValueDisconnected() );

      // Create 1 channel value after the end time and assign them to channel ghi
      final LocalDateTime endTime = LocalDateTime.now();
      final WicaChannel ghi = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( "ghi" ).build();
      injectValueUpdate( ghi, WicaChannelValue.createChannelValueDisconnected() );

      final var laterThanBeginTimeMap = testObject.getLaterThan( stream.getWicaChannels(), beginTime );
      assertThat( laterThanBeginTimeMap.size(), is( 3 )  );
      for ( WicaChannel wicaChannel : laterThanBeginTimeMap.keySet() )
      {
         switch ( wicaChannel.getName().asString() )
         {
            case "abc" : assertThat( laterThanBeginTimeMap.get( wicaChannel ).size(), is( 3) ); break;
            case "def" : assertThat( laterThanBeginTimeMap.get( wicaChannel ).size(), is( 2) ); break;
            case "ghi" : assertThat( laterThanBeginTimeMap.get( wicaChannel ).size(), is( 1) ); break;
         }
      }

      final var laterThanMiddleTimeMap = testObject.getLaterThan( stream.getWicaChannels(), middleTime );
      assertThat( laterThanMiddleTimeMap.size(), is( 3 )  );
      for ( WicaChannel wicaChannel : laterThanMiddleTimeMap.keySet() )
      {
         switch ( wicaChannel.getName().asString() )
         {
            case "def" : assertThat( laterThanMiddleTimeMap.get( wicaChannel ).size(), is( 2) ); break;
            case "ghi" : assertThat( laterThanMiddleTimeMap.get( wicaChannel ).size(), is( 1) ); break;
         }
      }

      final var laterThanEndTimeMap = testObject.getLaterThan( stream.getWicaChannels(), endTime );
      assertThat( laterThanEndTimeMap.size(), is( 3 )  );
      for ( WicaChannel wicaChannel : laterThanEndTimeMap.keySet() )
      {
         if ( "ghi".equals(wicaChannel.getName().asString()) )
         {
            assertThat(laterThanMiddleTimeMap.get(wicaChannel).size(), is(1));
         }
      }
   }

/*- Private methods ----------------------------------------------------------*/
   
   private void injectValueUpdate( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      testObject.saveDataPoint(WicaDataBufferStorageKey.getMonitoredValueStorageKey(wicaChannel ), wicaChannelValue );
   }   
   
/*- Nested Classes -----------------------------------------------------------*/

}
