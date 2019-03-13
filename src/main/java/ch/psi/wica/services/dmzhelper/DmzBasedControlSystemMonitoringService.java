/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.dmzhelper;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;

import ch.psi.wica.services.stream.ControlSystemMonitoringService;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service for monitoring multiple EPICS channels, for cacheing the received
 * information, making it subsequently available to Wica service consumers.
 *
 * @implNote.
 * Channels are reused.
 */
//@Service
@ThreadSafe
public class DmzBasedControlSystemMonitoringService implements ControlSystemMonitoringService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( DmzBasedControlSystemMonitoringService.class );

   private static final Map<ControlSystemName,Integer> controlSystemInterestMap = Collections.synchronizedMap( new HashMap<>() );

   private final WicaChannelMetadataStash channelMetadataStash;
   private final WicaChannelValueStash channelValueStash;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   DmzBasedControlSystemMonitoringService( WicaChannelMetadataStash wicaChannelMetadataStash,
                                           WicaChannelValueStash wicaChannelValueStash )
   {
      this.channelMetadataStash = Validate.notNull( wicaChannelMetadataStash );
      this.channelValueStash = Validate.notNull( wicaChannelValueStash );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public void activate()
   {

   }

   @Override
   public void startMonitoring( WicaStream wicaStream )
   {
       wicaStream.getWicaChannels().stream().map( WicaChannel::getName ).forEach( this::startMonitoring );
   }

   @Override
   public void stopMonitoring( WicaStream wicaStream )
   {
      wicaStream.getWicaChannels().stream().map( WicaChannel::getName ).forEach( this::stopMonitoring );
   }

/*- Package level methods ----------------------------------------------------*/

   /**
    * Provided for unit testing only.
    *
    * @param wicaChannelName the name of the channel.
    * @return the result.
    */
   int getInterestCountForChannel( WicaChannelName wicaChannelName)
   {
      final var controlSystemName = wicaChannelName.getControlSystemName();
      return controlSystemInterestMap.getOrDefault( controlSystemName, 0 );
   }

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
   void startMonitoring( WicaChannelName wicaChannelName )
   {
      logger.info( "Starting monitoring wica channel named: '{}'", wicaChannelName.asString() );

      Validate.notNull( wicaChannelName );
      final var controlSystemName = wicaChannelName.getControlSystemName();

      // If the channel is already being monitored increment the interest count.
      if ( controlSystemInterestMap.containsKey( controlSystemName ) )
      {
         final int currentInterestCount = controlSystemInterestMap.get( controlSystemName );
         final int newInterestCount = currentInterestCount + 1;
         logger.info( "Increasing interest level in control system channel named: '{}' to {}", controlSystemName.asString(), newInterestCount );
         controlSystemInterestMap.put( controlSystemName, newInterestCount );
      }
      // If the channel is NOT already being monitored start monitoring it.
      else
      {
         logger.info("Subscribing to new control system channel named: '{}'", controlSystemName.asString() );

         // Set the initial state for the value and metadata stashes.
         channelMetadataStash.put( wicaChannelName.getControlSystemName(), WicaChannelMetadata.createUnknownInstance() );
         channelValueStash.add( wicaChannelName.getControlSystemName(), WicaChannelValue.createChannelValueDisconnected() );

         // Now start monitoring
         // What to do here ??

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
   void stopMonitoring( WicaChannelName wicaChannelName )
   {
      logger.info( "Stopping monitoring wica channel named: '{}'", wicaChannelName.asString() );
      Validate.notNull( wicaChannelName );
      final var controlSystemName = wicaChannelName.getControlSystemName();

      Validate.validState( controlSystemInterestMap.containsKey( controlSystemName ) );
      Validate.validState(controlSystemInterestMap.get( controlSystemName ) > 0 );

      final int currentInterestCount = controlSystemInterestMap.get( controlSystemName );
      if ( currentInterestCount > 1 )
      {
         final int newInterestCount = currentInterestCount - 1;
         logger.info( "Reducing interest level in control system channel named: '{}' to {}" , controlSystemName.asString(), newInterestCount );
         controlSystemInterestMap.put( controlSystemName, newInterestCount );
      }
      else
      {
         logger.info( "Unsubscribing to control system channel named: '{}'", controlSystemName.asString() );
         controlSystemInterestMap.remove( controlSystemName );

         // what to do here ??

      }
   }


/*- Private methods ----------------------------------------------------------*/

   /**
    * Handles a connection state change on the underlying EPICS channel monitor.
    *
    * @param wicaChannelName the name of the channel whose connection state changed.
    * @param isConnected the new connection state.
    */
   private void stateChanged( WicaChannelName wicaChannelName, Boolean isConnected )
   {
      Validate.notNull( wicaChannelName, "The 'wicaChannelName' argument was null" );
      Validate.notNull( isConnected, "The 'isConnected' argument was null"  );
      logger.info("'{}' - connection state changed to '{}'.", wicaChannelName, isConnected);

      if ( ! isConnected )
      {
         logger.debug("'{}' - value changed to NULL to indicate the connection was lost.", wicaChannelName);
         final WicaChannelValue disconnectedValue = WicaChannelValue.createChannelValueDisconnected();
         channelValueStash.add( wicaChannelName.getControlSystemName(), disconnectedValue );
      }
   }

   /**
    * Handles a value change on the underlying EPICS channel monitor.
    *
    * @param wicaChannelName the name of the channel whose monitor changed.
    * @param wicaChannelValue the new value.
    */
   private void valueChanged( WicaChannelName wicaChannelName, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( wicaChannelName, "The 'wicaChannelName' argument was null");
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument was null");

      logger.trace("'{}' - value changed to: '{}'", wicaChannelName, wicaChannelValue);
      channelValueStash.add( wicaChannelName.getControlSystemName(), wicaChannelValue );
   }

   /**
    * Handles a value change on the metadata associated with an EPICS channel.
    *
    * @param wicaChannelName the name of the channel for whom metadata is now available
    * @param wicaChannelMetadata the metadata
    */
   private void metadataChanged( WicaChannelName wicaChannelName, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull( wicaChannelName, "The 'wicaChannelName' argument was null");
      Validate.notNull( wicaChannelMetadata, "The 'wicaChannelMetadata' argument was null");

      logger.trace("'{}' - metadata changed to: '{}'", wicaChannelName, wicaChannelMetadata);
      channelMetadataStash.put( wicaChannelName.getControlSystemName(), wicaChannelMetadata);
   }


/*- Nested Classes -----------------------------------------------------------*/

}
