/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;

import java.util.Set;

@JsonIgnoreProperties( ignoreUnknown=false )
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
   private final Integer valueUpdateFluxInterval;
   private final Integer numericPrecision;
   private final String fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamProperties()
   {
      this.heartbeatFluxInterval = DEFAULT_HEARTBEAT_FLUX_INTERVAL;
      this.valueUpdateFluxInterval = DEFAULT_VALUE_UPDATE_FLUX_INTERVAL;
      this.numericPrecision = DEFAULT_NUMERIC_PRECISION;
      this.fieldsOfInterest = DEFAULT_FIELDS_OF_INTEREST;
   }

   public WicaStreamProperties( @JsonProperty( "heartbeat-flux-interval" )    Integer heartbeatFluxInterval,
                                @JsonProperty( "value-update-flux-interval" ) Integer valueUpdateFluxInterval,
                                @JsonProperty( "prec" )   Integer numericPrecision,
                                @JsonProperty( "fields" ) String fieldsOfInterest )
   {
      this.heartbeatFluxInterval = heartbeatFluxInterval;
      this.valueUpdateFluxInterval = valueUpdateFluxInterval;
      this.numericPrecision = numericPrecision;
      this.fieldsOfInterest = fieldsOfInterest;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public int getHeartbeatFluxInterval()
   {
      return heartbeatFluxInterval == null ? DEFAULT_HEARTBEAT_FLUX_INTERVAL : heartbeatFluxInterval;
   }

   public int getChannelValueUpdateFluxInterval()
   {
      return valueUpdateFluxInterval == null ? DEFAULT_VALUE_UPDATE_FLUX_INTERVAL : valueUpdateFluxInterval;
   }

   public int getNumericPrecision()
   {
      return numericPrecision == null ? DEFAULT_NUMERIC_PRECISION : numericPrecision;
   }

   public Set<String> getFieldsOfInterest()
   {
      return fieldsOfInterest == null ? Set.of( DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) : Set.of( fieldsOfInterest.split( ";" ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
