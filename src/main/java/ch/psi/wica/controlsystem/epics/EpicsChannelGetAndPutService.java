/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class EpicsChannelGetAndPutService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelMonitoringService.class );
   private final Context context;
   private final EpicsChannelValueGetter epicsChannelValueGetter;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
   /**
    * Returns a new instance that will extract information from the EPICS channels
    * of interest using the supplied metadata and value getters.
    *
    * @param epicsCaLibraryMonitorNotifierImpl the CA library monitor notifier configuration.
    * @param epicsCaLibraryDebugLevel the CA library debug level.
    * @param epicsChannelValueGetter an object that can get and build the returned value.

    */
   public EpicsChannelGetAndPutService( @Value( "${wica.epics-ca-library-monitor-notifier-impl}") String  epicsCaLibraryMonitorNotifierImpl,
                                        @Value( "${wica.epics-ca-library-debug-level}") int epicsCaLibraryDebugLevel,
                                        @Autowired EpicsChannelValueGetter epicsChannelValueGetter )
   {
      this.epicsChannelValueGetter = Validate.notNull(epicsChannelValueGetter );

      logger.info( "Creating CA context for WicaChannelService..." );

      // Setup a context that uses the monitor notification policy and debug
      // message log level defined in the configuration file.
      System.setProperty( Context.Configuration.EPICS_CA_MAX_ARRAY_BYTES.toString(), "1000000");
      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", epicsCaLibraryMonitorNotifierImpl );
      System.setProperty( "CA_DEBUG", String.valueOf( epicsCaLibraryDebugLevel ) );

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
    * @param epicsChannelName the name of the channel.
    *
    * @param timeout the timeout to be applied when attempting to get the channel
    *     value from the underlying data source. If a timeout occurs the returned
    *     value will be WicaChannelValueDisconnected.
    * @param timeUnit the time units to be used.
    * @return the value.
    */
   public WicaChannelValue get( EpicsChannelName epicsChannelName, long timeout, TimeUnit timeUnit )
   {
      // Create a new channel
      final String channelName = epicsChannelName.asString();
      final Channel<Object> caChannel;
      try
      {
         logger.info( "'{}' - Creating channel...", channelName );
         caChannel = context.createChannel( channelName, Object.class );
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

      return epicsChannelValueGetter.get( caChannel );
   }

   /**
    * Sets the value of a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous (= confirmed) PUT operation.
    *
    * @param epicsChannelName the channel name.
    * @param channelValue the channel value.
    * @param timeout the timeout to be applied when attempting to put the channel
    *     value to the underlying data source. If a timeout occurs the returned
    *     value will be false.
    * @param timeUnit the time units to be used.
    * @return boolean set true when the put completed successfully.
    */
   public boolean put( EpicsChannelName epicsChannelName, String channelValue, long timeout, TimeUnit timeUnit )
   {
      // Create a new channel
      final String channelName = epicsChannelName.asString();
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

   // TODO - detect application close then clean up.
   public void dispose()
   {
      logger.info("Cleaning up context...");
      context.close();
      logger.info("Done.");
   }

/*- Nested Classes -----------------------------------------------------------*/

}
