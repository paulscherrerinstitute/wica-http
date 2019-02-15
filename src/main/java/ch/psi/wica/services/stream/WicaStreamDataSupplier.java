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
   private final WicaChannelProperties.DaqType defaultChannelDaqType;
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
      this.defaultChannelDaqType = wicaStream.getWicaStreamProperties().gettDaqType();
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
    * Returns a map of all channels and their most recent values obtained
    * by polling.
    *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @return the map.
    */
   Map<WicaChannelName,List<WicaChannelValue>> getPolledValues()
   {
      final var map = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );
      final var outputMap = new HashMap<WicaChannelName,List<WicaChannelValue>>();
      for ( WicaChannelName wicaChannelName : map.keySet() )
      {
         final var wicaChannelValue = map.get( wicaChannelName ).get( 0 );
         final var rewriter = new WicaChannelValueTimestampRewriter();
         final var newValue = rewriter.rewrite( wicaChannelValue, LocalDateTime.now() );
         outputMap.put( wicaChannelName, List.of( newValue ) );
      }
      return filter( outputMap, WicaChannelProperties.DaqType.POLLER );
   }

   /**
    * Returns a map of all channels and their most recent buffered values
    * received through notification from the channel's data source.
    *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @return the map.
    */
   Map<WicaChannelName, List<WicaChannelValue>> getNotifiedValues()
   {
      final var updatedChannelValueMap = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );

      // Validate the precondition that every channel in the stream is represented in the returned apply.
      Validate.isTrue( wicaStream.getWicaChannels().stream()
            .allMatch( c -> updatedChannelValueMap.containsKey( c.getName() ) ),
            "Programming Error: the EpicsChannelDataService did not know about" );

      return filter( updatedChannelValueMap, WicaChannelProperties.DaqType.MONITORER );
   }

   /**
    * Returns a map of all channels whose buffered notification values have changed
    * since the last time this method was invoked.
    *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @return the map of channels and list of changes that have occurred since the
    *     last time this method was invoked.
    */
   Map<WicaChannelName, List<WicaChannelValue>> getNotifiedValueChanges()
   {
      final var latestChannelValueMap = epicsChannelDataService.getLaterThan( wicaStream.getWicaChannels(), getLastPublicationTime() );
      updateLastPublicationTime();

      return filter( latestChannelValueMap, WicaChannelProperties.DaqType.MONITORER );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Transforms the supplied input apply according to the filtering rules
    * defined for each channel.
    *
    * @param inputMap the apply to transform.
    * @return the transformed apply.
    */
   private Map<WicaChannelName, List<WicaChannelValue>> filter( Map<WicaChannelName, List<WicaChannelValue>> inputMap,
                                                                WicaChannelProperties.DaqType targetDaqType )
   {
      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();

      for ( WicaChannel channel : wicaStream.getWicaChannels() )
      {
         final WicaChannelName channelName = channel.getName();

         if ( inputMap.containsKey( channelName ) && getChannelDaqType( channel).equals( targetDaqType ) )
         {
            // For each channel use the defined filtering function to take the supplied
            // input map and transform it to the output format.
            final List<WicaChannelValue> channelOutputList = channel.applyFilter( inputMap.get( channelName ) );

            // Only return map entries for channels which actually have information
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

   /**
    * Returns the Daqtype for the specified channel, either directly from the
    * channel properties, or when, not available from the properties of the
    * WicaStream.
    *
    * @param wicaChannel the channel to check.
    * @return the result.
    */
   private WicaChannelProperties.DaqType getChannelDaqType( WicaChannel wicaChannel )
   {
      final WicaStreamProperties streamProperties = wicaStream.getWicaStreamProperties();
      final WicaChannelProperties channelProperties = wicaChannel.getProperties();

      return channelProperties.getDaqType().isPresent() ?
             channelProperties.getDaqType().get() : streamProperties.gettDaqType();
   }

/*- Nested Classes -----------------------------------------------------------*/

}
