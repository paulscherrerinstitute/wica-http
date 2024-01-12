/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.infrastructure.stream.WicaStreamBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.*;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@DirtiesContext( classMode= DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD )
@SpringBootTest
class WicaStreamMonitoredValueRequesterServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamMonitoredValueRequesterServiceTest.class );

   @Autowired
   private WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService;

   @Autowired
   private WicaStreamMonitoredValueRequesterService service;

   private WicaChannel testChannel;
   private WicaStream wicaStream;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach()
   {
      testChannel = WicaChannelBuilder
            .create()
            .withChannelNameAndProperties( "XXX", WicaChannelPropertiesBuilder
                  .create().withDataAcquisitionMode( WicaDataAcquisitionMode.MONITOR )
                  .build() )
            .build();

      wicaStream = WicaStreamBuilder
            .create()
            .withId( "myStream" )
            .withChannel( testChannel )
            .build();

   }

   @Test
   void testStartMonitoring_addsEntryToEventMap()
   {
      assertThat( service.getLastEventForChannel( testChannel ).isEmpty(), is( true ) );
      service.startMonitoring( wicaStream );
      assertThat( service.getLastEventForChannel( testChannel ).isPresent(), is( true ) );
   }

   @Test
   void testStopMonitoring_removesEntryFromEventMap() throws InterruptedException
   {
      assertThat( service.getLastEventForChannel( testChannel ).isEmpty(), is( true ) );

      service.startMonitoring( wicaStream );
      assertThat( service.getLastEventForChannel( testChannel ).isPresent(), is( true ) );

      service.stopMonitoring( wicaStream );
      assertThat( service.getLastEventForChannel( testChannel ).isPresent(), is( true ) );

      Thread.sleep( 15000 );
      assertThat( service.getLastEventForChannel( testChannel ).isPresent(), is( false ) );
   }


   @Test
   void testStartMonitoring_IncreasesInterestCount()
   {
      assertThat( service.getInterestCountForChannel( testChannel ),is( 0 ) );
      service.startMonitoring( wicaStream );
      assertThat( service.getInterestCountForChannel( testChannel ),is( 1 ) );
      service.startMonitoring( wicaStream );
      assertThat( service.getInterestCountForChannel( testChannel ),is( 2 ) );
   }

   @Test
   void testStopMonitoring_DecreasesInterestCount()
   {
      service.startMonitoring( wicaStream );
      service.startMonitoring( wicaStream );
      service.startMonitoring( wicaStream );
      assertThat( service.getInterestCountForChannel( testChannel ),is( 3 ) );
      service.stopMonitoring( wicaStream );
      assertThat( service.getInterestCountForChannel( testChannel ),is( 2 ) );
      service.stopMonitoring( wicaStream );
      assertThat( service.getInterestCountForChannel( testChannel ),is( 1 ) );
      service.stopMonitoring( wicaStream );
      assertThat( service.getInterestCountForChannel( testChannel ),is( 0 ) );
   }

   @Test
   void testStartMonitoring_wicaStreamMonitoredValueCollectorServiceInitialisedOk()
   {
      final String myWicaChannelName = "simon:counter:01";
      final WicaChannelProperties wicaChannelProperties = WicaChannelPropertiesBuilder.create()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.MONITOR )
            .withFilterType(WicaFilterType.ALL_VALUE )
            .build();

      final WicaChannel myWicaChannel = WicaChannelBuilder.create().withChannelNameAndProperties( myWicaChannelName, wicaChannelProperties ).build();

      final WicaStream wicaStream = WicaStreamBuilder.create()
         .withId( "myStream" )
         .withChannel( myWicaChannel )
         .build();

      final Map<WicaChannel, List<WicaChannelValue>> preFirstValueMap = wicaStreamMonitoredValueCollectorService.get( wicaStream, LocalDateTime.MIN );
      assertThat( preFirstValueMap.size(), is( 0 ) );

      service.startMonitoring( wicaStream );

      final Map<WicaChannel, List<WicaChannelValue>> firstValueMap = wicaStreamMonitoredValueCollectorService.getLatest( wicaStream);
      assertThat( firstValueMap.size(), is( 1 ) );
      assertThat( firstValueMap.get( myWicaChannel ).get( 0 ).isConnected(), is( false) );
      assertThat( firstValueMap.get( myWicaChannel ).get( 0 ).getType(), is( WicaChannelType.UNKNOWN ) );

      final Map<WicaChannel, List<WicaChannelValue>> laterValueMap = wicaStreamMonitoredValueCollectorService.get( wicaStream, LocalDateTime.MIN );
      assertThat( laterValueMap.size(), is( 1 ) );
      assertThat( laterValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( firstValueMap.get( myWicaChannel ).get( 0 ).isConnected(), is( false ) );
      assertThat( laterValueMap.get( myWicaChannel ).get( 0 ).getType(), is( WicaChannelType.UNKNOWN ) );
   }


   @CsvSource( { "1", "10", "100", "1000", "10000" })
   @ParameterizedTest
   void testStartMonitoringPerformanceTest( int numberOfChannelsInStream )
   {
      final Set<WicaChannel> wicaChannelSet = new HashSet<>();
      for ( int i = 0; i < numberOfChannelsInStream; i++ )
      {
         final String myWicaChannelName = "chan-" + i;
         final WicaChannel myWicaChannel = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( myWicaChannelName ).build();
         wicaChannelSet.add( myWicaChannel );
      }

      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withId( "myStream" )
            .withChannels( wicaChannelSet )
            .build();

      final StopWatch stopWatch = StopWatch.createStarted();
      service.startMonitoring( wicaStream );
      final long elapsedTimeInMicroseconds = stopWatch.getTime(TimeUnit.MICROSECONDS);

      logger.info( "Started monitoring stream with {} channels in {} us.  Throughput = {} requests/second.", numberOfChannelsInStream, elapsedTimeInMicroseconds, (1_000_000L * numberOfChannelsInStream ) / elapsedTimeInMicroseconds  );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
