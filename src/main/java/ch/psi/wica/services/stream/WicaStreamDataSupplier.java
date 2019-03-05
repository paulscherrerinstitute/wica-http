/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
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
class WicaStreamDataSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private final WicaChannelMetadataStash wicaChannelMetadataStash;
   private final WicaChannelValueStash wicaChannelValueStash;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which can supply metadata and value information
    * about all channels in the specified stream.
    *
    * @param wicaChannelMetadataStash the stash of received metadata.
    * @param wicaChannelValueStash the stash of received values.
    */
   WicaStreamDataSupplier( @Autowired WicaChannelMetadataStash wicaChannelMetadataStash,
                           @Autowired WicaChannelValueStash wicaChannelValueStash )
   {
      this.wicaChannelMetadataStash = wicaChannelMetadataStash;
      this.wicaChannelValueStash = wicaChannelValueStash;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a map of all channels and their associated metadata.
    *
    * @param wicaStream the stream.
    * @return the apply.
    */
   Map<WicaChannelName, WicaChannelMetadata> getMetadataMap( WicaStream wicaStream)
   {
      return wicaChannelMetadataStash.get( wicaStream.getWicaChannels() );
   }

   /**
    * Returns a map of all channels and their associated values.
    *
    * @param wicaStream the stream.
    * @return the apply.
    */
   Map<WicaChannelName,List<WicaChannelValue>> getValueMap( WicaStream wicaStream)
   {
      return wicaChannelValueStash.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );
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
   Map<WicaChannelName,List<WicaChannelValue>> getPolledValues( WicaStream wicaStream )
   {
      // Poll the stash of cached values to get the notified values for each channel.
      final var latestChannelValueMap = wicaChannelValueStash.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );

      // Produce a map which includes the last notified value for each channel, but with
      // the timestamp information rewritten to reflect that the time is NOW.
      final var lastChannelValueMap = new HashMap<WicaChannelName,List<WicaChannelValue>>();
      for ( WicaChannelName wicaChannelName : latestChannelValueMap.keySet() )
      {
         final int latestEntryIndex = latestChannelValueMap.get( wicaChannelName ).size() - 1;
         final var wicaChannelValue = latestChannelValueMap.get( wicaChannelName ).get( latestEntryIndex );
         final var rewriter = new WicaChannelValueTimestampRewriter();
         final var newValue = rewriter.rewrite( wicaChannelValue, LocalDateTime.now() );
         lastChannelValueMap.put( wicaChannelName, List.of( newValue ) );
      }

      // Filter the map to only include entries where the channel's data acquisition mode
      // includes polling and where the configured polling interval indicates it is time
      // to obtain a new value.
      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter( c -> lastChannelValueMap.containsKey( c.getName() ) )
            .filter( c -> getChannelDataAcquisitionMode( wicaStream, c ).doesPolling() )
            .forEach( c -> {
               final var outputList = c.applyFilterForPolledChannels( lastChannelValueMap.get( c.getName() ) );
               if ( outputList.size() > 0 )
               {
                  outputMap.put( c.getName(), outputList );
               }
            } );

      return Collections.unmodifiableMap( outputMap );
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
   Map<WicaChannelName, List<WicaChannelValue>> getNotifiedValues( WicaStream wicaStream )
   {
      final var updatedChannelValueMap = wicaChannelValueStash.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );

      // Validate the precondition that every channel in the stream is represented in the returned map.
      Validate.isTrue( wicaStream.getWicaChannels().stream()
            .allMatch( c -> updatedChannelValueMap.containsKey( c.getName() ) ),
            "Programming Error: the EpicsChannelDataService did not know about one or more channels." );

      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter( c -> getChannelDataAcquisitionMode( wicaStream, c ).doesMonitoring() )
            .forEach( c -> {
               var outputList = c.applyFilterForMonitoredChannels( updatedChannelValueMap.get( c.getName() ) );
               if ( outputList.size() > 0 )
               {
                  outputMap.put( c.getName(), outputList );
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
   Map<WicaChannelName, List<WicaChannelValue>> getNotifiedValueChanges( WicaStream wicaStream)
   {
      final var latestChannelValueMap = wicaChannelValueStash.getLaterThan( wicaStream.getWicaChannels(), wicaStream.getLastPublicationTime() );
      wicaStream.updateLastPublicationTime();

      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter( c -> latestChannelValueMap.containsKey( c.getName() ) )
            .filter( c -> getChannelDataAcquisitionMode( wicaStream, c ).doesMonitoring() )
            .forEach( c -> {
               var outputList = c.applyFilterForMonitoredChannels( latestChannelValueMap.get( c.getName() ) );
               if ( outputList.size() > 0 )
               {
                  outputMap.put( c.getName(), outputList );
               }
            } );

      return Collections.unmodifiableMap( outputMap );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Returns the Daqmode for the specified channel, either directly from the
    * channel properties, or when, not available from the properties of the
    * WicaStream.
    *
    * @param wicaStream the stream.
    * @param wicaChannel the channel to check.
    * @return the result.
    */
   private WicaChannelProperties.DataAcquisitionMode getChannelDataAcquisitionMode( WicaStream wicaStream, WicaChannel wicaChannel )
   {
      final WicaStreamProperties streamProperties = wicaStream.getWicaStreamProperties();
      final WicaChannelProperties channelProperties = wicaChannel.getProperties();

      return channelProperties.getDataAcquisitionMode().isPresent() ?
             channelProperties.getDataAcquisitionMode().get() : streamProperties.getDataAcquisitionMode();
   }

/*- Nested Classes -----------------------------------------------------------*/

}
