/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.*;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
      final WicaStream wicaStream = WicaStream.createBuilder().withId( "myStream" ).withChannelName(testChannelNameAsString ).build();

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
      final WicaStream wicaStream = WicaStream.createBuilder().withId( "myStream" ).withChannelName(testChannelNameAsString ).build();

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
      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
      final WicaChannel myWicaChannel = WicaChannel.createFromName( myWicaChannelName );
      final WicaStream wicaStream = WicaStream.createBuilder()
            .withId( "myStream" )
            .withChannelName( myWicaChannelName )
            .build();

      service.startMonitoring( wicaStream );
      final Map<WicaChannel, WicaChannelMetadata> initialMetadataMap = wicaStreamMetadataCollectorService.get( wicaStream );
      assertThat( initialMetadataMap.size(), is( 1 ) );
      assertThat( initialMetadataMap.get( myWicaChannel ).getType(), is( WicaChannelType.UNKNOWN ) );
   }

   @Test
   void testStartMonitoring_wicaStreamMonitoredValueCollectorServiceInitialisedOk()
   {
      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
      final WicaChannel myWicaChannel = WicaChannel.createFromName( myWicaChannelName );
      final WicaStream wicaStream = WicaStream.createBuilder()
            .withId( "myStream" )
            .withChannelName( myWicaChannelName )
            .build();

      final Map<WicaChannel, Optional<WicaChannelValue>> preInitialValueMap = wicaStreamMonitoredValueCollectorService.getLatest(wicaStream );
      assertThat( preInitialValueMap.size(), is( 1 ) );
      assertThat( preInitialValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( preInitialValueMap.get( myWicaChannel).isPresent(), is( false ) );

      service.startMonitoring( wicaStream );

      final Map<WicaChannel, Optional<WicaChannelValue>> initialValueMap = wicaStreamMonitoredValueCollectorService.getLatest(wicaStream );
      assertThat( initialValueMap.size(), is( 1 ) );
      assertThat( initialValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( initialValueMap.get( myWicaChannel).isPresent(), is( true ) );
      assertThat( initialValueMap.get( myWicaChannel ).get().isConnected(), is( false ) );
   }

   @Test
   void testStartPolling_wicaStreamPolledValueCollectorServiceInitialisedOk()
   {
      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
      final WicaChannelProperties wicaChannelProperties = WicaChannelProperties.createBuilder()
            .withDataAcquisitionMode( WicaChannelProperties.DataAcquisitionMode.POLL )
            .build();

      final WicaChannel myWicaChannel = WicaChannel.createFromNameAndProperties( myWicaChannelName, wicaChannelProperties );

      final WicaStream wicaStream = WicaStream.createBuilder()
            .withId( "myStream" )
            .withChannel( myWicaChannel )
            .build();

      final Map<WicaChannel, Optional<WicaChannelValue>> preFirstValueMap = wicaStreamPolledValueCollectorService.getLatest( wicaStream );
      assertThat( preFirstValueMap.size(), is( 1 ) );
      assertThat( preFirstValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( preFirstValueMap.get( myWicaChannel).isPresent(), is( false ) );

      service.startPolling( wicaStream );

      final Map<WicaChannel, Optional<WicaChannelValue>> firstValueMap = wicaStreamPolledValueCollectorService.getLatest( wicaStream );
      assertThat( firstValueMap.size(), is( 1 ) );
      assertThat( firstValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( firstValueMap.get( myWicaChannel).isPresent(), is( true ) );
      assertThat( firstValueMap.get( myWicaChannel ).get().isConnected(), is( false ) );
   }

   @CsvSource( { "1", "10", "100", "1000", "10000" })
   @ParameterizedTest
   void testStartMonitoringPerformanceTest( int numberOfChannelsInStream )
   {
      final Set<WicaChannel> wicaChannelSet = new HashSet<>();
      for ( int i = 0; i < numberOfChannelsInStream; i++ )
      {
         final WicaChannelName myWicaChannelName = WicaChannelName.of( "chan-" + i );
         final WicaChannel myWicaChannel = WicaChannel.createFromName( myWicaChannelName );
         wicaChannelSet.add( myWicaChannel );
      }

      final WicaStream wicaStream = WicaStream.createBuilder()
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
