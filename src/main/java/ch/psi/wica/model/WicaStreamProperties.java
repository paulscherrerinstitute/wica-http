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
   public static final int DEFAULT_VALUE_UPDATE_FLUX_INTERVAL = 100;
   public static final int DEFAULT_NUMERIC_PRECISION = 8;
   public static final String DEFAULT_FIELDS_OF_INTEREST = "val;sevr";

/*- Private attributes -------------------------------------------------------*/

   private final Integer heartbeatFluxInterval;
   private final Integer channelValueUpdateFluxInterval;
   private final Integer numericPrecision;
   private final Set<String> fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamProperties()
   {
      this.heartbeatFluxInterval = DEFAULT_HEARTBEAT_FLUX_INTERVAL;
      this.channelValueUpdateFluxInterval = DEFAULT_VALUE_UPDATE_FLUX_INTERVAL;
      this.numericPrecision = DEFAULT_NUMERIC_PRECISION;
      this.fieldsOfInterest = Set.of( DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) ;
   }

   public WicaStreamProperties( @JsonProperty( "heartbeatInterval" ) Integer heartbeatFluxInterval,
                                @JsonProperty( "channelValueUpdateInterval" ) Integer channelValueUpdateFluxInterval,
                                @JsonProperty( "prec" ) Integer numericPrecision,
                                @JsonProperty( "fields" )String fieldsOfInterest )
   {
      this.heartbeatFluxInterval = heartbeatFluxInterval == null ? DEFAULT_HEARTBEAT_FLUX_INTERVAL : heartbeatFluxInterval;
      this.channelValueUpdateFluxInterval = channelValueUpdateFluxInterval == null ? DEFAULT_VALUE_UPDATE_FLUX_INTERVAL : channelValueUpdateFluxInterval;
      this.numericPrecision = numericPrecision == null ? DEFAULT_NUMERIC_PRECISION : numericPrecision;
      this.fieldsOfInterest = fieldsOfInterest == null ? stringToFieldsOfInterest( DEFAULT_FIELDS_OF_INTEREST ) : stringToFieldsOfInterest( fieldsOfInterest );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @JsonIgnore
   public int getHeartbeatFluxInterval()
   {
      return heartbeatFluxInterval;
   }

   @JsonIgnore
   public int getChannelValueUpdateFluxInterval()
   {
      return channelValueUpdateFluxInterval;
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
