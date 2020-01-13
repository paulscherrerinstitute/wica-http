/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.ca;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.time.StopWatch;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Context;
import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Ignore
@Disabled
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChannelTests
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(ChannelTests.class);

   private Context context;


   /*- Main ---------------------------------------------------------------------*/
   /*- Constructor --------------------------------------------------------------*/
   /*- Class methods ------------------------------------------------------------*/
   /*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   @Before
   public void before() throws Exception
   {
      // The following sleep is sometimes useful to allow profiling with tools like
      // jvisualvm (provides some time for the tool to connect).
      //Thread.sleep( 10000 );

      // Every test involves the use of at least one context so it is better to
      // set them up and close them down outside the test.

      logger.info("Creating CA context...");

      System.setProperty(Context.Configuration.EPICS_CA_MAX_ARRAY_BYTES.toString(), "1000000");
      //System.setProperty( Context.Configuration.EPICS_CA_ADDR_LIST.toString(), "129.129.130.255 129.129.131.255 129.129.137.255 129.129.145.255" );
      //System.setProperty( "CA_DEBUG", "1" );
      //System.setProperty( "CA_MONITOR_NOTIFIER", "MultipleWorkerBlockingQueueImpl" );
      System.setProperty( "CA_MONITOR_NOTIFIER", "MultipleWorkerBlockingQueueMonitorNotificationServiceImpl" );
      //System.setProperty( "CA_MONITOR_NOTIFIER", "DisruptorImpl" );

      context = new Context();
      logger.info("Done.");

      // Check the database is online
      try
      {
         context.createChannel("wica:test_db_ok", String.class).connectAsync().get(5, TimeUnit.SECONDS);
      }
      catch ( TimeoutException ex )
      {
         logger.error("The EPICS test database 'epics_tests.db' was not discoverable on the local network.");
         logger.error("Please ensure that it is running and available on the network before restarting these tests. ");
         throw new RuntimeException("EPICS Test Database Not Available - can't run tests !");
      }
   }

   @AfterEach
   @After
   public void after()
   {
      logger.info("Cleaning up context...");
      context.close();
      logger.info("Done.");
   }

   /**
    * Q10: How many channels can be created ?
    * Q11: What is the creation cost ?
    * Q12: Do all channels connected to the same PV share the same returned object ?
    */
   @Test
   public void q10q11q12()
   {
      logger.info("Performing Q10/Q11/Q12 Tests: please wait...");

      final List<Integer> samplePoint = Arrays.asList(1, 10, 100, 1_000, 10_000, 50_000, 100_000, 500_000, 1_000_000);
      final Map<Integer, Long> resultMap = new LinkedHashMap<>();
      final LinkedHashMap<Integer, Channel<String>> channelObjectMap = new LinkedHashMap<>();

      final StopWatch stopWatch = StopWatch.createStarted();
      int loopCounter = 0;

      while ( loopCounter < samplePoint.get(samplePoint.size() - 1) )
      {
         loopCounter++;

         try
         {
            if ( samplePoint.contains(loopCounter) )
            {
               // For sampling points the channels all have the same name. This allows us to check later
               // whether they apply onto the same object.
               final Channel<String> caChannel = context.createChannel("channel_name_will_be_the same", String.class);
               resultMap.put(loopCounter, stopWatch.getTime());
               channelObjectMap.put(loopCounter, caChannel);
               logger.info("Created {} channels.", loopCounter);
            }
            else
            {
               // For non-sampling points the channels have unique names. This is more typical
               // of real world usage.
               context.createChannel("channel_name_will_be_different" + String.valueOf(loopCounter), String.class);
            }
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after creating {} channels", loopCounter);
            break;
         }
      }

      logger.info("RESULTS:");
      logger.info("Q10: How many channels can be created ? Answer: **at least {}**", loopCounter);
      logger.info("Q11: What is the channel creation cost ? Answer: **See below.**");
      for ( int result : resultMap.keySet() )
      {
         logger.info("- Creating {} channels took {} ms. Average: {} ms", result, resultMap.get(result), String.format(Locale.ROOT, "%.3f", (float) resultMap.get(result) / result));
      }

      logger.info("Q12: Do all channels connected to the same PV share the same returned object ? Answer: {}.", resultMap.size() == channelObjectMap.size() ? "**NO**" : "**YES**");
      logger.info("Channel object names were as follows:");
      logger.info("```");
      for ( int sampleNumber : channelObjectMap.keySet() )
      {
         logger.info("- Channel object {} had name: '{}'", sampleNumber, channelObjectMap.get(sampleNumber));
      }
      logger.info("```");
   }

   /**
    * Q13: How many connected channels can the library simultaneously support ?
    * Q14: What is the cost of synchronously connecting channels (using Channel class) ?
    */
   @Test
   public void q13q14()
   {
      logger.info("Performing Q13/Q14 Tests: please wait...");

      final List<Integer> samplePoints = Arrays.asList(1, 10, 100, 500, 1_000, 2_000 );
      final int maxChannels = samplePoints.get(samplePoints.size() - 1);
      final Map<Integer, Long> resultMap = new LinkedHashMap<>();
      final StopWatch stopWatch = StopWatch.createStarted();
      int loopCounter = 0;

      while ( loopCounter < maxChannels )
      {
         loopCounter++;
         try
         {
            final Channel caChannel = context.createChannel("test:counter01", String.class);
            caChannel.connect();
            if ( samplePoints.contains(loopCounter) )
            {
               resultMap.put(loopCounter, stopWatch.getTime());
               logger.info("Synchronously connected {} channels.", loopCounter);
            }
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after synchronously connecting {} channels", loopCounter);
         }
      }

      logger.info("RESULTS:");
      logger.info("Q13: How many connected channels can the library simultaneously support ? Answer: **at least {}**", loopCounter);
      logger.info("Q14: What is the cost of synchronously connecting channels (using Channel class) ? Answer: **See below.**");
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         logger.info("- Synchronously connecting {} channels took {} ms. Average: {} ms", result, resultMap.get(result), String.format(Locale.ROOT, "%.3f", (float) resultMap.get(result) / result));
      }
      logger.info("```");
   }

   /**
    * Q15: What is the cost of creating channels which will asynchronously connect ?
    * Q16: How long does it take for channels to connect asynchronously ?
    */
   @Test
   public void q15q16() throws InterruptedException
   {
      logger.info("Performing Q15/Q16 Tests: please wait...");

      final List<Integer> samplePoints = Arrays.asList(1, 10, 100, 1_000, 10_000, 50_000, 100_000, 150_000, 200_000);
      int maxChannels = samplePoints.get(samplePoints.size() - 1);

      final Map<Integer, Long> creationTimeResultMap = new LinkedHashMap<>();
      final Map<Integer, Long> connectionTimeResultMap = new LinkedHashMap<>();
      final StopWatch stopWatch = StopWatch.createStarted();
      int loopCounter = 0;

      final AtomicInteger connectionCount = new AtomicInteger(0);

      while ( loopCounter < maxChannels )
      {
         loopCounter++;
         try
         {
            final Channel<String> caChannel = context.createChannel("test:counter01", String.class);
            caChannel.addConnectionListener(( c, b ) -> {
               final int count = connectionCount.incrementAndGet();
               if ( samplePoints.contains(count) )
               {
                  connectionTimeResultMap.put(count, stopWatch.getTime());
                  logger.info("Connected {} async channels", count);
               }
            });
            caChannel.connectAsync();

            if ( samplePoints.contains(loopCounter) )
            {
               creationTimeResultMap.put(loopCounter, stopWatch.getTime());
               logger.info("Created {} async channels", loopCounter);
            }
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after creating and asynchronously connecting {} channels", loopCounter);
            break;
         }
      }
      while ( connectionCount.get() < maxChannels )
      {
         Thread.sleep(100);
      }

      logger.info("RESULTS:");

      logger.info("Q15: What is the cost of creating channels which will asynchronously connect ? Answer: **See below.**");
      logger.info("```");
      for ( int result : creationTimeResultMap.keySet() )
      {
         logger.info("- Creating {} channels with asynchronous connect policy took {} ms. Average: {} ms", result, creationTimeResultMap.get(result), String.format(Locale.ROOT, "%3f", (float) creationTimeResultMap.get(result) / result));
      }
      logger.info("```");

      logger.info("Q16: How long does it take for channels to connect asynchronously ? Answer: **See below.**");
      logger.info("```");
      for ( int result : connectionTimeResultMap.keySet() )
      {
         logger.info("- Connecting {} channels asynchronously took {} ms. Average: {} ms.", result, connectionTimeResultMap.get(result), String.format(Locale.ROOT, "%.3f", (float) connectionTimeResultMap.get(result) / result));
      }
      logger.info("```");
   }

   /**
    * Q17: What is the cost of performing a synchronous get on multiple channels (all on the same PV) ?
    */
   @Test
   public void q17() throws InterruptedException
   {
      logger.info("Performing Q17 Test: please wait...");
      final List<Integer> samplePoints = Arrays.asList(1, 10, 100, 1_000, 10_000, 20_000, 40_000, 60_000, 80_000, 100_000);
      final int maxChannels = samplePoints.get(samplePoints.size() - 1);

      final Map<Integer, Long> resultMap = new LinkedHashMap<>();
      final List<Channel<String>> channelList = new ArrayList<>();
      final AtomicInteger connectionCount = new AtomicInteger(0);

      logger.info("Creating and asynchronously connecting {} channels...", maxChannels);

      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> caChannel = context.createChannel("test:counter01", String.class);
            channelList.add(caChannel);
            caChannel.addConnectionListener(( c, b ) -> connectionCount.incrementAndGet());
            caChannel.connectAsync();
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after creating and asynchronously connecting {} channels", i);
            return;
         }
      }
      logger.info("{} channels created.", maxChannels);

      while ( connectionCount.get() < maxChannels )
      {
         Thread.sleep(100);
      }
      logger.info("{} channels connected.", maxChannels);
      logger.info("Performing synchronous get on all channels...");
      final StopWatch stopWatch = StopWatch.createStarted();

      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> channel = channelList.get(i);
            channel.get();
            if ( samplePoints.contains(i) )
            {
               resultMap.put(i, stopWatch.getTime());
               logger.info("Synchronous Get completed on {} channels", i);
            }
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after getting from {} channels", i);
         }
      }

      logger.info("RESULTS:");
      logger.info("Q17: What is the cost of performing a synchronous get on multiple channels (same PV) ? Answer: **See below.**");
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         logger.info("- Synchronous Get from {} channels took {} ms. Average: {} ms", result, resultMap.get(result), String.format(Locale.ROOT, "%3f", (float) resultMap.get(result) / result));
      }
      logger.info("```");
   }

   /**
    * Q18: What is the cost of performing an asynchronous get on multiple channels (all on the same PV) ?
    */
   @Test
   public void q18() throws InterruptedException
   {
      logger.info("Performing Q15 Test: please wait...");

      final List<Integer> samplePoints = Arrays.asList(1, 10, 100, 1_000, 10_000, 20_000, 40_000, 60_000, 80_000, 100_000);
      final int maxChannels = samplePoints.get(samplePoints.size() - 1);
      final List<Channel<String>> channelList = new ArrayList<>();
      final AtomicInteger connectionCount = new AtomicInteger(0);


      logger.info("Creating and asynchronously connecting {} channels...", maxChannels);
      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> caChannel = context.createChannel("test:counter01", String.class);
            channelList.add(caChannel);
            caChannel.addConnectionListener(( c, b ) -> {
               if ( b )
               {
                  connectionCount.incrementAndGet();
               }
               else
               {
                  logger.info("ConnectionListener indicated unexpected disconnect");
               }
            });
            caChannel.connectAsync();
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after creating and asynchronously connecting {} channels", i);
            return;
         }
      }
      logger.info("{} channels created.", maxChannels);

      while ( connectionCount.get() < maxChannels )
      {
         logger.info("Waiting for completion {} / {} ", connectionCount.get(), maxChannels);
         Thread.sleep(1000);
      }
      logger.info("{} channels connected.", maxChannels);
      logger.info("Performing asynchronous Get on all channels...");

      final AtomicInteger getCompletionCount = new AtomicInteger(0);
      final StopWatch stopWatch = StopWatch.createStarted();
      final Map<Integer, Long> resultMap = new LinkedHashMap<>();

      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> channel = channelList.get(i);
            channel.getAsync()
                  .thenAccept(( v ) -> {
                     final int count = getCompletionCount.incrementAndGet();
                     if ( samplePoints.contains(count) )
                     {
                        resultMap.put(count, stopWatch.getTime());
                        logger.info("Asynchronous Get completed normally on {} channels. Last value was: {}", count, v);
                     }
                  });

            if ( samplePoints.contains(i) )
            {
               logger.info("Asynchronous Get requested on {} channels", i);
            }
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after getting from {} channels", i);
            break;
         }
      }
      logger.info("Asynchronous Get was requested on all channels.");

      while ( getCompletionCount.get() < maxChannels )
      {
         logger.info("Waiting for completion {} / {} ", getCompletionCount.get(), maxChannels);
         Thread.sleep(1000);
      }
      logger.info("{} channels delivered their get results.", maxChannels);

      logger.info("RESULTS:");
      logger.info("Q18: What is the cost of performing an asynchronous get on multiple channels (same PV) ? Answer: **See below.**");
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         logger.info("- Asynchronous Get from {} channels took {} ms. Average: {} ms", result, resultMap.get(result), String.format(Locale.ROOT, "%3f", (float) resultMap.get(result) / result));
      }
      logger.info("```");
   }

   /**
    * Q19: What is the cost of performing an asynchronous get on multiple channels (different PV's) ?
    */
   @Test
   public void q19() throws InterruptedException
   {
      logger.info("Performing Q19 Test: please wait...");
      final List<Integer> samplePoints = Arrays.asList(1, 1000_000);

//      final List<Integer> samplePoints = Arrays.asList( 1, 10, 100, 1_000, 10_000, 20_000, 40_000, 60_000, 80_000, 100_000 );
      final int maxChannels = samplePoints.get(samplePoints.size() - 1);
      final List<Channel<String>> channelList = new ArrayList<>();

      final AtomicInteger connectionCount = new AtomicInteger(0);

      logger.info("Creating and asynchronously connecting {} channels...", maxChannels);
      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> caChannel = context.createChannel("test:counter" + String.format("%02d", (i % 100)), String.class);
            channelList.add(caChannel);
            caChannel.addConnectionListener(( c, b ) -> connectionCount.incrementAndGet());
            caChannel.connectAsync();
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after creating and asynchronously connecting {} channels", i);
            return;
         }
      }
      logger.info("{} channels created.", maxChannels);

      while ( connectionCount.get() < maxChannels )
      {
         Thread.sleep(100);
      }
      logger.info("{} channels connected.", maxChannels);
      logger.info("Performing asynchronous get on all channels...");

      Thread.sleep(1000);
      final AtomicInteger getCompletionCount = new AtomicInteger(0);
      final StopWatch stopWatch = StopWatch.createStarted();
      final Map<Integer, Long> resultMap = new LinkedHashMap<>();
      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> channel = channelList.get(i);
            if ( channel.getConnectionState() != ConnectionState.CONNECTED ) logger.info("OOOOOOPPPS!!!!");
            channel.getAsync()
                  .thenAccept(( v ) -> {
                     final int count = getCompletionCount.incrementAndGet();
                     if ( samplePoints.contains(count) )
                     {
                        resultMap.put(count, stopWatch.getTime());
                        logger.info("Asynchronous Get completed on {} channels. Last value was: {}", count, v);
                     }
                  });

            if ( samplePoints.contains(i) )
            {
               logger.info("Asynchronous Get requested on {} channels", i);
            }
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after getting from {} channels", i);
            break;
         }
      }
      logger.info("Asynchronous get was requested on all channels.");

      while ( getCompletionCount.get() < maxChannels )
      {
         Thread.sleep(100);
      }
      logger.info("{} channels delivered their get results.", maxChannels);

      logger.info("RESULTS:");
      logger.info("Q19: What is the cost of performing an asynchronous get on multiple channels (different PVs) ? Answer: **See below.**");
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         logger.info("- Asynchronous Get from {} channels took {} ms. Average: {} ms", result, resultMap.get(result), String.format(Locale.ROOT, "%3f", (float) resultMap.get(result) / result));
      }
      logger.info("```");
   }

   /**
    * Q20: What is the cost of performing a monitor on multiple channels ?
    */
   @ParameterizedTest
   @ValueSource( strings = { "BlockingQueueSingleWorkerMonitorNotificationServiceImpl",
                             "BlockingQueueMultipleWorkerMonitorNotificationServiceImpl",
                             "DisruptorOldMonitorNotificationServiceImpl",
                             "DisruptorNewMonitorNotificationServiceImpl",
                             "StripedExecutorServiceMonitorNotificationServiceImpl"} )

   public void q20( String serviceImpl ) throws InterruptedException
   {
      logger.info("Performing Q22 Test using Monitor Notification Service Impl {}: please wait..." , serviceImpl );

      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", serviceImpl );
      final Context mySpecialContext = new Context();

      final List<Integer> samplePoints = Arrays.asList( 1, 100, 200, 500, 1000 );
      final int maxChannels = samplePoints.get(samplePoints.size() - 1);

      final List<Channel<String>> channelList = new ArrayList<>();
      final AtomicInteger connectionCount = new AtomicInteger(0);
      logger.info("Creating and asynchronously connecting {} channels...", maxChannels);
      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> caChannel = mySpecialContext.createChannel("test:counter01", String.class);
            channelList.add(caChannel);
            caChannel.addConnectionListener(( c, b ) -> {
               //logger.info("Channel: {} state: {}", c, b);
               connectionCount.incrementAndGet();
            });
            caChannel.connectAsync();
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after creating and asynchronously connecting {} channels", i);
            return;
         }
      }

      logger.info("{} channels created.", maxChannels);

      while ( connectionCount.get() < maxChannels )
      {
         Thread.sleep(100);
      }
      logger.info("{} channels connected.", maxChannels);

      logger.info("Performing addValueMonitor on all channels...");

      final AtomicInteger monitorUpdateCounter = new AtomicInteger(0);
      final StopWatch stopWatch = StopWatch.createStarted();
      final Map<Integer, Long> resultMap = new LinkedHashMap<>();
      final Map<Channel<String>, String> monitorMap = Collections.synchronizedMap(new LinkedHashMap<>());
      for ( int i = 0; i < maxChannels; i++ )
      {
         try
         {
            final Channel<String> channel = channelList.get(i);
            channel.addValueMonitor(( v ) -> {
               final int count = monitorUpdateCounter.incrementAndGet();
               //logger.info("Monitor update on channel: {}, had value: {} ", channel, v);
               monitorMap.put(channel, v);
               //logger.info("Number of unique channels is: {}", monitorMap.keySet().size());
               if ( samplePoints.contains(count) )
               {
                  resultMap.put(count, stopWatch.getTime());
                  logger.info("Monitor was established on {} channels. Observed value for channel {} was: {}", count, channel, v);
               }
            });

            if ( samplePoints.contains(i) )
            {
               logger.info("Monitor requested on {} channels", i);
            }
         }
         catch ( Throwable ex )
         {
            logger.info("Test terminated due to exception after adding value monitor to {} channels", i);
            return;
         }
      }
      logger.info("Monitor was requested on all channels.");

      while ( monitorUpdateCounter.get() < maxChannels )
      {
         Thread.sleep(100);
      }
      logger.info("{} channels delivered their addValueMonitor results.", maxChannels);

      logger.info("RESULTS:");
      logger.info("Q20: What is the cost of performing a monitor on multiple channels ? Answer: **See below.**");
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         logger.info("- Asynchronous Monitor from {} channels took {} ms. Average: {} ms", result, resultMap.get(result), String.format(Locale.ROOT, "%3f", (float) resultMap.get(result) / result));
      }
      logger.info("```");
   }

   /**
    * Q21: What is the cost/performance when using CA to transfer large arrays ?
    */

   @ParameterizedTest
   @ValueSource( strings = { "BlockingQueueSingleWorkerMonitorNotificationServiceImpl",
                             "BlockingQueueMultipleWorkerMonitorNotificationServiceImpl",
                             "DisruptorOldMonitorNotificationServiceImpl",
                             "DisruptorNewMonitorNotificationServiceImpl",
                             "StripedExecutorServiceMonitorNotificationServiceImpl"} )

   public void q21( String serviceImpl )
   {
      logger.info("Performing Q21 Test: please wait...");

      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", serviceImpl );

      final int MAX_ELEMENTS_TO_TRANSFER = 10_000_000;
      final int SIZE_OF_ELEMENT_IN_BYTES = Integer.SIZE / Byte.SIZE;
      final List<Integer> elementsToTransferSamplingPoints = Arrays.asList( 10_000, 20_000, 50_000,
                                                                            100_000, 200_000, 500_000,
                                                                            1_000_000, 2_000_000, 5_000_000, 10_000_000);

      // Set up a context which allocates enough room for the data to be transferred
      // Alternatively CA claims that it is ok to leave the value undefined
      final Properties properties = new Properties();
      //properties.setProperty( "CA_DEBUG", "1" );
      //properties.setProperty( "EPICS_CA_MAX_ARRAY_BYTES", String.valueOf( MAX_ELEMENTS_TO_TRANSFER * SIZE_OF_ELEMENT_IN_BYTES ) );
      final Context ctx = new Context(properties);

      final Map<Integer, Long> resultMap = new LinkedHashMap<>();

      try ( ctx )
      {
         logger.info("Creating channel...");
         final Channel<int[]> caChannel = ctx.createChannel("wica:test:rawdata", int[].class);

         logger.info("Connecting channel...");
         caChannel.connect();

         for ( int elementsToTransfer : elementsToTransferSamplingPoints )
         {
            logger.info("Measuring transfer time for array of size {} elements...", elementsToTransfer);

            // Set up an array of the appropriate transfer size
            final int arr[] = new int[ elementsToTransfer ];
            for ( int i = 0; i < elementsToTransfer; i++ )
            {
               arr[ i ] = i;
            }

            // By performing a caput of an array with the required size this will ensure that
            // the EPICS waveform .NORD field gets set correctly. This will ensure that the
            // full data set gets transferred when performing measurements on the subsequent
            // get transfer rates.
            caChannel.put(arr);

            final StopWatch totalElapsedTimeStopWatch = StopWatch.createStarted();
            caChannel.get();
            long elapsedTime = totalElapsedTimeStopWatch.getTime();
            resultMap.put(elementsToTransfer, elapsedTime);
         }
      }
      catch ( Throwable ex )
      {
         logger.info("Test terminated unexpectedly due to exception: '{}'", ex.toString() );
         return;
      }

      logger.info("RESULTS:");
      logger.info("Q21: What is the cost/performance when using CA to transfer large arrays ? Answer: **See below.**");
      logger.info("```");
      for ( int result : resultMap.keySet() )
      {
         final float transferRate = ((float) 1000 * SIZE_OF_ELEMENT_IN_BYTES * result) / (((float) resultMap.get(result)) * 1024 * 1024);
         logger.info("- Transfer time for integer array of {} elements took {} ms. Transfer rate: {} MB/s", result, resultMap.get(result), String.format(Locale.ROOT, "%.0f", transferRate));
      }
      logger.info("```");
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
