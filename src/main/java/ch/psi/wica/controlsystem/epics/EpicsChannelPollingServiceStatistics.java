/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class EpicsChannelPollingServiceStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);
   private final AtomicInteger pollCycleCount = new AtomicInteger(0);
   private final AtomicInteger pollSuccessCount = new AtomicInteger(0);
   private final AtomicInteger pollFailureCount = new AtomicInteger(0);

   private final Map<EpicsChannelPollingRequest, Channel<?>> channelMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelPollingServiceStatistics( Map<EpicsChannelPollingRequest, Channel<?>> channelMap )
   {
      this.channelMap = channelMap;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( "EPICS POLLING SERVICE",
                             List.of( new StatisticsItem("- Start Polling Requests", getStartRequests() ),
                                      new StatisticsItem("- Stop Polling Requests", getStopRequests() ),

                                      new StatisticsItem("- EPICS Channels: Total", getTotalChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Connected", getConnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Not Connected", getNotConnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Never Connected", getNeverConnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Disconnected", getDisconnectedChannelCount() ),
                                      new StatisticsItem("- EPICS Channels: Closed", getClosedChannelCount() ),

                                      new StatisticsItem("- EPICS Pollers: Polling Cycle: Total Count", getPollCycleCount() ),
                                      new StatisticsItem("- EPICS Pollers: Polling Cycle: Success Count", getPollSuccessCount() ),
                                      new StatisticsItem("- EPICS Pollers: Polling Cycle: Failure Count", getPollFailureCount() ) )
                             );
   }

   @Override
   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
      pollCycleCount.set( 0 );
      pollSuccessCount.set( 0 );
      pollFailureCount.set( 0 );
   }

   public List<String> getChannelNames()
   {
      return channelMap
            .keySet()
            .stream()
            .map( EpicsChannelPollingRequest::toString )
            .collect(Collectors.toUnmodifiableList() );
   }

   public List<String> getUnconnectedChannelNames()
   {
      return channelMap
            .keySet()
            .stream()
            .filter( channel -> channelMap.get( channel ).getConnectionState() != ConnectionState.CONNECTED )
            .map( EpicsChannelPollingRequest::toString )
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

   public String getPollCycleCount()
   {
      return String.valueOf( pollCycleCount );
   }

   public String getPollSuccessCount()
   {
      return String.valueOf( pollSuccessCount );
   }

   public String getPollFailureCount()
   {
      return String.valueOf( pollFailureCount );
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
   void incrementPollCycleCount()
   {
      pollCycleCount.incrementAndGet();
   }

   void updatePollingResult( boolean success )
   {
      if ( success )
      {
         pollSuccessCount.incrementAndGet();
      }
      else
      {
         pollFailureCount.incrementAndGet();
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}