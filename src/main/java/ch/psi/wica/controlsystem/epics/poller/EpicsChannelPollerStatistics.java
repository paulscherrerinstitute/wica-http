/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.poller;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides statistics related to the EPICS channel pollers.
 */
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


   /**
    * Creates a new EPICS channel poller statistics collector.
    *
    * @param requestMap initial list of poller requests.
    */
   public EpicsChannelPollerStatistics( Map<EpicsChannelPollerRequest, EpicsChannelPollerPublisher.Poller> requestMap )
   {
      this.requestMap = Validate.notNull( requestMap, "The 'requestMap' argument is null." );
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

   /**
    * Returns the names of the channels being polled.
    *
    * @return the list.
    */

   public List<String> getChannelNames()
   {
      return requestMap
              .keySet()
              .stream()
              .map(req -> req.getPublicationChannel().getName().asString()).toList();
   }

   /**
    * Returns the count of received EPICS channel poller start requests.
    *
    * @return the result.
    */
   public String getStartRequests()
   {
      return String.valueOf( startRequests.get());
   }

   /**
    * Returns the count of received EPICS channel poller stop requests.
    *
    * @return the result.
    */
   public String getStopRequests()
   {
      return String.valueOf( stopRequests.get());
   }

   /**
    * Returns the count of EPICS channel pollers that are currently active.
    *
    * @return the result.
    */
   public String getActiveRequests()
   {
      return String.valueOf( requestMap.size() );
   }

   /**
    * Returns the count of connected EPICS channels.
    *
    * @return the result.
    */
   public String getChannelConnectCount()
   {
      return String.valueOf( channelConnectCount.get() );
   }

   /**
    * Returns the count of disconnected EPICS channels.
    *
    * @return the result.
    */
   public String getChannelDisconnectCount()
   {
      return String.valueOf( channelDisconnectCount.get() );
   }

   /**
    * Returns the count of EPICS polling cycles.
    *
    * @return the result.
    */
   public String getPollCycleCount()
   {
      return String.valueOf( pollCycleCount );
   }

   /**
    * Returns the count of EPICS polling cycles that have been successful.
    *
    * @return the result.
    */
   public String getPollSuccessCount()
   {
      return String.valueOf( pollSuccessCount );
   }

   /**
    * Returns the count of EPICS polling cycles that have been failed.
    *
    * @return the result.
    */
   public String getPollFailureCount()
   {
      return String.valueOf( pollFailureCount );
   }

/*- Package-access methods ---------------------------------------------------*/

   /**
    * Increments the count of start requests.
    */
   void incrementStartRequests()
   {
      startRequests.incrementAndGet();
   }

   /**
    * Increments the count of stop requests.
    */
   void incrementStopRequests()
   {
      stopRequests.incrementAndGet();
   }

   /**
    * Increments the count of connected channels.
    */
   void incrementChannelConnectCount()
   {
      channelConnectCount.incrementAndGet();
   }

   /**
    * Increments the count of disconnected channels.
    */
   void incrementChannelDisconnectCount()
   {
      channelDisconnectCount.incrementAndGet();
   }

   /**
    * Increments the count of polling cycles.
    */
   void incrementPollCycleCount()
   {
      pollCycleCount.incrementAndGet();
   }

   /**
    * Updates the polling result statistics.
    *
    * @param success token indicating whether the last poll attempt was successful.
    */
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