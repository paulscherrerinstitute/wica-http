/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueSerializer
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   /**
    * Controls the serialized representation of double Nan values. Set to
    * TRUE for strict JSON compliance. Set to FALSE for "relaxed" JSON5
    * format.
    */
   @Value( "${wica.serialize_nan_as_string}" )
   private static final boolean writeNanAsStringDefault = false;

   /**
    * Controls the serialized representation of double Infinity values. Set to
    * TRUE for strict JSON compliance. Set to FALSE for "relaxed" JSON5
    * format.
    */
   @Value( "${wica.serialize_infinity_as_string}" )
   private static final boolean writeInfinityAsStringDefault = false;

   private final WicaChannelDataSerializer wicaChannelDataSerializer;

   /*- Main ---------------------------------------------------------------------*/
   /*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueSerializer( int numericScale )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( numericScale,
                                                                      writeNanAsStringDefault,
                                                                      writeInfinityAsStringDefault );
   }


   WicaChannelValueSerializer( int numericScale, String... fieldsOfInterest )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( numericScale,
                                                                      writeNanAsStringDefault,
                                                                      writeInfinityAsStringDefault,
                                                                      fieldsOfInterest );
   }

   WicaChannelValueSerializer( int numericScale,
                               boolean writeNanAsString,
                               boolean writeInfinityAsString,
                               String... fieldsOfInterest )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( numericScale,
                                                                      writeNanAsString,
                                                                      writeInfinityAsString,
                                                                      fieldsOfInterest );
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
