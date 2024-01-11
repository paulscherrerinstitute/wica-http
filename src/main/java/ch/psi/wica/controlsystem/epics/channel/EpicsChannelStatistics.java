/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the statistics associated with an EPICS channel.
 */
@ThreadSafe
public class EpicsChannelStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String contextSpecifier;
   private final AtomicInteger createChannelRequests = new AtomicInteger(0);
   private final AtomicInteger removeChannelRequests = new AtomicInteger(0);
   private final Map<EpicsChannelName, Channel<Object>> channels;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new instance based on the supplied channel data.
    *
    * @param contextSpecifier a string specifying the context (for example polling or monitoring) associated
    *    with these statistics.
    * @param channels the channel map.
    */
   public EpicsChannelStatistics( String contextSpecifier, Map<EpicsChannelName, Channel<Object>> channels )
   {
      this.contextSpecifier = Validate.notEmpty( contextSpecifier );
      this.channels = Validate.notNull( channels, "The 'channels' argument is null." );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( "EPICS CHANNEL MANAGER SERVICE - " + contextSpecifier.toUpperCase(),
                             List.of( new StatisticsItem("- Channels: Create Channel Requests", getCreateChannelRequests() ),
                                      new StatisticsItem("- Channels: Remove Channel Requests", getRemoveChannelRequests() ),
                                      new StatisticsItem("- Channels: Active Channel Requests", getActiveRequests() ),
                                      new StatisticsItem("- Channels: Connected", getConnectedChannelCount() ),
                                      new StatisticsItem("- Channels: Never Connected", getNeverConnectedChannelCount() ),
                                      new StatisticsItem("- Channels: Disconnected", getDisconnectedChannelCount() ),
                                      new StatisticsItem("- Channels: Closed", getClosedChannelCount() ) ) );
   }

   @Override
   public void reset()
   {
      createChannelRequests.set( 0 );
      removeChannelRequests.set( 0 );
   }

   /**
    * Returns the channel names.
    *
    * @return the channel names.
    */
   public List<String> getChannels()
   {
      return channels
              .keySet()
              .stream()
              .map(EpicsChannelName::toString).toList();
   }

   public List<String> getUnconnectedChannels()
   {
      return channels
              .keySet()
              .stream()
              .filter(channel -> channels.get(channel).getConnectionState() != ConnectionState.CONNECTED)
              .map(EpicsChannelName::toString).toList();
   }

   /**
    * Returns a string representation of the number of create channel requests.
    *
    * @return the result.
    */
   public String getCreateChannelRequests()
   {
      return String.valueOf( createChannelRequests.get());
   }

   /**
    * Returns a string representation of the number of remove channel requests.
    *
    * @return the result.
    */
   public String getRemoveChannelRequests()
   {
      return String.valueOf( removeChannelRequests.get());
   }

   /**
    * Returns a string representation of the number of channel requests that are currently active.
    *
    * @return the result.
    */
   public String getActiveRequests()
   {
      return String.valueOf( channels.size() );
   }

   /**
    * Returns a string representation of the number of connected channels.
    *
    * @return the result.
    */
   public String getConnectedChannelCount()
   {
      final long monitoredChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CONNECTED ).count();
      return String.valueOf( monitoredChannelCount );
   }

   /**
    * Returns a string representation of the number of channels that have never connected.
    *
    * @return the result.
    */
   public String getNeverConnectedChannelCount()
   {
      final long neverConnectedChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.NEVER_CONNECTED ).count();
      return String.valueOf( neverConnectedChannelCount );
   }

   /**
    * Returns a string representation of the number of channels that are currently disconnected.
    *
    * @return the result.
    */
   public String getDisconnectedChannelCount()
   {
      final long disconnectedChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.DISCONNECTED ).count();
     return String.valueOf( disconnectedChannelCount   );
   }

   /**
    * Returns a string representation of the number of channels that have been closed.
    *
    * @return the result.
    */
   public String getClosedChannelCount()
   {
      final long closedChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CLOSED ).count();
      return String.valueOf( closedChannelCount );
   }

/*- Package-access methods ---------------------------------------------------*/

   /**
    * Increments the count of create monitored channel requests.
    */
   void incrementCreateMonitoredChannelRequests()
   {
      createChannelRequests.incrementAndGet();
   }

   /**
    * Increments the count of removed monitored channel requests.
    */
   void incrementRemoveMonitoredChannelRequests()
   {
      removeChannelRequests.incrementAndGet();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}