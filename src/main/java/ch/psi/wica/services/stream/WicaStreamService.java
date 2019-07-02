/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.WicaStreamConfigurationDecoder;
import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamId;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Service
public class WicaStreamService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Map<WicaStreamId,WicaStreamPublisher> wicaStreamPublisherMap = new HashMap<>();

   private final ControlSystemMonitoringService controlSystemMonitoringService;
   private final WicaStreamDataSupplier wicaStreamDataSupplier;

   private int streamsCreated;
   private int streamsDeleted;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamService( @Autowired ControlSystemMonitoringService controlSystemMonitoringService,
                             @Autowired WicaStreamDataSupplier wicaStreamDataSupplier )
   {
      this.controlSystemMonitoringService = Validate.notNull( controlSystemMonitoringService );
      this.wicaStreamDataSupplier = wicaStreamDataSupplier;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public int getStreamsCreated()
   {
      return streamsCreated;
   }

   public int getStreamsDeleted()
   {
      return streamsDeleted;
   }

   /**
    * Creates an activated wica stream based on the supplied configuration string.
    *
    * @param jsonStreamConfiguration the configuration string.
    * @return the returned stream.
    */
   public WicaStream create( String jsonStreamConfiguration )
   {
      Validate.notEmpty( jsonStreamConfiguration, "The 'jsonStreamConfiguration' argument was null." );

      final WicaStreamConfigurationDecoder decoder;
      try
      {
         decoder = new WicaStreamConfigurationDecoder( jsonStreamConfiguration );
      }
      catch( Exception ex)
      {
         throw new IllegalArgumentException( "The JSON configuration string '" + jsonStreamConfiguration + "' was invalid.", ex );
      }

      if ( decoder.getWicaChannels().size() == 0 )
      {
         throw new IllegalArgumentException( "The JSON configuration string did not define any channels.");
      }

      streamsCreated++;
      final WicaStreamId wicaStreamId = WicaStreamId.createNext();
      final WicaStream wicaStream = new WicaStream( wicaStreamId, decoder.getWicaStreamProperties(), decoder.getWicaChannels() );
      final WicaStreamPublisher wicaStreamPublisher = new WicaStreamPublisher( wicaStream, wicaStreamDataSupplier );

      // Lastly start monitoring all the channels of interest.
      controlSystemMonitoringService.startMonitoring( wicaStream );

      // Update the map of known fluxes
      wicaStreamPublisherMap.put( wicaStreamId, wicaStreamPublisher );
      return wicaStream;
   }

   /**
    * Deletes the Wica stream with specified id.
    *
    * @param wicaStreamId the Id of the stream to delete.
    */
   public void delete( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null." );
      Validate.isTrue(( isKnownId( wicaStreamId ) ),"The 'wicaStreamId' argument was not recognised."  );

      streamsDeleted++;

      final WicaStreamPublisher wicaStreamPublisher = wicaStreamPublisherMap.get( wicaStreamId );
      wicaStreamPublisher.shutdown();

      // Lastly stop monitoring all the channels of interest.
      controlSystemMonitoringService.stopMonitoring( wicaStreamPublisher.getStream() );

      wicaStreamPublisherMap.remove( wicaStreamId );
   }

   /**
    * Gets the combined flux for the stream with the specified id.
    *
    * @param wicaStreamId the id of the flux to fetch.
    * @return the combined flux.
    */
   public Flux<ServerSentEvent<String>> getFluxFromId( WicaStreamId wicaStreamId  )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null." );
      return wicaStreamPublisherMap.get( wicaStreamId ).getFlux();
   }

   /**
    * Returns an indication saying whether the specified id is recognised within the system.
    *
    * @param wicaStreamId the id.
    * @return the result.
    */
   public boolean isKnownId( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null." );
      return wicaStreamPublisherMap.containsKey( wicaStreamId );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
