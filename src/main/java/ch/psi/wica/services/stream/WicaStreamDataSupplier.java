/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import ch.psi.wica.services.epics.EpicsChannelDataService;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.util.*;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
class WicaStreamDataSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private final WicaStream wicaStream;
   private final EpicsChannelDataService epicsChannelDataService;

   private LocalDateTime lastPublicationTime = LONG_AGO;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which can supply metadata and value information
    * about all channels in the specified stream.
    *
    * @param wicaStream the stream which specifies the channels of interest.
    * @param epicsChannelDataService reference to the provider of a service
    */
   WicaStreamDataSupplier( WicaStream wicaStream, EpicsChannelDataService epicsChannelDataService )
   {
      this.wicaStream = wicaStream;
      this.epicsChannelDataService = epicsChannelDataService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a apply of all channels and associated metadata.
    *
    * @return the apply.
    */
   Map<WicaChannelName, WicaChannelMetadata> getMetadataMap()
   {
      return epicsChannelDataService.getChannelMetadata( wicaStream.getWicaChannels() );
   }

   /**
    * Returns a apply of all channels and their most recent buffered values.
    *
    * @return the apply
    */
   Map<WicaChannelName, List<WicaChannelValue>> getValueMapAll()
   {
      final var updatedChannelValueMap = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );

      // Validate the precondition that every channel in the stream is represented in the returned apply.
      Validate.isTrue( wicaStream.getWicaChannels().stream()
            .allMatch( c -> updatedChannelValueMap.containsKey( c.getName() ) ),
            "Programming Error: the EpicsChannelDataService did not know about" );

      return transformStreamValues(updatedChannelValueMap );
   }

   /**
    * Returns a apply of all channels whose values have changed since the last time
    * this method was invoked.
    *
    * @return the apply of channels and any changes that have occurred since the last
    *     time this method was invoked.
    */
   Map<WicaChannelName, List<WicaChannelValue>> getValueChanges()
   {
      final var latestChannelValueMap = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), getLastPublicationTime() );
      updateLastPublicationTime();

      return transformStreamValues(latestChannelValueMap );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Transforms the supplied input apply according to the filtering rules
    * defined for each channel.
    *
    * @param inputMap the apply to transform.
    * @return the transformed apply.
    */
   private Map<WicaChannelName, List<WicaChannelValue>> transformStreamValues( Map<WicaChannelName, List<WicaChannelValue>> inputMap )
   {
      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();

      for ( WicaChannel channel : wicaStream.getWicaChannels() )
      {
         final WicaChannelName channelName = channel.getName();
         if ( inputMap.containsKey( channelName) )
         {
            // For each channel use the defined mapping function to take the supplied
            // input apply and transform it to the output format.
            final List<WicaChannelValue> channelOutputList = channel.apply(inputMap.get(channelName ) );

            // Only return apply entries for channels which actually have information
            // following the transformation above.
            if ( channelOutputList.size() > 0 )
            {
               outputMap.put( channelName, channelOutputList );
            }
         }
      }
      return Collections.unmodifiableMap( outputMap );
   }

   /**
    * Returns the timestamp for the last time the values from the
    * stream were published.
    *
    * @return the timestamp.
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
