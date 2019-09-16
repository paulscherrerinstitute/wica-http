/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.stream.WicaStreamPropertiesDefaults;
import net.jcip.annotations.Immutable;

/**
 * Represents the default wica channel property values that will be used
 * if the property is not explicitly specified when creating the channel.
 */
@Immutable
public class WicaChannelPropertiesDefaults
{

/*- Public attributes --------------------------------------------------------*/

   /**
    * Default value for the data acquisition mode.
    */
   public static final WicaDataAcquisitionMode DEFAULT_DATA_ACQUISITION_MODE = WicaStreamPropertiesDefaults.DEFAULT_DATA_ACQUISITION_MODE;

   /**
    * Default value for the polling interval.
    */
   public static final int DEFAULT_POLLING_INTERVAL_IN_MILLIS = WicaStreamPropertiesDefaults.DEFAULT_POLLING_INTERVAL_IN_MILLIS;

   /**
    * Default value for the numeric precision.
    */
   public static final int DEFAULT_NUMERIC_PRECISION = WicaStreamPropertiesDefaults.DEFAULT_NUMERIC_PRECISION;

   /**
    * Default value for the fields of interest that will be serialized in the channel's data stream.
    */
   public static final String DEFAULT_FIELDS_OF_INTEREST = WicaStreamPropertiesDefaults.DEFAULT_FIELDS_OF_INTEREST;

   /**
    * Default value for the type of filter that will be applied when mapping between the data samples
    * acquired by the channel and their representation in the wica data stream.
    */
   public static final WicaFilterType DEFAULT_FILTER_TYPE = WicaStreamPropertiesDefaults.DEFAULT_FILTER_TYPE;

   /**
    * Default value for the filter number-of-samples parameter (only relevant when the filter type is FilterType.LAST_N).
    */
   public static final int DEFAULT_FILTER_NUM_SAMPLES = WicaStreamPropertiesDefaults.DEFAULT_FILTER_NUM_SAMPLES;

   /**
    * Default value for the filter cycle-length parameter (only relevant when the filter type is FilterType.ONE_IN_M).
    */
   public static final int DEFAULT_FILTER_CYCLE_LENGTH = WicaStreamPropertiesDefaults.DEFAULT_FILTER_CYCLE_LENGTH;

   /**
    * Default value for the filter sampling-interval parameter (only relevant when the filter type is FilterType.RATE_LIMITER).
    */
   public static final int DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS = WicaStreamPropertiesDefaults.DEFAULT_FILTER_SAMPLING_INTERVAL_IN_MILLIS;

   /**
    * Default value for the filter deadband parameter (only relevant when the filter type is FilterType.CHANGE_FILTERER).
    */
   public static final double DEFAULT_FILTER_DEADBAND = WicaStreamPropertiesDefaults.DEFAULT_FILTER_DEADBAND;


/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}