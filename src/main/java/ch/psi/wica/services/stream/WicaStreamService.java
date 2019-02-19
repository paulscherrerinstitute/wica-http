/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.WicaStreamConfigurationDecoder;
import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamId;
import ch.psi.wica.services.epics.EpicsChannelDataService;
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

   private final Map<WicaStreamId,Flux<ServerSentEvent<String>>> wicaSteamFluxMap = new HashMap<>();
   private final EpicsChannelDataService epicsChannelDataService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamService( @Autowired EpicsChannelDataService epicsChannelDataService )
   {
      this.epicsChannelDataService = epicsChannelDataService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaStream create( String jsonStreamConfiguration )
   {
      Validate.notEmpty( jsonStreamConfiguration, "The 'jsonStreamConfiguration' argument was null" );

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

      final WicaStreamId wicaStreamId = WicaStreamId.createNext();
      final WicaStream wicaStream = new WicaStream( wicaStreamId, decoder.getWicaStreamProperties(), decoder.getWicaChannels() );

      final WicaStreamDataSupplier wicaStreamDataSupplier = new WicaStreamDataSupplier( wicaStream, epicsChannelDataService );
      final WicaStreamPublisher wicaStreamPublisher = new WicaStreamPublisher( wicaStream, wicaStreamDataSupplier );
      wicaStreamPublisher.activate();

      // Lastly set up monitors on all the channels of interest.
      epicsChannelDataService.startMonitoring( wicaStream.getWicaChannels() );

      // Update the apply of known fluxes
      wicaSteamFluxMap.put( wicaStreamId, wicaStreamPublisher.getFlux() );
      return wicaStream;
   }

   public void delete( WicaStreamId wicaStreamId )
   {
    // TODO to implement
   }

   public Flux<ServerSentEvent<String>> getFromId( WicaStreamId wicaStreamId  )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null" );
      return wicaSteamFluxMap.get( wicaStreamId );
   }

   public boolean isKnownId( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null" );
      return wicaSteamFluxMap.containsKey( wicaStreamId );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
