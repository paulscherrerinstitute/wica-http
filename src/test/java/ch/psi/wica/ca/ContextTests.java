/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.ca;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.time.StopWatch;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Context;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Ignore
@Disabled
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ContextTests
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( ContextTests.class );
   private Context context;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Q1: Can the context manual close feature be relied on to cleanup the created channels ?
    */
   @Test
   void q01()
   {
      logger.info( "Performing Q1 Test: please wait...");

      assertTimeoutPreemptively( Duration.ofSeconds( 5L ), () ->
      {
         final Context caContext = new Context();
         final Channel caChannel = caContext.createChannel("test:counter01", String.class);
         caChannel.connect();
         Assertions.assertEquals(ConnectionState.CONNECTED, caChannel.getConnectionState());
         caContext.close();
         Assertions.assertEquals(ConnectionState.CLOSED, caChannel.getConnectionState());

         logger.info("RESULTS:");
         logger.info("Q1: Can the context manual close feature be relied on to cleanup the created channels ? Answer: **YES**" );
      } );
   }

   /**
    * Q2: Can the context autoclose feature be relied on to cleanup the created channels ?
    */
   @Test
   void q02()
   {
      logger.info( "Performing Q2 Test: please wait...");

      assertTimeoutPreemptively( Duration.ofSeconds( 5L ), () ->
      {
         final Context caContext = new Context();
         final Channel caChannel;
         try ( caContext )
         {
            caChannel = caContext.createChannel("test:counter01", String.class);
            caChannel.connect();
            Assertions.assertEquals(ConnectionState.CONNECTED, caChannel.getConnectionState());
         }

         // After the try-with-resources statement the context should have closed
         // the channel
         Assertions.assertEquals(ConnectionState.CLOSED, caChannel.getConnectionState());

         // And it should no longer be possible to createNext new channels
         try
         {
            caContext.createChannel("test:counter01", String.class);
         }
         catch ( Throwable t )
         {
            Assertions.assertTrue( t instanceof RuntimeException);
         }
         logger.info( "RESULTS:");
         logger.info( "Q2: Can the context autoclose feature be relied on to cleanup the created channels ? Answer: **YES**");
      } );
   }

   /**
    * Q3: How many contexts can be created ?
    * Q4: What is the creation cost ?
    * Q5: Do all contexts share the same returned object ?
    */
   @Test
   void q03q04q05()
   {
      logger.info( "Performing Q3/Q4/Q5 Tests: please wait...");

      // With default behaviour of ca-1.1.0 release we can only createNext ~200 contexts (as
      // each context starts 10 threads)
      // With default behaviour of ca-1.2.0 release we can only createNext ~100 contexts (as
      // each context starts 16 threads)
      final List<Integer> samplePoint = Arrays.asList( 1, 10, 50, 100 );
      final Map<Integer,Long> resultMap = new LinkedHashMap<>();
      final Map<Integer,Context> contextObjectMap = new LinkedHashMap<>();
      final List<Context> contextList = new ArrayList<>();

      final StopWatch stopWatch = StopWatch.createStarted();
      int loopCounter = 0;

      while ( loopCounter < samplePoint.get( samplePoint.size() - 1 ) )
      {
         loopCounter++;
         final Context caContext;
         try
         {
            caContext= new Context();
            contextList.add( caContext );
         }
         catch( Throwable ex )
         {
            logger.info( "Test terminated due to exception after creating {} contexts", loopCounter );
            break;
         }
         if ( samplePoint.contains( loopCounter ) )
         {
            resultMap.put( loopCounter, stopWatch.getTime() );
            contextObjectMap.put( loopCounter, caContext );
            logger.info( "Created {} contexts.", loopCounter );
         }
      }

      logger.info( "RESULTS:" );
      logger.info( "Q3: How many contexts can be created ? Answer: **at least {}**", loopCounter );
      logger.info( "Q4: What is the context creation cost ? Answer: **See below.**" );
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         logger.info( "- Creating {} contexts took {} ms. Average: {} ms", result, resultMap.get( result ),  String.format( Locale.ROOT, "%.3f", (float) resultMap.get( result ) / result ) );
      }
      logger.info("```");

      logger.info( "Q5: Do all contexts share the same returned object ? Answer: {}.", resultMap.size() == contextObjectMap.size() ? "**NO**" : "**YES**" );
      logger.info ("Context object names were as follows:" );
      logger.info("```");
      for ( int sampleNumber : contextObjectMap.keySet() )
      {
         logger.info( "- Context object {} had name: '{}'", sampleNumber, contextObjectMap.get( sampleNumber )  );
      }
      logger.info("```");

      // Cleanup the contexts by closing them
      for ( Context caContext : contextList )
      {
         caContext.close();
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
