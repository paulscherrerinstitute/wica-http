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
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class EpicsChannelStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String instanceSpecifier;
   private final AtomicInteger createChannelRequests = new AtomicInteger(0);
   private final AtomicInteger removeChannelRequests = new AtomicInteger(0);
   private final Map<EpicsChannelName, Channel<Object>> channels;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelStatistics( String instanceSpecifier, Map<EpicsChannelName, Channel<Object>> channels )
   {
      this.instanceSpecifier = Validate.notEmpty( instanceSpecifier );
      this.channels = Validate.notNull( channels );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( "EPICS CHANNEL MANAGER SERVICE - " + instanceSpecifier.toUpperCase(),
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

   public List<String> getChannels()
   {
      return channels
            .keySet()
            .stream()
            .map( EpicsChannelName::toString )
            .collect(Collectors.toUnmodifiableList() );
   }

   public List<String> getUnconnectedChannels()
   {
      return channels
            .keySet()
            .stream()
            .filter( channel -> channels.get( channel ).getConnectionState() != ConnectionState.CONNECTED )
            .map( EpicsChannelName::toString )
            .collect( Collectors.toUnmodifiableList() );
   }
   
   public String getCreateChannelRequests()
   {
      return String.valueOf( createChannelRequests.get());
   }
   public String getRemoveChannelRequests()
   {
      return String.valueOf( removeChannelRequests.get());
   }
   public String getActiveRequests()
   {
      return String.valueOf( channels.size() );
   }

   public String getConnectedChannelCount()
   {
      final long monitoredChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CONNECTED ).count();
      return String.valueOf( monitoredChannelCount );
   }

   public String getNeverConnectedChannelCount()
   {
      final long neverConnectedChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.NEVER_CONNECTED ).count();
      return String.valueOf( neverConnectedChannelCount );
   }

   public String getDisconnectedChannelCount()
   {
      final long disconnectedChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.DISCONNECTED ).count();
     return String.valueOf( disconnectedChannelCount   );
   }
   public String getClosedChannelCount()
   {
      final long closedChannelCount = channels.values().stream().filter( channel -> channel.getConnectionState() == ConnectionState.CLOSED ).count();
      return String.valueOf( closedChannelCount );
   }
   

/*- Package-access methods ---------------------------------------------------*/

   void incrementCreateMonitoredChannelRequests()
   {
      createChannelRequests.incrementAndGet();
   }
   void incrementRemoveMonitoredChannelRequests()
   {
      removeChannelRequests.incrementAndGet();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}