/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

@SuppressWarnings( "WeakerAccess")
@JsonIgnoreProperties()
@Immutable
public class WicaChannelProperties
{

/*- Public attributes --------------------------------------------------------*/

   /**
    * Default value for the  data acquisition mode that will be used
    * when acquiring data samples for this channel.
    */
   public static final DataAcquisitionMode DEFAULT_DATA_ACQUISITION_MODE = DataAcquisitionMode.MONITOR;

   /**
    * Default value for the ratio of polling
    * if the property is not explicitly set by configuration on the client.
    */
   public static final int DEFAULT_POLLING_INTERVAL = 100;

   /**
    * Default value for the numeric precision that will be used when
    * serializing real numbers in the wica stream for this channel.
    */
   public static final int DEFAULT_NUMERIC_PRECISION = 8;

   /**
    * Default value for the fields of interest that will be serialized
    * in the wica data stream for this channel.
    */
   public static final String DEFAULT_FIELDS_OF_INTEREST = "val;sevr";

   /**
    * Default value for the type of filter that will be used when
    * mapping between the data samples acquired on a wica channel
    * and the values serialized in the wica data stream.
    */
   public static final FilterType DEFAULT_FILTER_TYPE = FilterType.LAST_N;

   /**
    * Default value for the number of samples ((used for channels whose
    * filter type is FilterType.LAST_N).
    */
   public static final int DEFAULT_FILTER_NUM_SAMPLES = 1;

   /**
    * Default value for the cycle length (used for channels whose
    * filter type is FilterType.ONE_IN_M).
    */
   public static final int DEFAULT_FILTER_CYCLE_LENGTH = 1;

   /**
    * Default value for the sampling interval (used for channels whose
    * filter type is FilterType.ONE_IN_M).
    */
   public static final int DEFAULT_FILTER_SAMPLING_INTERVAL = 1000;

   /**
    * Default value for the deadband (used for channels whose
    * filter type is FilterType.CHANGE_FILTERER).
    */
   public static final double DEFAULT_FILTER_DEADBAND = 1.0;


/*- Private attributes -------------------------------------------------------*/

   private static final WicaChannelProperties DEFAULT_INSTANCE = new Builder().build();

   private static final Logger logger = LoggerFactory.getLogger( WicaChannelProperties.class );
   private static final ObjectMapper objectMapper = new ObjectMapper();

   private final DataAcquisitionMode dataAcquisitionMode;
   private final Integer pollingIntervalInMillis;
   private final Integer numericPrecision;
   private final FilterType filterType ;
   private final Integer filterNumSamples;
   private final Integer filterCycleLength;
   private final Integer filterSamplingIntervalInMillis;
   private final Double filterDeadband;
   private final Set<String> fieldsOfInterest;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelProperties()
   {
      this( DEFAULT_DATA_ACQUISITION_MODE,
            DEFAULT_POLLING_INTERVAL,
            DEFAULT_FIELDS_OF_INTEREST,
            DEFAULT_NUMERIC_PRECISION,
            DEFAULT_FILTER_TYPE,
            DEFAULT_FILTER_NUM_SAMPLES,
            DEFAULT_FILTER_CYCLE_LENGTH,
            DEFAULT_FILTER_SAMPLING_INTERVAL,
            DEFAULT_FILTER_DEADBAND );
   }

   public WicaChannelProperties( @JsonProperty( "daqmode"   ) DataAcquisitionMode dataAcquisitionMode,
                                 @JsonProperty( "pollint" ) Integer pollingIntervalInMillis,
                                 @JsonProperty( "fields"    ) String fieldsOfInterest,
                                 @JsonProperty( "prec"      ) Integer numericPrecision,
                                 @JsonProperty( "filter"    ) FilterType filterType,
                                 @JsonProperty( "n"         ) Integer filterNumSamples,
                                 @JsonProperty( "m"         ) Integer filterCycleLength,
                                 @JsonProperty( "interval"  ) Integer filterSamplingIntervalInMillis,
                                 @JsonProperty( "deadband"  ) Double filterDeadband )
   {
      this.dataAcquisitionMode            = captureOrDefault( dataAcquisitionMode, DEFAULT_DATA_ACQUISITION_MODE );
      this.pollingIntervalInMillis        = captureOrDefault( pollingIntervalInMillis, DEFAULT_POLLING_INTERVAL);
      this.numericPrecision               = captureOrDefault( numericPrecision, DEFAULT_NUMERIC_PRECISION );
      this.filterType                     = captureOrDefault( filterType, DEFAULT_FILTER_TYPE );
      this.filterNumSamples               = captureOrDefault( filterNumSamples, DEFAULT_FILTER_NUM_SAMPLES );
      this.filterCycleLength              = captureOrDefault( filterCycleLength, DEFAULT_FILTER_CYCLE_LENGTH );
      this.filterSamplingIntervalInMillis = captureOrDefault( filterSamplingIntervalInMillis, DEFAULT_FILTER_SAMPLING_INTERVAL );
      this.filterDeadband                 = captureOrDefault( filterDeadband, DEFAULT_FILTER_DEADBAND );
      this.fieldsOfInterest               = captureOrDefault( fieldsOfInterest );
   }

/*- Class methods ------------------------------------------------------------*/

