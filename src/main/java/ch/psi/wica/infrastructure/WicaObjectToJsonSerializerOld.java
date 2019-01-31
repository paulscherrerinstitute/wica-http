/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelType;
import ch.psi.wica.model.WicaChannelValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaObjectToJsonSerializerOld
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final ObjectMapper jsonObjectMapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaObjectToJsonSerializerOld( Set<String> fieldSelector, int numberOfDigits )
   {
     // Validate.notNull( fieldSelectors );
    //  final SimpleBeanPropertyFilter simpleBeanPropertyFilter = SimpleBeanPropertyFilter.filterOutAllExcept( fieldSelectors );
     // final FilterProvider filterProvider = new SimpleFilterProvider().addFilter( "WicaChannelValueFilter", simpleBeanPropertyFilter );

      final SimpleModule simpleModule = new SimpleModule();
      simpleModule.addSerializer( Double.class, new MyDoubleSerializer( numberOfDigits ) );

      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedReal.class, new MyWicaChannelValueConnectedRealSerializer( "val" ) );
      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedInteger.class, new MyWicaChannelValueConnectedIntegerSerializer( "val" ) );
      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedString.class, new MyWicaChannelValueConnectedStringSerializer( "val" ) );
      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedRealArray.class, new MyWicaChannelValueConnectedRealArraySerializer( "val" ) );
      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedIntegerArray.class, new MyWicaChannelValueConnectedIntegerArraySerializer( "val" ) );
      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedStringArray.class, new MyWicaChannelValueConnectedStringArraySerializer( "val" ) );

      jsonObjectMapper = new Jackson2ObjectMapperBuilder().createXmlMapper( false ).build();
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );
      //jsonObjectMapper.setFilterProvider( filterProvider );
      jsonObjectMapper.registerModule( simpleModule );
  }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   String convertWicaChannelMetadataToJsonRepresentation( WicaChannelMetadata channelMetadata )
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

   String convertWicaChannelValueToJsonRepresentation( WicaChannelValue channelValue )
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

   String convertWicaChannelValueListToJsonRepresentation( List<WicaChannelValue> channelValueList )
   {
      Validate.notNull( channelValueList );
      try
      {
         return jsonObjectMapper.writeValueAsString( channelValueList );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }

   public String convertWicaChannelValueMapToJsonRepresentation( Map<WicaChannelName,List<WicaChannelValue>> channelValueMap )
   {
      Validate.notNull( channelValueMap );
      try
      {
         return jsonObjectMapper.writeValueAsString( channelValueMap );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error";
      }
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   public static class MyDoubleSerializer extends JsonSerializer<Double>
   {
      private final int numberOfDigits;

      MyDoubleSerializer( int numberOfDigits )
      {
         this.numberOfDigits = numberOfDigits;
      }

      @Override
      public void serialize( Double value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         final BigDecimal bd = BigDecimal.valueOf( value ).setScale( numberOfDigits, RoundingMode.HALF_UP );
         gen.writeNumber( bd.toPlainString());
      }
   }

   public static class MyWicaChannelValueConnectedRealSerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedReal>
   {
      private final String fieldName;
      MyWicaChannelValueConnectedRealSerializer( String fieldName ) { this.fieldName = fieldName; }

      @Override
      public void serialize( WicaChannelValue.WicaChannelValueConnectedReal value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         if ( value.getSerializableFields().contains( fieldName ) )
         {
            gen.writeStartObject();
            gen.writeFieldName( fieldName );
            gen.writeNumber(value.getValue());
            MyWicaChannelValueSerializeHelper.serialize( value, gen );
            gen.writeEndObject();
         }
      }
   }
   
   public static class MyWicaChannelValueConnectedRealArraySerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedRealArray>
   {
      private final String fieldName;
      MyWicaChannelValueConnectedRealArraySerializer( String fieldName ) { this.fieldName = fieldName; }

      @Override
      public void serialize( WicaChannelValue.WicaChannelValueConnectedRealArray value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         if ( value.getSerializableFields().contains( fieldName ) )
         {
            gen.writeStartObject();
            gen.writeFieldName(fieldName);
            double[] valToWrite = value.getValue();
            gen.writeArray(valToWrite, 0, valToWrite.length);
            MyWicaChannelValueSerializeHelper.serialize( value, gen );
            gen.writeEndObject();
         }
      }
   }

   public static class MyWicaChannelValueConnectedIntegerSerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedInteger>
   {
      private final String fieldName;
      MyWicaChannelValueConnectedIntegerSerializer( String fieldName ) { this.fieldName = fieldName; }

      @Override
      public void serialize( WicaChannelValue.WicaChannelValueConnectedInteger value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         if ( value.getSerializableFields().contains( fieldName ) )
         {
            gen.writeStartObject();
            gen.writeFieldName(fieldName);
            gen.writeNumber(value.getValue());
            MyWicaChannelValueSerializeHelper.serialize( value, gen );
            gen.writeEndObject();
         }
      }
   }

   public static class MyWicaChannelValueConnectedIntegerArraySerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedIntegerArray>
   {
      private final String fieldName;
      MyWicaChannelValueConnectedIntegerArraySerializer( String fieldName ) { this.fieldName = fieldName; }

      @Override
      public void serialize( WicaChannelValue.WicaChannelValueConnectedIntegerArray value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         if ( value.getSerializableFields().contains( fieldName ) )
         {
            gen.writeStartObject();
            gen.writeFieldName(fieldName);
            int[] valToWrite = value.getValue();
            gen.writeArray(valToWrite, 0, valToWrite.length);
            MyWicaChannelValueSerializeHelper.serialize( value, gen );
            gen.writeEndObject();
         }
      }
   }

   public static class MyWicaChannelValueConnectedStringSerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedString>
   {
      private final String fieldName;
      MyWicaChannelValueConnectedStringSerializer( String fieldName ) { this.fieldName = fieldName; }

      @Override
      public void serialize( WicaChannelValue.WicaChannelValueConnectedString value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         gen.writeStartObject();
         gen.writeFieldName( fieldName );
         gen.writeString( value.getValue() );
         MyWicaChannelValueSerializeHelper.serialize( value, gen );
         gen.writeEndObject();
      }
   }

   public static class MyWicaChannelValueConnectedStringArraySerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedStringArray>
   {
      private final String fieldName;
      MyWicaChannelValueConnectedStringArraySerializer( String fieldName ) { this.fieldName = fieldName; }

      @Override
      public void serialize( WicaChannelValue.WicaChannelValueConnectedStringArray value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         if ( value.getSerializableFields().contains( fieldName ) )
         {
            gen.writeStartObject();
            final String[] valToWrite = value.getValue();
            if ( valToWrite.length > 0 )
            {
               gen.writeFieldName(fieldName);
               for ( String strVal : valToWrite )
               {
                  gen.writeString(strVal);
               }
            }
            MyWicaChannelValueSerializeHelper.serialize( value, gen );
            gen.writeEndObject();
         }
      }
   }

   public static class MyWicaChannelValueSerializeHelper
   {
      public static void serialize( WicaChannelValue.WicaChannelValueConnected value, JsonGenerator gen )
            throws IOException
      {

         if ( value.getSerializableFields().contains( "dsts" ) )
         {
            gen.writeFieldName( "dsts" );
            final LocalDateTime dsts = value.getDataSourceTimestamp();
            gen.writeObject( dsts );
         }

         if ( value.getSerializableFields().contains( "sevr" ) )
         {
            gen.writeFieldName( "sevr" );
            gen.writeNumber( value.getWicaAlarmSeverityAsInt() );
         }

         if ( value.getSerializableFields().contains( "stat" ) )
         {
            gen.writeFieldName( "stat" );
            gen.writeNumber( value.getWicaChannelAlarmStatusAsInt() );
         }
      }

   }


}
