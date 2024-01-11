/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.*;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
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
import java.util.Objects;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for starting and stopping the acquisition of metadata
 * for the channels in a WicaStream.
 */
@Configuration
@EnableScheduling
@Service
@ThreadSafe
public class WicaStreamMetadataRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final int RESOURCE_RELEASE_SCAN_INTERVAL = 1000;

   private final Logger logger = LoggerFactory.getLogger( WicaStreamMetadataRequesterService.class );

   private final int wicaChannelResourceReleaseIntervalInSecs;
   private final boolean wicaChannelPublishChannelMetadataInitialState;
   private final ApplicationEventPublisher applicationEventPublisher;
   private final Map<WicaDataBufferStorageKey,Integer> channelInterestMap;
   private final Map<WicaDataBufferStorageKey,LocalDateTime> channelEventMap;

   /*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param wicaChannelResourceReleaseIntervalInSecs period after which the
    *    resources associated with a Wica Channel will be released if they are no
    *    longer in use.
    *
    * @param wicaChannelPublishChannelMetadataInitialState determines whether a
    *    channel's initial value will be published as UNKNOWN when a
    *    channel is first created or whether nothing will be published until
    *    the first metadata information is acquired.
    *
    * @param applicationEventPublisher reference to the application publisher
    *    which will be used to publish the channels to acquire metadata for
    *    or which are no longer of interest.
    */
   WicaStreamMetadataRequesterService( @Value( "${wica.channel-resource-release-interval-in-secs}" ) int wicaChannelResourceReleaseIntervalInSecs,
                                       @Value( "${wica.channel-publish-channel-metadata-initial-state}" ) boolean wicaChannelPublishChannelMetadataInitialState,
                                       @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.wicaChannelResourceReleaseIntervalInSecs = wicaChannelResourceReleaseIntervalInSecs;
      this.wicaChannelPublishChannelMetadataInitialState = wicaChannelPublishChannelMetadataInitialState;
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher, "The 'applicationEventPublisher' argument is null." );
      this.channelInterestMap = Collections.synchronizedMap( new HashMap<>() );
      this.channelEventMap = Collections.synchronizedMap( new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * This method runs periodically to scan the discard list for entries corresponding
    * to channels whose metadata is no longer of interest.
    */
   @Scheduled( fixedRate=RESOURCE_RELEASE_SCAN_INTERVAL )
   public void discardChannelsThatHaveReachedEndOfLife()
   {
      final var timeNow = LocalDateTime.now();
      channelInterestMap.keySet()
            .stream()
            .filter( key -> channelInterestMap.get( key ) == 0 )
            .filter( key -> timeNow.isAfter( channelEventMap.get( key ).plusSeconds( wicaChannelResourceReleaseIntervalInSecs ) ) )
            .collect( Collectors.toList() )
            .forEach( this::discardChannel );
   }

/*- Package-level methods ----------------------------------------------------*/

   /**
    * Starts metadata data acquisition for the channels in the specified stream.
    *
    * @param wicaStream the stream to acquire metadata for.
    */
   void startDataAcquisition( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );
      wicaStream.getWicaChannels().forEach( this::startDataAcquisitionChannel );
   }

   /**
    * Stops for the channels in the specified stream.
    *
    * @param wicaStream the stream that is no longer of interest.
    */
   void stopDataAcquisition( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream, "The 'wicaStream' argument is null." );
      wicaStream.getWicaChannels().forEach( this::stopDataAcquisitionChannel );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Starts metadata data acquisition for the Wica channel with the specified
    * name and/or increments the interest count for this channel.
    *
    * @param wicaChannel the name of the channel to acquire metadata for.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    */
   private void startDataAcquisitionChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.debug( "Request to start acquiring metadata for wica channel: '{}'", wicaChannel );

      final var storageKey = WicaDataBufferStorageKey.getMetadataStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();

      // Update the timestamp of the event that was most recently associated with the storage key.
      channelEventMap.put( storageKey, LocalDateTime.now() );

      // If a channel with these parameters already exists simply increment the interest count.
      if ( channelInterestMap.containsKey( storageKey ) )
      {
         final int newInterestCount = channelInterestMap.get( storageKey ) + 1;
         logger.debug( "Increasing interest level in metadata for control system channel: '{}' to {}", controlSystemName, newInterestCount );
         channelInterestMap.put( storageKey, newInterestCount );
         return;
      }

      // If a channel with these parameters DOES NOT exist then start acquiring metadata for it.
      logger.debug( "Starting acquiring metadata for control system channel named: '{}'", wicaChannel.getName() );

      // When the initial state publication feature is enabled publish the channel's initial metadata value
      // as being UNKNOWN.
      if ( this.wicaChannelPublishChannelMetadataInitialState )
      {
         applicationEventPublisher.publishEvent( new WicaChannelMetadataUpdateEvent( wicaChannel, WicaChannelMetadata.createUnknownInstance() ) );
      }

      // Publish an event instructing the underlying control system to start data acquisition.
      applicationEventPublisher.publishEvent( new WicaChannelStartMetadataDataAcquisitionEvent( wicaChannel ) );
      channelInterestMap.put( storageKey, 1 );
   }

   /**
    * Stops metadata data acquisition for the Wica channel with the specified
    * name (which should previously have been started) and/or reduces the interest
    * count for this channel.
    *
    * When/if the interest in the channel is reduced to zero then any
    * attempts to subsequently observe it's state will result in an
    * exception.
    *
    * @param wicaChannel the name of the channel which is no longer of interest.
    * @throws IllegalStateException if the channel was never previously active.
    * @throws NullPointerException if the 'wicaChannel' argument was null.
    */
   private void stopDataAcquisitionChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      logger.debug( "Request to stop acquiring metadata for wica channel: '{}'", wicaChannel );

      final var storageKey = WicaDataBufferStorageKey.getMetadataStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();
      Validate.validState( channelInterestMap.containsKey( storageKey ) );
      Validate.validState( channelInterestMap.get( storageKey ) > 0 );

      // Update the timestamp of the event that was most recently associated with the storage key.
      channelEventMap.put( storageKey, LocalDateTime.now() );

      // Reduce the level of interest in the channel.
      final int currentInterestCount = channelInterestMap.get( storageKey );
      final int newInterestCount = currentInterestCount - 1;
      logger.debug( "Reducing interest level in metadata for control system channel named: '{}' to {}" , controlSystemName.asString(), newInterestCount );
      channelInterestMap.put( storageKey, newInterestCount );

      if ( newInterestCount == 0 )
      {
         logger.debug( "No more interest in metadata for control system channel: '{}'", controlSystemName.asString() );
         logger.debug( "The resources for the channel will be discarded in {} seconds.", wicaChannelResourceReleaseIntervalInSecs );
      }
   }

   private void discardChannel( WicaDataBufferStorageKey storageKey )
   {
      Validate.isTrue( channelInterestMap.containsKey( storageKey ) );
      Validate.isTrue( channelEventMap.containsKey( storageKey ) );
      Validate.isTrue( channelInterestMap.get( storageKey ) == 0 );

      logger.debug( "Releasing resources for the control system channel associated with storage key: '{}'." , storageKey.toString() );
      applicationEventPublisher.publishEvent( new WicaChannelStopMetadataDataAcquisitionEvent( storageKey.getWicaChannel() ) );

      channelInterestMap.remove( storageKey );
      channelEventMap.remove( storageKey );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
