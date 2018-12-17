/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a store for the value information received from one or more Wica
 * channels.
 *
 * Unless explicitly stated otherwise in the javadoc all methods which
 * take object arguments will throw NullPointerException in the case
 * that a non null argument is passed.
 */
@ThreadSafe
public class WicaChannelValueStash
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelValueStash.class );

   private final Map<WicaChannelName, Deque<WicaChannelValue>> stash;
   private final int bufferSize;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueStash( @Value( "channel_value_stash_buffer_size" ) int bufferSize  )
   {
      logger.info( "Creating value stash with buffer size: {} ", bufferSize);
      this.bufferSize = bufferSize;
      stash = Collections.synchronizedMap( new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Updates the value associated with the specified channel.
    *
    * @implNote
    *
    * Even though the stash and queue are synchronized collections the synchronization
    * on this method IS necessary to achieve the @ThreadSafe contractual promise.
    * There is of practical importance since the Value stash may receive notifications
    * on multiple incoming threads.
    *
    * @param wicaChannelName the channel's name.
    * @param wicaChannelValue the channel's value.
    */
   public synchronized void add( WicaChannelName wicaChannelName, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( wicaChannelName );
      Validate.notNull( wicaChannelValue );

      // Lazily instantiate a Queue the first time a new channel is added
      if ( ! stash.containsKey( wicaChannelName ) )
      {
         final Deque<WicaChannelValue> queue = new ConcurrentLinkedDeque<>();
         stash.put( wicaChannelName, queue );
      }

      // Evict the oldest value from the queue when it has reached its configured size constraint
      final Queue<WicaChannelValue> queue;

      queue = stash.get(wicaChannelName);
      if ( queue.size() == bufferSize )
      {
         queue.remove();
      }
      queue.add( wicaChannelValue );
   }

   /**
    * Gets all channel values for the specified stream which arrived after the specified timestamp.
    *
    * @param wicaStream the stream of interest.
    * @param since the timestamp.
    *
    * @return the map of channel names and values which satisfy the arrival timing constraint.
    *
    * @throws IllegalStateException if the stash has no previously stored values any of the channels
    *         in this stream.
    */
   public synchronized Map<WicaChannelName, List<WicaChannelValue>> getLaterThan( WicaStream wicaStream, LocalDateTime since )
   {
      Validate.notNull( wicaStream );
      Validate.notNull( since );

      final Map<WicaChannelName, List<WicaChannelValue>> outputMap = new ConcurrentHashMap<>();
      wicaStream.getWicaChannels().forEach( c -> {
         final List<WicaChannelValue> laterThanList = getLaterThan( c.getName(), since );
         if ( laterThanList.size() > 0  )
         {
            outputMap.put( c.getName(), laterThanList );
         }
      } );

      return outputMap;
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Gets the most recent value for the specified channel (which must already exist in the stash).
    *
    * @param wicaChannelName the channel's name.
    * @return the channel's value.
    *
    * @throws IllegalStateException if the stash has no previously stored value for this channel.
    */
   public WicaChannelValue getLatest( WicaChannelName wicaChannelName )
   {
      Validate.notNull( wicaChannelName );
      Validate.validState( stash.containsKey( wicaChannelName ), "no value data for channel with name: ", wicaChannelName );
      Validate.validState(stash.get( wicaChannelName ).size() > 0, "no value data for channel with name: ", wicaChannelName );

      return stash.get( wicaChannelName ).getLast();
   }

   /**
    * Gets all values for the specified channel which arrived after the specified timestamp.
    *
    * @param wicaChannelName the channel's name.
    * @param since the timestamp.
    *
    * @return the list of channel values which satisfy the arrival timing constraint.
    *
    * @throws IllegalStateException if the stash has no previously stored values for this channel.
    */
   List<WicaChannelValue> getLaterThan( WicaChannelName wicaChannelName, LocalDateTime since )
   {
      Validate.notNull( wicaChannelName );
      Validate.notNull( since );
      Validate.validState( stash.containsKey( wicaChannelName ), "no value data for channel with name: ", wicaChannelName );
      Validate.validState(stash.get( wicaChannelName ).size() > 0, "no value data for channel with name: ", wicaChannelName );

      final Queue<WicaChannelValue> inputQueue = stash.get( wicaChannelName );
      final List<WicaChannelValue> outputQueue = new LinkedList<>();

      inputQueue.forEach( c -> {
         if ( c.getWicaServerTimestamp().compareTo( since ) > 0 )
         {
            outputQueue.add( c );
         }
      } );
      return outputQueue;
   }

/*- Nested Classes -----------------------------------------------------------*/

}
