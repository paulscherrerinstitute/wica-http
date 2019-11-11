/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import net.jcip.annotations.Immutable;

import java.util.Objects;
import java.util.Optional;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the configurable properties of a wica stream.
 * <p>
 * These include parameters for controlling the frequency at which
 * the various fluxes (heartbeat, metadata, monitored and polled values)
 * are sent down the wire to the end consumer.
 * <p>
 * Additionally the stream properties specify the default values that
 * will be applied to the configuration of the individual wica channels
 * that make up the stream, when they are not explicitly specified on
 * the channels themselves.
 */
@Immutable
public class WicaStreamProperties
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaDataAcquisitionMode dataAcquisitionMode;
   private final WicaFilterType filterType ;

   private final Integer heartbeatFluxIntervalInMillis;
   private final Integer metadataFluxIntervalInMillis;
   private final Integer monitoredValueFluxIntervalInMillis;
   private final Integer polledValueFluxIntervalInMillis;
   private final Integer pollingIntervalInMillis;
   private final Integer numericPrecision;
   private final Integer filterNumSamples;
   private final Integer filterNumSamplesInAverage;
   private final Integer filterCycleLength;
   private final Integer filterSamplingIntervalInMillis;
   private final Double filterDeadband;
   private final String fieldsOfInterest;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamProperties()
   {
      this.heartbeatFluxIntervalInMillis      = WicaStreamPropertiesDefaults.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS;
      this.metadataFluxIntervalInMillis       = WicaStreamPropertiesDefaults.DEFAULT_METADATA_FLUX_INTERVAL_IN_MILLIS;
      this.monitoredValueFluxIntervalInMillis = WicaStreamPropertiesDefaults.DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      this.polledValueFluxIntervalInMillis    = WicaStreamPropertiesDefaults.DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      this.dataAcquisitionMode                = WicaStreamPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE;
      this.pollingIntervalInMillis            = WicaStreamPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS;
      this.numericPrecision                   = WicaStreamPropertiesDefaults.DEFAULT_NUMERIC_PRECISION;
      this.filterType                         = WicaStreamPropertiesDefaults.DEFAULT_FILTER_TYPE;
      this.filterNumSamples                   = WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES;
      this.filterNumSamplesInAverage          = WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES_IN_AVERAGE;
      this.filterCycleLength                  = WicaStreamPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH;
      this.filterSamplingIntervalInMillis     = WicaStreamPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS;
      this.filterDeadband                     = WicaStreamPropertiesDefaults.DEFAULT_FILTER_DEADBAND;
      this.fieldsOfInterest                   = WicaStreamPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST;
   }

   // Support for deprecated property POLLRATIO. In the future this constructor can go.
   // WARNING: Signature here must match EXACTLY with that in WicaStreamPropertiesDeserializationMixin.
   public WicaStreamProperties( Integer pollingRatio, // deprecated
                                Integer heartbeatFluxIntervalInMillis,
                                Integer metadataFluxIntervalInMillis,
                                Integer monitoredValueFluxIntervalInMillis,
                                Integer polledValueFluxIntervalInMillis,
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
      this.heartbeatFluxIntervalInMillis      = heartbeatFluxIntervalInMillis;
      this.metadataFluxIntervalInMillis       = metadataFluxIntervalInMillis;
      this.monitoredValueFluxIntervalInMillis = monitoredValueFluxIntervalInMillis;
      this.polledValueFluxIntervalInMillis    = polledValueFluxIntervalInMillis;
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
         return pollingRatio * polledValueFluxIntervalInMillis;
      }
      else
      {
         return pollingIntervalInMillis;
      }
   }

   public WicaStreamProperties( Integer heartbeatFluxIntervalInMillis,
                                Integer metadataFluxIntervalInMillis,
                                Integer monitoredValueFluxIntervalInMillis,
                                Integer polledValueFluxIntervalInMillis,
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
      this.heartbeatFluxIntervalInMillis      = heartbeatFluxIntervalInMillis;
      this.metadataFluxIntervalInMillis       = metadataFluxIntervalInMillis;
      this.monitoredValueFluxIntervalInMillis = monitoredValueFluxIntervalInMillis;
      this.polledValueFluxIntervalInMillis    = polledValueFluxIntervalInMillis;
      this.dataAcquisitionMode                = dataAcquisitionMode;
      this.pollingIntervalInMillis            = pollingIntervalInMillis;
      this.numericPrecision                   = numericPrecision;
      this.filterType                         = filterType;
      this.filterNumSamples                   = filterNumSamples;
      this.filterNumSamplesInAverage          = filterNumSamplesInAverage;
      this.filterCycleLength                  = filterCycleLength;
      this.filterSamplingIntervalInMillis     = filterSamplingIntervalInMillis;
      this.filterDeadband                     = filterDeadband;
      this.fieldsOfInterest                   = fieldsOfInterest;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Optional<Integer> getOptionalHeartbeatFluxIntervalInMillis()
   {
      return Optional.ofNullable( heartbeatFluxIntervalInMillis );
   }

   public int getHeartbeatFluxIntervalInMillis()
   {
      return getOptionalHeartbeatFluxIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The heartbeat flux interval for this stream was not specified." ) );
   }

   public Optional<Integer> getOptionalMetadataFluxIntervalInMillis()
   {
      return Optional.ofNullable( metadataFluxIntervalInMillis );
   }

   public int getMetadataFluxIntervalInMillis()
   {
      return getOptionalMetadataFluxIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The metadata flux interval for this stream was not specified." ) );
   }

   public Optional<Integer> getOptionalMonitoredValueFluxIntervalInMillis()
   {
      return Optional.ofNullable( monitoredValueFluxIntervalInMillis );
   }

   public int getMonitoredValueFluxIntervalInMillis()
   {
      return getOptionalMonitoredValueFluxIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The monitored value flux interval for this stream was not specified." ) );
   }

   public Optional<Integer> getOptionalPolledValueFluxIntervalInMillis()
   {
      return Optional.ofNullable( polledValueFluxIntervalInMillis );
   }

   public int getPolledValueFluxIntervalInMillis()
   {
      return getOptionalPolledValueFluxIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The polled value flux interval for this stream was not specified." ) );
   }

   public Optional<WicaDataAcquisitionMode> getOptionalDataAcquisitionMode()
   {
      return Optional.ofNullable( dataAcquisitionMode );
   }

   public WicaDataAcquisitionMode getDataAcquisitionMode()
   {
      return getOptionalDataAcquisitionMode().orElseThrow( () -> new IllegalArgumentException( "The data acquisition mode for this stream was not specified." ) );
   }

   public Optional<String> getOptionalFieldsOfInterest()
   {
      return Optional.ofNullable( fieldsOfInterest );
   }

   public String getFieldsOfInterest()
   {
      return getOptionalFieldsOfInterest().orElseThrow(() -> new IllegalArgumentException( "The fields of interest for this stream were not specified."));
   }

   public Optional<WicaFilterType> getOptionalFilterType()
   {
      return Optional.ofNullable( filterType );
   }

   public WicaFilterType getFilterType()
   {
      return getOptionalFilterType().orElseThrow( () -> new IllegalArgumentException( "The filter type for this stream was not specified." ) );
   }

   public Optional<Integer> getOptionalNumericPrecision()
   {
      return Optional.ofNullable( numericPrecision );
   }

   public int getNumericPrecision()
   {
      return getOptionalNumericPrecision().orElseThrow( () -> new IllegalArgumentException( "The numeric precision for this stream was not specified." ) );
   }

   public Optional<Integer> getOptionalPollingIntervalInMillis()
   {
      return Optional.ofNullable( pollingIntervalInMillis );
   }

   public int getPollingIntervalInMillis()
   {
      return getOptionalPollingIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The polling interval for this stream was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterCycleLength()
   {
      return Optional.ofNullable( filterCycleLength );
   }

   public int getFilterCycleLength()
   {
      return getOptionalFilterCycleLength().orElseThrow( () -> new IllegalArgumentException( "The cycle length for this stream's ONE_IN_M filter was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterNumSamples()
   {
      return Optional.ofNullable( filterNumSamples );
   }

   public int getFilterNumSamples()
   {
      return getOptionalFilterNumSamples().orElseThrow( () -> new IllegalArgumentException( "The number of samples for this stream's LAST_N filter was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterNumSamplesInAverage()
   {
      return Optional.ofNullable( filterNumSamplesInAverage );
   }

   public int getFilterNumSamplesInAverage()
   {
      return getOptionalFilterNumSamplesInAverage().orElseThrow( () -> new IllegalArgumentException( "The number of samples for this stream's AVERAGER filter was not specified." ) );
   }

   public Optional<Double> getOptionalFilterDeadband()
   {
      return Optional.ofNullable( filterDeadband );
   }

   public double getFilterDeadband()
   {
      return getOptionalFilterDeadband().orElseThrow( () -> new IllegalArgumentException( "The deadband for this stream's CHANGE_DETECTOR filter was not specified." ) );
   }

   public Optional<Integer> getOptionalFilterSamplingIntervalInMillis()
   {
      return Optional.ofNullable( filterSamplingIntervalInMillis );
   }

   public int getFilterSamplingIntervalInMillis()
   {
      return getOptionalFilterSamplingIntervalInMillis().orElseThrow( () -> new IllegalArgumentException( "The sampling interval for this stream's RATE_LIMITER filter was not specified." ) );
   }

   // Note: The WicaStreamProperties class generates VALUE objects which are considered equal if the fields match.
   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaStreamProperties) ) return false;
      WicaStreamProperties that = (WicaStreamProperties) o;
      return dataAcquisitionMode == that.dataAcquisitionMode &&
            filterType == that.filterType &&
            Objects.equals(heartbeatFluxIntervalInMillis, that.heartbeatFluxIntervalInMillis) &&
            Objects.equals(metadataFluxIntervalInMillis, that.metadataFluxIntervalInMillis) &&
            Objects.equals(monitoredValueFluxIntervalInMillis, that.monitoredValueFluxIntervalInMillis) &&
            Objects.equals(polledValueFluxIntervalInMillis, that.polledValueFluxIntervalInMillis) &&
            Objects.equals(pollingIntervalInMillis, that.pollingIntervalInMillis) &&
            Objects.equals(numericPrecision, that.numericPrecision) &&
            Objects.equals(filterNumSamples, that.filterNumSamples) &&
            Objects.equals(filterNumSamplesInAverage, that.filterNumSamplesInAverage) &&
            Objects.equals(filterCycleLength, that.filterCycleLength) &&
            Objects.equals(filterSamplingIntervalInMillis, that.filterSamplingIntervalInMillis) &&
            Objects.equals(filterDeadband, that.filterDeadband) &&
            Objects.equals(fieldsOfInterest, that.fieldsOfInterest);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(dataAcquisitionMode, filterType, heartbeatFluxIntervalInMillis, metadataFluxIntervalInMillis, monitoredValueFluxIntervalInMillis, polledValueFluxIntervalInMillis, pollingIntervalInMillis, numericPrecision, filterNumSamples, filterCycleLength, filterSamplingIntervalInMillis, filterDeadband, fieldsOfInterest);
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
