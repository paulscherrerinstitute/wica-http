/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelPropertiesDefaults;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.apache.commons.lang3.Validate;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelPropertiesBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaDataAcquisitionMode dataAcquisitionMode;
   private Integer pollingIntervalInMillis;
   private Integer numericPrecision;
   private WicaFilterType filterType;
   private Integer filterNumSamples;
   private Integer filterCycleLength;
   private Integer filterSamplingIntervalInMillis;
   private Double filterDeadband;
   private String fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   // Private to force use of the create factory method.
   private WicaChannelPropertiesBuilder() {}

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelPropertiesBuilder create()
   {
      return new WicaChannelPropertiesBuilder();
   }

/*- Public methods -----------------------------------------------------------*/

   public WicaChannelPropertiesBuilder withDefaultProperties()
   {
      dataAcquisitionMode = WicaChannelPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE;
      pollingIntervalInMillis = WicaChannelPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS;
      fieldsOfInterest = WicaChannelPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST;
      numericPrecision = WicaChannelPropertiesDefaults.DEFAULT_NUMERIC_PRECISION;
      filterType = WicaChannelPropertiesDefaults.DEFAULT_FILTER_TYPE;
      filterNumSamples = WicaChannelPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES;
      filterCycleLength = WicaChannelPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH;
      filterSamplingIntervalInMillis = WicaChannelPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS;
      filterDeadband =  WicaChannelPropertiesDefaults.DEFAULT_FILTER_DEADBAND;
      return this;
   }

   public WicaChannelPropertiesBuilder withChannelPropertiesFromStream( WicaStreamProperties wicaStreamProperties )
   {
      Validate.notNull( wicaStreamProperties, "The 'wicaStreamProperties' argument was null." );
      wicaStreamProperties.getOptionalDataAcquisitionMode().ifPresent(            o -> dataAcquisitionMode = o            );
      wicaStreamProperties.getOptionalPollingIntervalInMillis().ifPresent(        o -> pollingIntervalInMillis = o        );
      wicaStreamProperties.getOptionalFieldsOfInterest().ifPresent(               o -> fieldsOfInterest = o               );
      wicaStreamProperties.getOptionalNumericPrecision().ifPresent(               o -> numericPrecision = o               );
      wicaStreamProperties.getOptionalFilterType().ifPresent(                     o -> filterType = o                     );
      wicaStreamProperties.getOptionalFilterNumSamples().ifPresent(               o -> filterNumSamples = o               );
      wicaStreamProperties.getOptionalFilterCycleLength().ifPresent(              o -> filterCycleLength = o              );
      wicaStreamProperties.getOptionalFilterSamplingIntervalInMillis().ifPresent( o -> filterSamplingIntervalInMillis = o );
      wicaStreamProperties.getOptionalFilterDeadband().ifPresent(                 o -> filterDeadband = o                 );

      return this;
   }

   public WicaChannelPropertiesBuilder withChannelProperties( WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaChannelProperties, "The 'wicaStreamProperties' argument was null." );
      wicaChannelProperties.getOptionalDataAcquisitionMode().ifPresent(            o -> dataAcquisitionMode = o            );
      wicaChannelProperties.getOptionalPollingIntervalInMillis().ifPresent(        o -> pollingIntervalInMillis = o        );
      wicaChannelProperties.getOptionalFieldsOfInterest().ifPresent(               o -> fieldsOfInterest = o               );
      wicaChannelProperties.getOptionalNumericPrecision().ifPresent(               o -> numericPrecision = o               );
      wicaChannelProperties.getOptionalFilterType().ifPresent(                     o -> filterType = o                     );
      wicaChannelProperties.getOptionalFilterNumSamples().ifPresent(               o -> filterNumSamples = o               );
      wicaChannelProperties.getOptionalFilterCycleLength().ifPresent(              o -> filterCycleLength = o              );
      wicaChannelProperties.getOptionalFilterSamplingIntervalInMillis().ifPresent( o -> filterSamplingIntervalInMillis = o );
      wicaChannelProperties.getOptionalFilterDeadband().ifPresent(                 o -> filterDeadband = o                 );

      return this;
   }

   public WicaChannelPropertiesBuilder withDataAcquisitionMode( WicaDataAcquisitionMode dataAcquisitionMode )
   {
      this.dataAcquisitionMode = Validate.notNull( dataAcquisitionMode,"The 'dataAcquisitionMode' argument was null." );
      return this;
   }

   public WicaChannelPropertiesBuilder withPollingIntervalInMillis( int pollingIntervalInMillis )
   {
      Validate.isTrue( pollingIntervalInMillis >= 0,"The 'pollingIntervalInMillis' argument was negative." );
      this.pollingIntervalInMillis = pollingIntervalInMillis;
      return this;
   }

   public WicaChannelPropertiesBuilder withFieldsOfInterest( String fieldsOfInterest )
   {
      this.fieldsOfInterest = Validate.notNull( fieldsOfInterest, "The 'fieldsOfInterest' argument was null." );
      return this;
   }

   public WicaChannelPropertiesBuilder withNumericPrecision( int numericPrecision )
   {
      Validate.isTrue( numericPrecision >= 0,"The 'numericPrecision' argument was negative." );
      this.numericPrecision = numericPrecision;
      return this;
   }

   public WicaChannelPropertiesBuilder withFilterType( WicaFilterType filterType )
   {
      this.filterType = Validate.notNull( filterType, "The 'filterType' argument was null." );
      return this;
   }

   public WicaChannelPropertiesBuilder withFilterNumSamples( int filterNumSamples )
   {
      Validate.isTrue( filterNumSamples >= 0,"The 'filterNumSamples' argument was negative." );
      this.filterNumSamples = filterNumSamples;
      return this;
   }

   public WicaChannelPropertiesBuilder withFilterCycleLength( int filterCycleLength )
   {
      Validate.isTrue( filterCycleLength >= 0,"The 'filterCycleLength' argument was negative." );
      this.filterCycleLength = filterCycleLength;
      return this;
   }

   public WicaChannelPropertiesBuilder withFilterSamplingIntervalInMillis( int filterSamplingIntervalInMillis )
   {
      Validate.isTrue( filterSamplingIntervalInMillis >= 0,"The 'filterSamplingIntervalInMillis' argument was negative." );
      this.filterSamplingIntervalInMillis = filterSamplingIntervalInMillis;
      return this;
   }

   public WicaChannelPropertiesBuilder withFilterDeadband( double filterDeadband )
   {
      Validate.isTrue( filterDeadband >= 0.0,"The 'filterDeadband' argument was negative." );
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
                                        filterSamplingIntervalInMillis,
                                        filterDeadband );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
