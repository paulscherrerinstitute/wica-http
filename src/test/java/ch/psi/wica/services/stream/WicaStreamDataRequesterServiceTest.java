/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.*;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.infrastructure.stream.WicaStreamBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@DirtiesContext( classMode= DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD )
@SpringBootTest
class WicaStreamDataRequesterServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamDataRequesterServiceTest.class );

   @Autowired
   private WicaStreamMetadataCollectorService wicaStreamMetadataCollectorService;

   @Autowired
   private WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService;

   @Autowired
   private WicaStreamPolledValueCollectorService wicaStreamPolledValueCollectorService;

   @Autowired
   private WicaStreamDataRequesterService service;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testStartMonitoring_IncreasesInterestCount()
   {
      final String testChannelNameAsString ="XXX";
      final WicaChannelName testChannelName = WicaChannelName.of( testChannelNameAsString );
      final WicaStream wicaStream = WicaStreamBuilder.create().withId( "myStream" ).withChannelNameAndDefaultProperties( testChannelNameAsString ).build();

      assertEquals(0, service.getInterestCountForChannel( testChannelName ) );

      service.startMonitoring( wicaStream );
      assertEquals(1, service.getInterestCountForChannel( testChannelName ) );

      service.startMonitoring( wicaStream );
      assertEquals(2, service.getInterestCountForChannel( testChannelName ) );
   }

   @Test
   void testStopMonitoring_DecreasesInterestCount()
   {
      final String testChannelNameAsString ="XXX";
      final WicaChannelName testChannelName = WicaChannelName.of( testChannelNameAsString );
      final WicaStream wicaStream = WicaStreamBuilder.create().withId( "myStream" ).withChannelNameAndDefaultProperties( testChannelNameAsString ).build();

      service.startMonitoring( wicaStream );
      service.startMonitoring( wicaStream );
      service.startMonitoring( wicaStream );
      assertEquals(3, service.getInterestCountForChannel(testChannelName ) );

      service.stopMonitoring( wicaStream );
      assertEquals(2, service.getInterestCountForChannel( testChannelName ) );
      service.stopMonitoring( wicaStream );
      assertEquals(1, service.getInterestCountForChannel( testChannelName ) );
      service.stopMonitoring( wicaStream );
      assertEquals(0, service.getInterestCountForChannel( testChannelName ) );
   }

   @Test
   void testStartMonitoring_wicaStreamMetadataCollectorServiceInitialisedOk()
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

      final Map<WicaChannel, WicaChannelMetadata> preInitialValueMap = wicaStreamMetadataCollectorService.get( wicaStream, LocalDateTime.MIN );
      assertThat( preInitialValueMap.size(), is( 0 ) );

      service.startMonitoring( wicaStream );

      final Map<WicaChannel, WicaChannelMetadata> initialMetadataMap = wicaStreamMetadataCollectorService.get( wicaStream, LocalDateTime.MIN );
      assertThat( initialMetadataMap.size(), is( 1 ) );
      assertThat( initialMetadataMap.get( myWicaChannel ).getType(), is( WicaChannelType.UNKNOWN ) );
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

      final Map<WicaChannel, List<WicaChannelValue>> preInitialValueMap = wicaStreamMonitoredValueCollectorService.get( wicaStream, LocalDateTime.MIN );
      assertThat( preInitialValueMap.size(), is( 0 ) );

      service.startMonitoring( wicaStream );

      final Map<WicaChannel, List<WicaChannelValue>> initialValueMap = wicaStreamMonitoredValueCollectorService.get( wicaStream, LocalDateTime.MIN );
      assertThat( initialValueMap.size(), is( 1 ) );
      assertThat( initialValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( initialValueMap.get( myWicaChannel ).get( 0 ).isConnected(), is( false ) );
   }

   @Test
   void testStartPolling_wicaStreamPolledValueCollectorServiceInitialisedOk()
   {
      final String myWicaChannelName = "simon:counter:01";
      final WicaChannelProperties wicaChannelProperties = WicaChannelPropertiesBuilder.create()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL )
            .withFilterType(WicaFilterType.ALL_VALUE )
            .withPollingIntervalInMillis( 200 )
            .build();

      final WicaChannel myWicaChannel = WicaChannelBuilder.create().withChannelNameAndProperties( myWicaChannelName, wicaChannelProperties ).build();

      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withId( "myStream" )
            .withChannel( myWicaChannel )
            .build();

      final Map<WicaChannel, List<WicaChannelValue>> preFirstValueMap = wicaStreamPolledValueCollectorService.get(wicaStream, LocalDateTime.MIN );
      assertThat( preFirstValueMap.size(), is( 0 ) );

      service.startPolling( wicaStream );

      final Map<WicaChannel, List<WicaChannelValue>> firstValueMap = wicaStreamPolledValueCollectorService.get( wicaStream, LocalDateTime.MIN  );
      assertThat( firstValueMap.size(), is( 1 ) );
      assertThat( firstValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( firstValueMap.get( myWicaChannel ).get( 0 ).isConnected(), is( false ) );
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

      logger.info( "Started monitoring stream with {} channels in {} us.  Throughput = {} requests/second.", numberOfChannelsInStream, elapsedTimeInMicroseconds, (1_000_000 * numberOfChannelsInStream ) / elapsedTimeInMicroseconds  );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
