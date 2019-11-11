/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import net.jcip.annotations.Immutable;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the property values that will be used if not explicitly specified
 * in the configuration information provided when the stream is created.
 */
@Immutable
public class WicaStreamPropertiesDefaults
{

/*- Public attributes --------------------------------------------------------*/

   /**
    * Default value for the heartbeat flux interval that will be assigned if
    * the property is not explicitly set in the stream configuration.
    */
   public static final int DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS = 10_000;

   /**
    * Default value for the metadata flux interval that will be assigned if
    * the property is not explicitly set in the stream configuration.
    */
   public static final int DEFAULT_METADATA_FLUX_INTERVAL_IN_MILLIS = 100;

   /**
    * Default value for the changed value flux interval that will be assigned if
    * the property is not explicitly set in the stream configuration.
    */
   public static final int DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS = 100;

   /**
    * Default value for the polled value flux interval that will be assigned if
    * the property is not explicitly set in the stream configuration.
    */
   public static final int DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS = 1000;

   /**
    * Default value for the data acquisition mode for channels in the stream
    * if the property is not explicitly set in the stream property or
    * channel property configuration.
    */
   public static final WicaDataAcquisitionMode DEFAULT_DATA_ACQUISITION_MODE = WicaDataAcquisitionMode.MONITOR;

   /**
    * Default value for the polling interval that will be used for channels
    * in the stream if the property is not explicitly set in the stream
    * property or individual channel property configuration.
    */
   public static final int DEFAULT_POLLING_INTERVAL_IN_MILLIS = 1000;

   /**
    * Default value for the numeric precision that will be used when
    * serializing real numbers for channels in the stream if the property
    * is not explicitly set in the stream property or individual channel
    * property configuration.
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
   public static final WicaFilterType DEFAULT_FILTER_TYPE = WicaFilterType.LAST_N;

   /**
    * Default value for the number of samples (used for channels whose
    * filter type is FilterType.LAST_N).
    */
   public static final int DEFAULT_FILTER_NUM_SAMPLES = 1;

   /**
    * Default value for the number of samples (used for channels whose
    * filter type is FilterType.AVERAGER).
    */
   public static final int DEFAULT_FILTER_NUM_SAMPLES_IN_AVERAGE = 1;
   /**
    * Default value for the cycle length (used for channels whose
    * filter type is FilterType.ONE_IN_M).
    */
   public static final int DEFAULT_FILTER_CYCLE_LENGTH = 1;

   /**
    * Default value for the sampling interval (used for channels whose
    * filter type is FilterType.RATE_LIMITER).
    */
   public static final int DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS = 1000;

   /**
    * Default value for the deadband (used for channels whose
    * filter type is FilterType.CHANGE_DETECTOR).
    */
   public static final double DEFAULT_FILTER_DEADBAND = 1.0;


/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
