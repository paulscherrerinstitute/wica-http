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
   private Monitor<Double> mon1;
   private Monitor<Double> mon2;
   private Channel<Double> chan;

   private Context caContext;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitor()
   {
      logger.info( "Constructing: ************* EpicsChannelMonitor" );
   }

/*- Class methods ------------------------------------------------------------*/



   /*- Public methods -----------------------------------------------------------*/

   public void connect( Consumer<Double> handler  ) {

      logger.info( "Constructing: ************* CONNECTING" );
      try
      {
         caContext = new Context();
         logger.info( "Constructing: ************* CONTEXT CREATED" );
         Channel<Double> adc = caContext.createChannel("simon:counter", Double.class);
         Listener cl = adc.addConnectionListener((channel, state) -> System.out.println(channel.getName() + " is connected? " + state));
         // remove listener, or use try-catch-resources
         //cl.close();

         Listener cl2 = adc.addAccessRightListener((channel, rights) -> System.out.println(channel.getName() + " is rights? " + rights));

         logger.info( "Constructing: ************* CHANNEL CREATED" );

         // wait until connected
         chan  = adc.connect();

         logger.info( "Constructing: ************* CHANNEL CONNECTED" );
         logger.info( "Initial value is: {}", chan.get() );

         //mon1 = adc.addValueMonitor(value -> System.out.println(value));
         mon2 = adc.addValueMonitor( v -> handler.accept( v ) );

         logger.info( "Constructing: ************* MONITOR CREATED" );
      }
      catch ( Exception ex ) {
         logger.info( "Constructing: ************* EXCEPTION" );
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

