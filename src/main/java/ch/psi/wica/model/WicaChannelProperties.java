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
   public static final int DEFAULT_FILTER_SAMPLE_GAP = 1000;
   public static final double DEFAULT_FILTER_DEADBAND = 1.0;
   public static final int DEFAULT_POLLING_INTERVAL = 1000;


/*- Private attributes -------------------------------------------------------*/

   private final DataAcquisitionMode dataAcquisitionMode;
   private final Integer pollingInterval;
   private final Integer numericPrecision;
   private final FilterType filterType ;
   private final Integer filterNumSamples;
   private final Integer filterCycleLength;
   private final Integer filterMinSampleGap;
   private final Double filterDeadband;
   private final String fieldsOfInterest;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelProperties()
   {
      this.dataAcquisitionMode = null;
      this.fieldsOfInterest = null;
      this.numericPrecision = null;
      this.pollingInterval = DEFAULT_POLLING_INTERVAL;
      this.filterType = DEFAULT_FILTER_TYPE;
      this.filterNumSamples = DEFAULT_FILTER_NUM_SAMPLES;
      this.filterCycleLength = DEFAULT_FILTER_CYCLE_LENGTH;
      this.filterMinSampleGap = DEFAULT_FILTER_SAMPLE_GAP;
      this.filterDeadband = DEFAULT_FILTER_DEADBAND;
   }

   public WicaChannelProperties( @JsonProperty( "daqmode"  ) DataAcquisitionMode dataAcquisitionMode,
                                 @JsonProperty( "pollint"  ) Integer pollingIntervalInMillis,
                                 @JsonProperty( "fields"   ) String fieldsOfInterest,
                                 @JsonProperty( "prec"     ) Integer numericPrecision,
                                 @JsonProperty( "filter"   ) FilterType filterType,
                                 @JsonProperty( "nsamples" ) Integer filterNumSamples,
                                 @JsonProperty( "cyclelen" ) Integer filterCycleLength,
                                 @JsonProperty( "minsgap"  ) Integer filterMinSampleGapInMillis,
                                 @JsonProperty( "deadband" ) Double filterDeadband
   )
   {
      this.dataAcquisitionMode = dataAcquisitionMode;
      this.pollingInterval = pollingIntervalInMillis == null ? DEFAULT_POLLING_INTERVAL : pollingIntervalInMillis;
      this.fieldsOfInterest = fieldsOfInterest;
      this.numericPrecision = numericPrecision;
      this.filterType = filterType == null ? DEFAULT_FILTER_TYPE : filterType;
      this.filterNumSamples = filterNumSamples == null ? DEFAULT_FILTER_NUM_SAMPLES : filterNumSamples;
      this.filterCycleLength = filterCycleLength == null ? DEFAULT_FILTER_CYCLE_LENGTH : filterCycleLength;
      this.filterMinSampleGap = filterMinSampleGapInMillis == null ? DEFAULT_FILTER_SAMPLE_GAP : filterMinSampleGapInMillis;
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
   public int getPollingIntervalInMillis()
   {
      return pollingInterval;
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
   public int getFilterMinSampleGapInMillis()
   {
      return filterMinSampleGap;
   }

   @Override
   public String toString()
   {
      return "WicaChannelProperties{" +
            "dataAcquisitionMode=" + dataAcquisitionMode +
            ", pollingInterval=" + pollingInterval +
            ", numericPrecision=" + numericPrecision +
            ", filterType=" + filterType +
            ", filterNumSamples=" + filterNumSamples +
            ", filterCycleLength=" + filterCycleLength +
            ", filterMinSampleGap=" + filterMinSampleGap +
            ", filterDeadband=" + filterDeadband +
            ", fieldsOfInterest='" + fieldsOfInterest + '\'' +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   public enum FilterType
   {
      @JsonProperty( "all-value" )       ALL_VALUE,
      @JsonProperty( "rate-limiter" )    RATE_LIMITER,
      @JsonProperty( "one-in-n" )        ONE_IN_N,
      @JsonProperty( "last-n" )          LAST_N,
      @JsonProperty( "change-filterer" ) CHANGE_FILTERER,
   }

   public enum DataAcquisitionMode
   {
      @JsonProperty( "poll" )              POLL( true, false ),
      @JsonProperty( "monitor" )           MONITOR( false, true ),
      @JsonProperty( "poll-and-monitor" )  POLL_AND_MONITOR( true, true );

      private boolean doesPolling;
      private boolean doesMonitoring;

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
