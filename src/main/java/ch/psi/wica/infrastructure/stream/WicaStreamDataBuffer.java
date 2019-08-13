/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelData;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
abstract class WicaStreamDataBuffer<T extends WicaChannelData>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Map<WicaDataBufferStorageKey, Deque<T>> stash;
   private final int bufferSize;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which is capable of holding a queue of received
    * data points up to the configured buffer size.
    */
   WicaStreamDataBuffer( int bufferSize  )
   {
      this.bufferSize = bufferSize;
      this.stash = Collections.synchronizedMap( new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel, List<T>> getLaterThan( Set<WicaChannel> wicaChannels, LocalDateTime since )
   {
      Validate.notNull( wicaChannels );
      Validate.notNull( since );

      return wicaChannels.stream().collect( Collectors.toUnmodifiableMap( c -> c , c-> {
         final WicaDataBufferStorageKey wicaDataBufferStorageKey = getStorageKey( c );
         return this.getLaterThan( wicaDataBufferStorageKey, since );
      } ) );
   }

   public void saveDataPoint( WicaDataBufferStorageKey key, T t )
   {
      Validate.notNull( key );
      Validate.notNull( t );

      synchronized ( this )
      {
         // Lazily instantiate a Queue the first time a data point comes in
         // for a control system name that was not previously known.
         if ( ! stash.containsKey( key ) )
         {
            final Deque<T> deque = new ConcurrentLinkedDeque<>();
            stash.put( key, deque);
         }

         // Evict the oldest value from the queue when it has reached its configured size limit.
         final Deque<T> deque = stash.get( key );
         if ( deque.size() == bufferSize )
         {
            deque.remove();
         }

         // Now adds the new data point as the last item in the data queue.
         deque.addLast( t );
      }
   }

   public T getLatest( WicaDataBufferStorageKey key )
   {
      Validate.notNull( key );
      Validate.isTrue( stash.containsKey( key ) );

      synchronized ( this )
      {
         final Deque<T> deque = stash.get( key );
         return deque.peekLast();
      }
   }

/*- Protected methods --------------------------------------------------------*/

   protected abstract WicaDataBufferStorageKey getStorageKey( WicaChannel wicaChannel );

/*- Private methods ----------------------------------------------------------*/

   private List<T> getLaterThan( WicaDataBufferStorageKey key, LocalDateTime since )
   {
      Validate.notNull( key );
      Validate.notNull( since );

      final Queue<T> inputQueue = stash.get( key );
      if ( inputQueue == null )
      {
         return List.of();
      }

      final List<T> outputList = new ArrayList<>();
      inputQueue.forEach( c -> {
         if ( c.getWicaServerTimestamp().compareTo( since ) > 0 )
         {
            outputList.add( c );
         }
      } );
      return Collections.unmodifiableList( outputList );
   }


/*- Nested Classes -----------------------------------------------------------*/


}
