/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.epics.EpicsChannelDataService;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.lang.instrument.UnmodifiableClassException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
class WicaStreamDataSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private LocalDateTime lastPublicationTime = LONG_AGO;

   private final WicaStream wicaStream;
   private final EpicsChannelDataService epicsChannelDataService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaStreamDataSupplier( WicaStream wicaStream, EpicsChannelDataService epicsChannelDataService )
   {
      this.wicaStream = wicaStream;
      this.epicsChannelDataService = epicsChannelDataService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   Map<WicaChannelName, WicaChannelMetadata> getMetadataMap()
   {
      return epicsChannelDataService.getChannelMetadata( wicaStream.getWicaChannels() );
   }

   Map<WicaChannelName, List<WicaChannelValue>> getValueMapAll()
   {
      final var updatedChannelValueMap = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );

      // Validate the precondition that every channel in the stream is represented in the returned map.
      Validate.isTrue( wicaStream.getWicaChannels().stream()
            .allMatch( c -> updatedChannelValueMap.containsKey( c.getName() ) ),
            "Programming Error: the EpicsChannelDataService did not know about" );

      return buildMap( updatedChannelValueMap );
   }

   Map<WicaChannelName, List<WicaChannelValue>> getValueMapLatest()
   {
      final var latestChannelValueMap = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), getLastPublicationTime() );
      updateLastPublicationTime();

      return buildMap( latestChannelValueMap );
   }

/*- Private methods ----------------------------------------------------------*/

   private Map<WicaChannelName, List<WicaChannelValue>> buildMap(  Map<WicaChannelName, List<WicaChannelValue>> inputMap )
   {
      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();

      for ( WicaChannel channel : wicaStream.getWicaChannels() )
      {
         final WicaChannelName channelName = channel.getName();
         if ( inputMap.containsKey( channelName) )
         {
            final List<WicaChannelValue> channelOutputList = channel.map( inputMap.get( channelName ) );
            if ( channelOutputList.size() > 0 )
            {
               outputMap.put( channelName, channelOutputList );
            }
         }
      }
      return Collections.unmodifiableMap( outputMap );
   }


   /**
    * Returns the last time the stream was published.
    *
    * @return the timestamp
    */
   private LocalDateTime getLastPublicationTime()
   {
      return lastPublicationTime;
   }

   /**
    * Sets with the current time the timestamp which indicates the last time
    * the stream was published.
    */
   private void updateLastPublicationTime()
   {
      lastPublicationTime = LocalDateTime.now();
   }

/*- Nested Classes -----------------------------------------------------------*/

}
