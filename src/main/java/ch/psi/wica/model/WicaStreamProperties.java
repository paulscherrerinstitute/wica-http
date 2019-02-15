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

   public static final int DEFAULT_HEARTBEAT_FLUX_INTERVAL = 10000;
   public static final int DEFAULT_CHANNEL_VALUE_CHANGE_FLUX_INTERVAL = 100;
   public static final int DEFAULT_CHANNEL_VALUE_POLLING_FLUX_INTERVAL = 100;
   public static final int DEFAULT_NUMERIC_PRECISION = 8;
   public static final String DEFAULT_FIELDS_OF_INTEREST = "val;sevr";
   public static final WicaChannelProperties.DaqType DEFAULT_DAQ_TYPE= WicaChannelProperties.DaqType.MONITORER;

/*- Private attributes -------------------------------------------------------*/

   private final Integer heartbeatFluxInterval;
   private final Integer channelValueChangeFluxInterval;
   private final Integer channelValuePollingFluxInterval;
   private final WicaChannelProperties.DaqType daqType;
   private final Integer numericPrecision;
   private final Set<String> fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamProperties()
   {
      this.heartbeatFluxInterval = DEFAULT_HEARTBEAT_FLUX_INTERVAL;
      this.channelValueChangeFluxInterval = DEFAULT_CHANNEL_VALUE_CHANGE_FLUX_INTERVAL;
      this.channelValuePollingFluxInterval = DEFAULT_CHANNEL_VALUE_POLLING_FLUX_INTERVAL;
      this.daqType = DEFAULT_DAQ_TYPE;
      this.numericPrecision = DEFAULT_NUMERIC_PRECISION;
      this.fieldsOfInterest = Set.of( DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) ;
   }

   public WicaStreamProperties( @JsonProperty( "heartbeatFluxInterval" ) Integer heartbeatFluxInterval,
                                @JsonProperty( "channelValueChangeFluxInterval" ) Integer channelValueChangeFluxInterval,
                                @JsonProperty( "channelValuePollingFluxInterval" ) Integer channelValuePollingFluxInterval,
                                @JsonProperty( "daqType" ) WicaChannelProperties.DaqType daqType,
                                @JsonProperty( "prec" ) Integer numericPrecision,
                                @JsonProperty( "fields" ) String fieldsOfInterest )
   {
      this.heartbeatFluxInterval = heartbeatFluxInterval == null ? DEFAULT_HEARTBEAT_FLUX_INTERVAL : heartbeatFluxInterval;
      this.channelValueChangeFluxInterval = channelValueChangeFluxInterval == null ? DEFAULT_CHANNEL_VALUE_CHANGE_FLUX_INTERVAL : channelValueChangeFluxInterval;
      this.channelValuePollingFluxInterval = channelValuePollingFluxInterval == null ? DEFAULT_CHANNEL_VALUE_POLLING_FLUX_INTERVAL : channelValuePollingFluxInterval;
      this.numericPrecision = numericPrecision == null ? DEFAULT_NUMERIC_PRECISION : numericPrecision;
      this.fieldsOfInterest = fieldsOfInterest == null ? stringToFieldsOfInterest( DEFAULT_FIELDS_OF_INTEREST ) : stringToFieldsOfInterest( fieldsOfInterest );
      this.daqType = daqType == null ? DEFAULT_DAQ_TYPE : daqType;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @JsonIgnore
   public int getHeartbeatFluxInterval()
   {
      return heartbeatFluxInterval;
   }

   @JsonIgnore
   public int getChannelValueChangeFluxInterval()
   {
      return channelValueChangeFluxInterval;
   }

   @JsonIgnore
   public int getChannelValuePollingFluxInterval()
   {
      return channelValuePollingFluxInterval;
   }

   @JsonIgnore
   public WicaChannelProperties.DaqType gettDaqType()
   {
      return daqType;
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

/*- Private methods ----------------------------------------------------------*/

   private static Set<String> stringToFieldsOfInterest( String inputString )
   {
      Validate.notBlank(inputString );
      return Set.of( inputString.split( ";" ) );
   }
/*- Nested Classes -----------------------------------------------------------*/

}
