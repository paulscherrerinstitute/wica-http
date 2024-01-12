/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.*;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.value.WicaChannelValueBuilder;
import ch.psi.wica.model.stream.WicaStream;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for starting and stopping the control system monitoring
 * of the channels in a WicaStream.
 */
@Configuration
@EnableScheduling
@Service
@ThreadSafe
public class WicaStreamMonitoredValueRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final int RESOURCE_RELEASE_SCAN_INTERVAL = 1000;

   private final Logger logger = LoggerFactory.getLogger( WicaStreamMonitoredValueRequesterService.class );

   private final int wicaChannelResourceReleaseIntervalInSecs;
   private final boolean wicaChannelPublishMonitorRestarts;
   private final boolean wicaChannelPublishChannelValueInitialState;
   private final ApplicationEventPublisher applicationEventPublisher;
   private final Map<WicaDataBufferStorageKey,Integer> monitoredChannelInterestMap;
   private final Map<WicaDataBufferStorageKey,LocalDateTime> monitoredChannelEventMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param wicaChannelResourceReleaseIntervalInSecs period after which the
    *    resources associated with a Wica Channel will be released if they are no
    *    longer in use.
    *
    * @param wicaChannelPublishMonitorRestarts determines whether a channel
    *    disconnect value will be published if the monitoring associated
    *    with the channel is restarted.
    *
    * @param wicaChannelPublishChannelValueInitialState determines whether a
    *    channel's initial value will be published as DISCONNECTED when a
    *    channel is first created or whether nothing will be published until
    *    the first monitored value is acquired.
    *
    * @param applicationEventPublisher reference to the application publisher
    *    which will be used to publish the channels that are to be monitored
    *    or which are no longer of interest.
    */
   WicaStreamMonitoredValueRequesterService( @Value( "${wica.channel-resource-release-interval-in-secs}" ) int wicaChannelResourceReleaseIntervalInSecs,
                                             @Value( "${wica.channel-publish-monitor-restarts}" ) boolean wicaChannelPublishMonitorRestarts,
                                             @Value( "${wica.channel-publish-channel-value-initial-state}" ) boolean wicaChannelPublishChannelValueInitialState,
                                             @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.wicaChannelResourceReleaseIntervalInSecs = wicaChannelResourceReleaseIntervalInSecs;
      this.wicaChannelPublishMonitorRestarts = wicaChannelPublishMonitorRestarts;
      this.wicaChannelPublishChannelValueInitialState = wicaChannelPublishChannelValueInitialState;
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher, "The 'applicationEventPublisher' argument was null." );
      this.monitoredChannelInterestMap = Collections.synchronizedMap( new HashMap<>() );
      this.monitoredChannelEventMap = Collections.synchronizedMap( new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * This method runs periodically to scan the discard list for entries corresponding to
    * monitored channels that are no longer of interest
    */
   @Scheduled( fixedRate=RESOURCE_RELEASE_SCAN_INTERVAL )
   public void discardMonitorsThatHaveReachedEndOfLife()
   {
      final var timeNow = LocalDateTime.now();
      monitoredChannelInterestMap.keySet()
            .stream()
            .filter( key -> monitoredChannelInterestMap.get( key ) == 0 )
            .filter( key -> timeNow.isAfter( monitoredChannelEventMap.get( key ).plusSeconds( wicaChannelResourceReleaseIntervalInSecs ) ) )
            .toList( )
            .forEach( this::discardMonitoredChannel );
   }

/*- Package-level methods ----------------------------------------------------*/

   /**
    * Restarts the control system monitoring of the channels in the specified stream.
    *
    * @param wicaStream the stream on which monitoring is to be restarted.
    */
   void restartMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );

      wicaStream.getWicaChannels()
            .stream()
            .filter( c -> c.getProperties().getDataAcquisitionMode().doesMonitoring() )
            .forEach( this::restartMonitoringChannel);
   }

   /**
    * Starts monitoring the channels in the specified stream.
    *
    * @param wicaStream the stream to monitor.
    */
   void startMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );
      wicaStream.getWicaChannels()
            .stream()
            .filter( c -> c.getProperties().getDataAcquisitionMode().doesMonitoring() )
            .forEach( this::startMonitoringChannel);
   }

   /**
    * Stops monitoring the channels in the specified stream.
    *
    * @param wicaStream the stream that is no longer of interest.
    */
   void stopMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );
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
      return monitoredChannelInterestMap.getOrDefault( storageKey, 0 );
   }

   /**
    * Returns the timestamp of the last event
    *
    * @implNote. this method is provided mainly for test purposes.
    *
    * @param wicaChannel the name of the channel to lookup.
    * @return the current interest count (or zero if the channel was
    *     not recognised).
    */
   Optional<LocalDateTime> getLastEventForChannel( WicaChannel wicaChannel )
   {
      final var storageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey( wicaChannel );
      return Optional.ofNullable( monitoredChannelEventMap.get( storageKey ) );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Restarts control system monitoring of the specified wica channel.
    *
    * @implNote
    * Restart is achieved by publishing STOP/START monitoring events
    * which will be acted on by the underlying control system.
    * <p>
    * When the feature is enabled each restart event results in the
    * publication of a new value to indicate that the channel has
    * become temporarily disconnected.
    *
    * @param wicaChannel the name of the channel on which monitoring.
    *   is to be restarted.
    */
   private void restartMonitoringChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.info( "Request to restart monitoring on wica channel: '{}'", wicaChannel);

      // Tell the underlying control system to STOP monitoring this channel.
      applicationEventPublisher.publishEvent( new WicaChannelStopMonitoringEvent( wicaChannel ) );

      // Publish a channel disconnect value
      if ( wicaChannelPublishMonitorRestarts )
      {
         applicationEventPublisher.publishEvent( new WicaChannelMonitoredValueUpdateEvent( wicaChannel, WicaChannelValueBuilder.createChannelValueDisconnected() ) );
      }

      // Tell the underlying control system to START monitoring this channel.
      applicationEventPublisher.publishEvent( new WicaChannelStartMonitoringEvent( wicaChannel ) );
   }

   /**
    * Starts monitoring the Wica channel with the specified name and/or
    * increments the interest count for this channel.
    *
    * @param wicaChannel the name of the channel to monitor.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    */
   private void startMonitoringChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.info( "Request to start monitoring on wica channel: '{}'", wicaChannel);

      final var storageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();

      // Update the timestamp associated with the storage key. These timestamps are used by
      // the periodic scheduling task to determine when to release a channel for which
      // there is no longer any interest.
      monitoredChannelEventMap.put( storageKey, LocalDateTime.now() );

      // If a channel with these monitoring parameters already exists simply increment the interest count.
      if ( monitoredChannelInterestMap.containsKey( storageKey ) )
      {
         final int newInterestCount = monitoredChannelInterestMap.get( storageKey ) + 1;
         logger.debug( "Increasing interest level in monitored control system channel: '{}' to {}", controlSystemName, newInterestCount );
         monitoredChannelInterestMap.put( storageKey, newInterestCount );
         return;
      }

      // If a channel with these monitoring parameters DOES NOT exist then start monitoring it using
      // the prescribed parameters.
      logger.debug( "Starting monitoring on control system channel named: '{}'", wicaChannel.getName() );

      // When the initial state publication feature is enabled publish the initial channel's state as being DISCONNECTED.
      if ( this.wicaChannelPublishChannelValueInitialState )
      {
         applicationEventPublisher.publishEvent( new WicaChannelMonitoredValueUpdateEvent(wicaChannel, WicaChannelValueBuilder.createChannelValueDisconnected() ) );
      }

      // Publish an event instructing the underlying control system to start monitoring.
      // Pretty soon the first monitored value should arrive.
      applicationEventPublisher.publishEvent( new WicaChannelStartMonitoringEvent( wicaChannel ) );
      monitoredChannelInterestMap.put( storageKey, 1 );
   }

   /**
    * Stops monitoring the Wica channel with the specified name (which
    * should previously have been monitored) and/or reduces the interest
    * count for this channel.
    * <p>
    * When/if the interest in the channel is reduced to zero then any
    * attempts to subsequently observe it's state will result in an
    * exception.
    *
    * @param wicaChannel the name of the channel which is no longer of interest.
    *
    * @throws IllegalStateException if the channel was never previously monitored.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    */
   private void stopMonitoringChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.debug( "Request to stop monitoring on wica channel: '{}'", wicaChannel );

      final var storageKey = WicaDataBufferStorageKey.getMonitoredValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();
      Validate.validState( monitoredChannelInterestMap.containsKey( storageKey ) );
      Validate.validState( monitoredChannelInterestMap.get( storageKey ) > 0 );

      // Update the timestamp of the event that was most recently associated with the storage key.
      monitoredChannelEventMap.put( storageKey, LocalDateTime.now() );

      // Reduce the level of interest in the channel.
      final int currentInterestCount = monitoredChannelInterestMap.get( storageKey );
      final int newInterestCount = currentInterestCount - 1;
      logger.debug( "Reducing interest level in monitored control system channel named: '{}' to {}" , controlSystemName.asString(), newInterestCount );
      monitoredChannelInterestMap.put( storageKey, newInterestCount );

      if ( newInterestCount == 0 )
      {
         logger.debug( "No more interest in control system channel: '{}'", controlSystemName.asString() );
         logger.debug( "The resources for the channel will be discarded in {} seconds.", wicaChannelResourceReleaseIntervalInSecs );
      }
   }

   private void discardMonitoredChannel( WicaDataBufferStorageKey storageKey )
   {
      Validate.isTrue( monitoredChannelInterestMap.containsKey( storageKey ) );
      Validate.isTrue( monitoredChannelEventMap.containsKey( storageKey ) );
      Validate.isTrue( monitoredChannelInterestMap.get(storageKey ) == 0 );

      logger.debug( "Releasing resources for monitored control system channel associated with storage key: '{}'." , storageKey.toString() );
      applicationEventPublisher.publishEvent( new WicaChannelStopMonitoringEvent( storageKey.getWicaChannel() ) );

      monitoredChannelInterestMap.remove( storageKey );
      monitoredChannelEventMap.remove( storageKey );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
