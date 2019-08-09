/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelPollMonitorEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStartPollingEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopPollingEvent;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannelName;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service for polling EPICS channels at a configurable rate and for
 * publishing the results using the Spring event service.
 */
@Service
@ThreadSafe
public class WicaChannelPollMonitorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaChannelPollMonitorService.class );

   private final ApplicationEventPublisher applicationEventPublisher;
   private final Map<WicaChannelName, ScheduledFuture> channelExecutorMap = new ConcurrentHashMap<>();
   private final ScheduledExecutorService executor;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelPollMonitorService( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
      this.executor = Executors.newSingleThreadScheduledExecutor();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartPollingEvent( WicaChannelStartPollingEvent wicaChannelStartPollingEvent )
   {
      Validate.notNull( wicaChannelStartPollingEvent );

      final WicaChannelName wicaChannelName = wicaChannelStartPollingEvent.get();
      final int pollingIntervalInMillis = wicaChannelStartPollingEvent.getPollingIntervalInMillis();
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannelStartPollingEvent.getWicaDataAcquisitionMode();

      if ( ! wicaDataAcquisitionMode.doesMonitorPolling() )
      {
         logger.trace( "Ignoring start poll request for wica channel named: '{}'", wicaChannelName );
         return;
      }
      startPolling( wicaChannelName, pollingIntervalInMillis );
   }

   @EventListener
   public void handleWicaChannelStopPollingEvent( WicaChannelStopPollingEvent wicaChannelStopPollingEvent )
   {
      Validate.notNull( wicaChannelStopPollingEvent );

      final WicaChannelName wicaChannelName = wicaChannelStopPollingEvent.get();
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannelStopPollingEvent.getWicaDataAcquisitionMode();

      if ( ! wicaDataAcquisitionMode.doesMonitorPolling() )
      {
         logger.trace( "Ignoring start poll request for wica channel named: '{}'", wicaChannelName );
         return;
      }
      stopPolling( wicaChannelName );
   }


/*- Private methods ----------------------------------------------------------*/

   private void startPolling( WicaChannelName wicaChannelName, int pollingIntervalInMillis )
   {
      final ScheduledFuture scheduledFuture = executor.scheduleAtFixedRate(() -> applicationEventPublisher.publishEvent(new WicaChannelPollMonitorEvent( wicaChannelName ) ), pollingIntervalInMillis, pollingIntervalInMillis, TimeUnit.MILLISECONDS );

      this.channelExecutorMap.put( wicaChannelName, scheduledFuture );
   }

   private void stopPolling( WicaChannelName wicaChannelName )
   {
      channelExecutorMap.get(  wicaChannelName).cancel( false );
   }

/*- Nested Classes -----------------------------------------------------------*/

}

