/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import ch.psi.wica.services.epics.EpicsChannelMonitorService;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class WicaChannelService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelMonitorService.class );
   private final Context context;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelService()
   {
      logger.info( "Creating CA context for WicaChannelService..." );

      System.setProperty(Context.Configuration.EPICS_CA_MAX_ARRAY_BYTES.toString(), "1000000");
      //System.setProperty( Context.Configuration.EPICS_CA_ADDR_LIST.toString(), "129.129.130.255 129.129.131.255 129.129.137.255 129.129.145.255" );
      //System.setProperty( "CA_DEBUG", "1" );
      //System.setProperty( "CA_MONITOR_NOTIFIER", "MultipleWorkerBlockingQueueImpl" );
      System.setProperty( "CA_MONITOR_NOTIFIER", "MultipleWorkerBlockingQueueMonitorNotificationServiceImpl" );
      //System.setProperty( "CA_MONITOR_NOTIFIER", "DisruptorImpl" );

      context = new Context();
      logger.info( "Done.");
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Gets the value of a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous (=confirmed) GET operation.
    *
    * @param wicaChannelName the name of the channel.
    * @param timeout the timeout to be applied when attempting to get the channel
    *     value from the underlying data source. If a timeout occurs the returned
    *     value will be WicaChannelValueDisconnected.
    * @param timeUnit the time units to be used.
    * @return the value.
    */
   public WicaChannelValue get( WicaChannelName wicaChannelName, long timeout, TimeUnit timeUnit )
   {
      // Create a new channel
      final String channelName = wicaChannelName.getControlSystemName().asString();
      final Channel<String> caChannel;
      try
      {
         logger.info( "'{}' - Creating channel...", channelName );
         caChannel = context.createChannel( channelName, String.class );
         logger.info( "'{}' - OK: channel created.", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "'{}' - ERROR: Exception whilst creating channel. Details: '{}'", channelName, ex.getMessage() );
         return WicaChannelValue.createChannelValueDisconnected();
      }

      // Wait for it to connect
      try
      {
         logger.info( "'{}' - Connecting channel with timeout {} {}...", channelName, timeout, timeUnit );
         caChannel.connectAsync().get( timeout, timeUnit );
         logger.info( "'{}' - OK: channel connected.", channelName );
      }
      catch ( InterruptedException | ExecutionException | TimeoutException ex )
      {
         logger.info( "'{}' - ERROR: Exception whilst connecting channel. Details: '{}'.", channelName, ex.toString() );
         return WicaChannelValue.createChannelValueDisconnected();
      }


      // Now get value, ensuring channel gets closed afterwards.
      final String channelValue;
      try( Channel<String> channel = caChannel )
      {
         logger.info( "'{}' - Getting from channel with timeout {} {}...", channelName, timeout, timeUnit );
         channelValue = channel.getAsync().get( timeout, timeUnit );
         logger.info( "'{}' - OK: channel GET completed.", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "ERROR: Exception whilst getting from channel '{}'. Details: '{}'.", channelName, ex.getMessage() );
         return WicaChannelValue.createChannelValueDisconnected();
      }

      // If we get here return the String representation of the channel we obtained.
      return WicaChannelValue.createChannelValueConnected( channelValue );
   }

   /**
    * Sets the value of a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous (= confirmed) PUT operation.
    *
    * @param wicaChannelName the channel name.
    * @param channelValue the channel value.
    * @param timeout the timeout to be applied when attempting to put the channel
    *     value to the underlying data source. If a timeout occurs the returned
    *     value will be false.
    * @param timeUnit the time units to be used.
    * @return boolean set true when the put completed successfully.
    */
   public boolean put( WicaChannelName wicaChannelName, String channelValue, long timeout, TimeUnit timeUnit )
   {
      // Create a new channel
      final String channelName = wicaChannelName.getControlSystemName().asString();
      final Channel<String> caChannel;
      try
      {
         logger.info( "'{}' - Creating channel...", channelName );
         caChannel = context.createChannel( channelName, String.class );
         logger.info( "'{}' - OK: channel created.", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "'{}' - ERROR: Exception whilst creating channel. Details: '{}'", channelName, ex.getMessage() );
         return false;
      }

      // Wait for it to connect
      try
      {
         logger.info( "'{}' - Connecting channel with timeout {} {}...", channelName, timeout, timeUnit );
         caChannel.connectAsync().get( timeout, timeUnit );
         logger.info( "'{}' - OK: channel connected.", channelName );
      }
      catch ( InterruptedException | ExecutionException | TimeoutException ex )
      {
         logger.info( "'{}' - ERROR: Exception whilst connecting channel. Details: '{}'.", channelName, ex.toString() );
         return false;
      }

      // Now set the value, ensuring channel gets closed afterwards.
      try( Channel<String> channel = caChannel )
      {
         logger.info( "'{}' - Putting to channel with timeout {} {}...", channelName, timeout, timeUnit );
         channel.putAsync( channelValue ).get( timeout, timeUnit );
         logger.info( "'{}' - OK: Channel PUT completed.", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "ERROR: Exception whilst putting to channel '{}'. Details: '{}'.", channelName, ex.getMessage() );
         return false;
      }

      // If we get here return a token to indicate that the put was successful.
      return true;
   }


/*- Private methods ----------------------------------------------------------*/

   public void dispose()
   {
      logger.info("Cleaning up context...");
      context.close();
      logger.info("Done.");
   }

/*- Nested Classes -----------------------------------------------------------*/

}
