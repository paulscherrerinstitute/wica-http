/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the statistics associated with EPICS channel's metadata.
 */
@ThreadSafe
public class EpicsChannelMetadataStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicInteger startRequests = new AtomicInteger(0);
   private final AtomicInteger stopRequests = new AtomicInteger(0);
   private final AtomicInteger channelConnectCount = new AtomicInteger(0);
   private final List<EpicsChannelMetadataRequest> requestList;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new instance based on the supplied channel metadata list.
    *
    * @param requestList the list.
    */
   public EpicsChannelMetadataStatistics( List<EpicsChannelMetadataRequest> requestList )
   {
      this.requestList = Validate.notNull( requestList, "The 'requestList' argument is null." );
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

   /**
    * Returns the channel names.
    *
    * @return the channel names.
    */
   @SuppressWarnings("unused")
   public List<String> getChannelNames()
   {
      return requestList
              .stream()
              .map(req -> req.getPublicationChannel().toString()).toList();
   }

   /**
    * Returns a string representation of the number of start requests.
    *
    * @return the result.
    */
   public String getStartRequests()
   {
      return String.valueOf( startRequests.get());
   }

   /**
    * Returns a string representation of the number of stop requests.
    *
    * @return the result.
    */
   public String getStopRequests()
   {
      return String.valueOf( stopRequests.get());
   }

   /**
    * Returns a string representation of the number of active channels.
    *
    * @return the result.
    */
   public String getActiveChannels()
   {
      return String.valueOf( requestList.size() );
   }

   /**
    * Returns a string representation of the number of connected channels.
    *
    * @return the result.
    */
   public String getChannelConnectCount()
   {
      return String.valueOf( channelConnectCount.get() );
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
    */void incrementStopRequests()
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


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}