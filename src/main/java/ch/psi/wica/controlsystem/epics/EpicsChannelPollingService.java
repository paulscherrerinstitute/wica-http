/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service for polling EPICS channels at a configurable rate and for
 * publishing the results using the Spring event service.
 */
@Service
@ThreadSafe
public class EpicsChannelPollingService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final EpicsChannelMonitoringService epicsChannelMonitoringService;
   private final EpicsChannelGetAndPutService epicsChannelGetAndPutService;

   private final ScheduledExecutorService executor;
   private final Map<EpicsChannelName, ScheduledFuture> channelExecutorMap = new ConcurrentHashMap<>();
   private final int timeoutInMillis;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelPollingService( @Value( "${wica.channel-get-timeout-interval-in-ms}") int timeoutInMillis,
                                      @Autowired EpicsChannelMonitoringService epicsChannelMonitoringService,
                                      @Autowired EpicsChannelGetAndPutService epicsChannelGetAndPutService )
   {
      this.timeoutInMillis = timeoutInMillis;
      this.epicsChannelMonitoringService = Validate.notNull( epicsChannelMonitoringService );
      this.epicsChannelGetAndPutService = Validate.notNull( epicsChannelGetAndPutService );

      this.executor = Executors.newSingleThreadScheduledExecutor();
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   void startPolling( EpicsChannelName epicsChannelName, int pollingIntervalInMillis, Consumer<WicaChannelValue> valueUpdateHandler  )
   {
      final ScheduledFuture scheduledFuture = executor.scheduleAtFixedRate(() -> {

         final var wicaChannelValue = pollDirect( epicsChannelName );
         valueUpdateHandler.accept( wicaChannelValue );

      }, pollingIntervalInMillis, pollingIntervalInMillis, TimeUnit.MILLISECONDS );

      this.channelExecutorMap.put( epicsChannelName, scheduledFuture );

   }

   void stopPolling( EpicsChannelName epicsChannelName )
   {
      channelExecutorMap.get( epicsChannelName ).cancel( false );
   }


/*- Private methods ----------------------------------------------------------*/

   private WicaChannelValue pollDirect( EpicsChannelName epicsChannelName )
   {
      return epicsChannelGetAndPutService.get( epicsChannelName, timeoutInMillis, TimeUnit.MILLISECONDS );
   }

   private WicaChannelValue pollLatestCachedValue( EpicsChannelName epicsChannelName )
   {
      return epicsChannelGetAndPutService.get( epicsChannelName, timeoutInMillis, TimeUnit.MILLISECONDS );
   }


/*- Nested Classes -----------------------------------------------------------*/

}

