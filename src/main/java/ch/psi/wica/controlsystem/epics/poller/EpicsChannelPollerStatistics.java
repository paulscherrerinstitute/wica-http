/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.poller;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class EpicsChannelPollerStatistics implements StatisticsCollectable
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);
   private final AtomicInteger channelConnectCount = new AtomicInteger( 0);
   private final AtomicInteger channelDisconnectCount = new AtomicInteger( 0);
   private final AtomicInteger pollCycleCount = new AtomicInteger(0);
   private final AtomicInteger pollSuccessCount = new AtomicInteger(0);
   private final AtomicInteger pollFailureCount = new AtomicInteger(0);
   private final  Map<EpicsChannelPollerRequest, EpicsChannelPollerPublisher.Poller> requestMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelPollerStatistics( Map<EpicsChannelPollerRequest, EpicsChannelPollerPublisher.Poller> requestMap )
   {
      this.requestMap = Validate.notNull( requestMap );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( "EPICS CHANNEL POLLER SERVICE",
                             List.of( new StatisticsItem( "- Pollers: Start Requests", getStartRequests() ),
                                      new StatisticsItem( "- Pollers: Stop Requests", getStopRequests() ),
                                      new StatisticsItem( "- Pollers: Active Requests", getActiveRequests() ),
                                      new StatisticsItem( "- Pollers: Channel Connects", getChannelConnectCount() ),
                                      new StatisticsItem( "- Pollers: Channel Disconnects", getChannelDisconnectCount() ),
                                      new StatisticsItem( "- Pollers: Total Cycles", getPollCycleCount() ),
                                      new StatisticsItem( "- Pollers: Total Successes", getPollSuccessCount() ),
                                      new StatisticsItem( "- Pollers: Total Failures", getPollFailureCount() ) ) );
   }
   @Override
   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
      channelConnectCount.set( 0 );
      channelDisconnectCount.set( 0 );
      pollCycleCount.set( 0 );
      pollSuccessCount.set( 0 );
      pollFailureCount.set( 0 );
   }

   public List<String> getChannelNames()
   {
      return requestMap
            .keySet()
            .stream()
            .map( req -> req.getPublicationChannel().getName().asString() )
            .collect(Collectors.toUnmodifiableList() );
   }

   public String getStartRequests()
   {
      return String.valueOf( startRequests.get());
   }

   public String getStopRequests()
   {
      return String.valueOf( stopRequests.get());
   }

   public String getActiveRequests()
   {
      return String.valueOf( requestMap.size() );
   }

   public String getChannelConnectCount()
   {
      return String.valueOf( channelConnectCount.get() );
   }
   public String getChannelDisconnectCount()
   {
      return String.valueOf( channelDisconnectCount.get() );
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
   void incrementChannelConnectCount()
   {
      channelConnectCount.incrementAndGet();
   }
   void incrementChannelDisconnectCount()
   {
      channelDisconnectCount.incrementAndGet();
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