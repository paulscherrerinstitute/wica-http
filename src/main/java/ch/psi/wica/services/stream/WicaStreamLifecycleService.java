/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.stream.WicaStreamConfigurationDecoder;
import ch.psi.wica.model.app.StatisticsCollectionService;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.services.channel.WicaChannelMetadataMapSerializerService;
import ch.psi.wica.services.channel.WicaChannelValueMapSerializerService;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@ThreadSafe
public class WicaStreamLifecycleService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamLifecycleService.class );

   private final Map<WicaStreamId, WicaStreamServerSentEventPublisher> wicaStreamPublisherMap = Collections.synchronizedMap( new HashMap<>() );

   private final WicaStreamConfigurationDecoder wicaStreamConfigurationDecoder;
   private final WicaStreamMonitoredValueRequesterService wicaStreamMonitoredValueRequesterService;
   private final WicaStreamPolledValueRequesterService wicaStreamPolledValueRequesterService;
   private final WicaStreamMetadataCollectorService wicaStreamMetadataCollectorService;
   private final WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService;
   private final WicaStreamPolledValueCollectorService wicaStreamPolledValueCollectorService;
   private final WicaChannelMetadataMapSerializerService wicaChannelMetadataMapSerializerService;
   private final WicaChannelValueMapSerializerService wicaChannelValueMapSerializerService;
   private final WicaStreamLifecycleStatistics wicaStreamLifecycleStatistics;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    *
    * @param wicaStreamConfigurationDecoder reference to the service that can decode the JSON stream configuration.
    *
    * @param wicaStreamMonitoredValueRequesterService reference to the service used to request the monitored
    *     *        values for the stream.
    *
    * @param wicaStreamPolledValueRequesterService reference to the service used to request the polled
    *        values for the stream.
    *
    * @param wicaStreamMetadataCollectorService reference to the service which collects metadata information
    *        for the stream.
    *
    * @param wicaStreamMonitoredValueCollectorService reference to the service which collects the monitored
    *        values for the stream.
    *
    * @param wicaStreamPolledValueCollectorService reference to the service which collects the polled
    *        values for the stream.
    * @param statisticsCollectionService an object which will collect the statistics associated with
    *        this class instance.
    * @param wicaChannelMetadataMapSerializerService reference to the service that serializes the metadata map.
    * @param wicaChannelValueMapSerializerService reference to the service that serializes the value map.
    */
   public WicaStreamLifecycleService( @Autowired WicaStreamConfigurationDecoder wicaStreamConfigurationDecoder,
                                      @Autowired WicaStreamMonitoredValueRequesterService wicaStreamMonitoredValueRequesterService,
                                      @Autowired WicaStreamPolledValueRequesterService wicaStreamPolledValueRequesterService,
                                      @Autowired WicaStreamMetadataCollectorService wicaStreamMetadataCollectorService,
                                      @Autowired WicaStreamMonitoredValueCollectorService wicaStreamMonitoredValueCollectorService,
                                      @Autowired WicaStreamPolledValueCollectorService wicaStreamPolledValueCollectorService,
                                      @Autowired WicaChannelMetadataMapSerializerService wicaChannelMetadataMapSerializerService,
                                      @Autowired WicaChannelValueMapSerializerService wicaChannelValueMapSerializerService,
                                      @Autowired StatisticsCollectionService statisticsCollectionService
   )
   {
      this.wicaStreamConfigurationDecoder = wicaStreamConfigurationDecoder;
      this.wicaStreamMonitoredValueRequesterService = Validate.notNull( wicaStreamMonitoredValueRequesterService);
      this.wicaStreamPolledValueRequesterService = Validate.notNull(wicaStreamPolledValueRequesterService);
      this.wicaStreamMetadataCollectorService = wicaStreamMetadataCollectorService;
      this.wicaStreamMonitoredValueCollectorService = wicaStreamMonitoredValueCollectorService;
      this.wicaStreamPolledValueCollectorService = wicaStreamPolledValueCollectorService;
      this.wicaChannelMetadataMapSerializerService = wicaChannelMetadataMapSerializerService;
      this.wicaChannelValueMapSerializerService = wicaChannelValueMapSerializerService;

      this.wicaStreamLifecycleStatistics = new WicaStreamLifecycleStatistics("WICA STREAM LIFECYCLE SERVICE" );
      statisticsCollectionService.addCollectable( wicaStreamLifecycleStatistics );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Creates and returns an active wica stream based on the supplied JSON
    * stream configuration string.
    *
    * @param jsonStreamConfiguration the configuration string.
    * @return the returned stream.
    */
   public WicaStream create( String jsonStreamConfiguration )
   {
      Validate.notEmpty( jsonStreamConfiguration, "The 'jsonStreamConfiguration' argument was null." );

      logger.info( "Creating new stream from config string: '{}'", jsonStreamConfiguration );

      // To ensure consistency the following operations are performed as a single atomic operation.
      synchronized( this )
      {
         // Attempt to decode the stream configuration.
         final StopWatch streamDecodeTimer = StopWatch.createStarted();

         final WicaStream wicaStream;
         try
         {
            wicaStream = wicaStreamConfigurationDecoder.decode( jsonStreamConfiguration);
         }
         catch ( Exception ex )
         {
            throw new IllegalArgumentException("The JSON configuration string '" + jsonStreamConfiguration + "' was invalid.", ex);
         }

         if ( wicaStream.getWicaChannels().size() == 0 )
         {
            throw new IllegalArgumentException("The JSON configuration string did not define any channels.");
         }

         final long streamDecodeTimeInMillis = streamDecodeTimer.getTime();
         logger.info("Stream decoding took: '{}' ms.,", streamDecodeTimeInMillis );

         logger.info( "Stream created OK from config string: '{}.'", wicaStream );

         // Tell the control system monitoring service to start monitoring
         // and/or polling the  control system channels in this stream.
         final StopWatch startMonitoringTimer = StopWatch.createStarted();
         wicaStreamMonitoredValueRequesterService.startMonitoring( wicaStream );
         wicaStreamPolledValueRequesterService.startPolling( wicaStream );
         final long startMonitoringTimeInMicroseconds = startMonitoringTimer.getTime(TimeUnit.MICROSECONDS );
         logger.info("Stream monitoring took: '{}' us.", startMonitoringTimeInMicroseconds);

         // Create a new publisher and add it to the map of recognized publishers.
         // Note:publication will not begin until there is at least one active subscriber.
         final var wicaStreamServerSentEventPublisher = new WicaStreamServerSentEventPublisher( wicaStream,
                                                                                                wicaStreamMetadataCollectorService,
                                                                                                wicaStreamMonitoredValueCollectorService,
                                                                                                wicaStreamPolledValueCollectorService,
                                                                                                wicaChannelMetadataMapSerializerService,
                                                                                                wicaChannelValueMapSerializerService);

         wicaStreamPublisherMap.put( wicaStream.getWicaStreamId(), wicaStreamServerSentEventPublisher );

         // Lastly increase the count of created streams.
         wicaStreamLifecycleStatistics.incrementStreamsCreated();

         // Return a reference to the newly created stream.
         return wicaStream;
      }
   }

   /**
    * Deletes the wica stream with the specified id.
    *
    * @param wicaStreamId the Id of the stream to delete.
    *
    * @throws NullPointerException if the stream ID was null.
    * @throws IllegalStateException if the stream ID was not recognised.
    */
   public void delete( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null." );
      Validate.isTrue(( isKnown(wicaStreamId ) ), "The 'wicaStreamId' argument was not recognised."  );

      // To ensure consistency the following operations are performed as a single atomic operation.
      synchronized( this )
      {
         // Shutdown the information publisher associated with this stream.
         final WicaStreamServerSentEventPublisher wicaStreamServerSentEventPublisher = wicaStreamPublisherMap.get( wicaStreamId );
         wicaStreamServerSentEventPublisher.shutdown();

         // Tell the control system monitoring service that we are no longer
         // interested in this stream.
         final WicaStream wicaStream = wicaStreamServerSentEventPublisher.getStream();
         wicaStreamMonitoredValueRequesterService.stopMonitoring(wicaStream ) ;
         wicaStreamPolledValueRequesterService.stopPolling(wicaStream ) ;

         // Remove the stream's ID from the list of recognised publishers.
         wicaStreamPublisherMap.remove( wicaStreamId );

         // Lastly increase the count of deleted streams.
         wicaStreamLifecycleStatistics.incrementStreamsDeleted();
      }
   }

   /**
    * Restarts the control system monitoring on the wica stream with the specified ID.
    *
    * @param wicaStreamId the ID of the stream to delete.
    *
    * @throws NullPointerException if the 'wicaStreamId' argument was null.
    * @throws IllegalStateException if the 'wicaStreamId' argument was not recognised.
    */
   public void restartMonitoring( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null." );
      Validate.isTrue(( isKnown(wicaStreamId ) ), "The 'wicaStreamId' argument was not recognised."  );

      // Get a reference to the stream
      final WicaStreamServerSentEventPublisher wicaStreamServerSentEventPublisher = wicaStreamPublisherMap.get( wicaStreamId );
      final WicaStream wicaStream = wicaStreamServerSentEventPublisher.getStream();

      // Now invoke the stream monitoring restart feature.
      wicaStreamMonitoredValueRequesterService.restartMonitoring( wicaStream );
   }

   /**
    * Gets the publication flux for the stream with the specified id.
    *
    * @param wicaStreamId the id of the flux to fetch.
    * @return the combined flux.
    */
   public Flux<ServerSentEvent<String>> getFlux( WicaStreamId wicaStreamId  )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null." );
      return wicaStreamPublisherMap.get( wicaStreamId ).getFlux();
   }

   /**
    * Returns an flag saying whether the specified id is recognised within the system.
    *
    * @param wicaStreamId the id of the stream to check.
    * @return the result.
    */
   public boolean isKnown( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null." );
      return wicaStreamPublisherMap.containsKey( wicaStreamId );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
