/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
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
   @Value( "${wica.serialize-nan-as-string}" )
   private static final boolean quoteNumericStrings = false;

   private final WicaChannelDataSerializer wicaChannelDataSerializer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance that will generate serialized string representations
    * of wica channel values which include ALL @JsonProperty annotations in the
    * WicaChannelValue object, that serialize numeric values using the specified
    * numeric scale, and with the specified approach to quoting numeric strings.
    *
    * @param numericScale a non-negative number specifying the number of digits to
    *     appear after the decimal point in the serialized representation.
    *
    * @param quoteNumericStrings determines whether the special double
    *     values NaN and Infinity will be serialised as numbers or strings.
    */
   public WicaChannelValueSerializer( int numericScale, boolean quoteNumericStrings )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( numericScale, quoteNumericStrings );
   }

   /**
    * Constructs a new instance that will generate serialized string representations
    * of wica channel values which include SELECTED @JsonProperty annotations in the
    * WicaChannelValue object, that serialize numeric values using the specified
    * numeric scale, and with the specified approach to quoting numeric strings.
    *
    * @param fieldsOfInterest specifies the fields that are to be serialised
    *     according to the @JsonProperty annotations in the ChannelDataObject.
    *
    * @param numericScale a non-negative number specifying the number of digits to
    *     appear after the decimal point in the serialized representation.
    *
    * @param quoteNumericStrings determines whether the special double
    *     values NaN and Infinity will be serialised as numbers or strings.
    */
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
