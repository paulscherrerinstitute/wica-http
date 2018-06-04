/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.epics;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.time.StopWatch;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Context;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ContextTests
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
   public void q01()
   {
      logger.info( "Performing Q1 Test: please wait...");

      final Context caContext = new Context();
      final Channel caChannel = caContext.createChannel("test:counter01", String.class);
      caChannel.connect();
      Assert.assertEquals( ConnectionState.CONNECTED, caChannel.getConnectionState() );
      caContext.close();
      Assert.assertEquals( ConnectionState.CLOSED, caChannel.getConnectionState() );

      logger.info( "RESULTS:" );
      logger.info( "Q1: Can the context manual close feature be relied on to cleanup the created channels ? Answer: **YES**" );
   }

   /**
    * Q2: Can the context autoclose feature be relied on to cleanup the created channels ?
    */
   @Test
   public void q02()
   {
      logger.info( "Performing Q2 Test: please wait...");

      final Context caContext = new Context();
      final Channel caChannel;
      try (caContext)
      {
         caChannel = caContext.createChannel("test:counter01", String.class);
         caChannel.connect();
         Assert.assertEquals(ConnectionState.CONNECTED, caChannel.getConnectionState());
      }

      // After the try-with-resources statment the context should have closed
      // the channel
      Assert.assertEquals(ConnectionState.CLOSED, caChannel.getConnectionState());

      // And it should no longer be possible to create new channels
      try
      {
         caContext.createChannel("test:counter01", String.class);
      }
      catch (Throwable t)
      {
         Assert.assertTrue(t instanceof RuntimeException);
      }
      logger.info( "RESULTS:" );
      logger.info( "Q2: Can the context autoclose feature be relied on to cleanup the created channels ? Answer: **YES**" );
   }

   /**
    * Q3: How many contexts can be created ?
    * Q4: What is the creation cost ?
    * Q5: Do all contexts share the same returned object ?
    */
   @Test
   public void q03q04q05()
   {
      logger.info( "Performing Q3/Q4/Q5 Tests: please wait...");

      final List<Integer> samplePoint = Arrays.asList( 1, 10, 50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750 );
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
