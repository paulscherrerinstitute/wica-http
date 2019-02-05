/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
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
import java.util.Set;


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

   public WicaChannelMetadataMapSerializer( int numericScale,
                                            boolean writeNanAsString,
                                            boolean writeInfinityAsString,
                                            FieldsOfInterestSupplier fieldsOfInterestSupplier )
   {
        final SimpleModule module = new SimpleModule();
        module.addSerializer( new MyCustomWicaChannelMetadataMapSerializer(numericScale, writeNanAsString, writeInfinityAsString, fieldsOfInterestSupplier ) );
        mapper = new Jackson2ObjectMapperBuilder().createXmlMapper( false ).build();
        mapper.registerModule(module );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

    public String serialize( Map<WicaChannelName, WicaChannelMetadata> channelMetadataMap )
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

   public interface FieldsOfInterestSupplier
   {
      Set<String> supplyForChannelNamed( WicaChannelName wicaChannelName );
   }

/*- Nested Classes -----------------------------------------------------------*/

   private static class MyCustomWicaChannelMetadataMapSerializer extends StdSerializer<Map>
   {

      final int numericScale;
      final boolean writeNanAsString;
      final boolean writeInfinityAsString;
      final FieldsOfInterestSupplier fieldsOfInterestSupplier;

      MyCustomWicaChannelMetadataMapSerializer( int numericScale,
                                                boolean writeNanAsString,
                                                boolean writeInfinityAsString,
                                                FieldsOfInterestSupplier fieldsOfInterestSupplier )
      {
         super( Map.class );
         this.numericScale = numericScale;
         this.writeNanAsString  = writeNanAsString;
         this.writeInfinityAsString = writeInfinityAsString;
         this.fieldsOfInterestSupplier = fieldsOfInterestSupplier;
      }

      @Override
      public void serialize( Map value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
      {
         gen.writeStartObject();
         for ( Object channelName : value.keySet() )
         {
            final WicaChannelName wicaChannelName = (WicaChannelName) channelName;

            final WicaChannelDataSerializer serializer;
            if ( fieldsOfInterestSupplier.supplyForChannelNamed( wicaChannelName ).size() == 0 )
            {
               serializer = new WicaChannelDataSerializer( numericScale, writeNanAsString, writeInfinityAsString );
            }
            else
            {
               final String[] fieldsOfInterest = fieldsOfInterestSupplier.supplyForChannelNamed( wicaChannelName ).toArray( new String[] {} );
               serializer = new WicaChannelDataSerializer( numericScale, writeNanAsString, writeInfinityAsString, fieldsOfInterest );
            }

            gen.writeFieldName( wicaChannelName.toString() );
            final WicaChannelMetadata wicaChannelMetadata = (WicaChannelMetadata) value.get( wicaChannelName );
            final String str = serializer.serialize( wicaChannelMetadata );
            gen.writeRawValue( str );
         }
         gen.writeEndObject();
      }
   }

}
