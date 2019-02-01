/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service to facilitate the monitoring of multiple EPICS channels,
 * and to cache the received information making it available subsequently
 * to multiple service clients.
 *
 * Channels are reused.
 */
@ThreadSafe
@Service
public class EpicsChannelDataService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger (EpicsChannelDataService.class );

   private static final Map<WicaChannelName,Integer> channelInterestMap = Collections.synchronizedMap( new HashMap<>() );

   private final WicaChannelMetadataStash channelMetadataStash = new WicaChannelMetadataStash();
   private final WicaChannelValueStash channelValueStash = new WicaChannelValueStash( 16 );

   private EpicsChannelMonitorService epicsChannelMonitorService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelDataService( EpicsChannelMonitorService epicsChannelMonitorService )
   {
      Validate.notNull ( epicsChannelMonitorService );
      this.epicsChannelMonitorService = epicsChannelMonitorService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

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
      Validate.notNull( wicaChannelName );

      // If the channel is already being monitored increment the interest count.
      if ( channelInterestMap.containsKey( wicaChannelName ) )
      {
         channelInterestMap.put( wicaChannelName, channelInterestMap.get( wicaChannelName ) + 1 );
      }
      // If the channel is NOT already being monitored start monitoring it.
      else
      {
         logger.info("subscribing to new channel: '{}'", wicaChannelName );

         // Set the initial state for the value and metadata stashes.
         channelMetadataStash.put( wicaChannelName, WicaChannelMetadata.createUnknownInstance() );
         channelValueStash.add( wicaChannelName, WicaChannelValue.createChannelValueDisconnected() );

         // Now startMonitoring
         final Consumer<Boolean> stateChangedHandler = b -> stateChanged( wicaChannelName, b );
         final Consumer<WicaChannelValue> valueChangedHandler = v -> valueChanged( wicaChannelName, v );
         final Consumer<WicaChannelMetadata> metadataChangedHandler = v -> metadataChanged( wicaChannelName, v );
         epicsChannelMonitorService.startMonitoring( wicaChannelName, stateChangedHandler, metadataChangedHandler, valueChangedHandler );
         channelInterestMap.put( wicaChannelName, 1 );
      }
   }

   /**
    * Starts monitoring the Wica channels associated with the specified
    * Wica stream and/or increments each channel's interest count.
    *
    * Subsequently the connection state, metadata and value information
    * for all channels in the stream will be observable.
    *
    * @param wicaStream the stream containing the names the channels
    *                   to monitor.
    */
   public void startMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      wicaStream.getWicaChannels().stream().map( WicaChannel::getName ).forEach( this::startMonitoring );
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
   private void stopMonitoring( WicaChannelName wicaChannelName )
   {
      Validate.notNull(wicaChannelName);
      Validate.validState( channelInterestMap.containsKey( wicaChannelName ) );
      Validate.validState(channelInterestMap.get( wicaChannelName ) >= 1 );

      if ( channelInterestMap.get( wicaChannelName ) > 1 )
      {
         channelInterestMap.put( wicaChannelName, channelInterestMap.get( wicaChannelName ) - 1 );
      }
      else
      {
         channelInterestMap.remove( wicaChannelName );
         logger.info( "unsubscribing to channel: '{}'", wicaChannelName ) ;
         epicsChannelMonitorService.stopMonitoring( wicaChannelName );
      }
   }

   /**
    * Stops monitoring the specified stream.
    *
    * If there is no longer any interest in the channel all monitoring operations on that channel will cease.
    *
    * @param wicaStream the stream whose channels are no longer of interest.
    */
   public void stopMonitoring( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      Validate.validState( wicaStream.getWicaChannels().stream().anyMatch( c -> channelInterestMap.containsKey( c.getName() ) ), "Programming error: interest in channel has already disappeared");
      wicaStream.getWicaChannels().stream().map( WicaChannel::getName ).forEach(this::stopMonitoring );
   }

   /**
    * Returns the latest received metadata for the specified Wica channel.
    *
    * @param channelName the name of the channel whose metadata is to be retrieved.
    *
    * @return the metadata.
    */
   public WicaChannelMetadata getChannelMetadata( WicaChannelName channelName )
   {
      Validate.notNull( channelName );
      Validate.isTrue(  channelInterestMap.containsKey( channelName ), "channel name not known" );

      return channelMetadataStash.get( channelName );
   }

   /**
    * Returns the latest received metadata for the specified Wica stream.
    *
    * @param wicaStream the stream of interest.
    *
    * @return the metadata.
    */
   public Map<WicaChannelName, WicaChannelMetadata> getChannelMetadata( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );

      return channelMetadataStash.get( wicaStream );
   }

   /**
    * Returns the latest received value for the specified Wica channel.
    *
    * @param channelName the name of the channel whose value is to be retrieved.
    *
    * @return the metadata.
    */
   public WicaChannelValue getChannelValue( WicaChannelName channelName )
   {
      Validate.notNull( channelName );
      Validate.isTrue(  channelInterestMap.containsKey( channelName ), "channel name not known" );

      return channelValueStash.getLatest( channelName );
   }

   public Map<WicaChannelName, List<WicaChannelValue>> getLaterThan( WicaStream wicaStream, LocalDateTime since )
   {
      Validate.notNull( wicaStream );

      return channelValueStash.getLaterThan( wicaStream, since );
   }


   public boolean waitForFirstValue( WicaChannelName channelName, TimeUnit timeUnit, long timeout ) throws InterruptedException
   {
      Validate.notNull( channelName );

      final CountDownLatch countDownLatch = new CountDownLatch( 1 );
      return countDownLatch.await( timeout, timeUnit );
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
         channelValueStash.add( wicaChannelName, disconnectedValue );
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
      channelValueStash.add( wicaChannelName, wicaChannelValue );
   }

   /**
    * Handles a value change on the metadata associated with an EPICS channel.
    *
    * @param wicaChannelName the name of the channel for whom metadata is now available
    * @param wicaChannelMetadata the metadata
    */
   private void metadataChanged( WicaChannelName wicaChannelName, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull(wicaChannelName, "The 'wicaChannelName' argument was null");
      Validate.notNull(wicaChannelMetadata, "The 'wicaChannelMetadata' argument was null");

      logger.trace("'{}' - metadata changed to: '{}'", wicaChannelName, wicaChannelMetadata);
      channelMetadataStash.put( wicaChannelName, wicaChannelMetadata);
   }

/*- Nested Classes -----------------------------------------------------------*/

}
