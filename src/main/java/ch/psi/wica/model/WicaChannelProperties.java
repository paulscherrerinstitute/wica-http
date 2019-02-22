/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;

import java.util.Optional;
import java.util.Set;

@JsonIgnoreProperties()
@Immutable
public class WicaChannelProperties
{

/*- Public attributes --------------------------------------------------------*/

   public static final FilterType DEFAULT_FILTER_TYPE = FilterType.LAST_N;
   public static final int DEFAULT_FILTER_NUM_SAMPLES = 1;
   public static final int DEFAULT_FILTER_CYCLE_LENGTH = 1;
   public static final int DEFAULT_FILTER_SAMPLING_INTERVAL = 1000;
   public static final double DEFAULT_FILTER_DEADBAND = 1.0;


/*- Private attributes -------------------------------------------------------*/

   private final DataAcquisitionMode dataAcquisitionMode;
   private final Integer polledValueSamplingRatio;
   private final Integer numericPrecision;
   private final FilterType filterType ;
   private final Integer filterNumSamples;
   private final Integer filterCycleLength;
   private final Integer filterSamplingInterval;
   private final Double filterDeadband;
   private final String fieldsOfInterest;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelProperties()
   {
      this.dataAcquisitionMode = null;
      this.fieldsOfInterest = null;
      this.numericPrecision = null;
      this.polledValueSamplingRatio = null;
      this.filterType = DEFAULT_FILTER_TYPE;
      this.filterNumSamples = DEFAULT_FILTER_NUM_SAMPLES;
      this.filterCycleLength = DEFAULT_FILTER_CYCLE_LENGTH;
      this.filterSamplingInterval = DEFAULT_FILTER_SAMPLING_INTERVAL;
      this.filterDeadband = DEFAULT_FILTER_DEADBAND;
   }

   public WicaChannelProperties( @JsonProperty( "daqmode"  ) DataAcquisitionMode dataAcquisitionMode,
                                 @JsonProperty( "pollrat"  ) Integer polledValueSamplingRatio,
                                 @JsonProperty( "fields"   ) String fieldsOfInterest,
                                 @JsonProperty( "prec"     ) Integer numericPrecision,
                                 @JsonProperty( "filter"   ) FilterType filterType,
                                 @JsonProperty( "n"        ) Integer filterNumSamples,
                                 @JsonProperty( "m"        ) Integer filterCycleLength,
                                 @JsonProperty( "interval" ) Integer filterSamplingIntervalInMillis,
                                 @JsonProperty( "deadband" ) Double filterDeadband
   )
   {
      this.dataAcquisitionMode = dataAcquisitionMode;
      this.polledValueSamplingRatio = polledValueSamplingRatio;
      this.fieldsOfInterest = fieldsOfInterest;
      this.numericPrecision = numericPrecision;
      this.filterType = filterType == null ? DEFAULT_FILTER_TYPE : filterType;
      this.filterNumSamples = filterNumSamples == null ? DEFAULT_FILTER_NUM_SAMPLES : filterNumSamples;
      this.filterCycleLength = filterCycleLength == null ? DEFAULT_FILTER_CYCLE_LENGTH : filterCycleLength;
      this.filterSamplingInterval = filterSamplingIntervalInMillis == null ? DEFAULT_FILTER_SAMPLING_INTERVAL : filterSamplingIntervalInMillis;
      this.filterDeadband = filterDeadband == null ? DEFAULT_FILTER_DEADBAND : filterDeadband;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @JsonIgnore
   public Optional<DataAcquisitionMode> getDataAcquisitionMode()
   {
      // Note: Can be overridden by same property of stream
      return dataAcquisitionMode == null ? Optional.empty() : Optional.of( dataAcquisitionMode );
   }

   @JsonIgnore
   public Optional<Set<String>> getFieldsOfInterest()
   {
      // Note: Can be overridden by same property of stream
      return fieldsOfInterest == null ? Optional.empty() : Optional.of( Set.of( fieldsOfInterest.split(";" ) ) );
   }

   @JsonIgnore
   public Optional<Integer> getNumericPrecision()
   {
      // Note: Can be overridden by same property of stream
      return numericPrecision == null ? Optional.empty() : Optional.of( numericPrecision );
   }

   @JsonIgnore
   public Optional<Integer> getPolledValueSampleRatio()
   {
      return polledValueSamplingRatio == null ? Optional.empty() : Optional.of( polledValueSamplingRatio );
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
      return filterSamplingInterval;
   }

   @Override
   public String toString()
   {
      return "WicaChannelProperties{" +
            "dataAcquisitionMode=" + dataAcquisitionMode +
            ", polledValueSamplingRatio=" + polledValueSamplingRatio +
            ", numericPrecision=" + numericPrecision +
            ", filterType=" + filterType +
            ", filterNumSamples=" + filterNumSamples +
            ", filterCycleLength=" + filterCycleLength +
            ", filterSamplingInterval=" + filterSamplingInterval +
            ", filterDeadband=" + filterDeadband +
            ", fieldsOfInterest='" + fieldsOfInterest + '\'' +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

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
