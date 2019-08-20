/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jcip.annotations.Immutable;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.Map;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelMetadataMapSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final ObjectMapper mapperWithoutQuotedStrings = Jackson2ObjectMapperBuilder.json().build();
   private static final ObjectMapper mapperWithQuotedStrings = Jackson2ObjectMapperBuilder.json().build();

   static {
      final SimpleModule moduleWithoutQuotedStrings = new SimpleModule();
      moduleWithoutQuotedStrings.addSerializer( new MyCustomWicaChannelMetadataMapSerializer(false ) );
      mapperWithoutQuotedStrings.registerModule(moduleWithoutQuotedStrings );

      final SimpleModule moduleWithQuotedStrings = new SimpleModule();
      moduleWithQuotedStrings.addSerializer( new MyCustomWicaChannelMetadataMapSerializer(true ) );
      mapperWithQuotedStrings.registerModule(moduleWithQuotedStrings );
   }

   private final boolean quoteNumericStrings;

   /*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelMetadataMapSerializer( boolean quoteNumericStrings)
   {
      this.quoteNumericStrings = quoteNumericStrings;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

    public String serialize( Map<WicaChannel, WicaChannelMetadata> channelMetadataMap )
    {
      final ObjectMapper mapper = getMapper();
      try
      {
         return mapper.writeValueAsString( channelMetadataMap );
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

   private static class MyCustomWicaChannelMetadataMapSerializer extends StdSerializer<Map>
   {
      final boolean quoteNumericStrings;

      MyCustomWicaChannelMetadataMapSerializer( boolean quoteNumericStrings)
      {
         super( Map.class );
         this.quoteNumericStrings = quoteNumericStrings;
      }

      @Override
      public void serialize( Map value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
      {
         gen.writeStartObject();
         for ( Object channel : value.keySet() )
         {
            final WicaChannel wicaChannel = (WicaChannel) channel;
            gen.writeFieldName( wicaChannel.getName().toString() );
            final WicaChannelMetadata wicaChannelMetadata = (WicaChannelMetadata) value.get( wicaChannel );

            final int numericScale = wicaChannel.getProperties().getNumericPrecision();

            final WicaChannelDataSerializer serializer = WicaChannelDataSerializerBuilder
                  .create()
                  .withNumericScale( numericScale )
                  .withQuotedNumericStrings( quoteNumericStrings )
                  .build();

            final String str = serializer.writeToJson( wicaChannelMetadata );
            gen.writeRawValue( str );
         }
         gen.writeEndObject();
      }
   }

}
