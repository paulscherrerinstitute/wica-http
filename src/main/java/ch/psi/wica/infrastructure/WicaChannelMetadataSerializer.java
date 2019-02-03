/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Map;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelMetadataSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ObjectMapper jsonMetadataMapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs an instance that will serialize all annotated fields in
    * a ChannelMetadata object, and which will send REAL types with the
    * specified numeric scale (= number of digits after the decimal point).
    *
    * @param numericScale a positive number specifying the number of digits to
    *     appear after the decimal point in the string representation.
    */
   public WicaChannelMetadataSerializer( int numericScale )
   {
      this( numericScale, getSerializeAllFieldsFilterProvider() );
   }

   /**
    * Constructs an instance that will serialize the selected fields of interest
    * within a ChannelMetadata object, and which will send REAL types with the
    * specified numeric scale (= number of digits after the decimal point).
    *
    * @param numericScale a positive number specifying the number of digits to
    *     appear after the decimal point in the string representation.
    *
    * @param fieldsOfInterest specifies the fields that are to be serialised
    *     according to the @JsonProperty annotations in the ChannelMetadataObject.
    */
   public WicaChannelMetadataSerializer( int numericScale, String... fieldsOfInterest )
   {
      this( numericScale, getSerializeSelectedFieldsFilterProvider( fieldsOfInterest) );
   }

   /**
    * Private constructor that selects the fields to serialize according to the
    * supplied filterProvider.
    *
    * @param numericScale a positive number specifying the number of digits to
    *     appear after the decimal point in the string representation.
    *
    * @param filterProvider the filter provider defining the field selection rules.
    */
   private WicaChannelMetadataSerializer( int numericScale, FilterProvider filterProvider )
   {
      Validate.isTrue(numericScale >= 0, String.format( "numericScale ('%d') cannot be negative", numericScale ) );
      Validate.notNull( filterProvider );

      // Start defining the special properties of the metadata serialiser
      final SimpleModule metadataModule = new SimpleModule();

      // It is "special" because (a) it is possible to control the number of digits
      // sent down the wire when representing doubles.
      metadataModule.addSerializer( double.class, new WicaDoubleSerializer( numericScale ) );
      jsonMetadataMapper = new Jackson2ObjectMapperBuilder().createXmlMapper( false ).build();

      // It is special because (b) we can select the fields of interest that get
      // sent down the wire
      jsonMetadataMapper.setFilterProvider( filterProvider );

      // Complete the registration of our special serializer.
      jsonMetadataMapper.registerModule( metadataModule );

   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Serializes the supplied ChannelMetadata object according to the
    * configuration rules specified in the class constructor.
    *
    * @param channelMetadata the object to serialize.
    * @return the JSON serialized representation.
    */
   public String serialize( WicaChannelMetadata channelMetadata )
   {
      Validate.notNull( channelMetadata );
      return serializeObject( channelMetadata );
   }

   /**
    * Serializes the supplied ChannelMetadataMap object according to the
    * configuration rules specified in the class constructor.
    *
    * @param channelMetadataMap the object to serialize.
    * @return the JSON serialized representation.
    */
   public String serialize( Map<WicaChannelName,WicaChannelMetadata> channelMetadataMap )
   {
      Validate.notNull( channelMetadataMap );
      return serializeObject( channelMetadataMap );
   }


/*- Private methods ----------------------------------------------------------*/

   private static FilterProvider getSerializeSelectedFieldsFilterProvider( String... fieldsOfInterest )
   {
      final SimpleBeanPropertyFilter metadataFilter = SimpleBeanPropertyFilter.filterOutAllExcept( fieldsOfInterest );
      return new SimpleFilterProvider().addFilter( "WicaChannelMetadataFilter", metadataFilter );
   }

   private static FilterProvider getSerializeAllFieldsFilterProvider()
   {
      final SimpleBeanPropertyFilter metadataFilter = SimpleBeanPropertyFilter.serializeAll();
      return new SimpleFilterProvider().addFilter( "WicaChannelMetadataFilter", metadataFilter );
   }

   private String serializeObject( Object object )
   {
      Validate.notNull( object );
      try
      {
         return jsonMetadataMapper.writeValueAsString( object );
      }
      catch ( JsonProcessingException ex )
      {
         throw new RuntimeException( String.format( "Json Processing Exception when serializing object of type '%s'. Details were: '%s'", object, ex.getMessage() ), ex.getCause() );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
