/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class StatisticsCollector
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger( 0 );
   private final AtomicInteger stopRequests = new AtomicInteger( 0 );

   private final AtomicInteger totalInstances = new AtomicInteger( 0 );
   private final AtomicInteger activeInstances = new AtomicInteger( 0 );


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String getStartRequests()
   {
      return String.valueOf( startRequests.get() );
   }
   public String getStopRequests()
   {
      return String.valueOf( stopRequests.get() );
   }

   public String getTotalChannels()
   {
      return String.valueOf( stopRequests.get() );
   }
   public String getConnectedChannels()
   {
      return String.valueOf( stopRequests.get() );
   }

   public String getTotalMonitors()
   {
      return String.valueOf( stopRequests.get() );
   }
   public String getConnectedMonitors()
   {
      return String.valueOf( stopRequests.get() );
   }

   public void incrementStartRequests()
   {
      startRequests.incrementAndGet();
   }
   public void incrementStopRequests()
   {
      stopRequests.incrementAndGet();
   }
   public void incrementChannelCount()
   {
      totalInstances.incrementAndGet();
   }
   public void incrementChannelsActive()
   {
      activeInstances.incrementAndGet();
   }

   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );

   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}