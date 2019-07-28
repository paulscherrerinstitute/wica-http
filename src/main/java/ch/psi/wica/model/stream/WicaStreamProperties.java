/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

import static ch.psi.wica.model.channel.WicaChannelProperties.DEFAULT_FIELDS_OF_INTEREST;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SuppressWarnings( { "WeakerAccess", "unused" } )
@JsonIgnoreProperties()
@Immutable
public class WicaStreamProperties
{

/*- Public attributes --------------------------------------------------------*/

   /**
    * Default value for the heartbeat flux interval that will be assigned if
    * the property is not explicitly set by configuration on the client.
    */
   public static final int DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS = 10_000;

   /**
    * Default value for the changed value flux interval that will be assigned if
    * the property is not explicitly set by configuration on the client.
    */
   public static final int DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS = 100;

   /**
    * Default value for the polled value flux interval that will be assigned if
    * the property is not explicitly set by configuration on the client.
    */
   public static final int DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS = 100;


/*- Private attributes -------------------------------------------------------*/

   private static final WicaStreamProperties DEFAULT_INSTANCE = new Builder().build();

   private static final Logger logger = LoggerFactory.getLogger(WicaChannelProperties.class);
   private static final ObjectMapper objectMapper = new ObjectMapper();

   private final Integer heartbeatFluxIntervalInMillis;
   private final Integer monitoredValueFluxIntervalInMillis;
   private final Integer polledValueFluxIntervalInMillis;

   private final WicaChannelProperties defaultWicaChannelProperties;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaStreamProperties( Integer heartbeatFluxIntervalInMillis,
                                 Integer monitoredValueFluxIntervalInMillis,
                                 Integer polledValueFluxIntervalInMillis )
   {
      this.defaultWicaChannelProperties = WicaChannelProperties.createDefaultInstance();
      this.heartbeatFluxIntervalInMillis = (heartbeatFluxIntervalInMillis == null) ? DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS : heartbeatFluxIntervalInMillis;
      this.monitoredValueFluxIntervalInMillis = (monitoredValueFluxIntervalInMillis == null) ? DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS : monitoredValueFluxIntervalInMillis;
      this.polledValueFluxIntervalInMillis = (polledValueFluxIntervalInMillis == null) ? DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS : polledValueFluxIntervalInMillis;
   }

   private WicaStreamProperties( @JsonProperty( "heartbeat" ) Integer heartbeatFluxIntervalInMillis,
                                 @JsonProperty( "monflux" ) Integer monitoredValueFluxIntervalInMillis,
                                 @JsonProperty( "pollflux" ) Integer polledValueFluxIntervalInMillis,
                                 @JsonProperty( "daqmode" ) WicaChannelProperties.DataAcquisitionMode channelDataAcquisitionMode,
                                 @JsonProperty( "pollint" ) Integer channelPollingIntervalInMillis,
                                 @JsonProperty( "fields" ) String channelFieldsOfInterest,
                                 @JsonProperty( "prec" ) Integer channelNumericPrecision,
                                 @JsonProperty( "filter" ) WicaChannelProperties.FilterType channelFilterType,
                                 @JsonProperty( "n" ) Integer channelFilterNumSamples,
                                 @JsonProperty( "m" ) Integer channelFilterCycleLength,
                                 @JsonProperty( "interval" ) Integer channelFilterSamplingIntervalInMillis,
                                 @JsonProperty( "deadband" ) Double channelFilterDeadband )
   {
      this.defaultWicaChannelProperties = new WicaChannelProperties( channelDataAcquisitionMode,
                                                                     channelPollingIntervalInMillis,
                                                                     channelFieldsOfInterest,
                                                                     channelNumericPrecision,
                                                                     channelFilterType,
                                                                     channelFilterNumSamples,
                                                                     channelFilterCycleLength,
                                                                     channelFilterSamplingIntervalInMillis,
                                                                     channelFilterDeadband );

      this.heartbeatFluxIntervalInMillis = captureOrDefault( heartbeatFluxIntervalInMillis, DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS );
      this.monitoredValueFluxIntervalInMillis = captureOrDefault(monitoredValueFluxIntervalInMillis, DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS);
      this.polledValueFluxIntervalInMillis = captureOrDefault( polledValueFluxIntervalInMillis, DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS );
   }

/*- Class methods ------------------------------------------------------------*/

