/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.event;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelData;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service which listens and buffers data update events
 * received via the Spring event listening service making them available
 * as a service to the rest of the application.
 */
@ThreadSafe
public class WicaChannelDataBuffer<T extends WicaChannelData>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * Stash of names in the control system and their most recently received data.
    */
   private final Map<ControlSystemName, Deque<T>> stash;
   private final int bufferSize;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which is capable of holding a queue of received
    * data points up to the configured buffer size.
    */
   public WicaChannelDataBuffer( int bufferSize  )
   {
      this.bufferSize = bufferSize;
      this.stash = Collections.synchronizedMap( new HashMap<>() );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Map<WicaChannel,Optional<T>> getLatest( Set<WicaChannel> wicaChannels )
   {
      Validate.notNull( wicaChannels );

      final Map<WicaChannel,Optional<T>> outputMap = new ConcurrentHashMap<>();
      for ( WicaChannel wicaChannel : wicaChannels )
      {
         final Deque<T> channelData = stash.get( wicaChannel.getName().getControlSystemName() );
         if ( ( channelData == null ) || ( channelData.size() == 0 ) )
         {
            outputMap.put( wicaChannel, Optional.empty() );
         }
         else
         {
            outputMap.put(wicaChannel, Optional.of(channelData.getFirst()));
         }
      }
      return outputMap;
   }

   public Map<WicaChannel, List<T>> getLaterThan( Set<WicaChannel> wicaChannels, LocalDateTime since )
   {
      Validate.notNull( wicaChannels );
      Validate.notNull( since );

      return wicaChannels.stream().collect( Collectors.toUnmodifiableMap( c -> c , c-> this.getLaterThan( c.getName().getControlSystemName(), since ) ) );
   }


/*- Private methods ----------------------------------------------------------*/

   private List<T> getLaterThan( ControlSystemName controlSystemName, LocalDateTime since )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( since );

      final Queue<T> inputQueue = stash.get( controlSystemName );
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


   public void saveControlSystemDataPoint( ControlSystemName controlSystemName, T t )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( t );

      synchronized ( this )
      {
         // Lazily instantiate a Queue the first time a data point comes in
         // for a control system name that was not previously known.
         if ( ! stash.containsKey(controlSystemName) )
         {
            final Deque<T> deque = new ConcurrentLinkedDeque<>();
            stash.put( controlSystemName, deque);
         }

         // Evict the oldest value from the queue when it has reached its configured size limit.
         final Deque<T> deque = stash.get( controlSystemName );
         if ( deque.size() == bufferSize )
         {
            deque.remove();
         }

         // Now adds the new data point as the last item in the data queue.
         deque.addLast( t );
      }
   }


/*- Nested Classes -----------------------------------------------------------*/


}
