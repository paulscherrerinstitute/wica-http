/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

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
   public static final int DEFAULT_CHANGED_VALUE_FLUX_INTERVAL_IN_MILLIS = 100;

   /**
    * Default value for the polled value flux interval that will be assigned if
    * the property is not explicitly set by configuration on the client.
    */
   public static final int DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS = 100;

   /**
    * Default value for the polled value sample ratio that will be assigned
    * if the property is not explicitly set by configuration on the client.
    */
   public static final int DEFAULT_POLLED_VALUE_SAMPLE_RATIO            = 10;

   /**
    * Default value for the numeric precision that will be assigned if
    * the property is not explicitly set by configuration on the client.
    */
   public static final int DEFAULT_NUMERIC_PRECISION = 8;

   /**
    * Default value for the fields of interest that will be assigned if
    * the property is not explicitly set by configuration on the client.
    */
   public static final String DEFAULT_FIELDS_OF_INTEREST = "val;sevr";

   /**
    * Default value for the data acqusition mode that will be assigned if
    * the property is not explicitly set by configuration on the client.
    */
   public static final WicaChannelProperties.DataAcquisitionMode DEFAULT_DATA_ACQUISITION_MODE = WicaChannelProperties.DataAcquisitionMode.MONITOR;

/*- Private attributes -------------------------------------------------------*/

   private final Integer heartbeatFluxInterval;
   private final Integer changedValueFluxInterval;
   private final Integer polledValueFluxInterval;
   private final Integer polledValueSampleRatio;
   private final Integer numericPrecision;
   private final Set<String> fieldsOfInterest;
   private final WicaChannelProperties.DataAcquisitionMode dataAcquisitionMode;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamProperties()
   {
      this.heartbeatFluxInterval    = DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS;
      this.changedValueFluxInterval = DEFAULT_CHANGED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      this.polledValueFluxInterval  = DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      this.polledValueSampleRatio   = DEFAULT_POLLED_VALUE_SAMPLE_RATIO;
      this.dataAcquisitionMode      = DEFAULT_DATA_ACQUISITION_MODE;
      this.numericPrecision         = DEFAULT_NUMERIC_PRECISION;
      this.fieldsOfInterest         = Set.of( DEFAULT_FIELDS_OF_INTEREST.split(";" ) ) ;
   }

   public WicaStreamProperties( @JsonProperty( "heartbeat" ) Integer heartbeatFluxIntervalInMillis,
                                @JsonProperty( "changeint" ) Integer changedValueFluxIntervalInMillis,
                                @JsonProperty( "pollint"   ) Integer polledValueFluxIntervalInMillis,
                                @JsonProperty( "pollrat"   ) Integer polledValueSampleRatio,
                                @JsonProperty( "prec"      ) Integer numericPrecision,
                                @JsonProperty( "fields"    ) String fieldsOfInterest,
                                @JsonProperty( "daqmode"   ) WicaChannelProperties.DataAcquisitionMode dataAcquisitionMode )
   {
      this.heartbeatFluxInterval    = heartbeatFluxIntervalInMillis    == null ? DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS     : heartbeatFluxIntervalInMillis;
      this.changedValueFluxInterval = changedValueFluxIntervalInMillis == null ? DEFAULT_CHANGED_VALUE_FLUX_INTERVAL_IN_MILLIS : changedValueFluxIntervalInMillis;
      this.polledValueFluxInterval  = polledValueFluxIntervalInMillis  == null ? DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS  : polledValueFluxIntervalInMillis;
      this.polledValueSampleRatio   = polledValueSampleRatio           == null ? DEFAULT_POLLED_VALUE_SAMPLE_RATIO             : polledValueSampleRatio;
      this.numericPrecision         = numericPrecision                 == null ? DEFAULT_NUMERIC_PRECISION                     : numericPrecision;
      this.dataAcquisitionMode      = dataAcquisitionMode              == null ? DEFAULT_DATA_ACQUISITION_MODE                 : dataAcquisitionMode;
      this.fieldsOfInterest         = fieldsOfInterest                 == null ? stringToFieldsOfInterest( DEFAULT_FIELDS_OF_INTEREST ) : stringToFieldsOfInterest(fieldsOfInterest );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @JsonIgnore
   public int getHeartbeatFluxIntervalInMillis()
   {
      return heartbeatFluxInterval;
   }

   @JsonIgnore
   public int getChangedValueFluxIntervalInMillis()
   {
      return changedValueFluxInterval;
   }

   @JsonIgnore
   public int getPolledValueFluxIntervalInMillis()
   {
      return polledValueFluxInterval;
   }

   @JsonIgnore
   public int getPolledValueSampleRatio() { return polledValueSampleRatio; }

   @JsonIgnore
   public WicaChannelProperties.DataAcquisitionMode getDataAcquisitionMode()
   {
      return dataAcquisitionMode;
   }

   @JsonIgnore
   public int getNumericPrecision()
   {
      return numericPrecision;
   }

   @JsonIgnore
   public Set<String> getFieldsOfInterest()
   {
      return fieldsOfInterest;
   }

   @Override
   public String toString()
   {
      return "WicaStreamProperties{" +
            "heartbeatFluxInterval=" + heartbeatFluxInterval +
            ", changedValueFluxInterval=" + changedValueFluxInterval +
            ", polledValueFluxInterval=" + polledValueFluxInterval +
            ", polledValueSampleRatio=" + polledValueSampleRatio +
            ", numericPrecision=" + numericPrecision +
            ", fieldsOfInterest=" + fieldsOfInterest +
            ", dataAcquisitionMode=" + dataAcquisitionMode +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/

   private static Set<String> stringToFieldsOfInterest( String inputString )
   {
      Validate.notBlank(inputString );
      return Set.of( inputString.split( ";" ) );
   }

/*- Nested Classes -----------------------------------------------------------*/


}
