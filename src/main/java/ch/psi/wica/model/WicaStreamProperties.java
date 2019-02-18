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

   public static final int DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS = 10_000;
   public static final int DEFAULT_VALUE_CHANGE_FLUX_INTERVAL_IN_MILLIS = 100;
   public static final int DEFAULT_VALUE_POLL_FLUX_INTERVAL_IN_MILLIS = 100;
   public static final int DEFAULT_NUMERIC_PRECISION = 8;
   public static final String DEFAULT_FIELDS_OF_INTEREST = "val;sevr";
   public static final WicaChannelProperties.DataAcquisitionMode DEFAULT_DAQ_MODE = WicaChannelProperties.DataAcquisitionMode.MONITOR;

/*- Private attributes -------------------------------------------------------*/

   private final Integer heartbeatFluxInterval;
   private final Integer valueChangeFluxInterval;
   private final Integer valuePollFluxInterval;
   private final Integer numericPrecision;
   private final Set<String> fieldsOfInterest;
   private final WicaChannelProperties.DataAcquisitionMode dataAcquisitionMode;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamProperties()
   {
      this.heartbeatFluxInterval   = DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS;
      this.valueChangeFluxInterval = DEFAULT_VALUE_CHANGE_FLUX_INTERVAL_IN_MILLIS;
      this.valuePollFluxInterval   = DEFAULT_VALUE_POLL_FLUX_INTERVAL_IN_MILLIS;
      this.dataAcquisitionMode     = DEFAULT_DAQ_MODE;
      this.numericPrecision        = DEFAULT_NUMERIC_PRECISION;
      this.fieldsOfInterest        = Set.of( DEFAULT_FIELDS_OF_INTEREST.split(";" ) ) ;
   }

   public WicaStreamProperties( @JsonProperty( "heartbeat" ) Integer heartbeatFluxIntervalInMillis,
                                @JsonProperty( "changeint" ) Integer valueChangeFluxIntervalInMillis,
                                @JsonProperty( "pollint"   ) Integer valuePollFluxIntervalInMillis,
                                @JsonProperty( "prec"      ) Integer numericPrecision,
                                @JsonProperty( "fields"    ) String fieldsOfInterest,
                                @JsonProperty( "daqmode"   ) WicaChannelProperties.DataAcquisitionMode dataAcquisitionMode
   )
   {
      this.heartbeatFluxInterval   = heartbeatFluxIntervalInMillis   == null ? DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS     : heartbeatFluxIntervalInMillis;
      this.valueChangeFluxInterval = valueChangeFluxIntervalInMillis == null ? DEFAULT_VALUE_CHANGE_FLUX_INTERVAL_IN_MILLIS  : valueChangeFluxIntervalInMillis;
      this.valuePollFluxInterval   = valuePollFluxIntervalInMillis   == null ? DEFAULT_VALUE_POLL_FLUX_INTERVAL_IN_MILLIS    : valuePollFluxIntervalInMillis;
      this.numericPrecision        = numericPrecision                == null ? DEFAULT_NUMERIC_PRECISION                     : numericPrecision;
      this.dataAcquisitionMode     = dataAcquisitionMode             == null ? DEFAULT_DAQ_MODE                              : dataAcquisitionMode;
      this.fieldsOfInterest        = fieldsOfInterest                == null ? stringToFieldsOfInterest( DEFAULT_FIELDS_OF_INTEREST ) : stringToFieldsOfInterest(fieldsOfInterest );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @JsonIgnore
   public int getHeartbeatFluxIntervalInMillis()
   {
      return heartbeatFluxInterval;
   }

   @JsonIgnore
   public int getValueChangeFluxIntervalInMillis()
   {
      return valueChangeFluxInterval;
   }

   @JsonIgnore
   public int getValuePollFluxIntervalInMillis()
   {
      return valuePollFluxInterval;
   }

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
            ", valueChangeFluxInterval=" + valueChangeFluxInterval +
            ", valuePollFluxInterval=" + valuePollFluxInterval +
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
