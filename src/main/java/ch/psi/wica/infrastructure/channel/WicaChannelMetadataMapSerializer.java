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

   private final ObjectMapper mapper;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelMetadataMapSerializer( boolean quoteNumericStrings )
   {
        final SimpleModule module = new SimpleModule();
        module.addSerializer( new MyCustomWicaChannelMetadataMapSerializer( quoteNumericStrings ) );
        mapper = new Jackson2ObjectMapperBuilder().createXmlMapper( false ).build();
        mapper.registerModule(module );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

    public String serialize( Map<WicaChannel, WicaChannelMetadata> channelMetadataMap )
    {
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
            final int numericScale = wicaChannel.getProperties().getNumericPrecision();
            final WicaChannelDataSerializer serializer = new WicaChannelDataSerializer( numericScale, quoteNumericStrings );
            gen.writeFieldName( wicaChannel.getName().toString() );
            final WicaChannelMetadata wicaChannelMetadata = (WicaChannelMetadata) value.get( wicaChannel );
            final String str = serializer.serialize( wicaChannelMetadata );
            gen.writeRawValue( str );
         }
         gen.writeEndObject();
      }
   }

}
