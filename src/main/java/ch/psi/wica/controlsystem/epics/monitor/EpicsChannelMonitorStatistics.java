/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.monitor;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class EpicsChannelMonitorStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);
   private final AtomicInteger channelConnectCount = new AtomicInteger( 0);
   private final AtomicInteger channelDisconnectCount = new AtomicInteger( 0);
   private final AtomicInteger monitorUpdateCount = new AtomicInteger(0);
   private final List<EpicsChannelMonitorRequest> requestList;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new EPICS channel monitoring statistics collector.
    *
    * @param requestList initial list of monitor requests.
    */
   public EpicsChannelMonitorStatistics( List<EpicsChannelMonitorRequest> requestList )
   {
      this.requestList = Validate.notNull( requestList );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the statistics object associated with EPICS channel monitoring.
    *
    * @return the result.
    */
   @Override
   public Statistics get()
   {
      return new Statistics( "EPICS CHANNEL MONITOR SERVICE",
                             List.of( new StatisticsItem( "- Monitors: Start Requests", getStartRequests() ),
                                      new StatisticsItem( "- Monitors: Stop Requests", getStopRequests() ),
                                      new StatisticsItem( "- Monitors: Active Requests", getActiveRequests() ),
                                      new StatisticsItem( "- Monitors: Channel Connects", getChannelConnectCount() ),
                                      new StatisticsItem( "- Monitors: Channel Disconnects", getChannelDisconnectCount() ),
                                      new StatisticsItem( "- Monitors: Total Updates", getMonitorUpdateCount() ) ) );
   }

   /*
    * Resets the EPICS channel monitoring statistics.
    */
   @Override
   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
      channelConnectCount.set( 0 );
      channelDisconnectCount.set( 0 );
      monitorUpdateCount.set( 0 );
   }

   public List<String> getChannelNames()
   {
      return requestList
            .stream()
            .map( req -> req.getPublicationChannel().getName().asString() )
            .collect( Collectors.toUnmodifiableList() );
   }

   /**
    * Returns the count of received EPICS channel monitor start requests.
    *
    * @return the result.
    */
   public String getStartRequests()
   {
      return String.valueOf( startRequests.get());
   }

   /**
    * Returns the count of received EPICS channel monitor stop requests.
    *
    * @return the result.
    */
   public String getStopRequests()
   {
      return String.valueOf( stopRequests.get());
   }

   /**
    * Returns the count of EPICS channel monitors that are currently active.
    *
    * @return the result.
    */
   public String getActiveRequests()
   {
      return String.valueOf( requestList.size() );
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
    * Returns the count of received EPICS monitor updates.
    * @return the result.
    */
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
   void incrementChannelConnectCount()
   {
      channelConnectCount.incrementAndGet();
   }
   void incrementChannelDisconnectCount()
   {
      channelDisconnectCount.incrementAndGet();
   }
   void incrementMonitorUpdateCount()
   {
      monitorUpdateCount.incrementAndGet();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}