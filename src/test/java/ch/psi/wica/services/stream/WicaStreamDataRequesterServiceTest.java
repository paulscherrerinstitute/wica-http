/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelType;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.controlsystem.event.WicaChannelMetadataBufferingService;
import ch.psi.wica.controlsystem.event.WicaChannelValueBufferingService;
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
   private WicaChannelMetadataBufferingService wicaChannelMetadataBufferingService;

   @Autowired
   private WicaChannelValueBufferingService wicaChannelValueBufferingService;

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
   void testStartMonitoring_wicaChannelMetadataBufferServiceInitialisedOk()
   {
      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
      final WicaChannel myWicaChannel = WicaChannel.createFromName( myWicaChannelName );
      final WicaStream wicaStream = WicaStream.createBuilder()
            .withId( "myStream" )
            .withChannelName(myWicaChannelName )
            .build();

      service.startMonitoring( wicaStream );
      final Map<WicaChannel, WicaChannelMetadata> initialMetadata = wicaChannelMetadataBufferingService.get( Set.of(myWicaChannel ) );
      assertThat( initialMetadata.get( myWicaChannel ).getType(), is( WicaChannelType.UNKNOWN ) );
   }

   @Test
   void testStartMonitoring_wicaChannelValueBufferServiceInitialisedOk()
   {
      final WicaChannelName myWicaChannelName = WicaChannelName.of( "simon:counter:01" );
      final WicaStream wicaStream = WicaStream.createBuilder()
            .withId( "myStream" )
            .withChannelName(myWicaChannelName )
            .build();

      service.startMonitoring( wicaStream );
      final var initialValue = wicaChannelValueBufferingService.getLatest( myWicaChannelName.getControlSystemName() );
      assertThat( initialValue.isConnected(), is( false ) );
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
