/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelMetadataSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * Controls the serialized representation of double Nan values. Set to
    * TRUE for strict JSON compliance. Set to FALSE for "relaxed" JSON5
    * format.
    */
   @Value( "${wica.serialize_nan_as_string}" )
   private static final boolean quoteNumericStrings = false;

   private final WicaChannelDataSerializer wicaChannelDataSerializer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaChannelMetadataSerializer( int numericScale )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( numericScale, quoteNumericStrings );
   }

   WicaChannelMetadataSerializer( Set<String> fieldsOfInterest, int numericScale )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( fieldsOfInterest, numericScale, quoteNumericStrings );
   }

   WicaChannelMetadataSerializer( Set<String> fieldsOfInterest, int numericScale, boolean quoteNumericStrings )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( fieldsOfInterest, numericScale, quoteNumericStrings );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Serializes the supplied WicaChannelMetadata object according to the
    * configuration rules specified in the class constructor.
    *
    * @param channelMetadata the object to serialize.
    * @return the JSON serialized representation.
    */
   public String serialize( WicaChannelMetadata channelMetadata )
   {
      Validate.notNull( channelMetadata );
      return wicaChannelDataSerializer.serialize( channelMetadata );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
