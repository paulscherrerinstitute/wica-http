/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelStartPollingEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopPollingEvent;
import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
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
 * Provides a service for starting and stopping the control system polling
 * of a WicaStream.
 */
@Service
@ThreadSafe
public class WicaStreamPolledValueRequesterService
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamMonitoredValueRequesterService.class );
   private final ApplicationEventPublisher applicationEventPublisher;

   private final Map<WicaDataBufferStorageKey,Integer> pollerInterestMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param applicationEventPublisher reference to the application publisher
    *    which will be used to publish the channels that are to be polleded
    *    or which are no longer of interest.
    */
   WicaStreamPolledValueRequesterService( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
      this.pollerInterestMap = Collections.synchronizedMap(new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Package-level methods ----------------------------------------------------*/

   /**
    * Starts polling operations on the specified stream.
    *
    * @param wicaStream the stream to poll.
    */
   void startPolling( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
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
      Validate.notNull( wicaStream );
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
      return pollerInterestMap.getOrDefault(storageKey, 0 );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Starts polling the Wica channel with the specified name and/or
    * increments the interest count for this channel.
    *
    * Immediately thereafter the channel's connection state, metadata and
    * value will become observable via the other methods in this class.
    *
    * Until the wica server receives its first value from the channel's
    * underlying data source the metadata will be set to type UNKNOWN and
    * the value set to show that the channel is disconnected.
    *
    * @param wicaChannel the name of the channel to poll.
    */
   private void startPollingChannel( WicaChannel wicaChannel )
   {
      logger.info( "Request to start polling wica channel: '{}' with polling interval '{}' ms.", wicaChannel, wicaChannel.getProperties().getPollingIntervalInMillis() );

      Validate.notNull( wicaChannel );
      final var storageKey = WicaDataBufferStorageKey.getPolledValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();

      // If a channel with these polling parameters already exists simply increment the interest count.
      if ( pollerInterestMap.containsKey( storageKey ) )
      {
         final int currentInterestCount = pollerInterestMap.get( storageKey );
         final int newInterestCount = currentInterestCount + 1;
         logger.info( "Increasing interest level in polled control system channel named: '{}' to {}", controlSystemName, newInterestCount );
         pollerInterestMap.put(storageKey, newInterestCount );
      }
      // If a channel with these polling parameters DOES NOT exist then start polling it using
      // the prescribed parameters.
      else
      {
         logger.info( "Starting polling control system channel named: '{}'", controlSystemName.asString() );

         // Note: we could consider here publishing events to set the initial state of the metadata stash
         // or the value stash, but the poller does not implement channel metadata fetching and the only
         // the first value that is published is the result of the first poll operation.

         // Now start polling
         applicationEventPublisher.publishEvent( new WicaChannelStartPollingEvent( wicaChannel ) );
         pollerInterestMap.put( storageKey, 1 );
      }
   }

   /**
    * Stops polling the Wica channel with the specified name (which
    * should previously have been polled) and/or reduces the interest
    * count for this channel.
    *
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
      logger.info( "Request to stop polling wica channel: '{}' with polling interval '{}' ms.", wicaChannel, wicaChannel.getProperties().getPollingIntervalInMillis() );

      Validate.notNull( wicaChannel );
      final var storageKey = WicaDataBufferStorageKey.getPolledValueStorageKey( wicaChannel );
      final var controlSystemName = wicaChannel.getName().getControlSystemName();

      Validate.validState(pollerInterestMap.containsKey(storageKey ) );
      Validate.validState(pollerInterestMap.get(storageKey ) > 0 );

      final int currentInterestCount = pollerInterestMap.get(storageKey );
      if ( currentInterestCount > 1 )
      {
         final int newInterestCount = currentInterestCount - 1;
         logger.info( "Reducing interest level in polled control system channel named: '{}' to {}" , controlSystemName.asString(), newInterestCount );
         pollerInterestMap.put(storageKey, newInterestCount );
      }
      else
      {
         logger.info( "Stopping polling control system channel named: '{}'", controlSystemName.asString() );
         pollerInterestMap.remove(storageKey );
         applicationEventPublisher.publishEvent( new WicaChannelStopPollingEvent( wicaChannel ) );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
