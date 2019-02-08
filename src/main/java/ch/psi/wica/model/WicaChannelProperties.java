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
public class WicaChannelProperties
{

/*- Public attributes --------------------------------------------------------*/

   public static final int DEFAULT_NUMERIC_PRECISION = 8;
   public static final FilterType DEFAULT_FILTER_TYPE = FilterType.LAST_N;
   public static final String DEFAULT_FILTER_PARAMETER = "1";
   public static final String DEFAULT_FIELDS_OF_INTEREST = "val;sevr";

/*- Private attributes -------------------------------------------------------*/

   private final Integer numericPrecision;
   private final FilterType filterType ;
   private final String filterParameter;
   private final String fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelProperties()
   {
      this.numericPrecision = DEFAULT_NUMERIC_PRECISION;
      this.filterType = DEFAULT_FILTER_TYPE;
      this.filterParameter = DEFAULT_FILTER_PARAMETER;
      this.fieldsOfInterest = DEFAULT_FIELDS_OF_INTEREST;
   }

   public WicaChannelProperties( @JsonProperty( "prec" )  Integer numericPrecision,
                                 @JsonProperty( "filter" ) FilterType filterType,
                                 @JsonProperty( "fparam" ) String filterParameter,
                                 @JsonProperty( "fields" ) String fieldsOfInterest )
   {
      this.numericPrecision = numericPrecision;
      this.filterType = filterType;
      this.filterParameter = filterParameter;
      this.fieldsOfInterest = fieldsOfInterest;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Integer getNumericPrecision()
   {
      return numericPrecision == null ? DEFAULT_NUMERIC_PRECISION : numericPrecision;
   }
   public FilterType getFilterType()
   {
      return filterType == null ? DEFAULT_FILTER_TYPE : filterType;
   }
   public String getFilterParameter()
   {
      return filterParameter == null ? DEFAULT_FILTER_PARAMETER : filterParameter;
   }

   public Set<String> getFieldsOfInterest()
   {
      return fieldsOfInterest == null ? Set.of( DEFAULT_FIELDS_OF_INTEREST.split( ";" ) ) : Set.of( fieldsOfInterest.split( ";" ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   public enum FilterType
   {
      @JsonProperty( "all-value" )       ALL_VALUE,
      @JsonProperty( "rate-limiter" )    RATE_LIMITER,
      @JsonProperty( "one-in-n" )        ONE_IN_N,
      @JsonProperty( "last-n" )          LAST_N,
      @JsonProperty( "change-filterer" ) CHANGE_FILTERER,
      @JsonProperty( "default" )         DEFAULT;

      String filterName;
      String filterParameterName;

//      FilterType( String filterName, String filterParameterName )
//      {
//         this.filterName = filterName;
//         this.filterParameterName = filterParameterName;
//      }
   }
}
