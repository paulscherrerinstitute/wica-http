/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.stream.WicaStreamPropertiesDefaults;
import net.jcip.annotations.Immutable;

import java.util.Objects;
import java.util.Optional;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the configurable properties of a wica channel.
 * <p>
 * These include the channel's <i>data acquisition mode</i>, its <i>filter
 * configuration</i> and the parameters used for controlling how its
 * associated state data is <i>serialized</i> when returned to the end
 * user.
 */
@Immutable
public class WicaChannelProperties
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaDataAcquisitionMode dataAcquisitionMode;
   private final Integer pollingIntervalInMillis;
   private final Integer numericPrecision;
   private final WicaFilterType filterType ;
   private final Integer filterNumSamples;
   private final Integer filterNumSamplesInAverage;
   private final Integer filterCycleLength;
   private final Integer filterSamplingIntervalInMillis;
   private final Double filterDeadband;
   private final String fieldsOfInterest;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelProperties()
   {
      this.dataAcquisitionMode            = WicaChannelPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE;
      this.pollingIntervalInMillis        = WicaChannelPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS;
      this.numericPrecision               = WicaChannelPropertiesDefaults.DEFAULT_NUMERIC_PRECISION;
      this.filterType                     = WicaChannelPropertiesDefaults.DEFAULT_FILTER_TYPE;
      this.filterNumSamples               = WicaChannelPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES;
      this.filterNumSamplesInAverage      = WicaChannelPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES_IN_AVERAGE;
      this.filterCycleLength              = WicaChannelPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH;
      this.filterSamplingIntervalInMillis = WicaChannelPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS;
      this.filterDeadband                 = WicaChannelPropertiesDefaults.DEFAULT_FILTER_DEADBAND;
      this.fieldsOfInterest               = WicaChannelPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST;
   }

   // Support for deprecated property POLLRATIO. In the future this constructor can go.
   // WARNING: Signature here must match EXACTLY with that in WicaStreamPropertiesDeserializationMixin.
   public WicaChannelProperties( Integer pollingRatio, // deprecated
                                 WicaDataAcquisitionMode dataAcquisitionMode,
                                 Integer pollingIntervalInMillis,
                                 String fieldsOfInterest,
                                 Integer numericPrecision,
                                 WicaFilterType filterType,
                                 Integer filterNumSamples,
                                 Integer filterNumSamplesInAverage,
                                 Integer filterCycleLength,
                                 Integer filterSamplingIntervalInMillis,
                                 Double filterDeadband )
   {
      this.dataAcquisitionMode                = dataAcquisitionMode;
      this.pollingIntervalInMillis            = extractPollingInterval( pollingIntervalInMillis, pollingRatio );
      this.numericPrecision                   = numericPrecision;
      this.filterType                         = filterType;
      this.filterNumSamples                   = filterNumSamples;
      this.filterNumSamplesInAverage          = filterNumSamplesInAverage;
      this.filterCycleLength                  = filterCycleLength;
      this.filterSamplingIntervalInMillis     = filterSamplingIntervalInMillis;
      this.filterDeadband                     = filterDeadband;
      this.fieldsOfInterest                   = fieldsOfInterest;
   }

   // Support for deprecated property pollratio. In the future this method can go.
   private Integer extractPollingInterval( Integer pollingIntervalInMillis, Integer pollingRatio )
   {
      if ( (pollingIntervalInMillis == null) && (pollingRatio != null) )
      {
         return pollingRatio * WicaStreamPropertiesDefaults.DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      }
      else
      {
         return pollingIntervalInMillis;
      }
   }

   // WARNING: Signature here must match EXACTLY with that in WicaStreamPropertiesDeserializationMixin.
   public WicaChannelProperties( WicaDataAcquisitionMode dataAcquisitionMode,
                                 Integer pollingIntervalInMillis,
                                 String fieldsOfInterest,
                                 Integer numericPrecision,
                                 WicaFilterType filterType,
                                 Integer filterNumSamples,
                                 Integer filterNumSamplesInAverage,
                                 Integer filterCycleLength,
                                 Integer filterSamplingIntervalInMillis,
                                 Double filterDeadband )
   {
      this.dataAcquisitionMode            = dataAcquisitionMode;
      this.pollingIntervalInMillis        = pollingIntervalInMillis;
      this.numericPrecision               = numericPrecision;
      this.filterType                     = filterType;
      this.filterNumSamples               = filterNumSamples;
      this.filterNumSamplesInAverage      = filterNumSamplesInAverage;
      this.filterCycleLength              = filterCycleLength;
      this.filterSamplingIntervalInMillis = filterSamplingIntervalInMillis;
      this.filterDeadband                 = filterDeadband;
      this.fieldsOfInterest               = fieldsOfInterest;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Optional<WicaDataAcquisitionMode> getOptionalDataAcquisitionMode()
   {
      return Optional.ofNullable( dataAcquisitionMode );
   }

   public WicaDataAcquisitionMode getDataAcquisitionMode()
   {
      return getOptionalDataAcquisitionMode().orElseThrow( () -> new IllegalArgumentException( "The data acquisition mode for this channel was not specified." ) );
   }

   public Optional<String> getOptionalFieldsOfInterest()
   {
      return Optional.ofNullable( fieldsOfInterest );
   }

   public String getFieldsOfInterest()
   {
      return getOptionalFieldsOfInterest().orElseThrow(() -> new IllegalArgumentException( "The fields of interest for this channel were not specified."));
   }

   public Optional<WicaFilterType> getOptionalFilterType()
   {
      return Optional.ofNullable( filterType );
   }

   public WicaFilterType getFilterType()
   {
      return getOptionalFilterType().orElseThrow( () -> new IllegalArgumentException( "The filter type for this channel was not specified." ) );
   }

   public Optional<Integer> getOptionalNumericPrecision()
   {
      return Optional.ofNullable( numericPrecision );
   }

   public int getNumericPrecision()
   {
      return getOptionalNumericPrecision().orElseThrow( () -> new IllegalArgumentException( "The numeric precision for this channel was not specified." ) );
   }

   public Optional<Integer> getOptionalPollingIntervalInMillis()
   {
      return Optional.ofNullable( pollingIntervalInMillis );
   }

   public int getPollingIntervalInMillis()
   {
      return getOptionalPollingIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The polling interval for this channel was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterCycleLength()
   {
      return Optional.ofNullable( filterCycleLength );
   }

   public int getFilterCycleLength()
   {
      return getOptionalFilterCycleLength().orElseThrow( () -> new IllegalArgumentException( "The cycle length for this channel's ONE_IN_M filter was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterNumSamples()
   {
      return Optional.ofNullable( filterNumSamples );
   }

   public int getFilterNumSamples()
   {
      return getOptionalFilterNumSamples().orElseThrow( () -> new IllegalArgumentException( "The number of samples for this channel's LAST_N filter was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterNumSamplesInAverage()
   {
      return Optional.ofNullable( filterNumSamplesInAverage );
   }

   public int getFilterNumSamplesInAverage()
   {
      return getOptionalFilterNumSamplesInAverage().orElseThrow( () -> new IllegalArgumentException( "The number of samples for this channel's AVERAGING filter was not specified." ) );
   }

   public Optional<Double> getOptionalFilterDeadband()
   {
      return Optional.ofNullable( filterDeadband );
   }

   public double getFilterDeadband()
   {
      return getOptionalFilterDeadband().orElseThrow( () -> new IllegalArgumentException( "The deadband for this channel's CHANGE_DETECTOR filter was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterSamplingIntervalInMillis()
   {
      return Optional.ofNullable( filterSamplingIntervalInMillis );
   }

   public int getFilterSamplingIntervalInMillis()
   {
      return getOptionalFilterSamplingIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The sampling interval for this channel's RATE_LIMITER filter was not specified." ) );
   }

   // Note: The WicaChannelProperties class generates VALUE objects which are considered equal if the fields match.
   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaChannelProperties) ) return false;
      WicaChannelProperties that = (WicaChannelProperties) o;
      return dataAcquisitionMode == that.dataAcquisitionMode &&
            Objects.equals(pollingIntervalInMillis, that.pollingIntervalInMillis) &&
            Objects.equals( numericPrecision, that.numericPrecision ) &&
            filterType == that.filterType &&
            Objects.equals( filterNumSamples, that.filterNumSamples ) &&
            Objects.equals( filterNumSamplesInAverage, that.filterNumSamplesInAverage ) &&
            Objects.equals( filterCycleLength, that.filterCycleLength ) &&
            Objects.equals( filterSamplingIntervalInMillis, that.filterSamplingIntervalInMillis ) &&
            Objects.equals( filterDeadband, that.filterDeadband ) &&
            Objects.equals( fieldsOfInterest, that.fieldsOfInterest );
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(dataAcquisitionMode, pollingIntervalInMillis, numericPrecision, filterType, filterNumSamples, filterCycleLength, filterSamplingIntervalInMillis, filterDeadband, fieldsOfInterest);
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
