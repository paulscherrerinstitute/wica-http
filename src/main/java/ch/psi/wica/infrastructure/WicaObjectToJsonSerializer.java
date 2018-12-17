/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.SerializeExceptFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaObjectToJsonSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ObjectMapper jsonObjectMapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaObjectToJsonSerializer()
   {
      this( Set.of() );
   }

   public WicaObjectToJsonSerializer( Set<String> fieldSelectors )
   {
      Validate.notNull( fieldSelectors );

      final SimpleBeanPropertyFilter sbf = SimpleBeanPropertyFilter.filterOutAllExcept( fieldSelectors );
      final FilterProvider fp = new SimpleFilterProvider().addFilter( "WicaChannelValueFilter", sbf );

      jsonObjectMapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false ).build();
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );
      jsonObjectMapper.setFilterProvider( fp );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String convertWicaChannelMetadataToJsonRepresentation( WicaChannelMetadata channelMetadata )
   {
      Validate.notNull( channelMetadata );
      try
      {
         return jsonObjectMapper.writeValueAsString(channelMetadata );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }

   public String convertWicaChannelMetadataMapToJsonRepresentation( Map<WicaChannelName, WicaChannelMetadata> channelMetadataMap )
   {
      Validate.notNull( channelMetadataMap );
      try
      {
         return jsonObjectMapper.writeValueAsString( channelMetadataMap );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }

   public String convertWicaChannelValueListToJsonRepresentation( Map<WicaChannelName,List<WicaChannelValue>> channelValueMap )
   {
      Validate.notNull( channelValueMap );
      try
      {
         final String result = jsonObjectMapper.writeValueAsString( channelValueMap );
         return result;
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }

   <T>String convertWicaChannelValueToJsonRepresentation( WicaChannelValue channelValue )
   {
      Validate.notNull( channelValue );
      try
      {
         return jsonObjectMapper.writeValueAsString(channelValue);
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }

   String convertWicaChannelValueMapToJsonRepresentation( Map<WicaChannelName, WicaChannelValue> channelValueMap )
   {
      Validate.notNull( channelValueMap );
      try
      {
         final String result = jsonObjectMapper.writeValueAsString( channelValueMap );
         return result;
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }

   String convertWicaChannelValueListToJsonRepresentation( List<WicaChannelValue> channelValueList )
   {
      Validate.notNull( channelValueList );
      try
      {
         final String result = jsonObjectMapper.writeValueAsString( channelValueList );
         return result;
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }

   String convertWicaChannelMapToJsonRepresentation( Map<WicaChannelName, Map<String,String>> genericMap )
   {
      Validate.notNull( genericMap );
      try
      {
         final String result = jsonObjectMapper.writeValueAsString( genericMap );
         return result;
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
