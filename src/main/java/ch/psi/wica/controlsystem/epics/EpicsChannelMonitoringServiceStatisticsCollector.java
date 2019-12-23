/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import net.jcip.annotations.ThreadSafe;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Monitor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class EpicsChannelMonitoringServiceStatisticsCollector
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);
   private final AtomicInteger errors = new AtomicInteger(0);
   private final AtomicReference<Map<EpicsChannelName, Channel<?>>> channelMap = new AtomicReference<>();
   private final AtomicReference<Map<EpicsChannelName, Monitor<?>>> monitorMap = new AtomicReference<>();

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

   public String getTotalChannelCount()
   {
      return String.valueOf( channelMap.get().keySet().size() );
   }

   public String getConnectedChannelCount()
   {
      return String.valueOf( channelMap.get().values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CONNECTED ).count() );
   }

   public String getTotalMonitorCount()
   {
      return String.valueOf( monitorMap.get().keySet().size() );
   }

   public List<String> getChannelNames()
   {
      return channelMap.get()
            .keySet()
            .stream()
            .map( ControlSystemName::asString)
            .collect(Collectors.toUnmodifiableList() );
   }

   public List<String> getUnconnectedChannelNames()
   {
      return channelMap.get()
            .keySet()
            .stream()
            .filter( channel -> channelMap.get().get( channel ).getConnectionState() != ConnectionState.CONNECTED )
            .map( ControlSystemName::asString )
            .collect( Collectors.toUnmodifiableList() );
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

   void setChannelMapTracker( Map<EpicsChannelName, Channel<?>> channelMap )
   {
      this.channelMap.set( channelMap );
   }

   void setMonitorMapTracker( Map<EpicsChannelName, Monitor<?>> monitorMap )
   {
      this.monitorMap .set( monitorMap );
   }

   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
      errors.set( 0 );
      channelMap.set( null );
      monitorMap.set( null );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}