   /**
    * Returns an instance constructed from the information provided in the JSON
    * string descriptor.
    *
    * The following keys and values are supported:
    *
    * <code>daqmode</code>
    * <code>pollint</code>
    * <code>fields</code>
    * <code>prec</code>
    * <code>filter</code>
    * <code>n</code>
    * <code>m</code>
    * <code>interval</code>
    * <code>deadband</code>
    *
    * @param jsonStringSpecifier the JSON string representation containing the property
    *     keys and values.
    * @return the instance.
    * @throws IllegalArgumentException if the string speicifier syntax was invalid.
    */
   public static WicaChannelProperties of( String jsonStringSpecifier)
   {
      try
      {
         return objectMapper.readValue( jsonStringSpecifier, WicaChannelProperties.class);
      }
      catch ( Exception ex )
      {
         final String msg =  "The input string: '" + jsonStringSpecifier + "' was not a valid descriptor for the properties of a wica channel.";
         logger.warn( msg );
         logger.warn( "The underlying exception cause was: '{}'.", ex.getMessage() );
         throw new IllegalArgumentException( msg );
      }
   }

   /**
    * Returns an instance with default properties.
    *
    * @return the instance.
    */
   public static WicaChannelProperties createDefaultInstance()
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

   @JsonIgnore
   public DataAcquisitionMode getDataAcquisitionMode()
   {
      return dataAcquisitionMode;
   }

   @JsonIgnore
   public Set<String> getFieldsOfInterest()
   {
      return fieldsOfInterest;
   }
   public String getFieldsOfInterestAsString()
   {
      return String.join(";", fieldsOfInterest);
   }

   @JsonIgnore
   public int getNumericPrecision()
   {
      return numericPrecision;
   }

   @JsonIgnore
   public int getPollingIntervalInMillis()
   {
      return pollingIntervalInMillis;
   }

   @JsonIgnore
   public FilterType getFilterType()
   {
      return filterType;
   }

   @JsonIgnore
   public int getFilterCycleLength()
   {
      return filterCycleLength;
   }

   @JsonIgnore
   public int getFilterNumSamples()
   {
      return filterNumSamples;
   }

   @JsonIgnore
   public double getFilterDeadband()
   {
      return filterDeadband;
   }

   @JsonIgnore
   public int getFilterSamplingIntervalInMillis()
   {
      return filterSamplingIntervalInMillis;
   }

   @Override
   public String toString()
   {
      return "WicaChannelProperties{" +
            "  dataAcquisitionMode=" + dataAcquisitionMode +
            ", pollingIntervalInMillis=" + pollingIntervalInMillis +
            ", numericPrecision=" + numericPrecision +
            ", filterType=" + filterType +
            ", filterNumSamples=" + filterNumSamples +
            ", filterCycleLength=" + filterCycleLength +
            ", filterSamplingIntervalInMillis=" + filterSamplingIntervalInMillis +
            ", filterDeadband=" + filterDeadband +
            ", fieldsOfInterest='" + fieldsOfInterest + '\'' +
            '}';
   }

