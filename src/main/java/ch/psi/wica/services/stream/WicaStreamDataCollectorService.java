/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelMetadataBufferingService;
import ch.psi.wica.controlsystem.event.WicaChannelValueBufferingService;
import ch.psi.wica.model.channel.*;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.services.channel.WicaChannelValueFilteringService;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Service
@Immutable
class WicaStreamDataCollectorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private final WicaChannelMetadataBufferingService wicaChannelMetadataBufferingService;
   private final WicaChannelValueBufferingService wicaChannelValueBufferingService;
   private final WicaChannelValueFilteringService wicaChannelValueFilteringService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which can supply metadata and value information
    * about all channels in the specified stream.
    *
    * @param wicaChannelMetadataBufferingService the stash of received metadata.
    * @param wicaChannelValueBufferingService the stash of received values.
    */
   WicaStreamDataCollectorService( @Autowired WicaChannelMetadataBufferingService wicaChannelMetadataBufferingService,
                                   @Autowired WicaChannelValueBufferingService wicaChannelValueBufferingService,
                                   @Autowired WicaChannelValueFilteringService wicaChannelValueFilteringService )
   {
      this.wicaChannelMetadataBufferingService = wicaChannelMetadataBufferingService;
      this.wicaChannelValueBufferingService = wicaChannelValueBufferingService;
      this.wicaChannelValueFilteringService = wicaChannelValueFilteringService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a map of all channels and their associated metadata.
    *
    * @param wicaStream the stream.
    * @return the map.
    */
   Map<WicaChannel, WicaChannelMetadata> getMetadataMap( WicaStream wicaStream)
   {
      return wicaChannelMetadataBufferingService.get( wicaStream.getWicaChannels() );
   }

   /**
    * Returns a map of all channels and the most recent N values that have been
    * received for them, where N is the size of the internal received value
    * notification buffer.
    *
    * @param wicaStream the stream.
    * @return the map.
    */
   Map<WicaChannel,List<WicaChannelValue>> getInitialValueMap( WicaStream wicaStream)
   {
      return this.getUnfilteredValueMap( wicaStream );
   }

   /**
    * Returns a map of all channels and their most recent values obtained by POLLING
    * (either the data source directly, or the most recently cached value obtained
    * through notification).
    *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @param wicaStream the stream.
    * @return the map.
    */
   Map<WicaChannel,List<WicaChannelValue>> getPolledValueMap( WicaStream wicaStream )
   {
      // Poll the stash of cached values to get the notified values for each channel.
      final var latestChannelValueMap = wicaChannelValueBufferingService.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );

      // Produce a map which includes the last notified value for each channel, but with
      // the timestamp information rewritten to reflect that the time is NOW.
      final var lastChannelValueMap = new HashMap<WicaChannel,List<WicaChannelValue>>();
      for ( WicaChannel wicaChannel : latestChannelValueMap.keySet() )
      {
         final int latestEntryIndex = latestChannelValueMap.get( wicaChannel ).size() - 1;
         final var wicaChannelValue = latestChannelValueMap.get( wicaChannel ).get( latestEntryIndex );
         final var rewriter = new WicaChannelValueTimestampRewriter();
         final var newValue = rewriter.rewrite( wicaChannelValue, LocalDateTime.now() );
         lastChannelValueMap.put( wicaChannel, List.of( newValue ) );
      }

      // Filter the map to only include entries where the channel's data acquisition mode
      // includes polling and where the configured polling interval indicates it is time
      // to obtain a new value.
      final Map<WicaChannel,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter(lastChannelValueMap::containsKey)
            .filter( chan -> chan.getProperties().getDataAcquisitionMode().doesPolling() )
            .forEach( chan -> {
               final var outputList = wicaChannelValueFilteringService.filterPolledValues( chan, lastChannelValueMap.get( chan ) );
               if ( outputList.size() > 0 )
               {
                  outputMap.put( chan, outputList );
               }
            } );

      return Collections.unmodifiableMap( outputMap );
    }


   /**
    * Returns a map of all channels whose MONITORED values have changed since the
    * last time this method was invoked.
    *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @param wicaStream the stream.
    * @return the map of channels and list of changes that have occurred since the
    *     last time this method was invoked.
    */
   Map<WicaChannel, List<WicaChannelValue>> getChangedValueMap( WicaStream wicaStream)
   {
      final var latestChannelValueMap = wicaChannelValueBufferingService.getLaterThan( wicaStream.getWicaChannels(), wicaStream.getLastPublicationTime() );
      wicaStream.updateLastPublicationTime();

      final Map<WicaChannel,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter(latestChannelValueMap::containsKey)
            .filter( chan -> chan.getProperties().getDataAcquisitionMode().doesMonitoring() )
            .forEach( chan -> {
               var outputList = wicaChannelValueFilteringService.filterMonitoredValues( chan, latestChannelValueMap.get(chan ) );
               if ( outputList.size() > 0 )
               {
                  outputMap.put( chan, outputList );
               }
            } );

      return Collections.unmodifiableMap( outputMap );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Returns a map of all channels and the most recent N values that have been
    * received for them, where N is the size of the internal received value
    * notification buffer.
    *
    * @param wicaStream the stream.
    * @return the map.
    */
   private Map<WicaChannel,List<WicaChannelValue>> getUnfilteredValueMap( WicaStream wicaStream)
   {
      return wicaChannelValueBufferingService.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );
   }


   /**
    * Returns a map of all channels and their most recent buffered values obtained
    * through MONITORING the channel's data source.
    *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @param wicaStream the stream.
    * @return the map.
    */
   private Map<WicaChannel, List<WicaChannelValue>> getFilteredMonitoredValueMap( WicaStream wicaStream )
   {
      final Map<WicaChannel, List<WicaChannelValue>> updatedChannelValueMap = wicaChannelValueBufferingService.getLaterThan(wicaStream.getWicaChannels(), LONG_AGO );

      // Validate the precondition that every channel in the stream is represented in the returned map.
      Validate.isTrue( wicaStream.getWicaChannels().stream()
                             .allMatch(updatedChannelValueMap::containsKey),"Programming Error: the EpicsChannelDataService did not know about one or more channels." );

      final Map<WicaChannel,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter( chan -> chan.getProperties().getDataAcquisitionMode().doesMonitoring() )
            .forEach( chan -> {
               var outputList = wicaChannelValueFilteringService.filterMonitoredValues( chan, updatedChannelValueMap.get(chan ) );
               if ( outputList.size() > 0 )
               {
                  outputMap.put( chan, outputList );
               }
            } );

      return Collections.unmodifiableMap( outputMap );
   }

   /*- Nested Classes -----------------------------------------------------------*/

}
