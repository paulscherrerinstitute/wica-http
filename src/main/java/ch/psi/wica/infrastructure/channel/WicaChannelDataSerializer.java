/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Supports serialization of objects of type WicaChannelData and its children,
 * including WicaChannelMetadata and WicaChannelValue.
 */
@Immutable
public class WicaChannelDataSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Map<Integer,ObjectMapper> mapperPool = new ConcurrentHashMap<>();
   private final ObjectMapper jsonObjectMapper;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

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
   WicaChannelDataSerializer( Set<String> fieldsOfInterest, int numericScale, boolean quoteNumericStrings )
   {
      Validate.notNull( fieldsOfInterest );
      Validate.isTrue(numericScale >= 0, String.format( "numericScale ('%d') cannot be negative", numericScale ) );
      jsonObjectMapper = getMapper( fieldsOfInterest, numericScale, quoteNumericStrings );
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
   public String writeToJson( WicaChannelData wicaChannelData )
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

   private ObjectMapper getMapper( Set<String> fieldsOfInterest,
                                   int numericScale,
                                   boolean quoteNumericStrings)
   {
      final int hash = getHash( fieldsOfInterest, numericScale, quoteNumericStrings );
      if ( mapperPool.containsKey( hash ) )
      {
         return mapperPool.get( hash );
      }
      else
      {
         final ObjectMapper objectMapper = getNewMapper( fieldsOfInterest, numericScale, quoteNumericStrings );
         mapperPool.put( hash, objectMapper );
      }
      return mapperPool.get( hash );
   }

   private ObjectMapper getNewMapper( Set<String> fieldsOfInterest,
                                      int numericScale,
                                      boolean quoteNumericStrings )
   {
      // Start defining the special properties of this serialiser
      final SimpleModule module = new SimpleModule();

      // It is "special" because (a) it is possible to control the number of digits
      // sent down the wire when representing doubles and/or double arrays.
      module.addSerializer( double.class, new WicaDoubleSerializer( numericScale ) );
      module.addSerializer( double[].class, new WicaDoubleArraySerializer( numericScale ) );

      final ObjectMapper mapper = JsonMapper.builder()
            // Turn off the feature whereby date/time values are written as timestamps.
            .configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false )
            .configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false )
            .configure( SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true )
            // It is "special" because (b) it is possible to control programmatically
            // the serialized representation of NaN and Infinity.
            .configure( JsonWriteFeature.WRITE_NAN_AS_STRINGS, quoteNumericStrings )
            .build();

      // It is "special" because (c) we can select the fields of interest that get
      // sent down the wire.
      final FilterProvider filterProvider = fieldsOfInterest.isEmpty() ?
            getSerializeAllFieldsFilterProvider() :
            getSerializeSelectedFieldsFilterProvider( fieldsOfInterest );
      mapper.setFilterProvider( filterProvider );

      // It is "special" because (d) it uses WicaChannelData and WicaChannelValue Mixin types.
      mapper.addMixIn( WicaChannelData.class, WicaChannelDataSerializerMixin.class );
      mapper.addMixIn( WicaChannelValue.class, WicaChannelValueMixins.WicaChannelValueSerializerMixin.class );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueConnected.class, WicaChannelValueMixins.WicaChannelValueConnectedSerializerMixin.class );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueDisconnected.class, WicaChannelValueMixins.WicaChannelValueDisconnectedSerializerMixin.class );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueConnectedInteger.class, WicaChannelValueMixins.WicaChannelValueConnectedIntegerSerializerMixin.class  );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueConnectedIntegerArray.class, WicaChannelValueMixins.WicaChannelValueConnectedIntegerArraySerializerMixin.class  );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueConnectedReal.class, WicaChannelValueMixins.WicaChannelValueConnectedRealSerializerMixin.class  );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueConnectedRealArray.class, WicaChannelValueMixins.WicaChannelValueConnectedRealArraySerializerMixin.class  );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueConnectedString.class, WicaChannelValueMixins.WicaChannelValueConnectedStringSerializerMixin.class  );
      mapper.addMixIn( WicaChannelValue.WicaChannelValueConnectedStringArray.class, WicaChannelValueMixins.WicaChannelValueConnectedStringArraySerializerMixin.class  );
      mapper.addMixIn( WicaChannelAlarmStatus.class, WicaChannelValueMixins.WicaChannelAlarmStatusMixin.class  );

      // It is "special" because (e) it uses WicaChannelMetadata Mixin types.
      mapper.addMixIn( WicaChannelMetadata.class, WicaChannelMetadataMixins.WicaChannelMetadataSerializerMixin.class );
      mapper.addMixIn( WicaChannelMetadata.WicaChannelMetadataUnknown.class, WicaChannelMetadataMixins.WicaChannelMetadataUnknownSerializerMixin.class );
      mapper.addMixIn( WicaChannelMetadata.WicaChannelMetadataString.class, WicaChannelMetadataMixins.WicaChannelMetadataStringSerializerMixin.class );
      mapper.addMixIn( WicaChannelMetadata.WicaChannelMetadataStringArray.class, WicaChannelMetadataMixins.WicaChannelMetadataStringArraySerializerMixin.class );
      mapper.addMixIn( WicaChannelMetadata.WicaChannelMetadataInteger.class, WicaChannelMetadataMixins.WicaChannelMetadataIntegerSerializerMixin.class );
      mapper.addMixIn( WicaChannelMetadata.WicaChannelMetadataIntegerArray.class, WicaChannelMetadataMixins.WicaChannelMetadataIntegerArraySerializerMixin.class );
      mapper.addMixIn( WicaChannelMetadata.WicaChannelMetadataReal.class, WicaChannelMetadataMixins.WicaChannelMetadataRealSerializerMixin.class );
      mapper.addMixIn( WicaChannelMetadata.WicaChannelMetadataRealArray.class, WicaChannelMetadataMixins.WicaChannelMetadataRealArraySerializerMixin.class );

      mapper.setVisibility( mapper.getSerializationConfig()
                                  .getDefaultVisibilityChecker()
                                  .withFieldVisibility(JsonAutoDetect.Visibility.NONE )
                                  .withGetterVisibility( JsonAutoDetect.Visibility.NONE )
                                  .withSetterVisibility( JsonAutoDetect.Visibility.NONE )
                                  .withCreatorVisibility( JsonAutoDetect.Visibility.NONE ) );

      // Complete the registration of our special serializer.
      mapper.registerModule( module );
      return mapper;
   }

   private int getHash( Set<String> fieldsOfInterest, int numericScale, boolean quoteNumericStrings)
   {
      final int fieldsOfInterestHash = String.join("", fieldsOfInterest ).hashCode();
      return Objects.hash( fieldsOfInterestHash, numericScale, quoteNumericStrings );
   }

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

/*- Nested Class: WicaChannelDataMixin ---------------------------------------*/

   // Note the Filter Id in the annotation here must match the definitions
   // given earlier in this class.
   @JsonFilter( "WicaChannelDataFilter" )
   @JsonPropertyOrder( { "type", "wsts", "egu", "prec", "hopr", "lopr", "drvh", "drvl",
                         "hihi", "lolo", "high", "low", "conn", "stat", "sevr", "ts", "val" })
   public static abstract class WicaChannelDataSerializerMixin extends WicaChannelData
   {
      private  WicaChannelDataSerializerMixin() { super(); }
      @Override public abstract @JsonProperty( "type" ) WicaChannelType getType();
      @Override public abstract @JsonProperty( "wsts" ) LocalDateTime getWicaServerTimestamp();
   }

}