   // Implemented to simplify testing
   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaChannelProperties) ) return false;
      WicaChannelProperties that = (WicaChannelProperties) o;
      return dataAcquisitionMode == that.dataAcquisitionMode &&
            Objects.equals( pollingIntervalInMillis, that.pollingIntervalInMillis) &&
            Objects.equals(numericPrecision, that.numericPrecision) &&
            filterType == that.filterType &&
            Objects.equals(filterNumSamples, that.filterNumSamples) &&
            Objects.equals(filterCycleLength, that.filterCycleLength) &&
            Objects.equals(filterSamplingIntervalInMillis, that.filterSamplingIntervalInMillis) &&
            Objects.equals(filterDeadband, that.filterDeadband) &&
            Objects.equals(fieldsOfInterest, that.fieldsOfInterest);
   }

   // Implemented to simplify testing
   @Override
   public int hashCode()
   {
      return Objects.hash(dataAcquisitionMode, pollingIntervalInMillis, numericPrecision, filterType, filterNumSamples, filterCycleLength, filterSamplingIntervalInMillis, filterDeadband, fieldsOfInterest);
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

   public static class Builder
   {
      private DataAcquisitionMode dataAcquisitionMode = DEFAULT_DATA_ACQUISITION_MODE;
      private int pollingIntervalInMillis = DEFAULT_POLLING_INTERVAL;
      private int numericPrecision = DEFAULT_NUMERIC_PRECISION;
      private FilterType filterType = DEFAULT_FILTER_TYPE;
      private int filterNumSamples = DEFAULT_FILTER_NUM_SAMPLES;
      private int filterCycleLength = DEFAULT_FILTER_CYCLE_LENGTH;
      private int filterSamplingInterval = DEFAULT_FILTER_SAMPLING_INTERVAL;
      private double filterDeadband = DEFAULT_FILTER_DEADBAND;
      private String fieldsOfInterest = DEFAULT_FIELDS_OF_INTEREST;

      // Private to force use of the createBuilder factory method.
      private Builder() {}

      public Builder withDataAcquisitionMode( DataAcquisitionMode dataAcquisitionMode )
      {
         this.dataAcquisitionMode = dataAcquisitionMode;
         return this;
      }

      public Builder withPollingInterval( int pollingIntervalInMillis )
      {
         this.pollingIntervalInMillis = pollingIntervalInMillis;
         return this;
      }

      public Builder withFieldsOfInterest( String fieldsOfInterest )
      {
         this.fieldsOfInterest = fieldsOfInterest;
         return this;
      }

      public Builder withNumericPrecision( int numericPrecision )
      {
         this.numericPrecision = numericPrecision;
         return this;
      }

      public Builder withFilterType( FilterType filterType )
      {
         this.filterType = filterType;
         return this;
      }

      public Builder withNumSamples( int filterNumSamples )
      {
         this.filterNumSamples = filterNumSamples;
         return this;
      }

      public Builder withFilterCycleLength( int filterCycleLength )
      {
         this.filterCycleLength = filterCycleLength;
         return this;
      }

      public Builder withFilterSamplingInterval( int filterSamplingInterval )
      {
         this.filterSamplingInterval = filterSamplingInterval;
         return this;
      }

      public Builder withFilterDeadband( double filterDeadband )
      {
         this.filterDeadband = filterDeadband;
         return this;
      }

      public WicaChannelProperties build()
      {
         return new WicaChannelProperties( dataAcquisitionMode,
                                           pollingIntervalInMillis,
                                           fieldsOfInterest,
                                           numericPrecision,
                                           filterType,
                                           filterNumSamples,
                                           filterCycleLength,
                                           filterSamplingInterval,
                                           filterDeadband );
      }
   }


   public enum FilterType
   {
      @JsonProperty( "all-value" )    ALL_VALUE,
      @JsonProperty( "rate-limiter" ) RATE_LIMITER,
      @JsonProperty( "last-n" )       LAST_N,
      @JsonProperty( "one-in-m" )     ONE_IN_M,
      @JsonProperty( "changes" )      CHANGE_FILTERER,
   }

   public enum DataAcquisitionMode
   {
      @JsonProperty( "poll" )              POLL( true, false ),
      @JsonProperty( "monitor" )           MONITOR( false, true ),
      @JsonProperty( "poll-and-monitor" )  POLL_AND_MONITOR( true, true );

      private final boolean doesPolling;
      private final boolean doesMonitoring;

      DataAcquisitionMode( boolean doesPolling, boolean doesMonitoring )
      {
         this.doesPolling = doesPolling;
         this.doesMonitoring = doesMonitoring;
      }

      public boolean doesPolling()
      {
         return doesPolling;
      }
      public boolean doesMonitoring()
      {
         return doesMonitoring;
      }
   }
}
