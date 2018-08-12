/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.ChannelName;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.Monitor;
import org.epics.ca.data.Control;
import org.epics.ca.data.Graphic;
import org.epics.ca.data.Metadata;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

   private static final List<Channel> channels = new ArrayList<>();
   private static final List<Monitor> monitors = new ArrayList<>();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitorService()
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );

      // Setup a context that does no buffering. This is good enough
      // for most human display purposes.
      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", "BlockingQueueMultipleWorkerMonitorNotificationServiceImpl,16,1" );
      caContext = new Context();
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Starts monitoring the specified EPICS channel and sets up a plan
    * whereby future changes to the underlying channel will be informed to
    * the supplied state and value change handlers.
    *
    * @param channelName the name of the channel to be monitored.
    * @param valueChangeHandler the handler to be informed of value changes.
    *
    */
   public void startMonitoring( ChannelName channelName, Consumer<Boolean> stateChangeHandler, Consumer<String> valueChangeHandler )
   {
      logger.debug( "'{}' - starting to monitor... ", channelName );

      try
      {
         logger.debug( "'{}' - creating channel of type '{}'...", channelName, "generic" );

         final Channel<Object> channel = caContext.createChannel( channelName.toString(), Object.class ) ;
         channels.add( channel );

         logger.debug( "'{}' - channel created ok.", channelName );

         logger.debug( "'{}' - adding connection listener... ", channelName );
         channel.addConnectionListener(( chan, isConnected ) -> {
            stateChangeHandler.accept( isConnected );
         } );
         logger.debug( "'{}' - connection listener created ok.", channelName );


         logger.debug( "'{}' - connecting asynchronously to... ", channelName );
         final CompletableFuture<Channel<Object>> completableFuture = channel.connectAsync();
         logger.debug( "'{}' - asynchronous connect completed ok.", channelName );

         completableFuture.thenRunAsync( () -> {
            logger.debug( "'{}' - channel connected ok.", channelName );
            logger.debug( "'{}' - adding value change monitor...", channelName );

            final Object firstValue = channel.get();
            if ( firstValue instanceof Double )
            {
               final Monitor<Graphic<Object,Integer>> monitor = channel.addMonitor( Graphic.class, v -> {
                  final int precision = v.getPrecision();
                  final String formatExpr = "%." + precision + "f" + " %s";
                  valueChangeHandler.accept( String.format( formatExpr, (Double) v.getValue(), v.getUnits() ) );
               } );
               monitors.add( monitor );
            }
            else if ( firstValue instanceof Integer )
            {
               final Monitor<Graphic<Object,Integer>> monitor = channel.addMonitor( Graphic.class, v -> {
                  final String formatExpr = "%d" + "%s";
                  valueChangeHandler.accept( String.format( formatExpr, (Integer) v.getValue(), v.getUnits() ) );
               } );
               monitors.add( monitor );
            }
            else if ( firstValue instanceof String )
            {
               final Monitor<Object> monitor = channel.addValueMonitor( v -> {
                  valueChangeHandler.accept( (String) v ) ;
               } );
               monitors.add( monitor );
            }
            else {
               logger.warn( "'{}' - this channel was of a type that is not currently supported ", channelName );
            }
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

