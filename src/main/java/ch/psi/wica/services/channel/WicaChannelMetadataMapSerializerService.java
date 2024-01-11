/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelDataSerializer;
import ch.psi.wica.infrastructure.channel.WicaChannelDataSerializerBuilder;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Service
@Immutable
public class WicaChannelMetadataMapSerializerService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ObjectMapper mapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelMetadataMapSerializerService( @Value( "${wica.stream-metadata-fields-of-interest}" ) String fieldsOfInterest,
                                                   @Value( "${wica.stream-quote-numeric-strings}" ) boolean quoteNumericStrings )
   {
      Validate.notNull( fieldsOfInterest, "The 'fieldsOfInterest' argument is null." );
      final Set<String> fieldsOfInterestSet = Set.of( fieldsOfInterest.split( ";" ) );

      mapper = Jackson2ObjectMapperBuilder.json().build();
      final SimpleModule module = new SimpleModule();
      module.addSerializer( new MyCustomWicaChannelMetadataMapSerializer( fieldsOfInterestSet, quoteNumericStrings ) );
      mapper.registerModule( module );
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
      final Set<String> fieldsOfInterest;
      final boolean quoteNumericStrings;

      MyCustomWicaChannelMetadataMapSerializer( Set<String> fieldsOfInterest, boolean quoteNumericStrings)
      {
         super( Map.class );
         this.fieldsOfInterest = fieldsOfInterest;
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
                  .withFieldsOfInterest( fieldsOfInterest )
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
