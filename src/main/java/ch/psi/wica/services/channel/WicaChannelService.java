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

@Service
public class WicaChannelService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelMonitorService.class );

   private Context context;

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
    * Synchronously gets the value of a channel.
    *
    * This method incurs the network cost of establishing a channel
    * to the remote data source and performing a synchronous (=
    * confirmed) get.
    *
    * @param wicaChannelName the name of the channel.
    * @return the value.
    */
   public WicaChannelValue get( WicaChannelName wicaChannelName )
   {
      final String channelName = wicaChannelName.getControlSystemName().asString();
      final Channel<String> caChannel;
      try
      {
         logger.info( "Synchronously connecting to channel '{}'...", channelName );
         caChannel = context.createChannel( channelName, String.class );
         caChannel.connect();
         logger.info( "Synchronously connected to channel '{}'.", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "Exception whilst synchronously connecting to channel '{}'", channelName );
         return WicaChannelValue.createChannelValueDisconnected();
      }

      // Perform try with resources, ie cleanup channel after put operation.
      final String channelValue;
      try( Channel<String> channel = caChannel )
      {
         logger.info( "Synchronously Getting from channel '{}'...", channelName );
         channelValue = channel.get();
         logger.info( "Synchronous Get completed on channel '{}'", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "Exception whilst getting from channel '{}'", channelName );
         return WicaChannelValue.createChannelValueDisconnected();
      }

      return WicaChannelValue.createChannelValueConnected( channelValue );
   }

   /**
    * Synchronously sets the value of a channel.
    *
    * This method incurs the network cost of establishing a channel
    * to the remote data source and performing a synchronous (= confirmed) put.
    *
    * @param wicaChannelName the channel name.
    * @param channelValue the channel value.
    */
   public void put( WicaChannelName wicaChannelName, String channelValue )
   {
      final String channelName = wicaChannelName.getControlSystemName().asString();
      final Channel<String> caChannel;
      try
      {
         logger.info( "Synchronously connecting to channel '{}'...", channelName );
         caChannel = context.createChannel( channelName, String.class );
         caChannel.connect();
         logger.info( "Synchronously connected to channel '{}'.", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "Exception whilst synchronously connecting to channel '{}'", channelName );
         return;
      }

      // Perform try with resources, ie cleanup channel after put operation.
      try( Channel<String> channel = caChannel )
      {
         logger.info( "Synchronously Putting value '{}' to channel '{}'...", channelValue, channelName );
         channel.put( channelValue );
         logger.info( "Synchronous Put completed on channel '{}'", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "Exception whilst putting to channel '{}'", channelName );
      }
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
