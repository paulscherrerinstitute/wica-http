/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
class WicaStreamDataSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );

   private final WicaStream wicaStream;
   private final WicaChannelMetadataStash wicaChannelMetadataStash;
   private final WicaChannelValueStash wicaChannelValueStash;

   private LocalDateTime lastPublicationTime = LONG_AGO;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which can supply metadata and value information
    * about all channels in the specified stream.
    *
    * @param wicaStream the stream which specifies the channels of interest.
    * @param wicaChannelMetadataStash the stash of received metadata.
    * @param wicaChannelValueStash the stash of received values.
    */
   WicaStreamDataSupplier( WicaStream wicaStream,
                           WicaChannelMetadataStash wicaChannelMetadataStash,
                           WicaChannelValueStash wicaChannelValueStash )
   {
      this.wicaStream = wicaStream;
      this.wicaChannelMetadataStash = wicaChannelMetadataStash;
      this.wicaChannelValueStash = wicaChannelValueStash;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a map of all channels and their associated metadata.
    *
    * @return the apply.
    */
   Map<WicaChannelName, WicaChannelMetadata> getMetadataMap()
   {
      return wicaChannelMetadataStash.get( wicaStream.getWicaChannels() );
   }

   /**
    * Returns a map of all channels and their most recent values obtained by POLLING
    * (either the data source directly, or the most recently cached value obtained
    * through notification).
    *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @return the map.
    */
   Map<WicaChannelName,List<WicaChannelValue>> getPolledValues()
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
            .filter( c -> getChannelDataAcquisitionMode( c ).doesPolling() )
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
    * *
    * Note: following data acquisition the returned map will be FILTERED according
    * to the rules defined for each channel.
    *
    * @return the map.
    */
   Map<WicaChannelName, List<WicaChannelValue>> getNotifiedValues()
   {
      final var updatedChannelValueMap = wicaChannelValueStash.getLaterThan( wicaStream.getWicaChannels(), LONG_AGO );

      // Validate the precondition that every channel in the stream is represented in the returned map.
      Validate.isTrue( wicaStream.getWicaChannels().stream()
            .allMatch( c -> updatedChannelValueMap.containsKey( c.getName() ) ),
            "Programming Error: the EpicsChannelDataService did not know about one or more channels." );

      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter( c -> getChannelDataAcquisitionMode( c ).doesMonitoring() )
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
    * @return the map of channels and list of changes that have occurred since the
    *     last time this method was invoked.
    */
   Map<WicaChannelName, List<WicaChannelValue>> getNotifiedValueChanges()
   {
      final var latestChannelValueMap = wicaChannelValueStash.getLaterThan( wicaStream.getWicaChannels(), getLastPublicationTime() );
      updateLastPublicationTime();

      final Map<WicaChannelName,List<WicaChannelValue>> outputMap = new HashMap<>();
      wicaStream.getWicaChannels().stream()
            .filter( c -> latestChannelValueMap.containsKey( c.getName() ) )
            .filter( c -> getChannelDataAcquisitionMode( c ).doesMonitoring() )
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
    * Returns the Daqmode for the specified channel, either directly from the
    * channel properties, or when, not available from the properties of the
    * WicaStream.
    *
    * @param wicaChannel the channel to check.
    * @return the result.
    */
   private WicaChannelProperties.DataAcquisitionMode getChannelDataAcquisitionMode( WicaChannel wicaChannel )
   {
      final WicaStreamProperties streamProperties = wicaStream.getWicaStreamProperties();
      final WicaChannelProperties channelProperties = wicaChannel.getProperties();

      return channelProperties.getDataAcquisitionMode().isPresent() ?
             channelProperties.getDataAcquisitionMode().get() : streamProperties.getDataAcquisitionMode();
   }

/*- Nested Classes -----------------------------------------------------------*/

}
