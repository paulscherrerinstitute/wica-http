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

   public WicaChannelMetadataMapSerializer( FieldsOfInterestSupplier fieldsOfInterestSupplier,
                                            NumericScaleSupplier numericScaleSupplier,
                                            boolean quoteNumericStrings )
   {
        final SimpleModule module = new SimpleModule();
        module.addSerializer( new MyCustomWicaChannelMetadataMapSerializer( fieldsOfInterestSupplier, numericScaleSupplier, quoteNumericStrings ) );
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
/*- Nested Classes -----------------------------------------------------------*/

   private static class MyCustomWicaChannelMetadataMapSerializer extends StdSerializer<Map>
   {
      final FieldsOfInterestSupplier fieldsOfInterestSupplier;
      final NumericScaleSupplier numericScaleSupplier;
      final boolean quoteNumericStrings;

      MyCustomWicaChannelMetadataMapSerializer( FieldsOfInterestSupplier fieldsOfInterestSupplier,
                                                NumericScaleSupplier numericScaleSupplier,
                                                boolean quoteNumericStrings)
      {
         super( Map.class );
         this.numericScaleSupplier = numericScaleSupplier;
         this.quoteNumericStrings  = quoteNumericStrings;
         this.fieldsOfInterestSupplier = fieldsOfInterestSupplier;
      }

      @Override
      public void serialize( Map value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
      {
         gen.writeStartObject();
         for ( Object channelName : value.keySet() )
         {
            final WicaChannelName wicaChannelName = (WicaChannelName) channelName;
            final int numericScale = numericScaleSupplier.supplyForChannelNamed( wicaChannelName );

            final WicaChannelDataSerializer serializer;
            if ( fieldsOfInterestSupplier.supplyForChannelNamed( wicaChannelName ).size() == 0 )
            {
               serializer = new WicaChannelDataSerializer( numericScale, quoteNumericStrings );
            }
            else
            {
               final Set<String> fieldsOfInterest = fieldsOfInterestSupplier.supplyForChannelNamed( wicaChannelName );
               serializer = new WicaChannelDataSerializer( fieldsOfInterest, numericScale, quoteNumericStrings );
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
