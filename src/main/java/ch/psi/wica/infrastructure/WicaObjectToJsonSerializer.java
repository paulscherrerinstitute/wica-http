/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
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
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

   private final ObjectMapper jsonValueMapper;
   private final ObjectMapper jsonMetadataMapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaObjectToJsonSerializer( Map<WicaChannelName,Set<String>> fieldsOfInterest, int numberOfDigits )
   {
     // Validate.notNull( fieldSelectors );
      final SimpleBeanPropertyFilter simpleBeanPropertyFilter = SimpleBeanPropertyFilter.filterOutAllExcept( "val", "sevr", "dsts" );
      final FilterProvider filterProvider = new SimpleFilterProvider().addFilter( "WicaChannelValueFilter", simpleBeanPropertyFilter );

      final SimpleModule simpleModule = new SimpleModule();
      simpleModule.addSerializer( Double.class, new MyDoubleSerializer( numberOfDigits ) );
      //simpleModule.addSerializer( new MyMetadataMapSerializer() );
      simpleModule.addSerializer( new MyValueMapSerializer( fieldsOfInterest ) );

//      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedReal.class, new MyWicaChannelValueConnectedRealSerializer( "val" ) );
//      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedInteger.class, new MyWicaChannelValueConnectedIntegerSerializer( "val" ) );
//      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedString.class, new MyWicaChannelValueConnectedStringSerializer( "val" ) );
//      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedRealArray.class, new MyWicaChannelValueConnectedRealArraySerializer( "val" ) );
//      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedIntegerArray.class, new MyWicaChannelValueConnectedIntegerArraySerializer( "val" ) );
//      simpleModule.addSerializer(WicaChannelValue.WicaChannelValueConnectedStringArray.class, new MyWicaChannelValueConnectedStringArraySerializer( "val" ) );

      jsonValueMapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false ).build();
      jsonValueMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      jsonValueMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );
      jsonValueMapper.setFilterProvider(filterProvider );
      jsonValueMapper.registerModule(simpleModule );

      jsonMetadataMapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false ).build();
      jsonMetadataMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      jsonMetadataMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );

   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   String convertWicaChannelMetadataToJsonRepresentation( WicaChannelMetadata channelMetadata )
   {
      Validate.notNull( channelMetadata );
      try
      {
         return jsonMetadataMapper.writeValueAsString(channelMetadata );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }

   public String convertWicaChannelMetadataMapToJsonRepresentation( Map<WicaChannelName,WicaChannelMetadata> channelMetadataMap )
   {
      Validate.notNull( channelMetadataMap );
      try
      {
         return jsonMetadataMapper.writeValueAsString(channelMetadataMap );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }

   String convertWicaChannelValueToJsonRepresentation( WicaChannelValue channelValue )
   {
      Validate.notNull( channelValue );
      try
      {
         return jsonValueMapper.writeValueAsString(channelValue);
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }

   String convertWicaChannelValueListToJsonRepresentation( List<WicaChannelValue> channelValueList )
   {
      Validate.notNull( channelValueList );
      try
      {
         return jsonValueMapper.writeValueAsString(channelValueList );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }

   public String convertWicaChannelValueMapToJsonRepresentation( Map<WicaChannelName,List<WicaChannelValue>> channelValueMap )
   {
      Validate.notNull( channelValueMap );
      try
      {
         return jsonValueMapper.writeValueAsString(channelValueMap );
      }
      catch ( JsonProcessingException ex )
      {
         return "json serialisation error: " + ex.getMessage();
      }
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   public static class MyMetadataMapSerializer extends StdSerializer<Map>
   {
      MyMetadataMapSerializer()
      {
         super( Map.class );
      }

      @Override
      public void serialize( Map value, JsonGenerator gen, SerializerProvider provider ) throws IOException
      {
         gen.writeStartObject();

         for ( Object wicaChannelName : value.keySet() )
         {
            gen.writeFieldName( wicaChannelName.toString() );
            gen.writeStartObject();
            gen.writeObject( value.get( wicaChannelName ) );
            gen.writeEndObject();
         }
         gen.writeEndObject();
      }
   }


   public static class MyValueMapSerializer extends StdSerializer<Map>
   {
      final Map<WicaChannelName,Set<String>> channelFieldsOfInterestMap;

      MyValueMapSerializer( Map<WicaChannelName,Set<String>> channelFieldsOfInterestMap )
      {
         super( Map.class );
         this.channelFieldsOfInterestMap = channelFieldsOfInterestMap;
      }

      @Override
      public void serialize( Map value, JsonGenerator gen, SerializerProvider serializers )
            throws IOException
      {
         gen.writeStartObject();
         for ( Object channelName : value.keySet() )
         {
            final WicaChannelName wicaChannelName = (WicaChannelName) channelName;
            final Set<String> fieldsOfInterest = channelFieldsOfInterestMap.get( wicaChannelName );
            gen.writeFieldName( wicaChannelName.toString() );
            final SimpleModule simpleModule = new SimpleModule();
            final var jsonObjMapper = new Jackson2ObjectMapperBuilder().createXmlMapper( false ).build();
            jsonObjMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
            jsonObjMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );
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

//   public static class MyWicaChannelValueConnectedRealSerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedReal>
//   {
//      private final String fieldName;
//      MyWicaChannelValueConnectedRealSerializer( String fieldName ) { this.fieldName = fieldName; }
//
//      @Override
//      public void serialize( WicaChannelValue.WicaChannelValueConnectedReal value, JsonGenerator gen, SerializerProvider serializers )
//            throws IOException
//      {
//         if ( value.getSerializableFields().contains( fieldName ) )
//         {
//            gen.writeStartObject();
//            gen.writeFieldName( fieldName );
//            gen.writeNumber(value.getValue());
//            MyWicaChannelValueSerializeHelper.serialize(value, gen );
//            gen.writeEndObject();
//         }
//      }
//   }
//
//   public static class MyWicaChannelValueConnectedRealArraySerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedRealArray>
//   {
//      private final String fieldName;
//      MyWicaChannelValueConnectedRealArraySerializer( String fieldName ) { this.fieldName = fieldName; }
//
//      @Override
//      public void serialize( WicaChannelValue.WicaChannelValueConnectedRealArray value, JsonGenerator gen, SerializerProvider serializers )
//            throws IOException
//      {
//         if ( value.getSerializableFields().contains( fieldName ) )
//         {
//            gen.writeStartObject();
//            gen.writeFieldName(fieldName);
//            double[] valToWrite = value.getValue();
//            gen.writeArray(valToWrite, 0, valToWrite.length);
//            MyWicaChannelValueSerializeHelper.serialize(value, gen );
//            gen.writeEndObject();
//         }
//      }
//   }
//
//   public static class MyWicaChannelValueConnectedIntegerSerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedInteger>
//   {
//      private final String fieldName;
//      MyWicaChannelValueConnectedIntegerSerializer( String fieldName ) { this.fieldName = fieldName; }
//
//      @Override
//      public void serialize( WicaChannelValue.WicaChannelValueConnectedInteger value, JsonGenerator gen, SerializerProvider serializers )
//            throws IOException
//      {
//         if ( value.getSerializableFields().contains( fieldName ) )
//         {
//            gen.writeStartObject();
//            gen.writeFieldName(fieldName);
//            gen.writeNumber(value.getValue());
//            MyWicaChannelValueSerializeHelper.serialize(value, gen );
//            gen.writeEndObject();
//         }
//      }
//   }
//
//   public static class MyWicaChannelValueConnectedIntegerArraySerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedIntegerArray>
//   {
//      private final String fieldName;
//      MyWicaChannelValueConnectedIntegerArraySerializer( String fieldName ) { this.fieldName = fieldName; }
//
//      @Override
//      public void serialize( WicaChannelValue.WicaChannelValueConnectedIntegerArray value, JsonGenerator gen, SerializerProvider serializers )
//            throws IOException
//      {
//         if ( value.getSerializableFields().contains( fieldName ) )
//         {
//            gen.writeStartObject();
//            gen.writeFieldName(fieldName);
//            int[] valToWrite = value.getValue();
//            gen.writeArray(valToWrite, 0, valToWrite.length);
//            MyWicaChannelValueSerializeHelper.serialize(value, gen );
//            gen.writeEndObject();
//         }
//      }
//   }
//
//   public static class MyWicaChannelValueConnectedStringSerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedString>
//   {
//      private final String fieldName;
//      MyWicaChannelValueConnectedStringSerializer( String fieldName ) { this.fieldName = fieldName; }
//
//      @Override
//      public void serialize( WicaChannelValue.WicaChannelValueConnectedString value, JsonGenerator gen, SerializerProvider serializers )
//            throws IOException
//      {
//         gen.writeStartObject();
//         gen.writeFieldName( fieldName );
//         gen.writeString( value.getValue() );
//         MyWicaChannelValueSerializeHelper.serialize(value, gen );
//         gen.writeEndObject();
//      }
//   }
//
//   public static class MyWicaChannelValueConnectedStringArraySerializer extends JsonSerializer<WicaChannelValue.WicaChannelValueConnectedStringArray>
//   {
//      private final String fieldName;
//      MyWicaChannelValueConnectedStringArraySerializer( String fieldName ) { this.fieldName = fieldName; }
//
//      @Override
//      public void serialize( WicaChannelValue.WicaChannelValueConnectedStringArray value, JsonGenerator gen, SerializerProvider serializers )
//            throws IOException
//      {
//         if ( value.getSerializableFields().contains( fieldName ) )
//         {
//            gen.writeStartObject();
//            final String[] valToWrite = value.getValue();
//            if ( valToWrite.length > 0 )
//            {
//               gen.writeFieldName(fieldName);
//               for ( String strVal : valToWrite )
//               {
//                  gen.writeString(strVal);
//               }
//            }
//            MyWicaChannelValueSerializeHelper.serialize(value, gen );
//            gen.writeEndObject();
//         }
//      }
//   }
//
//   public static class MyWicaChannelValueSerializeHelper
//   {
//      public static void serialize( WicaChannelValue.WicaChannelValueConnected value, JsonGenerator gen )
//            throws IOException
//      {
//
//         if ( value.getSerializableFields().contains( "dsts" ) )
//         {
//            gen.writeFieldName( "dsts" );
//            final LocalDateTime dsts = value.getDataSourceTimestamp();
//            gen.writeObject( dsts );
//         }
//
//         if ( value.getSerializableFields().contains( "sevr" ) )
//         {
//            gen.writeFieldName( "sevr" );
//            gen.writeNumber( value.getWicaAlarmSeverity() );
//         }
//
//         if ( value.getSerializableFields().contains( "stat" ) )
//         {
//            gen.writeFieldName( "stat" );
//            gen.writeNumber( value.getWicaChannelAlarmStatus() );
//         }
//      }
//
//   }
//





}
