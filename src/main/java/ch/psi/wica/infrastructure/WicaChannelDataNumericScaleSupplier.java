/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/
import ch.psi.wica.model.*;
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
public class WicaChannelDataNumericScaleSupplier implements NumericScaleSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelDataNumericScaleSupplier.class );

   private final WicaStreamProperties wicaStreamProperties;
   private final Set<WicaChannel> wicaChannels;
   private final Map<WicaChannelName, Integer> map = new HashMap<>();

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelDataNumericScaleSupplier( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      this.wicaStreamProperties = wicaStream.getWicaStreamProperties();
      this.wicaChannels = wicaStream.getWicaChannels();
      addWicaStreamPropertyDefaultValues();
      addWicaChannelPropertyOverrides();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public int supplyForChannelNamed( WicaChannelName wicaChannelName)
   {
      return map.get( wicaChannelName );
   }


/*- Private methods ----------------------------------------------------------*/

   private void addWicaStreamPropertyDefaultValues()
   {
      final int numericScale = wicaStreamProperties.getNumericPrecision();
      logger.info( "Stream default numericScale is: '{}'", numericScale );
      wicaChannels.forEach( (ch) -> map.put( ch.getName(), numericScale ) );
   }


   private void addWicaChannelPropertyOverrides()
   {
      wicaChannels.stream()
            .filter( ch -> ch.getProperties().getNumericPrecision().isPresent() )
            .forEach( ch -> {
               @SuppressWarnings( "OptionalGetWithoutIsPresent" )
               final int numericScaleOverride = ch.getProperties().getNumericPrecision().get();
               logger.info("Channel '{}' had numericScale override '{}'", ch, numericScaleOverride );
               map.put( ch.getName(), numericScaleOverride);
            } );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
