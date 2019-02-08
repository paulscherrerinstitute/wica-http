/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.epics.EpicsChannelDataService;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaStreamDataSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private LocalDateTime lastPublicationTime;

   private final WicaStream wicaStream;
   private final EpicsChannelDataService epicsChannelDataService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamDataSupplier( WicaStream wicaStream, EpicsChannelDataService epicsChannelDataService )
   {
      this.wicaStream = wicaStream;
      this.epicsChannelDataService = epicsChannelDataService;


    //  this.channelMap = Collections.unmodifiableMap( wicaChannels.stream().collect(Collectors.toConcurrentMap(WicaChannel::getName, c -> c ) ) );
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
      return updatedChannelValueMap;
   }

   Map<WicaChannelName, List<WicaChannelValue>> getValueMapLatest()
   {
      final var latestChannelValueMap = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), getLastPublicationTime() );
      updateLastPublicationTime();
      return latestChannelValueMap;
   }


/*- Private methods ----------------------------------------------------------*/


//   /**
//    *
//    * @param inputMap
//    * @return
//    */
//   public Map<WicaChannelName,List<WicaChannelValue>> map( Map<WicaChannelName,List<WicaChannelValue>> inputMap )
//   {
//      Validate.isTrue(inputMap.keySet().stream().allMatch( channelMap::containsKey ), "One or more channels in the inputMap were unknown" );
//
//      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
//      inputMap.keySet().forEach( c -> {
//         final List<WicaChannelValue> inputList = inputMap.get( c );
//         final WicaChannel wicaChannel = this.channelMap.get(c );
//         final List<WicaChannelValue> outputList = wicaChannel.map( inputList );
//         if ( outputList.size() > 0 ) {
//            outputMap.put( c, outputList );
//         }
//      } );
//
//      return outputMap;
//   }


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
