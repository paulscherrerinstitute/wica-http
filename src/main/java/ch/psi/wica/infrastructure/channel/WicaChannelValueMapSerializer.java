/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jcip.annotations.Immutable;
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

   private static final ObjectMapper mapperWithoutQuotedStrings = Jackson2ObjectMapperBuilder.json().build();
   private static final ObjectMapper mapperWithQuotedStrings = Jackson2ObjectMapperBuilder.json().build();

   static {
      final SimpleModule moduleWithoutQuotedStrings = new SimpleModule();
      moduleWithoutQuotedStrings.addSerializer( new MyCustomWicaChannelValueMapSerializer( false ) );
      mapperWithoutQuotedStrings.registerModule(moduleWithoutQuotedStrings );

      final SimpleModule moduleWithQuotedStrings = new SimpleModule();
      moduleWithQuotedStrings.addSerializer( new MyCustomWicaChannelValueMapSerializer( true ) );
      mapperWithQuotedStrings.registerModule(moduleWithQuotedStrings );
   }

   private final boolean quoteNumericStrings;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueMapSerializer( boolean quoteNumericStrings)
   {
      this.quoteNumericStrings = quoteNumericStrings;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String serialize( Map<WicaChannel,List<WicaChannelValue>> channelValueMap )
   {
      final ObjectMapper mapper = getMapper();
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

   private ObjectMapper getMapper()
   {
      return quoteNumericStrings ? mapperWithQuotedStrings : mapperWithoutQuotedStrings;
   }

/*- Nested Interfaces --------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   private static class MyCustomWicaChannelValueMapSerializer extends StdSerializer<Map>
   {
      final boolean quoteNumericStrings;

      MyCustomWicaChannelValueMapSerializer( boolean quoteNumericStrings )
      {
         super( Map.class );
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
               final String str = serializer.writeToJson(wicaChannelValue );
               gen.writeRawValue(str);
            }
            gen.writeEndArray();
         }
         gen.writeEndObject();
      }
   }

}
