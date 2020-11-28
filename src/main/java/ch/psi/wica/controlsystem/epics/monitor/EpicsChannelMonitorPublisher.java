/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.monitor;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import ch.psi.wica.controlsystem.event.channel.EpicsChannelDisconnectedEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelEventPublisher;
import ch.psi.wica.controlsystem.event.channel.EpicsChannelConnectedEvent;
import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
@ThreadSafe
public class EpicsChannelMonitorPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitorPublisher.class );

   private final EpicsChannelMonitorSubscriber epicsChannelMonitorSubscriber;
   private final WicaChannelEventPublisher wicaChannelEventPublisher;
   private final List<EpicsChannelMonitorRequest> requestList;
   private final EpicsChannelMonitorStatistics statisticsCollector;
   private final Map<EpicsChannelName,WicaChannelValue> lastValueMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelMonitorPublisher( @Autowired EpicsChannelMonitorSubscriber epicsChannelMonitorSubscriber,
                                 @Autowired WicaChannelEventPublisher wicaChannelEventPublisher,
                                 @Autowired StatisticsCollectionService statisticsCollectionService )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorPublisher instance...", this );

      this.epicsChannelMonitorSubscriber = Validate.notNull( epicsChannelMonitorSubscriber );
      this.wicaChannelEventPublisher = Validate.notNull( wicaChannelEventPublisher );
      this.requestList = Collections.synchronizedList( new ArrayList<>() );
      this.statisticsCollector = new EpicsChannelMonitorStatistics( requestList );
      statisticsCollectionService.addCollectable( statisticsCollector );

      this.lastValueMap = new ConcurrentHashMap<>();

      logger.debug( "'{}' - service instance constructed ok.", this );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public EpicsChannelMonitorStatistics getStatistics()
   {
      return statisticsCollector;
   }

   public boolean isRequestObjectRecognised( EpicsChannelMonitorRequest requestObject )
   {
      Validate.notNull( requestObject );
      return this.requestList.contains( requestObject );
   }

   public void addChannel( EpicsChannelMonitorRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! requestList.contains( requestObject ) );

      logger.info( "'{}' - adding monitor publication channel.", requestObject.getPublicationChannel() );

      this.statisticsCollector.incrementStartRequests();
      requestList.add( requestObject );

      // When a new monitor request is made the publication channel always receives
      // the previously received monitor value (if any)
      if ( lastValueMap.containsKey( requestObject.getEpicsChannelName() ) )
      {
         wicaChannelEventPublisher.publishMonitoredValueUpdated( requestObject.getPublicationChannel(), lastValueMap.get( requestObject.getEpicsChannelName() ) );
      }
   }

   public void removeChannel( EpicsChannelMonitorRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( requestList.contains( requestObject ) );

      logger.info( "'{}' - removing monitor publication channel.", requestObject.getPublicationChannel() );

      this.statisticsCollector.incrementStopRequests();
      requestList.remove( requestObject );
   }

   public void removeAllChannels()
   {
      final var toRemoveList = new ArrayList<>( requestList );
      toRemoveList.forEach( this::removeChannel );
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

   // TODO: the current behaviour resubscribes the monitor on every connection event, but it's not
   // TODO: currently clear whether this is strictly necessary or whether it would be done automatically
   // TODO: by the CA library when (re)establishing the Virtual Circuit. Furthermore the EPICS CA protocol
   // TODO: specification says the following:
   // TODO: "Clients SHOULD NOT create two monitors on the same channel with the same Event Mask.
   // TODO: Further testing should be performed to verify the validity of the current implementation.
   // TODO: Consider attaching to EpicsChannelFirstConnectedEvent

   @EventListener(condition = "#event.scope == 'monitored'" )
   public void handleChannelConnectedEvent( EpicsChannelConnectedEvent event )
   {
      final var epicsChannelName = event.getEpicsChannelName();
      logger.info( "'{}' - channel connected.", epicsChannelName );
      this.statisticsCollector.incrementChannelConnectCount();

      logger.info( "'{}' - subscribing to channel...", epicsChannelName );
      epicsChannelMonitorSubscriber.subscribe( event.getCaChannel(), ( wicaChannelValue) -> {
         lastValueMap.put( epicsChannelName, wicaChannelValue );
         publishMonitorValueUpdate( epicsChannelName, wicaChannelValue );
      } );
   }

   @EventListener( condition = "#event.scope == 'monitored'" )
   public void handleChannelDisconnectedEvent( EpicsChannelDisconnectedEvent event )
   {
      final var epicsChannelName = EpicsChannelName.of( event.getCaChannel().getName() );
      logger.info( "'{}' - channel disconnected.", epicsChannelName );

      this.statisticsCollector.incrementChannelDisconnectCount();

      logger.info( "'{}' - publishing channel disconnect event to all monitor listeners...", epicsChannelName );

      final WicaChannelValue disconnectedValue = WicaChannelValue.createChannelValueDisconnected();
      requestList.stream()
         .filter( req -> req.getEpicsChannelName().equals( epicsChannelName ) )
         .forEach( req -> wicaChannelEventPublisher.publishMonitoredValueUpdated( req.getPublicationChannel(), disconnectedValue ) );
   }

/*- Private methods ----------------------------------------------------------*/

   private void publishMonitorValueUpdate( EpicsChannelName epicsChannelName, WicaChannelValue wicaChannelValue )
   {
      this.statisticsCollector.incrementMonitorUpdateCount();

      // Look for any items in the monitor request list that match the name of the channel
      // whose value has just been updated. Publish a notification to the wica channel(s)
      // that are interested in it.
      requestList.stream()
         .filter( x -> x.getEpicsChannelName().equals( epicsChannelName ) )
         .forEach( req -> wicaChannelEventPublisher.publishMonitoredValueUpdated( req.getPublicationChannel(), wicaChannelValue ) );
}

/*- Nested Classes -----------------------------------------------------------*/

}

