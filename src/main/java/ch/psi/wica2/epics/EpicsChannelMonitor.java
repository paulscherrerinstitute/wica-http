/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica2.epics;

/*- Imported packages --------------------------------------------------------*/

import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.Listener;
import org.epics.ca.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.function.Consumer;

import static javafx.scene.input.KeyCode.T;


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
      logger.info( "Constructing: ************* EpicsChannelMonitor" );
      caContext = new Context();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public void connect( String channelName, Class<T> channelType, Consumer<T> handler  ) {

      logger.info( "Constructing: ************* CONNECTING" );
      try
      {
         logger.info( "Constructing: ************* CONTEXT CREATED" );
         final Channel<T> channel = caContext.createChannel( channelName, channelType );

         final Listener cl1 = channel.addConnectionListener((chan, state) -> System.out.println(chan.getName() + " is connected? " + state));
         // remove listener, or use try-catch-resources
         //cl.close();

         final Listener cl2 = channel.addAccessRightListener((chan, rights) -> System.out.println(chan.getName() + " is rights? " + rights));
         logger.info( "Constructing: ************* CHANNEL CREATED" );

         // wait until connected
         final Channel<T> chan  = channel.connect();

         logger.info( "Constructing: ************* CHANNEL CONNECTED" );
         logger.info( "Initial value is: {}", chan.get() );

         final Monitor mon = channel.addValueMonitor( v -> handler.accept( v ) );

         logger.info( "Constructing: ************* MONITOR CREATED" );
      }
      catch ( Exception ex )
      {
         logger.info( "Constructing: ************* EXCEPTION" );
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

