/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Supports serialization of objects of type WicaChannelData and its children,
 * including WicaChannelMetadata and WicaChannelValue.
 */
@Immutable
class WicaChannelDataSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private ObjectMapper jsonObjectMapper;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns an instance that will serialize ALL @JsonProperty annotated
    * fields in a ChannelData object, or any of its children.
    *
    * In cases where the underlying type of the annotated field is double
    * then the value will be serialized with the specified numeric scale
    * (= number of digits after the decimal point).
    *
    * Special double values such a Nan and Infinity will be written as
    * either numbers or strings depending on the quoteNumericStrings
    * setting.
    *
    * For strict JSON compliance string format should be selected, although
    * this may imply extra work on the decoding end to reconstruct the
    * original types. For JSON5 compliance number format can be selected.
    *
    * @param numericScale a non-negative number specifying the number of
    *     digits to appear after the decimal point in the serialized
    *     representation.
    *
    * @param quoteNumericStrings - determines whether the special double
    *     values NaN and Infinity will be serialised as numbers or strings.
    *
    * @throws IllegalArgumentException if the numericScale was negative.
    */
   WicaChannelDataSerializer( int numericScale, boolean quoteNumericStrings )
   {
      this( getSerializeAllFieldsFilterProvider(), numericScale, quoteNumericStrings );
   }

   /**
    * Returns an instance that will serialize SELECTED @JsonProperty
    * annotated fields in a ChannelData object, or any of its children.
    *
    * In cases where the underlying type of the annotated field is double
    * then the value will be serialized with the specified numeric scale
    * (= number of digits after the decimal point).
    *
    * Special double values such a Nan and Infinity will be written as
    * either numbers or strings depending on the quoteNumericStrings
    * setting.
    *
    * For strict JSON compliance string format should be selected, although
    * this may imply extra work on the decoding end to reconstruct the
    * original types. For JSON5 compliance number format can be selected.
    *
    * @param fieldsOfInterest specifies the fields that are to be serialised
    *     according to the @JsonProperty annotations in the ChannelDataObject.

    * @param numericScale a non-negative number specifying the number of
    *     digits to appear after the decimal point in the serialized
    *     representation.
    *
    * @param quoteNumericStrings - determines whether the special double
    *     values NaN and Infinity will be serialised as numbers or strings.
    *
    * @throws IllegalArgumentException if the numericScale was negative.
    * @throws NullPointerException if the supplied filterProvider was null.
    */
   WicaChannelDataSerializer(  Set<String> fieldsOfInterest, int numericScale, boolean quoteNumericStrings )
   {
      this( getSerializeSelectedFieldsFilterProvider( fieldsOfInterest), numericScale, quoteNumericStrings );
   }


/*- Private Constructor ------------------------------------------------------*/

   /**
    * Private constructor that selects the fields to serialize according to the
    * supplied filterProvider and where the serialized representation of
    * Nan's/Infinity can be configured to be of either type number or string.
    *
    * @param filterProvider the filter provider defining the field selection
    *     rules.
    *
    * @param numericScale a non-negative number specifying the number of
    *     digits to appear after the decimal point in the serialized
    *     representation.
    *
    * @param quoteNumericStrings - determines whether the special double
    *     values NaN and Infinity will be serialised as numbers or strings.
    *
    * @throws IllegalArgumentException if the numericScale was negative.
    * @throws NullPointerException if the supplied filterProvider was null.

    */
   private WicaChannelDataSerializer( FilterProvider filterProvider,
                                      int numericScale,
                                      boolean quoteNumericStrings )
   {
      Validate.isTrue(numericScale >= 0, String.format( "numericScale ('%d') cannot be negative", numericScale ) );
      Validate.notNull( filterProvider );

      // Start defining the special properties of this serialiser
      final SimpleModule module = new SimpleModule();

      // It is "special" because (a) it is possible to control the number of digits
      // sent down the wire when representing doubles.
      module.addSerializer( double.class, new WicaDoubleSerializer( numericScale ) );
      jsonObjectMapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false ).build();

      // Turn off the feature whereby date/time values are written as timestamps.
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );

      // It is "special" because (b) it is possible to control programmatically
      // the serialized representation of NaN and Infinity.
      if ( quoteNumericStrings )
      {
         jsonObjectMapper.enable( JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
      }
      else
      {
         jsonObjectMapper.disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
      }

      // It is special because (c) we can select the fields of interest that get
      // sent down the wire.
      jsonObjectMapper.setFilterProvider(filterProvider );

      // Complete the registration of our special serializer.
      jsonObjectMapper.registerModule( module );

   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Serializes the supplied WicaChannelData object according to the
    * configuration rules specified in the class constructor.
    *
    * @param wicaChannelData the object to serialize.
    * @return the JSON serialized representation.
    */
   public String serialize( WicaChannelData wicaChannelData )
   {
      Validate.notNull( wicaChannelData );
      try
      {
         return jsonObjectMapper.writeValueAsString( wicaChannelData );
      }
      catch ( JsonProcessingException ex )
      {
         throw new RuntimeException( String.format( "Json Processing Exception when serializing object of type '%s'. Details were: '%s'", wicaChannelData, ex.getMessage() ), ex.getCause() );
      }
   }

/*- Private methods ----------------------------------------------------------*/

   private static FilterProvider getSerializeSelectedFieldsFilterProvider( Set<String> fieldsOfInterest )
   {
      final SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept( fieldsOfInterest );
      return new SimpleFilterProvider().addFilter( "WicaChannelDataFilter", filter );
   }

   private static FilterProvider getSerializeAllFieldsFilterProvider()
   {
      final SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();
      return new SimpleFilterProvider().addFilter( "WicaChannelDataFilter", filter );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
