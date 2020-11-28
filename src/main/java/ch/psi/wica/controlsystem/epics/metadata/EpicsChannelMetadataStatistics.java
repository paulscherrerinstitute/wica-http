/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.metadata;

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
public class EpicsChannelMetadataStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);
   private final AtomicInteger channelConnectCount = new AtomicInteger(0);
   private final List<EpicsChannelMetadataRequest> requestMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMetadataStatistics( List<EpicsChannelMetadataRequest> requestMap )
   {
      this.requestMap = Validate.notNull( requestMap );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( "EPICS CHANNEL METADATA SERVICE",
                             List.of( new StatisticsItem( "- Metadata: Start Requests", getStartRequests() ),
                                      new StatisticsItem( "- Metadata: Stop Requests", getStopRequests() ),
                                      new StatisticsItem( "- Metadata: Channels Active", getActiveChannels() ),
                                      new StatisticsItem( "- Metadata: Channel Connects", getChannelConnectCount() ) ) );
   }

   @Override
   public void reset()
   {
      startRequests.set( 0 );
      stopRequests.set( 0 );
      channelConnectCount.set( 0 );
   }

   public List<String> getChannelNames()
   {
      return requestMap
            .stream()
            .map( req -> req.getPublicationChannel().toString() )
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
   public String getActiveChannels()
   {
      return String.valueOf( requestMap.size() );
   }
   public String getChannelConnectCount()
   {
      return String.valueOf( channelConnectCount.get() );
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


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}