/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

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

   private final ObjectMapper jsonObjectMapper;

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
    * either numbers or strings depending on the specified settings.
    * For strict JSON compliance string format should be selected, although
    * this may imply extra work on the decoding end to reconstruct the
    * original types. For JSON5 compliance number format can be selected.
    *
    * @param numericScale a non-negative number specifying the number of
    *     digits to appear after the decimal point in the serialized
    *     representation.
    *
    * @param writeNanAsString - determines whether the special value
    *     Double.NAN will be serialised as a number or a string.
    *
    * @param writeInfinityAsString - determines whether the special values
    *     Double.POSITIVE_INFINITY and DOUBLE.NEGATIVE_INFINITY will be
    *     serialised as a number or a string.

    * @throws IllegalArgumentException if the numericScale was negative.
    */
   WicaChannelDataSerializer( int numericScale,
                              boolean writeNanAsString,
                              boolean writeInfinityAsString )
   {
      this( numericScale, writeNanAsString, writeInfinityAsString, getSerializeAllFieldsFilterProvider() );
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
    * either numbers or strings depending on the specified settings.
    * For strict JSON compliance string format should be selected, although
    * this may imply extra work on the decoding end to reconstruct the
    * original types. For JSON5 compliance number format can be selected.
    *
    * @param numericScale a non-negative number specifying the number of
    *     digits to appear after the decimal point in the serialized
    *     representation.
    *
    * @param writeNanAsString - determines whether the special value
    *     Double.NAN will be serialised as a number or a string.
    *
    * @param writeInfinityAsString - determines whether the special values
    *     Double.POSITIVE_INFINITY and DOUBLE.NEGATIVE_INFINITY will be
    *     serialised as a number or a string.
    *
    * @param fieldsOfInterest specifies the fields that are to be serialised
    *     according to the @JsonProperty annotations in the ChannelDataObject.
    *
    * @throws IllegalArgumentException if the numericScale was negative.
    */
   WicaChannelDataSerializer( int numericScale,
                              boolean writeNanAsString,
                              boolean writeInfinityAsString,
                              String... fieldsOfInterest )
   {
      this( numericScale, writeNanAsString, writeInfinityAsString,
            getSerializeSelectedFieldsFilterProvider( fieldsOfInterest) );
   }

   /**
    * Private constructor that selects the fields to serialize according to the
    * supplied filterProvider and where the representation of Nan's/Infinity
    * can be configured.
    *
    * @param numericScale a non-negative number specifying the number of
    *     digits to appear after the decimal point in the serialized
    *     representation.
    *
    * @param writeNanAsString - determines whether the special value
    *     Double.NAN will be serialised as a number or a string.
    *
    * @param writeInfinityAsString - determines whether the special values
    *     Double.POSITIVE_INFINITY and DOUBLE.NEGATIVE_INFINITY will be
    *     serialised as a number or a string.
    *
    * @param filterProvider the filter provider defining the field selection rules.
    *
    * @throws IllegalArgumentException if the numericScale was negative.
    * @throws NullPointerException if the supplied filterProvider was null.

    */
   private WicaChannelDataSerializer( int numericScale,
                                      boolean writeNanAsString,
                                      boolean writeInfinityAsString,
                                      FilterProvider filterProvider )
   {
      Validate.isTrue(numericScale >= 0, String.format( "numericScale ('%d') cannot be negative", numericScale ) );
      Validate.notNull( filterProvider );

      // Start defining the special properties of the metadata serialiser
      final SimpleModule metadataModule = new SimpleModule();

      // It is "special" because (a) it is possible to control the number of digits
      // sent down the wire when representing doubles.
      metadataModule.addSerializer( double.class, new WicaDoubleSerializer( numericScale, writeNanAsString, writeInfinityAsString ) );
      jsonObjectMapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false ).build();
      jsonObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );

      // It is special because (b) we can select the fields of interest that get
      // sent down the wire
      jsonObjectMapper.setFilterProvider(filterProvider );

      // Complete the registration of our special serializer.
      jsonObjectMapper.registerModule(metadataModule );

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

   private static FilterProvider getSerializeSelectedFieldsFilterProvider( String... fieldsOfInterest )
   {
      final SimpleBeanPropertyFilter metadataFilter = SimpleBeanPropertyFilter.filterOutAllExcept( fieldsOfInterest );
      return new SimpleFilterProvider().addFilter( "WicaChannelDataFilter", metadataFilter );
   }

   private static FilterProvider getSerializeAllFieldsFilterProvider()
   {
      final SimpleBeanPropertyFilter metadataFilter = SimpleBeanPropertyFilter.serializeAll();
      return new SimpleFilterProvider().addFilter( "WicaChannelDataFilter", metadataFilter );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
