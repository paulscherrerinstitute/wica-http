/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.epics;

/*- Imported packages --------------------------------------------------------*/

import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Controller
public class EpicsChannelMonitor<T>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitor.class );
   private final Context caContext;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitor()
   {
      logger.info( "************* Constructing: EpicsChannelMonitor" );
      caContext = new Context();
      logger.info( "************* CONTEXT CREATED" );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public void connect( String channelName, Class<T> channelType, Consumer<T> handler ) {

      logger.info( "************* CONNECTING" );
      try
      {
         logger.info( "************* CREATING CHANNEL named '{}' of type '{}'", channelName, channelType );
         final Channel<T> channel = caContext.createChannel( channelName, channelType );

         final Listener cl1 = channel.addConnectionListener((chan, state) -> System.out.println(chan.getName() + " is connected? " + state));
         // remove listener, or use try-catch-resources
         //cl.close();

         final Listener cl2 = channel.addAccessRightListener((chan, rights) -> System.out.println(chan.getName() + " is rights? " + rights));
         logger.info( "************* CHANNEL CREATED" );

         // wait until connected
         logger.info( "************* CONNECTING..." );
         //final Channel<T> chan2  = channel.connect();

         final CompletableFuture<Channel<T>> completableFuture = channel.connectAsync();

         completableFuture.thenRunAsync( () -> {
                                                  logger.info( "************* CHANNEL CONNECTED" );
                                                  channel.addValueMonitor( v -> handler.accept( v ) );
                                                  logger.info( "************* MONITOR CREATED" );
                                               } );
      }
      catch ( Exception ex )
      {
         logger.info( "************* EXCEPTION" );
      }
   }

   public void destroy()
   {
      caContext.close();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

