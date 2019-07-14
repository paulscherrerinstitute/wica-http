/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelMetadata;
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
   @Value( "${wica.serialize-nan-as-string}" )
   private static final boolean quoteNumericStrings = false;

   private final WicaChannelDataSerializer wicaChannelDataSerializer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance that will generate serialized string representations
    * of wica channel metadata which include ALL @JsonProperty annotations in the
    * WicaChannelMetadata object, that serialize numeric values using the specified
    * numeric scale, and with the default (= configured) approach to quoting numeric
    * strings.
    *
    * @param numericScale a non-negative number specifying the number of digits to
    *     appear after the decimal point in the serialized representation.
    */
   WicaChannelMetadataSerializer( int numericScale )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( numericScale, quoteNumericStrings );
   }

   /**
    * Constructs a new instance that will generate serialized string representations
    * of wica channel metadata which include SELECTED @JsonProperty annotations in the
    * WicaChannelMetadata object, that serialize numeric values using the specified
    * numeric scale, and with the specified approach to quoting numeric strings.
    *
    * @param fieldsOfInterest specifies the fields that are to be serialised
    *     according to the @JsonProperty annotations in the ChannelDataObject.
    *
    * @param numericScale a non-negative number specifying the number of digits to
    *     appear after the decimal point in the serialized representation.
    */
   WicaChannelMetadataSerializer( Set<String> fieldsOfInterest, int numericScale )
   {
      this.wicaChannelDataSerializer = new WicaChannelDataSerializer( fieldsOfInterest, numericScale, quoteNumericStrings );
   }

   /**
    * Constructs a new instance that will generate serialized string representations
    * of wica channel metadata which include SELECTED @JsonProperty annotations in the
    * WicaChannelMetadata object, that serialize numeric values using the specified
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
