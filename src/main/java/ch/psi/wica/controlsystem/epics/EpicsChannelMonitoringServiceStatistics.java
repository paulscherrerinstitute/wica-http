/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class EpicsChannelMonitoringServiceStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);

   private final Map<EpicsChannelName, Channel<?>> channelMap;
   private final Map<EpicsChannelName, Monitor<?>> monitorMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitoringServiceStatistics( Map<EpicsChannelName, Channel<?>> channelMap,
                                                   Map<EpicsChannelName, Monitor<?>> monitorMap )
   {
      this.channelMap = channelMap;
      this.monitorMap = monitorMap;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<StatisticsEntry> getEntries()
   {
      return List.of(
         new StatisticsHeader( "EPICS MONITORING SERVICE:" ),
         new StatisticsItem("- Start Monitor Requests", getStartRequests() ),
         new StatisticsItem("- Stop Monitor Requests", getStopRequests() ),
         new StatisticsItem("- EPICS Channels: Total", getTotalChannelCount() ),
         new StatisticsItem("- EPICS Channels: Connected", getConnectedChannelCount() ),
         new StatisticsItem("- EPICS Monitors: Total", getTotalMonitorCount() )
      );
   }

   @Override
   public void clearEntries()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
   }

   public List<String> getChannelNames()
   {
      return channelMap
            .keySet()
            .stream()
            .map(ControlSystemName::asString)
            .collect(Collectors.toUnmodifiableList() );
   }

   public List<String> getUnconnectedChannelNames()
   {
      return channelMap
            .keySet()
            .stream()
            .filter( channel -> channelMap.get( channel ).getConnectionState() != ConnectionState.CONNECTED )
            .map( ControlSystemName::asString )
            .collect( Collectors.toUnmodifiableList() );
   }

   /*- Package-access methods ---------------------------------------------------*/

   void incrementStartRequests()
   {
      startRequests.incrementAndGet();
   }
   void incrementStopRequests()
   {
      stopRequests.incrementAndGet();
   }


/*- Private methods ----------------------------------------------------------*/

   private String getStartRequests()
   {
      return String.valueOf( startRequests.get());
   }
   private String getStopRequests()
   {
      return String.valueOf( stopRequests.get());
   }

   private String getTotalChannelCount()
   {
      return String.valueOf( channelMap.keySet().size() );
   }

   private String getConnectedChannelCount()
   {
      return String.valueOf( channelMap.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CONNECTED ).count() );
   }

   private String getTotalMonitorCount()
   {
      return String.valueOf( monitorMap.keySet().size() );
   }


/*- Nested Classes -----------------------------------------------------------*/

}