   /**
    * Returns an instance constructed from the information provided in the JSON
    * string descriptor.
    * <p>
    * The following keys define the properties that are unique to the stream.
    *
    * <code>heartbeat</code>
    * <code>monflux</code>
    * <code>pollflux</code>
    * <p>
    * The following keys define the default properties for the channels in
    * the stream when not explicitly specified in the optional channel
    * properties descriptor:
    *
    * <code>daqmode</code>
    * <code>pollratio</code>
    * <code>fields</code>
    * <code>prec</code>
    * <code>filter</code>
    * <code>n</code>
    * <code>m</code>
    * <code>interval</code>
    * <code>deadband</code>
    *
    * @param jsonStringSpecifier the JSON string representation containing the property
    *                            keys and values.
    * @return the instance.
    * @throws IllegalArgumentException if the string specifier syntax was invalid.
    */
   public static WicaStreamProperties of( String jsonStringSpecifier )
   {
      try
      {
         return objectMapper.readValue(jsonStringSpecifier, WicaStreamProperties.class);
      }
      catch ( Exception ex )
      {
         final String msg = "The input string: '" + jsonStringSpecifier + "' was not a valid descriptor for the properties of a wica stream.";
         logger.warn(msg);
         logger.warn("The underlying exception cause was: '{}'.", ex.getMessage());
         throw new IllegalArgumentException(msg);
      }
   }

   /**
    * Returns an instance with default properties.
    *
    * @return the instance.
    */
   public static WicaStreamProperties createDefaultInstance()
   {
      return DEFAULT_INSTANCE;
   }


   /**
    * Returns a builder.
    *
    * @return the builder.
    */
   public static Builder createBuilder()
   {
      return new Builder();
   }

/*- Public methods -----------------------------------------------------------*/

   @JsonProperty( "heartbeat" )
   public Integer getHeartbeatFluxIntervalInMillis()
   {
      return heartbeatFluxIntervalInMillis;
   }

   @JsonProperty( "monflux" )
   public Integer getMonitoredValueFluxIntervalInMillis()
   {
      return monitoredValueFluxIntervalInMillis;
   }

   @JsonProperty( "pollflux" )
   public Integer getPolledValueFluxIntervalInMillis()
   {
      return polledValueFluxIntervalInMillis;
   }

   @JsonIgnore
   public WicaChannelProperties getDefaultWicaChannelProperties()
   {
      return this.defaultWicaChannelProperties;
   }

   @JsonIgnore
   public WicaChannelProperties.DataAcquisitionMode getDataAcquisitionMode()
   {
      return defaultWicaChannelProperties.getDataAcquisitionMode();
   }

   @JsonIgnore
   public Set<String> getFieldsOfInterest()
   {
      return defaultWicaChannelProperties.getFieldsOfInterest();
   }

   @JsonIgnore
   public int getNumericPrecision()
   {
      return defaultWicaChannelProperties.getNumericPrecision();
   }

   @JsonIgnore
   public int getPolledValueSampleRatio()
   {
      return defaultWicaChannelProperties.getPollingIntervalInMillis();
   }

   @JsonIgnore
   public WicaChannelProperties.FilterType getFilterType()
   {
      return defaultWicaChannelProperties.getFilterType();
   }

   @JsonIgnore
   public int getFilterCycleLength()
   {
      return defaultWicaChannelProperties.getFilterCycleLength();
   }

   @JsonIgnore
   public int getFilterNumSamples()
   {
      return defaultWicaChannelProperties.getFilterNumSamples();
   }

   @JsonIgnore
   public double getFilterDeadband()
   {
      return defaultWicaChannelProperties.getFilterDeadband();
   }

   @JsonIgnore
   public int getFilterSamplingIntervalInMillis()
   {
      return defaultWicaChannelProperties.getFilterSamplingIntervalInMillis();
   }

   @Override
   public String toString()
   {
      return "WicaStreamProperties{" +
            "heartbeatFluxIntervalInMillis=" + heartbeatFluxIntervalInMillis +
            ", monitoredValueFluxIntervalInMillis=" + monitoredValueFluxIntervalInMillis +
            ", polledValueFluxIntervalInMillis=" + polledValueFluxIntervalInMillis +
            ", defaultWicaChannelProperties=" + defaultWicaChannelProperties +
            '}';
   }

   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaStreamProperties) ) return false;
      WicaStreamProperties that = (WicaStreamProperties) o;
      return Objects.equals(heartbeatFluxIntervalInMillis, that.heartbeatFluxIntervalInMillis) &&
             Objects.equals(monitoredValueFluxIntervalInMillis, that.monitoredValueFluxIntervalInMillis) &&
             Objects.equals(polledValueFluxIntervalInMillis, that.polledValueFluxIntervalInMillis) &&
             Objects.equals(defaultWicaChannelProperties, that.defaultWicaChannelProperties);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(heartbeatFluxIntervalInMillis, monitoredValueFluxIntervalInMillis, polledValueFluxIntervalInMillis, defaultWicaChannelProperties);
   }

