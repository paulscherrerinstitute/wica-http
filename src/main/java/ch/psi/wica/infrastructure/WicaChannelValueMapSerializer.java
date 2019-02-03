/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueMapSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ObjectMapper mapper;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueMapSerializer( Map<WicaChannelName,Set<String>> fieldsOfInterest, int numberOfDigits )
   {
     // Validate.notNull( fieldSelectors );
//      final SimpleBeanPropertyFilter simpleBeanPropertyFilter = SimpleBeanPropertyFilter.filterOutAllExcept( "val", "sevr", "dsts" );
//      final FilterProvider filterProvider = new SimpleFilterProvider().addFilter( "WicaChannelValueFilter", simpleBeanPropertyFilter );

      final SimpleModule module = new SimpleModule();
      module.addSerializer( Double.class, new WicaDoubleSerializer( numberOfDigits ) );
      module.addSerializer( new MyCustomWicaChannelValueMapSerializer(fieldsOfInterest ) );

      mapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false ).build();
//      mapper.setFilterProvider(filterProvider );
      mapper.registerModule(module );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   String serialize( WicaChannelValue channelValue )
   {
      Validate.notNull( channelValue );
      try
      {
         return mapper.writeValueAsString(channelValue);
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }

   String serialize( List<WicaChannelValue> channelValueList )
   {
      Validate.notNull( channelValueList );
      try
      {
         return mapper.writeValueAsString(channelValueList );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }

   public String serialize( Map<WicaChannelName,List<WicaChannelValue>> channelValueMap )
   {
      Validate.notNull( channelValueMap );
      try
      {
         return mapper.writeValueAsString(channelValueMap );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   static class MyCustomWicaChannelValueMapSerializer extends StdSerializer<Map>
   {
      final Map<WicaChannelName,Set<String>> channelFieldsOfInterestMap;

      MyCustomWicaChannelValueMapSerializer( Map<WicaChannelName,Set<String>> channelFieldsOfInterestMap )
      {
         super( Map.class );
         this.channelFieldsOfInterestMap = channelFieldsOfInterestMap;
      }

      @Override
      public void serialize( Map value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
      {
         gen.writeStartObject();
         for ( Object channelName : value.keySet() )
         {
            final WicaChannelName wicaChannelName = (WicaChannelName) channelName;
            final Set<String> fieldsOfInterest = channelFieldsOfInterestMap.get( wicaChannelName );
            gen.writeFieldName( wicaChannelName.toString() );
            final SimpleModule simpleModule = new SimpleModule();
            final var jsonObjMapper = new Jackson2ObjectMapperBuilder().createXmlMapper( false ).build();
            jsonObjMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
            jsonObjMapper.configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );
            final SimpleBeanPropertyFilter simpleBeanPropertyFilter = SimpleBeanPropertyFilter.filterOutAllExcept( fieldsOfInterest );
            final FilterProvider filterProvider = new SimpleFilterProvider().addFilter("WicaChannelValueFilter", simpleBeanPropertyFilter );
            jsonObjMapper.setFilterProvider( filterProvider );
            jsonObjMapper.registerModule( simpleModule );

            final List<WicaChannelValue> wicaChannelValueList = (List<WicaChannelValue>) value.get( wicaChannelName );
            gen.writeStartArray();
            for ( WicaChannelValue wicaChannelValue : wicaChannelValueList )
            {
               final String str = jsonObjMapper.writeValueAsString( wicaChannelValue);
               gen.writeRawValue(str);
            }
            gen.writeEndArray();
         }
         gen.writeEndObject();
      }
   }

}
