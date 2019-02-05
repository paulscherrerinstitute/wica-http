/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaStream;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelDataNumericScaleSupplier implements  WicaChannelValueMapSerializer.NumericScaleSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamConfigurationDecoder.class );
   private final WicaStream wicaStream;

   private final Map<WicaChannelName, Integer> map = new HashMap<>();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelDataNumericScaleSupplier( WicaStream wicaStream )
   {
      this.wicaStream = Validate.notNull(wicaStream );
      addWicaStreamDefaultValues();
      addWicaChannelPropertiesOverrides();
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public int supplyForChannelNamed( WicaChannelName wicaChannelName)
   {
      return map.get( wicaChannelName );
   }


/*- Private methods ----------------------------------------------------------*/

   private void addWicaStreamDefaultValues()
   {
      final var wicaStreamProperties = wicaStream.getWicaStreamProperties();

      final int numericScale = wicaStreamProperties.hasProperty( "prec" ) ?
                               Integer.parseInt( wicaStreamProperties.getPropertyValue( "prec" ) ) : 4;

      logger.info( "Default numeric scale is '{}'", numericScale );

      map.keySet().forEach( (c) -> map.put( c, numericScale ) );
   }

   private void addWicaChannelPropertiesOverrides()
   {
      //wicaStream.getWicaChannels().stream().map( c -> c.getName(), c -> c. ).
   }

/*- Nested Classes -----------------------------------------------------------*/

}
