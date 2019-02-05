/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.infrastructure.WicaStreamConfigurationDecoder;
import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamId;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WicaStreamManager
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private Map<WicaStreamId,WicaStream> map = new HashMap<>();

   @Value( "${wica.default_heartbeat_flux_interval:9999}" )
   private int defaultHeartBeatFluxInterval;

   @Value( "${wica.default_channel_value_update_flux_interval:99}" )
   private int defaultChannelValueUpdateFluxInterval;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
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
         throw new IllegalArgumentException( "The JSON configuration string was invalid.", ex );
      }

      if ( decoder.getWicaChannels().size() == 0 )
      {
         throw new IllegalArgumentException( "The JSON configuration string did not define any channels.");
      }

      final WicaStreamId wicaStreamId = WicaStreamId.createNext();
      final WicaStream stream = new WicaStream( wicaStreamId,
                                                decoder.getWicaStreamProperties(),
                                                decoder.getWicaChannels(),
                                                defaultHeartBeatFluxInterval,
                                                defaultChannelValueUpdateFluxInterval );

      map.put( stream.getWicaStreamId(), stream );
      return stream;
   }

   public WicaStream getFromId( WicaStreamId wicaStreamId  )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null" );
      return map.get( wicaStreamId );
   }

   public boolean isKnownId( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId, "The 'wicaStreamId' argument was null" );
      return map.containsKey( wicaStreamId );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
