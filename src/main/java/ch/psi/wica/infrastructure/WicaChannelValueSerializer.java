/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueSerializer
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   /**
    * Controls the serialized representation of double Nan and Infinity values.
    * Set to TRUE for strict JSON compliance. Set to FALSE for "relaxed" JSON5
    * format.
    */
   @Value( "${wica.serialize_nan_as_string}" )
   private static final boolean quoteNumericStrings = false;


   private final WicaChannelDataSerializer wicaChannelDataSerializer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueSerializer( int numericScale, boolean quoteNumericStrings )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( numericScale, quoteNumericStrings );
   }

   public WicaChannelValueSerializer( Set<String> fieldsOfInterest, int numericScale, boolean quoteNumericStrings )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( fieldsOfInterest, numericScale, quoteNumericStrings );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Serializes the supplied WicaChannelValue object according to the
    * configuration rules specified in the class constructor.
    *
    * @param channelValue the object to serialize.
    * @return the JSON serialized representation.
    */
   public String serialize( WicaChannelValue channelValue )
   {
      Validate.notNull( channelValue );
      return wicaChannelDataSerializer.serialize( channelValue );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
