/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.stream.WicaStreamProperties;
import ch.psi.wica.model.stream.WicaStreamPropertiesDefaults;
import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SuppressWarnings( "WeakerAccess" )
public class WicaStreamPropertiesBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private Integer heartbeatFluxIntervalInMillis;
   private Integer metadataFluxIntervalInMillis;
   private Integer monitoredValueFluxIntervalInMillis;
   private Integer polledValueFluxIntervalInMillis;
   private WicaDataAcquisitionMode dataAcquisitionMode;
   private Integer pollingIntervalInMillis;
   private Integer numericPrecision;
   private WicaFilterType filterType;
   private Integer filterNumSamples;
   private Integer filterNumSamplesInAverage;
   private Integer filterCycleLength;
   private Integer filterSamplingIntervalInMillis;
   private Double filterDeadband;
   private String fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   // Private to force use of the create factory method.
   private WicaStreamPropertiesBuilder() {}


/*- Class methods ------------------------------------------------------------*/

   public static WicaStreamPropertiesBuilder create()
   {
      return new WicaStreamPropertiesBuilder();
   }


/*- Public methods -----------------------------------------------------------*/

   public WicaStreamPropertiesBuilder withDefaultProperties()
   {
      heartbeatFluxIntervalInMillis = WicaStreamPropertiesDefaults.DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS;
      metadataFluxIntervalInMillis = WicaStreamPropertiesDefaults.DEFAULT_METADATA_FLUX_INTERVAL_IN_MILLIS;
      monitoredValueFluxIntervalInMillis = WicaStreamPropertiesDefaults.DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      polledValueFluxIntervalInMillis = WicaStreamPropertiesDefaults.DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      dataAcquisitionMode = WicaStreamPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE;
      pollingIntervalInMillis = WicaStreamPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS;
      fieldsOfInterest = WicaStreamPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST;
      numericPrecision = WicaStreamPropertiesDefaults.DEFAULT_NUMERIC_PRECISION;
      filterType = WicaStreamPropertiesDefaults.DEFAULT_FILTER_TYPE;
      filterNumSamples = WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES;
      filterNumSamplesInAverage = WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES_IN_AVERAGE;
      filterCycleLength = WicaStreamPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH;
      filterSamplingIntervalInMillis = WicaStreamPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS;
      filterDeadband =  WicaStreamPropertiesDefaults.DEFAULT_FILTER_DEADBAND;
      return this;
   }

   public WicaStreamPropertiesBuilder withStreamProperties( WicaStreamProperties wicaStreamProperties )
   {
      Validate.notNull( wicaStreamProperties, "The 'wicaStreamProperties' argument was null." );

      wicaStreamProperties.getOptionalHeartbeatFluxIntervalInMillis().ifPresent(       o -> heartbeatFluxIntervalInMillis = o      );
      wicaStreamProperties.getOptionalMetadataFluxIntervalInMillis().ifPresent(        o -> metadataFluxIntervalInMillis = o       );
      wicaStreamProperties.getOptionalMonitoredValueFluxIntervalInMillis().ifPresent(  o -> monitoredValueFluxIntervalInMillis = o );
      wicaStreamProperties.getOptionalPolledValueFluxIntervalInMillis().ifPresent(     o -> polledValueFluxIntervalInMillis = o    );
      wicaStreamProperties.getOptionalDataAcquisitionMode().ifPresent(                 o -> dataAcquisitionMode = o                );
      wicaStreamProperties.getOptionalPollingIntervalInMillis().ifPresent(             o -> pollingIntervalInMillis = o            );
      wicaStreamProperties.getOptionalFieldsOfInterest().ifPresent(                    o -> fieldsOfInterest = o                   );
      wicaStreamProperties.getOptionalNumericPrecision().ifPresent(                    o -> numericPrecision = o                   );
      wicaStreamProperties.getOptionalFilterType().ifPresent(                          o -> filterType = o                         );
      wicaStreamProperties.getOptionalFilterNumSamples().ifPresent(                    o -> filterNumSamples = o                   );
      wicaStreamProperties.getOptionalFilterNumSamplesInAverage().ifPresent(           o -> filterNumSamplesInAverage = o          );
      wicaStreamProperties.getOptionalFilterCycleLength().ifPresent(                   o -> filterCycleLength = o                  );
      wicaStreamProperties.getOptionalFilterSamplingIntervalInMillis().ifPresent(      o -> filterSamplingIntervalInMillis = o     );
      wicaStreamProperties.getOptionalFilterDeadband().ifPresent(                      o -> filterDeadband = o                     );
      return this;
   }

   public WicaStreamPropertiesBuilder withHeartbeatFluxInterval( int heartbeatFluxIntervalInMillis )
   {
      this.heartbeatFluxIntervalInMillis = heartbeatFluxIntervalInMillis;
      return this;
   }

   public WicaStreamPropertiesBuilder withMetadataFluxInterval( int metadataFluxIntervalInMillis )
   {
      this.metadataFluxIntervalInMillis = metadataFluxIntervalInMillis;
      return this;
   }

   public WicaStreamPropertiesBuilder withMonitoredValueFluxInterval( int monitoredValueFluxIntervalInMillis )
   {
      this.monitoredValueFluxIntervalInMillis = monitoredValueFluxIntervalInMillis;
      return this;
   }

   public WicaStreamPropertiesBuilder withPolledValueFluxInterval( int polledValueFluxIntervalInMillis )
   {
      this.polledValueFluxIntervalInMillis = polledValueFluxIntervalInMillis;
      return this;
   }

   public WicaStreamPropertiesBuilder withDataAcquisitionMode( WicaDataAcquisitionMode dataAcquisitionMode )
   {
      this.dataAcquisitionMode = Validate.notNull( dataAcquisitionMode, "The 'dataAcquisitionMode' argument was null." );
      return this;
   }

   public WicaStreamPropertiesBuilder withPollingIntervalInMillis( int pollingInterval )
   {
      this.pollingIntervalInMillis = pollingInterval;
      return this;
   }

   public WicaStreamPropertiesBuilder withFieldsOfInterest( String fieldsOfInterest )
   {
      this.fieldsOfInterest = Validate.notNull( fieldsOfInterest,"The 'fieldsOfInterest' argument was null."  );
      return this;
   }

   public WicaStreamPropertiesBuilder withNumericPrecision( int numericPrecision )
   {
      this.numericPrecision = numericPrecision;
      return this;
   }

   public WicaStreamPropertiesBuilder withFilterType( WicaFilterType filterType )
   {
      this.filterType = Validate.notNull( filterType, "The 'filterType' argument was null." );
      return this;
   }

   public WicaStreamPropertiesBuilder withFilterNumSamples( int filterNumSamples )
   {
      this.filterNumSamples = filterNumSamples;
      return this;
   }

   public WicaStreamPropertiesBuilder withFilterNumSamplesInAverage( int filterNumSamplesInAverage )
   {
      this.filterNumSamplesInAverage = filterNumSamplesInAverage;
      return this;
   }

   public WicaStreamPropertiesBuilder withFilterCycleLength( int filterCycleLength )
   {
      this.filterCycleLength = filterCycleLength;
      return this;
   }

   public WicaStreamPropertiesBuilder withFilterSamplingIntervalInMillis( int filterSamplingInterval )
   {
      this.filterSamplingIntervalInMillis = filterSamplingInterval;
      return this;
   }

   public WicaStreamPropertiesBuilder withFilterDeadband( double filterDeadband )
   {
      this.filterDeadband = filterDeadband;
      return this;
   }

   public WicaStreamProperties build()
   {
      return new WicaStreamProperties( heartbeatFluxIntervalInMillis,
                                       metadataFluxIntervalInMillis,
                                       monitoredValueFluxIntervalInMillis,
                                       polledValueFluxIntervalInMillis,
                                       dataAcquisitionMode,
                                       pollingIntervalInMillis,
                                       fieldsOfInterest,
                                       numericPrecision,
                                       filterType,
                                       filterNumSamples,
                                       filterNumSamplesInAverage,
                                       filterCycleLength,
                                       filterSamplingIntervalInMillis,
                                       filterDeadband );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
