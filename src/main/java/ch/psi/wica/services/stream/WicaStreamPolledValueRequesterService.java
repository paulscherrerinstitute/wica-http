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

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for starting and stopping the control system polling
 * of the cnhannels in a WicaStream.
 */
@Configuration
@EnableScheduling
@Service
@ThreadSafe
public class WicaStreamPolledValueRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final int RESOURCE_RELEASE_SCAN_INTERVAL = 1000;

   private final Logger logger = LoggerFactory.getLogger( WicaStreamPolledValueRequesterService.class );

   private final int wicaChannelResourceReleaseIntervalInSecs;
   private final boolean wicaChannelPublishPollerRestarts;
   private final boolean wicaChannelPublishChannelValueInitialState;
   private final ApplicationEventPublisher applicationEventPublisher;
   private final Map<WicaDataBufferStorageKey,Integer> polledChannelInterestMap;
   private final Map<WicaDataBufferStorageKey, LocalDateTime> polledChannelEventMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param wicaChannelResourceReleaseIntervalInSecs period after which the
    *    resources associated with a Wica Channel will be released if they are no
    *    longer in use.
    *
    * @param wicaChannelPublishPollerRestarts determines whether a channel
    *    disconnect value will be published if the polling associated
    *    with the channel is restarted.
    *
    * @param wicaChannelPublishChannelValueInitialState determines whether a
    *    channel's initial value will be published as DISCONNECTED when a
    *    channel is first created or whether nothing will be published until
    *    the first polled value is acquired.
    *
    * @param applicationEventPublisher reference to the application publisher
    *    which will be used to publish the channels that are to be polled
    *    or which are no longer of interest.
    */
   WicaStreamPolledValueRequesterService( @Value( "${wica.channel-resource-release-interval-in-secs}" ) int wicaChannelResourceReleaseIntervalInSecs,
                                          @Value( "${wica.channel-publish-poller-restarts}" ) boolean wicaChannelPublishPollerRestarts,
                                          @Value( "${wica.channel-publish-channel-value-initial-state}" ) boolean wicaChannelPublishChannelValueInitialState,
                                          @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.wicaChannelResourceReleaseIntervalInSecs = wicaChannelResourceReleaseIntervalInSecs;
      this.wicaChannelPublishPollerRestarts = wicaChannelPublishPollerRestarts;
      this.wicaChannelPublishChannelValueInitialState = wicaChannelPublishChannelValueInitialState;
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher, "The 'applicationEventPublisher' argument is null." );
      this.polledChannelInterestMap = Collections.synchronizedMap( new HashMap<>() );
      this.polledChannelEventMap = Collections.synchronizedMap( new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * This method runs periodically to scan the discard list for entries corresponding to
    * polled channels that are no longer of interest
    */
   @Scheduled( fixedRate=RESOURCE_RELEASE_SCAN_INTERVAL )
   public void discardPollersThatHaveReachedEndOfLife()
   {
      final var timeNow = LocalDateTime.now();
      polledChannelInterestMap.keySet()
            .stream()
            .filter( key -> polledChannelInterestMap.get( key ) == 0 )
            .filter( key -> timeNow.isAfter( polledChannelEventMap.get( key ).plusSeconds( wicaChannelResourceReleaseIntervalInSecs ) ) )
            .toList()
            .forEach( this::discardPolledChannel );
   }

/*- Package-level methods ----------------------------------------------------*/

   /**
    * Restarts the control system polling of the channels in the specified stream.
    *
    * @param wicaStream the stream on which polling is to be restarted.
    */
   void restartPolling( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );

      wicaStream.getWicaChannels()
            .stream()
            .filter( c -> c.getProperties().getDataAcquisitionMode().doesPolling() )
            .forEach( this::restartPollingChannel);
   }

   /**
    * Starts polling operations on the specified stream.
    *
    * @param wicaStream the stream to poll.
    */
   void startPolling( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );
      wicaStream.getWicaChannels()
            .stream()
            .filter( c -> c.getProperties().getDataAcquisitionMode().doesPolling() )
            .forEach( this::startPollingChannel);
   }

   /**
    * Stops polling operations on the specified stream.
    *
    * @param wicaStream the stream that is no longer of interest.
    */
   void stopPolling( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );
      wicaStream.getWicaChannels()
            .stream()
            .filter( c -> c.getProperties().getDataAcquisitionMode().doesPolling() )
            .forEach( this::stopPollingChannel);
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
      final var storageKey = WicaDataBufferStorageKey.getPolledValueStorageKey( wicaChannel );
      return polledChannelInterestMap.getOrDefault( storageKey, 0 );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Restarts control system polling of the specified wica channel.
    *
    * @implNote
    * Restart is achieved by publishing STOP/START polling events
    * which will be acted on by the underlying control system.
    * <p>
    * When the feature is enabled each restart event results in the
    * publication of a new value to indicate that the channel has
    * become temporarily disconnected.
    *
    * @param wicaChannel the name of the channel on which polling.
    *   is to be restarted.
    */
   private void restartPollingChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.info( "Request to restart polling on wica channel: '{}'", wicaChannel);

      // Tell the underlying control system to STOP polling this channel.
      applicationEventPublisher.publishEvent( new WicaChannelStopPollingEvent( wicaChannel ) );

      // Publish a channel disconnect value
      if ( wicaChannelPublishPollerRestarts )
      {
         applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannel, WicaChannelValueBuilder.createChannelValueDisconnected() ) );
      }

      // Tell the underlying control system to START polling this channel.
      applicationEventPublisher.publishEvent( new WicaChannelStartPollingEvent( wicaChannel ) );
   }

   /**
    * Starts polling the Wica channel with the specified name and/or
    * increments the interest count for this channel.
    *
    * @param wicaChannel the name of the channel to poll.
    */
   private void startPollingChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.debug( "Request to start polling wica channel: '{}' with polling interval '{}' ms.", wicaChannel, wicaChannel.getProperties().getPollingIntervalInMillis() );

      final var storageKey = WicaDataBufferStorageKey.getPolledValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();

      // Update the timestamp associated with the storage key. These timestamps are used by
      // the periodic scheduling task to determine when to release a channel for which
      // there is no longer any interest.
      polledChannelEventMap.put( storageKey, LocalDateTime.now() );

      // If a channel with these polling parameters already exists simply increment the interest count.
      if ( polledChannelInterestMap.containsKey( storageKey ) )
      {
         final int newInterestCount = polledChannelInterestMap.get( storageKey ) + 1;
         logger.debug( "Increasing interest level in polled control system channel named: '{}' to {}", controlSystemName, newInterestCount );
         polledChannelInterestMap.put( storageKey, newInterestCount );
         return;
      }

      // If a channel with these polling parameters DOES NOT exist then start polling it using
      // the prescribed parameters.
      logger.debug( "Starting polling control system channel named: '{}'", wicaChannel.getName() );

      // When the initial state publication feature is enabled publish the initial channel's state as being DISCONNECTED.
      if ( this.wicaChannelPublishChannelValueInitialState )
      {
         applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent(wicaChannel, WicaChannelValueBuilder.createChannelValueDisconnected() ) );
      }

      // Publish an event instructing the underlying control system to start polling.
      // Pretty soon the first polled value should arrive.
      applicationEventPublisher.publishEvent( new WicaChannelStartPollingEvent( wicaChannel ) );
      polledChannelInterestMap.put( storageKey, 1 );
   }

   /**
    * Stops polling the Wica channel with the specified name (which
    * should previously have been polled) and/or reduces the interest
    * count for this channel.
    * <p>
    * When/if the interest in the channel is reduced to zero then any
    * attempts to subsequently observe it's state will result in an
    * exception.
    *
    * @param wicaChannel the name of the channel which is no longer of interest.
    *
    * @throws IllegalStateException if the channel was never previously polled.
    */
   private void stopPollingChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.debug( "Request to stop polling wica channel: '{}'", wicaChannel );

      final var storageKey = WicaDataBufferStorageKey.getPolledValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();
      Validate.validState( polledChannelInterestMap.containsKey( storageKey ) );
      Validate.validState( polledChannelInterestMap.get( storageKey ) > 0 );

      // Update the timestamp of the event that was most recently associated with the storage key.
      polledChannelEventMap.put( storageKey, LocalDateTime.now() );

      // Reduce the level of interest in the channel.
      final int currentInterestCount = polledChannelInterestMap.get( storageKey );
      final int newInterestCount = currentInterestCount - 1;
      logger.debug( "Reducing interest level in polled control system channel named: '{}' to {}" , controlSystemName.asString(), newInterestCount );
      polledChannelInterestMap.put( storageKey, newInterestCount );

      if ( newInterestCount == 0 )
      {
         logger.debug( "No more interest in control system channel: '{}'", controlSystemName.asString() );
         logger.debug( "The resources for the channel will be discarded in {} seconds.", wicaChannelResourceReleaseIntervalInSecs );
      }
   }

   private void discardPolledChannel( WicaDataBufferStorageKey storageKey )
   {
      Validate.isTrue( polledChannelInterestMap.containsKey( storageKey ) );
      Validate.isTrue( polledChannelEventMap.containsKey( storageKey ) );
      Validate.isTrue( polledChannelInterestMap.get(storageKey ) == 0 );

      logger.debug( "Releasing resources for polled control system channel associated with storage key: '{}'." , storageKey.toString() );
      applicationEventPublisher.publishEvent( new WicaChannelStopPollingEvent( storageKey.getWicaChannel() ) );

      polledChannelInterestMap.remove( storageKey );
      polledChannelEventMap.remove( storageKey );
   }

   /*- Nested Classes -----------------------------------------------------------*/

}
