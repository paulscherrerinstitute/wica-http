/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import ch.psi.wica.controlsystem.event.wica.WicaChannelEventPublisher;
import ch.psi.wica.controlsystem.event.channel.EpicsChannelConnectedEvent;
import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.channel.WicaChannelMetadata;

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
public class EpicsChannelMetadataPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMetadataPublisher.class );

   private final EpicsChannelMetadataGetter epicsChannelMetadataGetter;
   private final WicaChannelEventPublisher wicaChannelEventPublisher;
   private final List<EpicsChannelMetadataRequest> requestList;
   private final EpicsChannelMetadataStatistics statisticsCollector;

   private final Map<EpicsChannelName, WicaChannelMetadata> lastMetadataMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelMetadataPublisher( @Autowired EpicsChannelMetadataGetter epicsChannelMetadataGetter,
                                  @Autowired WicaChannelEventPublisher wicaChannelEventPublisher,
                                  @Autowired StatisticsCollectionService statisticsCollectionService )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMetadataPublisher instance...", this );

      this.epicsChannelMetadataGetter = Validate.notNull( epicsChannelMetadataGetter, "The 'epicsChannelMetadataGetter' argument is null." );
      this.wicaChannelEventPublisher = Validate.notNull( wicaChannelEventPublisher, "The 'wicaChannelEventPublisher' argument is null." );
      this.requestList = Collections.synchronizedList( new ArrayList<>() );
      this.statisticsCollector = new EpicsChannelMetadataStatistics( requestList );
      statisticsCollectionService.addCollectable( statisticsCollector );

      this.lastMetadataMap = new ConcurrentHashMap<>();

      logger.debug( "'{}' - service instance constructed ok.", this );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public EpicsChannelMetadataStatistics getStatistics()
   {
      return statisticsCollector;
   }

   public boolean isRequestObjectRecognised( EpicsChannelMetadataRequest requestObject )
   {
      return this.requestList.contains( requestObject );
   }

   public void addChannel( EpicsChannelMetadataRequest requestObject )
   {
      Validate.notNull( requestObject, "The 'requestObject' argument is null." );
      Validate.validState( ! requestList.contains( requestObject ) );

      logger.info( "'{}' - adding metadata publication channel.", requestObject.getPublicationChannel() );

      this.statisticsCollector.incrementStartRequests();
      requestList.add( requestObject );

      // When a new metadata request is made the publication channel always receives
      // the previously received metadata value (if any).
      if ( lastMetadataMap.containsKey( requestObject.getEpicsChannelName() ) )
      {
         wicaChannelEventPublisher.publishMetadataUpdated( requestObject.getPublicationChannel(), lastMetadataMap.get( requestObject.getEpicsChannelName() ) );
      }
   }

   public void removeChannel( EpicsChannelMetadataRequest requestObject )
   {
      Validate.notNull( requestObject, "The 'requestObject' argument is null." );
      Validate.validState( requestList.contains( requestObject ) );

      logger.info( "'{}' - removing metadata publication channel.", requestObject.getPublicationChannel() );

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
   // c) the IOC hosting the channel has just come online following a reboot.

//   @EventListener(condition = "#event.scope == 'monitored' || #event.scope == 'polled'" )
   @EventListener
   public void handleEpicsChannelConnectedEvent( EpicsChannelConnectedEvent event )
   {
      final var epicsChannelName = event.getEpicsChannelName();
      logger.info( "'{}' - channel connected.", epicsChannelName );
      this.statisticsCollector.incrementChannelConnectCount();

      logger.info( "'{}' - getting channel metadata...", epicsChannelName );
      final WicaChannelMetadata wicaChannelMetadata = epicsChannelMetadataGetter.get( event.caChannel() );
      lastMetadataMap.put( epicsChannelName, wicaChannelMetadata );
      logger.info( "'{}' - channel metadata obtained ok.", epicsChannelName );

      logger.info( "'{}' - publishing channel metadata to all metadata listeners...", epicsChannelName );
      publishMetadataUpdate( epicsChannelName, wicaChannelMetadata );

      logger.info( "'{}' - channel metadata published ok.", epicsChannelName );
   }

/*- Private methods ----------------------------------------------------------*/

   private void publishMetadataUpdate( EpicsChannelName epicsChannelName, WicaChannelMetadata wicaChannelMetadata )
   {
      // Look for any items in the metadata request list that match the name of the channel
      // whose metadata has just been updated. Publish a notification to the wica channel(s)
      // that are interested in it.
      requestList.stream()
                 .filter( x -> x.getEpicsChannelName().equals( epicsChannelName ) )
                 .forEach( req -> wicaChannelEventPublisher.publishMetadataUpdated( req.getPublicationChannel(), wicaChannelMetadata ) );
   }

/*- Nested Classes -----------------------------------------------------------*/

}

