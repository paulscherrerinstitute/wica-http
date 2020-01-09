/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStartMonitoringEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopMonitoringEvent;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for starting and stopping the control system monitoring
 * of a WicaStream.
 */
@Service
@ThreadSafe
public class WicaStreamMonitoredValueRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamMonitoredValueRequesterService.class );
   private final ApplicationEventPublisher applicationEventPublisher;

   private final Map<WicaDataBufferStorageKey,Integer> monitorInterestMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param applicationEventPublisher reference to the application publisher
    *    which will be used to publish the channels that are to be monitored
    *    or which are no longer of interest.
    */
   WicaStreamMonitoredValueRequesterService( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
      this.monitorInterestMap = Collections.synchronizedMap(new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Package-level methods ----------------------------------------------------*/

   /**
    * Starts monitoring the specified stream.
    *
    * @param wicaStream the stream to monitor.
    */
   void startMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      wicaStream.getWicaChannels()
            .stream()
            .filter( c -> c.getProperties().getDataAcquisitionMode().doesMonitoring() )
            .forEach( this::startMonitoringChannel);
   }

   /**
    * Stops monitoring the specified stream.
    *
    * @param wicaStream the stream that is no longer of interest.
    */
   void stopMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      wicaStream.getWicaChannels()
            .stream()
            .filter( c -> c.getProperties().getDataAcquisitionMode().doesMonitoring() )
            .forEach( this::stopMonitoringChannel);
   }

   /**
    * Returns the level of interest in a WicaChannel.
    *
    * @implNote. this method is provided mainly for test purposes.
    *
    * @param wicaChannel the name of the channel to lookup.
    * @return the current interest count (or zero if the channel was
    *     not recognised).
    */
   int getInterestCountForChannel( WicaChannel wicaChannel )
   {
      final var storageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey( wicaChannel );
      return monitorInterestMap.getOrDefault(storageKey, 0 );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Starts monitoring the Wica channel with the specified name and/or
    * increments the interest count for this channel.
    *
    * Immediately thereafter the channel's connection state, metadata and
    * value will become observable via the other methods in this class.
    *
    * Until the wica server receives its first value from the channel's
    * underlying data source the metadata will be set to type UNKNOWN and
    * the value set to show that the channel is disconnected.
    *
    * @param wicaChannel the name of the channel to monitor.
    */
   private void startMonitoringChannel( WicaChannel wicaChannel )
   {
      logger.info( "Request to start monitoring wica channel: '{}'", wicaChannel);

      Validate.notNull( wicaChannel );
      final var storageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();

      // If the channel is already being monitored increment the interest count.
      if ( monitorInterestMap.containsKey( storageKey ) )
      {
         final int currentInterestCount = monitorInterestMap.get(storageKey );
         final int newInterestCount = currentInterestCount + 1;
         logger.info( "Increasing interest level in monitored control system channel named: '{}' to {}", controlSystemName, newInterestCount );
         monitorInterestMap.put(storageKey, newInterestCount );
      }
      // If the channel is NOT already being monitored start monitoring it.
      else
      {
         logger.info( "Starting monitoring on control system channel named: '{}'", controlSystemName.asString() );

         // Set the initial state for the value and metadata stashes.
         applicationEventPublisher.publishEvent( new WicaChannelMetadataUpdateEvent( wicaChannel, WicaChannelMetadata.createUnknownInstance()  ));
         applicationEventPublisher.publishEvent( new WicaChannelMonitoredValueUpdateEvent(wicaChannel, WicaChannelValue.createChannelValueDisconnected() ));

         // Now start monitoring
         applicationEventPublisher.publishEvent( new WicaChannelStartMonitoringEvent( wicaChannel ) );
         monitorInterestMap.put(storageKey, 1 );
      }
   }

   /**
    * Stops monitoring the Wica channel with the specified name (which
    * should previously have been monitored) and/or reduces the interest
    * count for this channel.
    *
    * When/if the interest in the channel is reduced to zero then any
    * attempts to subsequently observe it's state will result in an
    * exception.
    *
    * @param wicaChannel the name of the channel which is no longer of interest.
    *
    * @throws IllegalStateException if the channel was never previously monitored.
    */
   private void stopMonitoringChannel( WicaChannel wicaChannel )
   {
      logger.info( "Request to stop monitoring wica channel: '{}'", wicaChannel );

      Validate.notNull( wicaChannel );
      final var storageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();

      Validate.validState( monitorInterestMap.containsKey( storageKey ) );
      Validate.validState(monitorInterestMap.get( storageKey ) > 0 );

      final int currentInterestCount = monitorInterestMap.get( storageKey );
      if ( currentInterestCount > 1 )
      {
         final int newInterestCount = currentInterestCount - 1;
         logger.info( "Reducing interest level in monitored control system channel named: '{}' to {}" , controlSystemName.asString(), newInterestCount );
         monitorInterestMap.put( storageKey, newInterestCount );
      }
      else
      {
         logger.info( "Stopping monitoring on control system channel named: '{}'", controlSystemName.asString() );
         monitorInterestMap.remove( storageKey );
         applicationEventPublisher.publishEvent( new WicaChannelStopMonitoringEvent( wicaChannel ) );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
