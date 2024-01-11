/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.WicaChannelPolledMonitorValueUpdateEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelStartPollingEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelStopPollingEvent;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
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

   private final ApplicationEventPublisher applicationEventPublisher;
   private final Map<WicaChannel, ScheduledFuture<?>> channelExecutorMap = new ConcurrentHashMap<>();
   private final ScheduledExecutorService executor;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelPollMonitorService( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher, "The 'applicationEventPublisher' argument is null." );
      this.executor = Executors.newSingleThreadScheduledExecutor();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartPollingEvent( WicaChannelStartPollingEvent wicaChannelStartPollingEvent )
   {
      Validate.notNull( wicaChannelStartPollingEvent, "The 'wicaChannelStartPollingEvent' argument is null." );

      // Note:
      // This service only handles the indirect, local, polling of a monitor which should
      // already have been established on the remote IOC as part of the stream setup.
      // All other requests will be  silently ignored.
      // This poller type does not need to obtain metadata for the channel since this
      // is the responsibility of the WicaStreamMonitoredValueRequesterService.
      final WicaChannel wicaChannel = wicaChannelStartPollingEvent.get();
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannel.getProperties().getDataAcquisitionMode();
      if ( wicaDataAcquisitionMode.doesMonitorPolling() )
      {
         final int pollingIntervalInMillis = wicaChannel.getProperties().getPollingIntervalInMillis();
         startPolling(wicaChannel, pollingIntervalInMillis);
      }
   }

   @EventListener
   public void handleWicaChannelStopPollingEvent( WicaChannelStopPollingEvent wicaChannelStopPollingEvent )
   {
      Validate.notNull( wicaChannelStopPollingEvent, "The 'wicaChannelStopPollingEvent' argument is null." );

      // Note: this service only handles the indirect, local, polling of a monitor which should
      // already have been established on the remote IOC. All other requests will be  silently ignored.
      final WicaChannel wicaChannel = wicaChannelStopPollingEvent.get();
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannel.getProperties().getDataAcquisitionMode();
      if ( wicaDataAcquisitionMode.doesMonitorPolling() )
      {
         stopPolling(wicaChannel);
      }
   }

/*- Private methods ----------------------------------------------------------*/

   private void startPolling( WicaChannel wicaChannel, int pollingIntervalInMillis )
   {
      final ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(() -> applicationEventPublisher.publishEvent( new WicaChannelPolledMonitorValueUpdateEvent(wicaChannel ) ), pollingIntervalInMillis, pollingIntervalInMillis, TimeUnit.MILLISECONDS );

      this.channelExecutorMap.put( wicaChannel, scheduledFuture );
   }

   private void stopPolling( WicaChannel wicaChannel )
   {
      channelExecutorMap.get(  wicaChannel ).cancel( false );
   }

/*- Nested Classes -----------------------------------------------------------*/

}

