/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStartMonitoringEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopMonitoringEvent;
import ch.psi.wica.controlsystem.event.WicaChannelValueUpdateEvent;
import ch.psi.wica.model.app.*;
import ch.psi.wica.model.channel.*;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.controlsystem.event.WicaChannelMetadataBufferingService;
import ch.psi.wica.controlsystem.event.WicaChannelValueBufferingService;
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
 * for a WicaStream.
 */
@Service
@ThreadSafe
public class WicaStreamDataRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamDataRequesterService.class );
   private final ApplicationEventPublisher applicationEventPublisher;

   private final Map<ControlSystemName,Integer> controlSystemInterestMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param applicationEventPublisher reference to the application publisher
    *    which will be used to publish the channels that are to be monitored
    *    or which are no longer of interest.
    */
   WicaStreamDataRequesterService( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
      this.controlSystemInterestMap = Collections.synchronizedMap( new HashMap<>() );
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
      wicaStream.getWicaChannels().stream().map(WicaChannel::getName ).forEach(this::startMonitoringChannel );
   }

   /**
    * Stops monitoring the specified stream.
    *
    * @param wicaStream the stream that is no longer of interest.
    */
   void stopMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      wicaStream.getWicaChannels().stream().map( WicaChannel::getName ).forEach( this::stopMonitoringChannel);
   }

   /**
    * Returns the level of interest in a WicaChannel.
    *
    * @implNote. this method is provided mainly for test purposes.
    *
    * @param wicaChannelName the name of the channel to lookup.
    * @return the current interest count (or zero if the channel was
    *     not recognised).
    */
   int getInterestCountForChannel( WicaChannelName wicaChannelName)
   {
      final var controlSystemName = wicaChannelName.getControlSystemName();
      return controlSystemInterestMap.getOrDefault( controlSystemName, 0 );
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
    * @param wicaChannelName the name of the channel to monitor.
    */
   private void startMonitoringChannel( WicaChannelName wicaChannelName )
   {
      logger.trace( "Request to start monitoring wica channel named: '{}'", wicaChannelName.asString() );

      Validate.notNull( wicaChannelName );
      final var controlSystemName = wicaChannelName.getControlSystemName();

      // If the channel is already being monitored increment the interest count.
      if ( controlSystemInterestMap.containsKey( controlSystemName ) )
      {
         final int currentInterestCount = controlSystemInterestMap.get( controlSystemName );
         final int newInterestCount = currentInterestCount + 1;
         logger.trace( "Increasing interest level in control system channel named: '{}' to {}", controlSystemName.asString(), newInterestCount );
         controlSystemInterestMap.put( controlSystemName, newInterestCount );
      }
      // If the channel is NOT already being monitored start monitoring it.
      else
      {
         logger.trace( "Subscribing to new control system channel named: '{}'", controlSystemName.asString() );

         // Set the initial state for the value and metadata stashes.
         applicationEventPublisher.publishEvent( new WicaChannelMetadataUpdateEvent(wicaChannelName.getControlSystemName(), WicaChannelMetadata.createUnknownInstance()  ));
         applicationEventPublisher.publishEvent( new WicaChannelValueUpdateEvent(wicaChannelName.getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() ));

         // Now start monitoring
         applicationEventPublisher.publishEvent( new WicaChannelStartMonitoringEvent(wicaChannelName ) );
         controlSystemInterestMap.put( controlSystemName, 1 );
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
    * @param wicaChannelName the name of the channel which is no longer
    *                        of interest.
    *
    * @throws IllegalStateException if the channel name was never previously monitored.
    */
   private void stopMonitoringChannel( WicaChannelName wicaChannelName )
   {
      logger.trace( "Request to stop monitoring wica channel named: '{}'", wicaChannelName.asString() );

      Validate.notNull( wicaChannelName );
      final var controlSystemName = wicaChannelName.getControlSystemName();

      Validate.validState( controlSystemInterestMap.containsKey( controlSystemName ) );
      Validate.validState(controlSystemInterestMap.get( controlSystemName ) > 0 );

      final int currentInterestCount = controlSystemInterestMap.get( controlSystemName );
      if ( currentInterestCount > 1 )
      {
         final int newInterestCount = currentInterestCount - 1;
         logger.trace( "Reducing interest level in control system channel named: '{}' to {}" , controlSystemName.asString(), newInterestCount );
         controlSystemInterestMap.put( controlSystemName, newInterestCount );
      }
      else
      {
         logger.trace( "Unsubscribing from control system channel named: '{}'", controlSystemName.asString() );
         controlSystemInterestMap.remove( controlSystemName );
         applicationEventPublisher.publishEvent( new WicaChannelStopMonitoringEvent(wicaChannelName ) );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
