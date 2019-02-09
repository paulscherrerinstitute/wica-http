/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;

import java.util.Set;

@JsonIgnoreProperties()
@Immutable
public class WicaStreamProperties
{

/*- Public attributes --------------------------------------------------------*/

   public static final int DEFAULT_HEARTBEAT_FLUX_INTERVAL = 10000;
   public static final int DEFAULT_VALUE_UPDATE_FLUX_INTERVAL = 100;
   public static final int DEFAULT_NUMERIC_PRECISION = 8;
   public static final String DEFAULT_FIELDS_OF_INTEREST = "type;val;sevr";

/*- Private attributes -------------------------------------------------------*/

   private final Integer heartbeatFluxInterval;
   private final Integer valueUpdateFluxInterval;
   private final Integer numericPrecision;
   private final Set<String> fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamProperties()
   {
      this.heartbeatFluxInterval = DEFAULT_HEARTBEAT_FLUX_INTERVAL;
      this.valueUpdateFluxInterval = DEFAULT_VALUE_UPDATE_FLUX_INTERVAL;
      this.numericPrecision = DEFAULT_NUMERIC_PRECISION;
      this.fieldsOfInterest = Set.of( DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) ;
   }

   public WicaStreamProperties( @JsonProperty( "heartbeatInterval" )    Integer heartbeatFluxInterval,
                                @JsonProperty( "value-update-interval" ) Integer valueUpdateFluxInterval,
                                @JsonProperty( "prec" )   Integer numericPrecision,
                                @JsonProperty( "fields" ) String fieldsOfInterest )
   {
      this.heartbeatFluxInterval = heartbeatFluxInterval == null ? DEFAULT_HEARTBEAT_FLUX_INTERVAL : heartbeatFluxInterval;
      this.valueUpdateFluxInterval = valueUpdateFluxInterval== null ? DEFAULT_VALUE_UPDATE_FLUX_INTERVAL : valueUpdateFluxInterval;
      this.numericPrecision = numericPrecision == null ? DEFAULT_NUMERIC_PRECISION : numericPrecision;
      this.fieldsOfInterest = fieldsOfInterest == null ? Set.of( DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) : Set.of( fieldsOfInterest.split( ";" ) );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public int getHeartbeatFluxInterval()
   {
      return heartbeatFluxInterval;
   }

   public int getChannelValueUpdateFluxInterval()
   {
      return valueUpdateFluxInterval;
   }

   public int getNumericPrecision()
   {
      return numericPrecision;
   }

   public Set<String> getFieldsOfInterest()
   {
      return fieldsOfInterest;
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
