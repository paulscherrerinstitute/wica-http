/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service which schedules the polling of user-specified EPICS channels of
 * interest, subsequently publishing the results of each poll operation to
 * interested consumers within the application.
 */
@Service
@ThreadSafe
public class EpicsChannelPollingService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelMonitoringService.class );
   private final EpicsChannelPollingServiceStatistics statisticsCollector;

   private final EpicsChannelGetAndPutService epicsChannelGetAndPutService;
   private final ScheduledExecutorService executor;
   private final Map<WicaChannelName, ScheduledFuture<?>> channelExecutorMap;
   private final int timeoutInMillis;
   private final EpicsEventPublisher epicsEventPublisher;

   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance.
    *
    * @param timeoutInMillis the timeout for each poll operation.
    * @param epicsChannelGetAndPutService the service which will be used for polling.
    * @param epicsEventPublisher an object which publishes events of interest to consumers within the application.
    */
   public EpicsChannelPollingService( @Value( "${wica.channel-get-timeout-interval-in-ms}") int timeoutInMillis,
                                      @Autowired EpicsChannelGetAndPutService epicsChannelGetAndPutService,
                                      @Autowired EpicsEventPublisher epicsEventPublisher,
                                      @Autowired StatisticsCollectionService statisticsCollectionService
                                      )
   {
      logger.debug( "'{}' - constructing new EpicsChannelPollingService instance...", this );

      this.timeoutInMillis = timeoutInMillis;
      this.epicsChannelGetAndPutService = Validate.notNull( epicsChannelGetAndPutService );
      this.epicsEventPublisher = Validate.notNull( epicsEventPublisher );
      this.channelExecutorMap = new ConcurrentHashMap<>();

      this.statisticsCollector = new EpicsChannelPollingServiceStatistics(channelExecutorMap );
      statisticsCollectionService.addCollectable( statisticsCollector );

      this.executor = Executors.newSingleThreadScheduledExecutor();
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Starts polling the EPICS control system channel associated with the
    * specified Wica Channel. Publishes the results of each poll operation
    * using the configured EPICS Event Publisher.
    *
    * The EPICS channel may or may not be online when this method is invoked.
    * If the channel is offline the poll operation will result in a timeout
    * and the returned value will indicate that the channel is disconnected.
    *
    * It is the responsibility of the caller to ensure that polling 
    * @param wicaChannel the channel to be polled.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    * @throws IllegalStateException if this polling service was previously closed.
    * @throws IllegalStateException if the EPICS channel to be polled is already
    * being polled at the same rate.
    */
   void startPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel);

      Validate.validState( ! closed, "The polling service was previously closed and can no longer be used." );
      statisticsCollector.incrementStartRequests();

      final WicaChannelName wicaChannelName= wicaChannel.getName();

      final int pollingIntervalInMillis = wicaChannel.getProperties().getPollingIntervalInMillis();
      logger.trace("'{}' - starting to poll with periodicity of {} milliseconds.", wicaChannelName, pollingIntervalInMillis );

      final var scheduledFuture = executor.scheduleAtFixedRate(() -> {

         final var wicaChannelValue = doPoll( wicaChannelName );
         epicsEventPublisher.publishPolledValueUpdated( wicaChannel, wicaChannelValue );

      }, pollingIntervalInMillis, pollingIntervalInMillis, TimeUnit.MILLISECONDS );

      this.channelExecutorMap.put( wicaChannelName, scheduledFuture );

      logger.trace("'{}' - channel polling has been scheduled ok.", wicaChannelName);
   }

   /**
    * Stops polling the specified channel.
    *
    * It is the responsibility of the caller to ensure that polling should
    * already have been started on the targeted EPICS channel. Should
    * this precondition be violated an IllegalStateException will be thrown.
    *
    * @param wicaChannel the channel which no longer needs to be polled.
    * @throws IllegalStateException if this polling service was previously closed.
    * @throws IllegalStateException if the channel whose polling is to be stopped
    *     was not already being polled.
    */
   void stopPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      Validate.validState( ! closed, "The polling service was previously closed and can no longer be used." );
      statisticsCollector.incrementStopRequests();

      final WicaChannelName wicaChannelName = wicaChannel.getName();
      Validate.validState( channelExecutorMap.containsKey( wicaChannelName ), "The channel name: '" + wicaChannelName.asString() + "' was not recognised."  );

      // Cancel the scheduled task.
      logger.trace("'{}' - stopping polling.", wicaChannelName) ;
      channelExecutorMap.get( wicaChannelName ).cancel( false );
      channelExecutorMap.remove( wicaChannelName );
   }

   /**
    * Disposes of all resources associated with this class instance.
    */
   @Override
   public void close()
   {
      // Set a flag to prevent further usage
      closed = true;

      // Dispose of any references that are no longer required
      logger.debug( "'{}' - disposing resources...", this );

      // Send a cancel request to all the scheduled futures.
      channelExecutorMap.values().forEach( c ->  c.cancel( false ) );
      channelExecutorMap.clear();

      // Close the service that provides the data source.
      epicsChannelGetAndPutService.close();

      logger.debug( "'{}' - resources disposed ok.", this );
   }

   public EpicsChannelPollingServiceStatistics getStatistics()
   {
      return statisticsCollector;
   }


/*- Private methods ----------------------------------------------------------*/

   private WicaChannelValue doPoll( WicaChannelName wicaChannelName )
   {
      final EpicsChannelName epicsChannelName = EpicsChannelName.of( wicaChannelName.getControlSystemName() );
      return epicsChannelGetAndPutService.get( epicsChannelName, timeoutInMillis, TimeUnit.MILLISECONDS );
   }

/*- Nested Classes -----------------------------------------------------------*/

}

