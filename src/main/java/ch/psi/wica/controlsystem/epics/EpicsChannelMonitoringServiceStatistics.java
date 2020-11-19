/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

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
   private final AtomicInteger monitorUpdateCount = new AtomicInteger(0);

   private final Map<EpicsChannelMonitoringRequest, Channel<?>> channelMap;
   private final Map<EpicsChannelMonitoringRequest, Monitor<?>> monitorMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitoringServiceStatistics( Map<EpicsChannelMonitoringRequest, Channel<?>> channelMap,
                                                   Map<EpicsChannelMonitoringRequest, Monitor<?>> monitorMap )
   {
      this.channelMap = channelMap;
      this.monitorMap = monitorMap;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( "EPICS MONITORING SERVICE",
                             List.of( new StatisticsItem("- Start Monitor Requests", getStartRequests() ),
                                      new StatisticsItem("- Stop Monitor Requests", getStopRequests() ),
                                      new StatisticsItem("- EPICS Channels: Total", getTotalChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Connected", getConnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Not Connected", getNotConnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Never Connected", getNeverConnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Disconnected", getDisconnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Closed", getClosedChannelCount() ),
                                      new StatisticsItem("- EPICS Monitors: Total", getTotalMonitorCount() ),
                                      new StatisticsItem("- EPICS Monitors: Updates", getMonitorUpdateCount() ) ) );
   }

   @Override
   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
      monitorUpdateCount.set( 0 );
   }

   public List<String> getChannelNames()
   {
      return channelMap
            .keySet()
            .stream()
            .map( EpicsChannelMonitoringRequest::toString )
            .collect(Collectors.toUnmodifiableList() );
   }

   public List<String> getUnconnectedChannelNames()
   {
      return channelMap
            .keySet()
            .stream()
            .filter( channel -> channelMap.get( channel ).getConnectionState() != ConnectionState.CONNECTED )
            .map( EpicsChannelMonitoringRequest::toString )
            .collect( Collectors.toUnmodifiableList() );
   }

   public String getStartRequests()
   {
      return String.valueOf( startRequests.get());
   }
   public String getStopRequests()
   {
      return String.valueOf( stopRequests.get());
   }

   public String getTotalChannelCount()
   {
      return String.valueOf( channelMap.keySet().size() );
   }

   public String getConnectedChannelCount()
   {
      return String.valueOf( channelMap.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CONNECTED ).count() );
   }

   public String getNotConnectedChannelCount()
   {
      return String.valueOf( channelMap.values().stream().filter( channel -> channel.getConnectionState() != ConnectionState.CONNECTED ).count() );
   }

   public String getNeverConnectedChannelCount()
   {
      return String.valueOf( channelMap.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.NEVER_CONNECTED ).count() );
   }

   public String getDisconnectedChannelCount()
   {
      return String.valueOf( channelMap.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.DISCONNECTED ).count() );
   }

   public String getClosedChannelCount()
   {
      return String.valueOf( channelMap.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CLOSED ).count() );
   }

   public String getTotalMonitorCount()
   {
      return String.valueOf( monitorMap.keySet().size() );
   }

   public String getMonitorUpdateCount()
   {
      return String.valueOf( monitorUpdateCount.get() );
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

   void incrementMonitorUpdateCount()
   {
      monitorUpdateCount.incrementAndGet();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}