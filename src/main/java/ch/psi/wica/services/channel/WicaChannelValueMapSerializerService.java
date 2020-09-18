/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelDataSerializerBuilder;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.MapType;
import net.jcip.annotations.Immutable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Service
@Immutable
public class WicaChannelValueMapSerializerService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ObjectMapper mapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueMapSerializerService( @Value( "${wica.stream-quote-numeric-strings}" ) boolean quoteNumericStrings)
   {
      mapper = Jackson2ObjectMapperBuilder.json().build();
      final SimpleModule module = new SimpleModule();

      // Note: the approach here avoids the use of raw types by following the suggested approach
      // in this SO article:
      // https://stackoverflow.com/questions/47442795/jackson-register-custom-json-serializer
      final MapType type = mapper.getTypeFactory().constructMapType( HashMap.class, WicaChannel.class, WicaChannelValue.class );
      module.addSerializer( new WicaChannelValueMapSerializerService.MyCustomWicaChannelValueMapSerializer( quoteNumericStrings, type ) );

      mapper.registerModule( module );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String serialize( Map<WicaChannel,List<WicaChannelValue>> channelValueMap )
   {
      try
      {
         return mapper.writeValueAsString( channelValueMap );
      }
      catch( JsonProcessingException ex )
      {
         throw new RuntimeException( "RuntimeException: " + ex.getMessage(), ex.getCause() );
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Interfaces --------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   private static class MyCustomWicaChannelValueMapSerializer extends StdSerializer<Map<WicaChannel,List<WicaChannelValue>>>
   {
      final boolean quoteNumericStrings;

      // Note: the approach here avoids the use of raw types by following the suggested approach
      // in this SO article:
      // https://stackoverflow.com/questions/47442795/jackson-register-custom-json-serializer
      MyCustomWicaChannelValueMapSerializer( boolean quoteNumericStrings, JavaType type )
      {
         super( type );
         this.quoteNumericStrings  = quoteNumericStrings;
      }

      @Override
      public void serialize( Map value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
      {
         gen.writeStartObject();
         for ( Object channel : value.keySet() )
         {
            final WicaChannel wicaChannel = (WicaChannel) channel;
            final int numericScale = wicaChannel.getProperties().getNumericPrecision();
            final Set<String> fieldsOfInterest = Set.of( wicaChannel.getProperties().getFieldsOfInterest().split(";" ) );

            final var serializer = WicaChannelDataSerializerBuilder.create()
               .withFieldsOfInterest( fieldsOfInterest )
               .withNumericScale( numericScale )
               .withQuotedNumericStrings( quoteNumericStrings )
               .build();

            gen.writeFieldName( wicaChannel.getName().toString() );

            // This cast is ok. Unfortunately this method cannot be generified because
            // then it would not satisfy the requirements of the interface.
            @SuppressWarnings( "unchecked")
            final List<WicaChannelValue> wicaChannelValueList = (List<WicaChannelValue>) value.get( wicaChannel );
            gen.writeStartArray();
            for ( WicaChannelValue wicaChannelValue : wicaChannelValueList )
            {
               final String str = serializer.writeToJson( wicaChannelValue );
               gen.writeRawValue(str);
            }
            gen.writeEndArray();
         }
         gen.writeEndObject();
      }
   }

}
