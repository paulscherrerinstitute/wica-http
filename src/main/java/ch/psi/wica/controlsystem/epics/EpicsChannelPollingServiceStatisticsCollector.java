/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelName;
import net.jcip.annotations.ThreadSafe;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class EpicsChannelPollingServiceStatisticsCollector
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);
   private final AtomicInteger errors = new AtomicInteger(0);
   private final AtomicReference<Map<WicaChannelName, ScheduledFuture<?>>> executorMap = new AtomicReference<>();

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   // Getters
   public String getStartRequests()
   {
      return String.valueOf( startRequests.get());
   }
   public String getStopRequests()
   {
      return String.valueOf( stopRequests.get());
   }
   public String getErrors()
   {
      return String.valueOf(errors.get());
   }

   public String getTotalPollerCount()
   {
      return String.valueOf( executorMap.get().keySet().size() );
   }

   public String getCompletedPollerCount()
   {
      return String.valueOf( executorMap.get().values().stream().filter( Future::isDone).count() );
   }

   public String getCancelledPollerCount()
   {
      return String.valueOf( executorMap.get().values().stream().filter( Future::isCancelled).count() );
   }

   public List<String> getChannelNames()
   {
      return executorMap.get()
            .keySet()
            .stream()
            .map(WicaChannelName::asString)
            .collect(Collectors.toUnmodifiableList() );
   }

   // Setters
   void incrementStartRequests()
   {
      startRequests.incrementAndGet();
   }
   void incrementStopRequests()
   {
      stopRequests.incrementAndGet();
   }
   void incrementErrors()
   {
      errors.incrementAndGet();
   }

   void setExecutorMapTracker( Map<WicaChannelName, ScheduledFuture<?>> executorMap )
   {
      this.executorMap.set( executorMap );
   }

   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
      errors.set( 0 );
      executorMap.set( null );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}