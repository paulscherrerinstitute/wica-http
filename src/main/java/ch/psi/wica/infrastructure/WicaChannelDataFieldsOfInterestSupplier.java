/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannel;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaStream;
import ch.psi.wica.model.WicaStreamProperties;
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
public class WicaChannelDataFieldsOfInterestSupplier implements FieldsOfInterestSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelDataFieldsOfInterestSupplier.class );

   private final WicaStreamProperties wicaStreamProperties;
   private final Set<WicaChannel> wicaChannels;
   private final Map<WicaChannelName, Set<String>> map = new HashMap<>();

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelDataFieldsOfInterestSupplier( WicaStream wicaStream )
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
   public Set<String> supplyForChannelNamed( WicaChannelName wicaChannelName)
   {
      return map.get( wicaChannelName );
   }


/*- Private methods ----------------------------------------------------------*/

   private void addWicaStreamPropertyDefaultValues()
   {
      final Set<String> fieldsOfInterest = wicaStreamProperties.getFieldsOfInterest();
      logger.info( "Stream default fieldsOfInterest are: '{}'", fieldsOfInterest );
      wicaChannels.forEach( ch -> map.put( ch.getName(), fieldsOfInterest ) );
   }

   private void addWicaChannelPropertyOverrides()
   {
      wicaChannels.stream()
                  .filter( ch -> ch.getProperties().getFieldsOfInterest().isPresent() )
                  .forEach( ch -> {
                     @SuppressWarnings( "OptionalGetWithoutIsPresent" )
                     final Set<String> fieldsOfInterestOverride = ch.getProperties().getFieldsOfInterest().get();
                     logger.info("Channel '{}' had fieldsOfInterest override '{}'", ch, fieldsOfInterestOverride );
                     map.put( ch.getName(), fieldsOfInterestOverride);
                  } );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
