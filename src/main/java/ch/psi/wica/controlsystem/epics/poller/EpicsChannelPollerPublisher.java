/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.poller;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import ch.psi.wica.controlsystem.epics.channel.EpicsChannelValueGetter;
import ch.psi.wica.controlsystem.event.channel.EpicsChannelConnectedEvent;
import ch.psi.wica.controlsystem.event.channel.EpicsChannelDisconnectedEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelEventPublisher;
import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
@ThreadSafe
public class EpicsChannelPollerPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelPollerPublisher.class );

   private final EpicsChannelValueGetter epicsChannelValueGetter;
   private final WicaChannelEventPublisher wicaChannelEventPublisher;
   private final EpicsChannelPollerStatistics statisticsCollector;
   private final ScheduledExecutorService executor;
   private final Map<EpicsChannelPollerRequest,Poller> requestMap;
   private final Map<EpicsChannelName,Channel<Object>> channelMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelPollerPublisher( @Autowired EpicsChannelValueGetter epicsChannelValueGetter,
                                       @Autowired WicaChannelEventPublisher wicaChannelEventPublisher,
                                       @Autowired StatisticsCollectionService statisticsCollectionService)
   {
      logger.debug( "'{}' - constructing new EpicsChannelPollerPublisher instance...", this );

      this.epicsChannelValueGetter = Validate.notNull( epicsChannelValueGetter );
      this.wicaChannelEventPublisher = Validate.notNull( wicaChannelEventPublisher );
      this.executor = Executors.newScheduledThreadPool( 4 );
      this.requestMap = new ConcurrentHashMap<>();
      this.channelMap = new ConcurrentHashMap<>();
      this.statisticsCollector = new EpicsChannelPollerStatistics( requestMap );
      statisticsCollectionService.addCollectable( statisticsCollector );

      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public EpicsChannelPollerStatistics getStatistics()
   {
      return statisticsCollector;
   }

   public boolean isRequestObjectRecognised( EpicsChannelPollerRequest requestObject )
   {
      Validate.notNull( requestObject );
      return this.requestMap.containsKey( requestObject );
   }

   public void addChannel( EpicsChannelPollerRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( !requestMap.containsKey( requestObject ) );

      logger.info( "'{}' - adding poller publication channel.", requestObject.getPublicationChannel() );

      this.statisticsCollector.incrementStartRequests();
      final Poller poller = new Poller( executor, requestObject, epicsChannelValueGetter, wicaChannelEventPublisher, statisticsCollector );
      requestMap.put( requestObject, poller );

      final Channel<Object> caChannel = channelMap.get( requestObject.getEpicsChannelName() );
      if ( caChannel != null )
      {
         poller.start( caChannel );
      }
   }

   public void removeChannel( EpicsChannelPollerRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( requestMap.containsKey( requestObject ) );

      logger.info( "'{}' - removing poller publication channel.", requestObject.getPublicationChannel() );

      this.statisticsCollector.incrementStopRequests();
      requestMap.remove( requestObject ).cancel();
   }

   public void removeAllChannels()
   {
      final var toRemoveList = new ArrayList<>( requestMap.keySet() );
      toRemoveList.forEach( this::removeChannel );
   }

   public void close()
   {
      removeAllChannels();
      this.executor.shutdownNow();
   }

   // The processing below will be scheduled every time a channel comes online.
   // This could be for any of the following reasons:
   //
   // a) this EPICS channel service has been requested to create a new EPICS channel
   //    and the channel has just connected for the very first time.
   // b) the IOC hosting the channel has just come online following a loss of network
   //    connectivity. In this case monitors that were already established on the
   //    IOC will be intact.
   // c) the IOC hosting the channel has just come online following a reboot. In
   //    this case monitors that were already established on the IOC will be lost.
   @EventListener(condition = "#event.scope == 'polled'" )
   public void handleChannelConnectedEvent( EpicsChannelConnectedEvent event )
   {
      final var epicsChannelName = event.getEpicsChannelName();
      logger.info( "'{}' - channel connected.", epicsChannelName );
      this.statisticsCollector.incrementChannelConnectCount();
      final Channel<Object> caChannel = event.getCaChannel();
      channelMap.put( epicsChannelName, caChannel );
      this.enableAllPollersForEpicsChannel( epicsChannelName, caChannel );
   }

   @EventListener(condition = "#event.scope == 'polled'" )
   public void handleChannelDisconnectedEvent( EpicsChannelDisconnectedEvent event )
   {
      final var epicsChannelName = event.getEpicsChannelName();
      logger.info( "'{}' - channel disconnected.", epicsChannelName );
      channelMap.remove( epicsChannelName );
      this.statisticsCollector.incrementChannelDisconnectCount();
      this.disableAllPollersForEpicsChannel( epicsChannelName );
   }


/*- Private methods ----------------------------------------------------------*/

   private void enableAllPollersForEpicsChannel( EpicsChannelName epicsChannelName, Channel<Object> caChannel )
   {
      logger.info( "'{}' - enabling poller...", epicsChannelName );
      requestMap.entrySet()
                .stream()
                .filter( e -> e.getKey().getEpicsChannelName().equals( epicsChannelName ) )
                .forEach( e -> {
                   final Poller poller = e.getValue();
                   if ( poller.isStarted() )
                   {
                      logger.info( "'{}' - resuming poller...", epicsChannelName );
                      poller.resume();
                   }
                   else
                   {
                      logger.info( "'{}' - starting poller...", epicsChannelName );
                      poller.start( caChannel );
                   }
                });
   }

   private void disableAllPollersForEpicsChannel( EpicsChannelName epicsChannelName )
   {
      logger.info( "'{}' - pausing poller...", epicsChannelName );
      requestMap.entrySet()
                .stream()
                .filter( e -> e.getKey().getEpicsChannelName().equals( epicsChannelName ) )
                .forEach( e -> e.getValue().pause() );
   }



/*- Nested Classes -----------------------------------------------------------*/

   @ThreadSafe
   public static class Poller
   {
      private final Logger logger = LoggerFactory.getLogger( Poller.class );
      private final ScheduledExecutorService executor;
      private final EpicsChannelValueGetter epicsChannelValueGetter;
      private final WicaChannelEventPublisher wicaChannelEventPublisher;
      private final WicaChannel publicationChannel;
      private final EpicsChannelPollerStatistics statisticsCollector;
      private final AtomicBoolean pause;
      private final EpicsChannelName epicsChannelName;
      private final int pollingIntervalInMillis;

      private ScheduledFuture<?> scheduledFuture;

      public Poller( ScheduledExecutorService executor,
                     EpicsChannelPollerRequest requestObject,
                     EpicsChannelValueGetter epicsChannelValueGetter,
                     WicaChannelEventPublisher wicaChannelEventPublisher,
                     EpicsChannelPollerStatistics statisticCollector )
      {
         this.executor = executor;
         this.epicsChannelValueGetter = epicsChannelValueGetter;
         this.wicaChannelEventPublisher = wicaChannelEventPublisher;
         this.publicationChannel = requestObject.getPublicationChannel();
         this.epicsChannelName = requestObject.getEpicsChannelName();
         this.pollingIntervalInMillis = requestObject.getPollingInterval();
         this.statisticsCollector = statisticCollector;
         this.pause = new AtomicBoolean( false );
      }

      boolean isStarted()
      {
         return scheduledFuture != null;
      }

      void start( Channel<Object> caChannel )
      {
         logger.trace( "'{}' - starting to poll...", epicsChannelName );
         this.scheduledFuture = executor.scheduleAtFixedRate( () -> {

            if ( pause.get() )
            {
               logger.trace( "'{}' - polling is paused so the we are done.", epicsChannelName );
               return;
            }

            try
            {
               logger.trace( "'{}' - polling now...", epicsChannelName );
               statisticsCollector.incrementPollCycleCount();
               final int getTimeoutInMillis = pollingIntervalInMillis / 2;
               final WicaChannelValue wicaChannelValue = this.epicsChannelValueGetter.get( caChannel, getTimeoutInMillis, TimeUnit.MILLISECONDS );
               logger.trace( "'{}' - posting SUCCESSFUL poll result...", epicsChannelName );
               statisticsCollector.updatePollingResult( true );
               this.publishChannelValue(  wicaChannelValue );
               logger.trace( "'{}' - done.", epicsChannelName );
               return;
            }
            catch ( InterruptedException ex )
            {
               logger.warn( "'{}' - InterruptedException", epicsChannelName );
            }
            catch ( TimeoutException ex )
            {
               logger.error( "'{}' - TimeoutException", epicsChannelName );
            }
            catch ( ExecutionException ex )
            {
               logger.error( "'{}' - ExecutionException", epicsChannelName );
            }

            logger.trace( "'{}' - posting FAILED poll result...", epicsChannelName );
            statisticsCollector.updatePollingResult( false );
            this.publishChannelDisconnect();
            logger.trace( "'{}' - done.", epicsChannelName );

         }, 0, pollingIntervalInMillis, TimeUnit.MILLISECONDS );
      }

      void cancel()
      {
         if ( isStarted() )
         {
            this.scheduledFuture.cancel( false );
         }
      }

      void pause()
      {
         pause.set( true );
         this.publishChannelDisconnect();
      }

      void resume()
      {
         pause.set( false );
      }

      private void publishChannelValue( WicaChannelValue wicaChannelValue )
      {
         wicaChannelEventPublisher.publishPolledValueUpdated( publicationChannel, wicaChannelValue );
      }

      private void publishChannelDisconnect()
      {
         wicaChannelEventPublisher.publishPolledValueUpdated( publicationChannel, WicaChannelValue.createChannelValueDisconnected() );

      }
   }

}