/*- Private methods ----------------------------------------------------------*/

   protected static <T> T captureOrDefault( T arg, T defaultValue )
   {
      return arg == null ? defaultValue: arg;
   }

   private static Set<String> captureOrDefault( String inputString )
   {
      final String str = (inputString == null ) ? DEFAULT_FIELDS_OF_INTEREST : inputString;
      return Set.of( str.split( ";" ) );
   }

/*- Nested Classes -----------------------------------------------------------*/

   @SuppressWarnings( "unused" )
   public static class Builder
   {
      private int heartbeatFluxIntervalInMillis = DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS;
      private int monitoredValueFluxIntervalInMillis = DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      private int polledValueFluxIntervalInMillis = DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      private WicaChannelProperties.Builder wicaChannelPropertiesBuilder = WicaChannelProperties.createBuilder();

      // Private to force use of the createBuilder factory method.
      private Builder() {}

      public Builder withHeartbeatFluxInterval( int heartbeatFluxIntervalInMillis )
      {
         this.heartbeatFluxIntervalInMillis = heartbeatFluxIntervalInMillis;
         return this;
      }

      public Builder withMonitoredValueFluxInterval( int monitoredValueFluxIntervalInMillis )
      {
         this.monitoredValueFluxIntervalInMillis = monitoredValueFluxIntervalInMillis;
         return this;
      }

      public Builder withPolledValueFluxInterval( int polledValueFluxIntervalInMillis )
      {
         this.polledValueFluxIntervalInMillis = polledValueFluxIntervalInMillis;
         return this;
      }

      public Builder withDataAcquisitionMode( WicaChannelProperties.DataAcquisitionMode dataAcquisitionMode )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withDataAcquisitionMode( dataAcquisitionMode );
         return this;
      }

      public Builder withPolledValueSamplingRatio( int polledValueSamplingRatio )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withPollingInterval(polledValueSamplingRatio );
         return this;
      }

      public Builder withFieldsOfInterest( String fieldsOfInterest )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withFieldsOfInterest( fieldsOfInterest );
         return this;
      }

      public Builder withNumericPrecision( int numericPrecision )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withNumericPrecision( numericPrecision );
         return this;
      }

      public Builder withFilterType( WicaChannelProperties.FilterType filterType )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withFilterType( filterType );
         return this;
      }

      public Builder withNumSamples( int filterNumSamples )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withNumSamples( filterNumSamples );
         return this;
      }

      public Builder withFilterCycleLength( int filterCycleLength )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withFilterCycleLength( filterCycleLength );
         return this;
      }

      public Builder withFilterSamplingInterval( int filterSamplingInterval )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withFilterSamplingInterval( filterSamplingInterval );
         return this;
      }

      public Builder withFilterDeadband( double filterDeadband )
      {
         this.wicaChannelPropertiesBuilder = this.wicaChannelPropertiesBuilder.withFilterDeadband( filterDeadband );
         return this;
      }

      public WicaStreamProperties build()
      {
         final WicaChannelProperties defaultWicaChannelProperties = wicaChannelPropertiesBuilder.build();
         return new WicaStreamProperties( heartbeatFluxIntervalInMillis,
                                          monitoredValueFluxIntervalInMillis,
                                          polledValueFluxIntervalInMillis,
                                          defaultWicaChannelProperties.getDataAcquisitionMode(),
                                          defaultWicaChannelProperties.getPollingIntervalInMillis(),
                                          defaultWicaChannelProperties.getFieldsOfInterestAsString(),
                                          defaultWicaChannelProperties.getNumericPrecision(),
                                          defaultWicaChannelProperties.getFilterType(),
                                          defaultWicaChannelProperties.getFilterNumSamples(),
                                          defaultWicaChannelProperties.getFilterCycleLength(),
                                          defaultWicaChannelProperties.getFilterSamplingIntervalInMillis(),
                                          defaultWicaChannelProperties.getFilterDeadband());
      }

   }
}
