/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.epics;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.time.StopWatch;
import org.epics.ca.Channels;
import org.epics.ca.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChannelsTests
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( ChannelsTests.class );
   private Context context;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Before
   public void before() throws Exception
   {
      // The following sleep is sometimes useful to allow profiling with tools like
      // jvisualvm (provides some time for the tool to connect).
      // Thread.sleep( 10000 );

      // Every test involves the use of at least one context so it is better to
      // set them up and close them down outside the test.

      logger.info( "Creating CA context...");
      final Properties properties = new Properties();
      properties.setProperty(Context.Configuration.EPICS_CA_MAX_ARRAY_BYTES.toString(), "1000000" );
      //properties.setProperty( "CA_DEBUG", "1" );
      context = new Context( properties );
      logger.info( "Done.");

      // Check the database is online
      try {
         context.createChannel("test:db_ok", String.class).connectAsync().get(5, TimeUnit.SECONDS );
      }
      catch ( TimeoutException ex ) {
         logger.error( "The EPICS test database 'epics_tests.db' was not discoverable on the local network." );
         logger.error( "Please ensure that it is running and available on the network before restarting these tests. ");
         throw new RuntimeException( "EPICS Test Database Not Available - can't run tests !" );
      }
   }

   @After
   public void after()
   {
      logger.info( "Cleaning up context...");
      context.close();
      logger.info( "Done.");
   }

   /**
    *  Q31: What is the cost of synchronously connecting channels (using Channels class) ?
    */
   @Test
   public void q31()
   {
      logger.info( "Performing Q31 Test: please wait...");

      final List<Integer> samplePoints = Arrays.asList( 1, 10, 100, 500, 1_000, 2_000, 5_000, 10_000, 15_000, 20_000 );
      final int maxChannels = samplePoints.get( samplePoints.size() - 1 );

      final Map<Integer,Long> resultMap = new LinkedHashMap<>();
      final StopWatch stopWatch = StopWatch.createStarted();

      for ( int i =0; i < maxChannels; i++ )
      {
         try
         {
            Channels.create( context, "test:counter01", String.class );

            if ( samplePoints.contains( i ) )
            {
               resultMap.put( i, stopWatch.getTime() );
               logger.info( "Synchronously connected {} channels.", i );
            }
         }
         catch( Throwable ex )
         {
            logger.info( "Test terminated due to exception after synchronously connecting {} channels", i );
         }
      }

      logger.info( "RESULTS:" );
      logger.info( "Q31: What is the cost of synchronously connecting channels (using Channels class) ? Answer: **See below.**" );
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         logger.info( "- Synchronously connecting {} channels took {} ms. Average: {} ms", result, resultMap.get( result ),  String.format( Locale.ROOT, "%.3f", (float) resultMap.get( result ) / result ) );
      }
      logger.info("```");
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
