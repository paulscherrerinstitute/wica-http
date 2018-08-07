/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.ChannelName;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service to facilitate the monitoring of multiple EPICS
 * channels, and to subsequently inform interested third-parties of changes
 * of interest.
 *
 * @ImplNote
 * The current implementation creates a single Context per class instance.
 * The context is disposed of when the service instance is closed.
 */
@Service
public class EpicsChannelMonitorService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitorService.class );
   private final Context caContext;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitorService()
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );
      caContext = new Context();
      logger.debug( "'{}' - service instance constrcuted ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Starts monitoring the specified EPICS channel and sets up a plan
    * whereby future changes to the underlying channel will be informed to
    * the supplied state and value change handlers.
    *
    * @param channelName the name of the channel to be monitored.
    * @param channelType the type of the channel that is to be created.
    * @param valueChangeHandler the handler to be informed of value changes.
    *
    * @param <T> the type to be used for the underlying connection to
    *            the remote PV.
    */
   public <T> void startMonitoring( ChannelName channelName, Class<T> channelType, Consumer<Boolean> stateChangeHandler, Consumer<T> valueChangeHandler )
   {
      logger.debug( "'{}' - starting to monitor... ", channelName );

      try
      {
         logger.debug( "'{}' - creating channel of type '{}'...", channelName, channelType );

         final Channel<T> channel = caContext.createChannel( channelName.toString(), channelType ) ;
         logger.debug( "'{}' - channel created ok.", channelName );

         logger.debug( "'{}' - adding connection listener... ", channelName );
         channel.addConnectionListener(( chan, isConnected ) -> {
            stateChangeHandler.accept(isConnected);
         } );
         logger.debug( "'{}' - connection listener created ok.", channelName );


         logger.debug( "'{}' - connecting asynchronously to... ", channelName );
         final CompletableFuture<Channel<T>> completableFuture = channel.connectAsync();
         logger.debug( "'{}' - asynchronous connect completed ok.", channelName );

         completableFuture.thenRunAsync(() -> {
            logger.debug( "'{}' - channel connected ok.", channelName );
            logger.debug( "'{}' - adding value change monitor...", channelName );
            channel.addValueMonitor(valueChangeHandler);
            logger.debug( "'{}' - value change monitor added ok.", channelName );
         });
      }
      catch ( Exception ex )
      {
         logger.debug( "'{}' - exception on channel, details were as follows: ", channelName, ex.toString() );
      }
   }

   @Override
   public void close()
   {
      logger.debug( "'{}' - disposing resources...", this );
      caContext.close();
      logger.debug( "'{}' - resources disposed ok.", this );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

