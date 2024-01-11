/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelPropertiesDefaults;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamProperties;
import ch.psi.wica.model.stream.WicaStreamPropertiesDefaults;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SuppressWarnings("WeakerAccess")
public class WicaStreamSerializer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaStreamSerializer.class );

   private static final ObjectMapper serializerMapper = new ObjectMapper();
   private static final ObjectMapper deserializerMapper = new ObjectMapper();

   static
   {
      // This module provides the support needed for Optional serialization.
      serializerMapper.registerModule( new Jdk8Module() );

      // Needed  for sensible serializarion of DataAcquisitionMode and FilterType
      serializerMapper.enable( SerializationFeature.WRITE_ENUMS_USING_TO_STRING );

      serializerMapper.setVisibility( serializerMapper.getSerializationConfig()
                                                      .getDefaultVisibilityChecker()
                                                      .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                                                      .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                                      .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                                      .withCreatorVisibility(JsonAutoDetect.Visibility.NONE ) );

      // Needed  for sensible deserialization of DataAcquisitionMode and FilterType
      deserializerMapper.enable( DeserializationFeature.READ_ENUMS_USING_TO_STRING );

      // Add the required mixins
      serializerMapper.addMixIn( WicaStream.class, WicaStreamSerializerMixin.class );
      serializerMapper.addMixIn( WicaStreamProperties.class, WicaStreamPropertiesSerializerMixin.class );
      serializerMapper.addMixIn( WicaChannel.class, WicaChannelSerializerMixin.class );
      serializerMapper.addMixIn( WicaChannelProperties.class, WicaChannelPropertiesSerializerMixin.class );
      
      deserializerMapper.addMixIn( WicaStream.class, WicaStreamDeserializerMixin.class );
      deserializerMapper.addMixIn( WicaStreamProperties.class, WicaStreamPropertiesDeserializerMixin.class );
      deserializerMapper.addMixIn( WicaChannel.class, WicaChannelDeserializerMixin.class );
      deserializerMapper.addMixIn( WicaChannelProperties.class, WicaChannelPropertiesDeserializerMixin.class );
   }

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public static <T> String writeToJson( T t )
   {
      try
      {
         return serializerMapper.writeValueAsString( t );
      }
      catch ( JsonProcessingException ex )
      {
         final String msg =  "The wica object: '" + t.toString() + "' could not be serialized.";
         logger.warn( msg );
         logger.warn( "The underlying exception cause was: '{}'.", ex.getMessage() );
         throw new IllegalArgumentException( msg,ex );
      }
   }

   public static <T> T readFromJson( String jsonString, Class<T> valueType )
   {
      Validate.notNull( jsonString, "The 'jsonString' argument was null." );
      Validate.notNull( valueType, "The 'valueType' argument was null." );
      try
      {
         return deserializerMapper.readValue( jsonString, valueType );
      }
      catch( IOException ex )
      {
         final String msg = "The input string: '" + jsonString + "' could not be deserialized.";
         logger.warn( msg );
         logger.warn( "The underlying exception cause was: '{}'.", ex.getMessage() );
         throw new IllegalArgumentException( msg,ex );
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes: Serializers ----------------------------------------------*/
   
   /**
    * WicaStreamSerializerMixin
    */
   @JsonPropertyOrder( { "props", "channels" } )
   public static abstract class WicaStreamSerializerMixin extends WicaStream
   {
      @JsonInclude( value = JsonInclude.Include.CUSTOM, valueFilter = WicaStreamDefaultPropertiesFilter.class )
      @Override public abstract @JsonProperty( "props"    ) WicaStreamProperties getWicaStreamProperties();
   
      @JsonInclude( value = JsonInclude.Include.NON_ABSENT)
      @Override public abstract @JsonProperty( "channels" ) Set<WicaChannel> getWicaChannels();
   }

   /**
    * WicaStreamPropertiesSerializerMixin
    */
   @JsonPropertyOrder( { "hbflux", "metaflux", "monflux", "pollflux", "daqmode", "pollint", "fields", "prec", "filter", "n", "x", "m", "interval", "deadband" } )
   @JsonInclude( JsonInclude.Include.NON_DEFAULT )
   public static abstract class WicaStreamPropertiesSerializerMixin extends WicaStreamProperties
   {
      @Override public abstract @JsonProperty( "hbflux"   ) Optional<Integer> getOptionalHeartbeatFluxIntervalInMillis();
      @Override public abstract @JsonProperty( "metaflux" ) Optional<Integer> getOptionalMetadataFluxIntervalInMillis();
      @Override public abstract @JsonProperty( "monflux"  ) Optional<Integer> getOptionalMonitoredValueFluxIntervalInMillis();
      @Override public abstract @JsonProperty( "pollflux" ) Optional<Integer> getOptionalPolledValueFluxIntervalInMillis();
      @Override public abstract @JsonProperty( "daqmode"  ) Optional<WicaDataAcquisitionMode> getOptionalDataAcquisitionMode();
      @Override public abstract @JsonProperty( "pollint"  ) Optional<Integer> getOptionalPollingIntervalInMillis();
      @Override public abstract @JsonProperty( "fields"   ) Optional<String> getOptionalFieldsOfInterest();
      @Override public abstract @JsonProperty( "prec"     ) Optional<Integer> getOptionalNumericPrecision();
      @Override public abstract @JsonProperty( "filter"   ) Optional<WicaFilterType> getOptionalFilterType();
      @Override public abstract @JsonProperty( "n"        ) Optional<Integer> getOptionalFilterNumSamples();
      @Override public abstract @JsonProperty( "x"        ) Optional<Integer> getOptionalFilterNumSamplesInAverage();
      @Override public abstract @JsonProperty( "m"        ) Optional<Integer> getOptionalFilterCycleLength();
      @Override public abstract @JsonProperty( "interval" ) Optional<Integer> getOptionalFilterSamplingIntervalInMillis();
      @Override public abstract @JsonProperty( "deadband" ) Optional<Double> getOptionalFilterDeadband();
   }

   /**
    * WicaChannelSerializerMixin
    */
   @JsonInclude( JsonInclude.Include.NON_DEFAULT )
   @JsonPropertyOrder( { "name", "props" } )
   public static abstract class WicaChannelSerializerMixin extends WicaChannel
   {
      @Override public abstract @JsonProperty( "name" ) String getNameAsString();

      @JsonInclude( value = JsonInclude.Include.CUSTOM, valueFilter = WicaChannelDefaultPropertiesFilter.class )
      @Override public abstract @JsonProperty( "props" ) WicaChannelProperties getProperties();
   }

   /**
    * WicaChannelPropertiesSerializerMixin
    */
   // Note: Include.NON_DEFAULT is selected so that serializer will only send the values
   // that are different from the defaults.
   @JsonInclude( value = JsonInclude.Include.NON_DEFAULT)
   @JsonPropertyOrder( { "daqmode", "pollint", "fields", "prec", "filter", "n", "x", "m", "interval", "deadband" } )
   public static abstract class WicaChannelPropertiesSerializerMixin extends WicaChannelProperties
   {
      @Override public abstract @JsonProperty( "daqmode"  ) Optional<WicaDataAcquisitionMode> getOptionalDataAcquisitionMode();
      @Override public abstract @JsonProperty( "pollint"  ) Optional<Integer> getOptionalPollingIntervalInMillis();
      @Override public abstract @JsonProperty( "fields"   ) Optional<String> getOptionalFieldsOfInterest();
      @Override public abstract @JsonProperty( "prec"     ) Optional<Integer> getOptionalNumericPrecision();
      @Override public abstract @JsonProperty( "filter"   ) Optional<WicaFilterType> getOptionalFilterType();
      @Override public abstract @JsonProperty( "n"        ) Optional<Integer> getOptionalFilterNumSamples();
      @Override public abstract @JsonProperty( "x"        ) Optional<Integer> getOptionalFilterNumSamplesInAverage();
      @Override public abstract @JsonProperty( "m"        ) Optional<Integer> getOptionalFilterCycleLength();
      @Override public abstract @JsonProperty( "interval" ) Optional<Integer> getOptionalFilterSamplingIntervalInMillis();
      @Override public abstract @JsonProperty( "deadband" ) Optional<Double> getOptionalFilterDeadband();
   }
   
/*- Nested Classes: Deserializers --------------------------------------------*/
   
   /**
    * WicaStreamDeserializerMixin
    */
   public static abstract class WicaStreamDeserializerMixin extends WicaStream
   {
      // WARNING: Signature here must match EXACTLY with that in WicaStream otherwise deserialisation will fail.
      @SuppressWarnings( "unused" )
      @JsonCreator
      public WicaStreamDeserializerMixin( @JsonProperty( "props"    ) WicaStreamProperties wicaStreamProperties,
                                          @JsonProperty( "channels" ) Set<WicaChannel> wicaChannels) {}
   }
   
   /**
    * WicaStreamPropertiesDeserializerMixin
    */
   public static abstract class WicaStreamPropertiesDeserializerMixin extends WicaStreamProperties
   {
      // WARNING: Signature here must match EXACTLY with that in WicaStreamProperties otherwise deserialisation will fail.
      @SuppressWarnings( "unused" )
      @JsonCreator
      public WicaStreamPropertiesDeserializerMixin( @JsonProperty( "quietMode" ) Boolean quietMode,
                                                    @JsonProperty( "hbflux"    ) Integer heartbeatFluxIntervalInMillis,
                                                    @JsonProperty( "metaflux"  ) Integer metadataFluxIntervalInMillis,
                                                    @JsonProperty( "monflux"   ) Integer monitoredValueFluxIntervalInMillis,
                                                    @JsonProperty( "pollflux"  ) Integer polledValueFluxIntervalInMillis,
                                                    @JsonProperty( "daqmode"   ) WicaDataAcquisitionMode dataAcquisitionMode,
                                                    @JsonProperty( "pollint"   ) Integer pollingIntervalInMillis,
                                                    @JsonProperty( "fields"    ) String fieldsOfInterest,
                                                    @JsonProperty( "prec"      ) Integer numericPrecision,
                                                    @JsonProperty( "filter"    ) WicaFilterType filterType,
                                                    @JsonProperty( "n"         ) Integer filterNumSamples,
                                                    @JsonProperty( "x"         ) Integer filterNumSamplesInAverage,
                                                    @JsonProperty( "m"         ) Integer filterCycleLength,
                                                    @JsonProperty( "interval"  ) Integer filterSamplingIntervalInMillis,
                                                    @JsonProperty( "deadband"  ) Double filterDeadband ) {}
   }

   /**
    * WicaChannelDeserializerMixin
    */
   public static abstract class WicaChannelDeserializerMixin extends WicaChannel
   {
      // WARNING: Signature here must match EXACTLY with that in WicaChannel otherwise deserialisation will fail.
      @SuppressWarnings( "unused" )
      @JsonCreator
      public WicaChannelDeserializerMixin( @JsonProperty( "name"  ) WicaChannelName wicaChannelName,
                                           @JsonProperty( "props" ) WicaChannelProperties wicaChannelProperties ) {}
   }

   /**
    * WicaChannelPropertiesDeserializerMixin
    */
   public static abstract class WicaChannelPropertiesDeserializerMixin extends WicaChannelProperties
   {
      // WARNING: Signature here must match EXACTLY with that in WicaChannelProperties otherwise deserialisation will fail.
      @SuppressWarnings( "unused" )
      @JsonCreator
      public WicaChannelPropertiesDeserializerMixin( @JsonProperty( "daqmode"  ) WicaDataAcquisitionMode dataAcquisitionMode,
                                                     @JsonProperty( "pollint"  ) Integer pollingIntervalInMillis,
                                                     @JsonProperty( "fields"   ) String fieldsOfInterest,
                                                     @JsonProperty( "prec"     ) Integer numericPrecision,
                                                     @JsonProperty( "filter"   ) WicaFilterType filterType,
                                                     @JsonProperty( "n"        ) Integer filterNumSamples,
                                                     @JsonProperty( "x"        ) Integer filterNumSamplesInAverage,
                                                     @JsonProperty( "m"        ) Integer filterCycleLength,
                                                     @JsonProperty( "interval" ) Integer filterSamplingIntervalInMillis,
                                                     @JsonProperty( "deadband" ) Double filterDeadband ) {}
   }

/*- Nested Classes: Filters --------------------------------------------------*/

   /**
    * WicaStreamDefaultPropertiesFilter
    */
   public static class WicaStreamDefaultPropertiesFilter
   {
      @Override
      public boolean equals( Object other )
      {
         final boolean suppressOutput;
         if ( other instanceof WicaStreamProperties props )
         {
            suppressOutput =
               optEqualsDefaultValue( props.getOptionalHeartbeatFluxIntervalInMillis(),      WicaStreamPropertiesDefaults.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS       ) &&
               optEqualsDefaultValue( props.getOptionalMetadataFluxIntervalInMillis(),       WicaStreamPropertiesDefaults.DEFAULT_METADATA_FLUX_INTERVAL_IN_MILLIS        ) &&
               optEqualsDefaultValue( props.getOptionalMonitoredValueFluxIntervalInMillis(), WicaStreamPropertiesDefaults.DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS ) &&
               optEqualsDefaultValue( props.getOptionalPolledValueFluxIntervalInMillis(),    WicaStreamPropertiesDefaults.DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS    ) &&
               optEqualsDefaultValue( props.getOptionalPollingIntervalInMillis(),            WicaStreamPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS              ) &&
               optEqualsDefaultValue( props.getOptionalDataAcquisitionMode(),                WicaStreamPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE                   ) &&
               optEqualsDefaultValue( props.getOptionalFieldsOfInterest(),                   WicaStreamPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST                      ) &&
               optEqualsDefaultValue( props.getOptionalNumericPrecision(),                   WicaStreamPropertiesDefaults.DEFAULT_NUMERIC_PRECISION                       ) &&
               optEqualsDefaultValue( props.getOptionalFilterType(),                         WicaStreamPropertiesDefaults.DEFAULT_FILTER_TYPE                             ) &&
               optEqualsDefaultValue( props.getOptionalFilterNumSamples(),                   WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES                      ) &&
               optEqualsDefaultValue( props.getOptionalFilterNumSamplesInAverage(),          WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES_IN_AVERAGE           ) &&
               optEqualsDefaultValue( props.getOptionalFilterCycleLength(),                  WicaStreamPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH                     ) &&
               optEqualsDefaultValue( props.getOptionalFilterSamplingIntervalInMillis(),     WicaStreamPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS      ) &&
               optEqualsDefaultValue( props.getOptionalFilterDeadband(),                     WicaStreamPropertiesDefaults.DEFAULT_FILTER_DEADBAND                         );
         }
         else
         {
            suppressOutput = false;
         }
         return suppressOutput;
      }

      @SuppressWarnings( "OptionalUsedAsFieldOrParameterType" )
      private <T> boolean optEqualsDefaultValue( Optional<T> opt, T defaultValue )
      {
         return opt.isPresent() && opt.get().equals( defaultValue );
      }
   }

   /**
    * WicaChannelDefaultPropertiesFilter
    */
   public static class WicaChannelDefaultPropertiesFilter
   {
      @Override
      public boolean equals( Object other )
      {
         final boolean suppressOutput;
         if ( other instanceof WicaChannelProperties props )
         {
            suppressOutput =
               optEqualsDefaultValue( props.getOptionalPollingIntervalInMillis(),        WicaChannelPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS              ) &&
               optEqualsDefaultValue( props.getOptionalDataAcquisitionMode(),            WicaChannelPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE                   ) &&
               optEqualsDefaultValue( props.getOptionalFieldsOfInterest(),               WicaChannelPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST                      ) &&
               optEqualsDefaultValue( props.getOptionalNumericPrecision(),               WicaChannelPropertiesDefaults.DEFAULT_NUMERIC_PRECISION                       ) &&
               optEqualsDefaultValue( props.getOptionalFilterType(),                     WicaChannelPropertiesDefaults.DEFAULT_FILTER_TYPE                             ) &&
               optEqualsDefaultValue( props.getOptionalFilterNumSamples(),               WicaChannelPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES                      ) &&
               optEqualsDefaultValue( props.getOptionalFilterCycleLength(),              WicaChannelPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH                     ) &&
               optEqualsDefaultValue( props.getOptionalFilterSamplingIntervalInMillis(), WicaChannelPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS      ) &&
               optEqualsDefaultValue( props.getOptionalFilterDeadband(),                 WicaChannelPropertiesDefaults.DEFAULT_FILTER_DEADBAND                         );
         }
         else
         {
            suppressOutput = false;
         }
         return suppressOutput;
      }

      @SuppressWarnings( "OptionalUsedAsFieldOrParameterType" )
      private <T> boolean optEqualsDefaultValue( Optional<T> opt, T defaultValue )
      {
         return opt.isPresent() && opt.get().equals( defaultValue );
      }
   }

}
