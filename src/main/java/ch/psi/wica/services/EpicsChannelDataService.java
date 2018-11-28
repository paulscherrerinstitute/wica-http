/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import ch.psi.wica.model.WicaStream;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
@Service
public class EpicsChannelDataService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger (EpicsChannelDataService.class );

   private static final Map<WicaChannelName,Integer> channelInterestMap = Collections.synchronizedMap( new HashMap<>() );
   private static final Map<WicaChannelName, WicaChannelValue> channelValueStash = Collections.synchronizedMap( new HashMap<>() );
   private static final Map<WicaChannelName, WicaChannelMetadata> channelMetadataStash = Collections.synchronizedMap( new HashMap<>() );

   private EpicsChannelMonitorService epicsChannelMonitorService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private EpicsChannelDataService( EpicsChannelMonitorService epicsChannelMonitorService )
   {
      Validate.notNull ( epicsChannelMonitorService );
      this.epicsChannelMonitorService = new EpicsChannelMonitorService();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public void startMonitoring( WicaChannelName wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // If the channel is already being monitored increment the interest count.
      if ( channelInterestMap.containsKey( wicaChannel ) )
      {
         channelInterestMap.put( wicaChannel, channelInterestMap.get( wicaChannel ) + 1 );
      }
      // If the channel is NOT already being monitored start monitoring it.
      else
      {
         logger.info("subscribing to new channel: '{}'", wicaChannel );

         // Now startMonitoring
         final Consumer<Boolean> stateChangedHandler = b -> stateChanged( wicaChannel, b );
         final Consumer<WicaChannelValue> valueChangedHandler = v -> valueChanged( wicaChannel, v );
         final Consumer<WicaChannelMetadata> metadataChangedHandler = v -> metadataChanged( wicaChannel, v );
         epicsChannelMonitorService.startMonitoring( wicaChannel, stateChangedHandler, metadataChangedHandler, valueChangedHandler );
         channelInterestMap.put( wicaChannel, 1 );
      }
   }


   public void startMonitoring( WicaStream wicaStream )
   {
      Validate.notNull(wicaStream);

      for ( WicaChannelName channelName : wicaStream.getChannels() )
      {
         // If the channel is already being monitored increment the interest count.
         if ( channelInterestMap.containsKey( channelName ) )
         {
            channelInterestMap.put( channelName, channelInterestMap.get( channelName ) + 1 );
         }
         // If the channel is NOT already being monitored start monitoring it.
         else
         {
            logger.info("subscribing to new channel: '{}'", channelName );

            // Now startMonitoring
            final Consumer<Boolean> stateChangedHandler = b -> stateChanged( channelName, b );
            final Consumer<WicaChannelValue> valueChangedHandler = v -> valueChanged(channelName, v );
            final Consumer<WicaChannelMetadata> metadataChangedHandler = v -> metadataChanged(channelName, v );
            epicsChannelMonitorService.startMonitoring( channelName, stateChangedHandler, metadataChangedHandler, valueChangedHandler );
            channelInterestMap.put( channelName, 1 );
         }
      }
   }

   public void stopMonitoring( WicaStream WicaStream )
   {
      Validate.notNull(WicaStream);

      for ( WicaChannelName channelName : WicaStream.getChannels() )
      {
         // Validate the precondition that
         Validate.isTrue( channelInterestMap.containsKey( channelName ) );

         channelInterestMap.put( channelName, channelInterestMap.get( channelName ) - 1 );

         if ( channelInterestMap.get( channelName ) == 0 )
         {
            logger.info( "unsubscribing to channel: '{}'", channelName ) ;
            epicsChannelMonitorService.stopMonitoring( channelName );
         }
      }
   }

   public WicaChannelMetadata getChannelMetadata( WicaChannelName channelName )
   {
      Validate.notNull( channelName );
      return channelMetadataStash.get( channelName );
   }


   public WicaChannelValue getChannelValue( WicaChannelName channelName )
   {
      Validate.notNull( channelName );
      return channelValueStash.get( channelName );
   }

   public boolean waitForFirstValue( WicaChannelName channelName, TimeUnit timeUnit, long timeout ) throws InterruptedException
   {
      Validate.notNull( channelName );

      final CountDownLatch countDownLatch = new CountDownLatch( 1 );
      return countDownLatch.await( timeout, timeUnit );
   }


   public Map<WicaChannelName, WicaChannelValue> getChannelValues( WicaStream WicaStream )
   {
      Validate.notNull(WicaStream);

      return WicaStream.getChannels().stream()
                                     .filter( channelValueStash::containsKey )
                                     .collect( Collectors.toMap( Function.identity(), channelValueStash::get ) );
   }


   public Map<WicaChannelName, WicaChannelValue> getChannelValuesUpdatedSince( WicaStream WicaStream, LocalDateTime sinceDateTime )
   {
      Validate.notNull(WicaStream);

      return WicaStream.getChannels().stream()
                                     .filter( channelValueStash::containsKey )
                                     .filter( c -> channelValueStash.get( c ).getTimestamp().isAfter( sinceDateTime ) )
                                     .collect( Collectors.toMap( Function.identity(), channelValueStash::get ) );
   }

   public Map<WicaChannelName, WicaChannelMetadata> getChannelMetadata( WicaStream WicaStream )
   {
      Validate.notNull(WicaStream);

      return WicaStream.getChannels().stream()
                                     .filter( channelMetadataStash::containsKey )
                                     .collect( Collectors.toMap( Function.identity(), channelMetadataStash::get ) );
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
      Validate.notNull(wicaChannelName, "The 'wicaChannelName' argument was null" );
      Validate.notNull( isConnected, "The 'isConnected' argument was null"  );

      logger.info("'{}' - connection state changed to '{}'.", wicaChannelName, isConnected);

      if ( ! isConnected )
      {
         logger.debug("'{}' - value changed to NULL to indicate the connection was lost.", wicaChannelName);
         channelValueStash.put( wicaChannelName, WicaChannelValue.createChannelDisconnectedValue( LocalDateTime.now() ) );
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
      Validate.notNull(wicaChannelName, "The 'wicaChannelName' argument was null");
      Validate.notNull(wicaChannelValue, "The 'newValue' argument was null");

      logger.trace("'{}' - value changed to: '{}'", wicaChannelName, wicaChannelValue);
      channelValueStash.put( wicaChannelName, wicaChannelValue );
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
      channelMetadataStash.put(wicaChannelName, wicaChannelMetadata);
   }

/*- Nested Classes -----------------------------------------------------------*/

}
