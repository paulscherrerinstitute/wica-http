/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
@Service
@ThreadSafe
public class WicaChannelValueStash
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * Stash of names in the controls system and their most recently
    * obtained values.
    */
   private final Map<ControlSystemName, Deque<WicaChannelValue>> stash;
   private final int bufferSize;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   // @Value( "channel_value_stash_buffer_size" ) int bufferSize

   public WicaChannelValueStash( @Value( "16" ) int bufferSize  )
   {
      final Logger logger = LoggerFactory.getLogger( WicaChannelValueStash.class);
      logger.info("Creating value stash with buffer size: {} ", bufferSize);
      this.bufferSize = bufferSize;
      stash = Collections.synchronizedMap( new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Updates the value associated with the specified channel.
    *
    * @implNote.
    *
    * Even though the stash and queue are synchronized collections the synchronization
    * on this method IS necessary to achieve the @ThreadSafe contractual promise.
    * There is of practical importance since the Value stash may receive notifications
    * on multiple incoming threads.
    *
    * @param controlSystemName the channel's name.
    * @param wicaChannelValue the channel's value.
    */
   public synchronized void add( ControlSystemName controlSystemName, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( wicaChannelValue );

      // Lazily instantiate a Queue the first time a new channel is added
      if ( ! stash.containsKey( controlSystemName ) )
      {
         final Deque<WicaChannelValue> queue = new ConcurrentLinkedDeque<>();
         stash.put( controlSystemName, queue );
      }

      // Evict the oldest value from the queue when it has reached its configured size constraint
      final Queue<WicaChannelValue> queue;

      queue = stash.get( controlSystemName);
      if ( queue.size() == bufferSize )
      {
         queue.remove();
      }
      queue.add( wicaChannelValue );
   }

   /**
    * Return a map of all channels from the specified set with values which arrived after
    * the specified timestamp.
    *
    * @param wicaChannels the channels of interest.
    * @param since the timestamp.
    *
    * @return the apply of channel names and values which satisfy the arrival timing constraint.
    *
    * @throws IllegalStateException if the stash has no previously stored values any of the channels
    *         in this stream.
    */
   public synchronized Map<WicaChannelName, List<WicaChannelValue>> getLaterThan( Set<WicaChannel> wicaChannels, LocalDateTime since )
   {
      Validate.notNull( wicaChannels );
      Validate.notNull( since );

      final Map<WicaChannelName, List<WicaChannelValue>> outputMap = new ConcurrentHashMap<>();
      wicaChannels.forEach( c -> {
         final List<WicaChannelValue> laterThanList = getLaterThan( c.getName().getControlSystemName(), since );
         // Only return apply entries where there is at least one new value.
         // TODO: probably this could be done more elegantly using some declarative approach
         if ( laterThanList.size() > 0 )
         {
            outputMap.put( c.getName(), laterThanList );
         }
      } );
      return outputMap;
   }

   /**
    * Gets all values for the specified channel which arrived after the specified timestamp.
    *
    * @param controlSystemName the channel's name.
    * @param since the timestamp.
    *
    * @return the list of channel values which satisfy the arrival timing constraint. When
    *     no values satisfy the timing criteria an empty list will be returned.
    *
    * @throws IllegalStateException if the stash has no previously stored values for this channel.
    */
   public List<WicaChannelValue> getLaterThan( ControlSystemName controlSystemName, LocalDateTime since )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( since );
      Validate.validState( stash.containsKey( controlSystemName ), "Value stash did not recognise name: '" + controlSystemName + "'+" );
      Validate.validState(stash.get( controlSystemName ).size() > 0, "Value stash had not vale data for name: '" + controlSystemName + "'" );

      final Queue<WicaChannelValue> inputQueue = stash.get( controlSystemName );
      final List<WicaChannelValue> outputQueue = new LinkedList<>();

      inputQueue.forEach( c -> {
         if ( c.getWicaServerTimestamp().compareTo( since ) > 0 )
         {
            outputQueue.add( c );
         }
      } );
      return outputQueue;
   }

   /**
    * Gets the most recent value for the specified channel (which must already exist in the stash).
    *
    * @param controlSystemName the channel's name.
    * @return the channel's value.
    *
    * @throws IllegalStateException if the stash has no previously stored value for this channel.
    */
   public WicaChannelValue getLatest( ControlSystemName controlSystemName )
   {
      Validate.notNull( controlSystemName );
      Validate.validState( stash.containsKey( controlSystemName ), "no value data for channel with name: ", controlSystemName );
      Validate.validState(stash.get( controlSystemName ).size() > 0, "no value data for channel with name: ", controlSystemName );

      return stash.get( controlSystemName ).getLast();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
