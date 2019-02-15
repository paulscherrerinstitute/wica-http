/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;

import java.util.Optional;
import java.util.Set;

@JsonIgnoreProperties()
@Immutable
public class WicaChannelProperties
{

/*- Public attributes --------------------------------------------------------*/

   public static final FilterType DEFAULT_FILTER_TYPE = FilterType.LAST_N;
   public static final int DEFAULT_N = 1;
   public static final int DEFAULT_INTERVAL = 1000;
   public static final double DEFAULT_DEADBAND = 1.0;


/*- Private attributes -------------------------------------------------------*/

   private final Integer numericPrecision;
   private final FilterType filterType ;
   private final DaqType daqType ;
   private final Integer n;
   private final Integer interval;
   private final Double deadband;
   private final String fieldsOfInterest;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelProperties()
   {
      this.daqType = null;
      this.fieldsOfInterest = null;
      this.numericPrecision = null;
      this.filterType = DEFAULT_FILTER_TYPE;
      this.n = DEFAULT_N;
      this.interval = DEFAULT_INTERVAL;
      this.deadband = DEFAULT_DEADBAND;
   }

   public WicaChannelProperties( @JsonProperty( "daqType" )    DaqType daqType,
                                 @JsonProperty( "fields" )     String fieldsOfInterest,
                                 @JsonProperty( "prec" )       Integer numericPrecision,
                                 @JsonProperty( "filterType" ) FilterType filterType,
                                 @JsonProperty( "n" )          Integer n,
                                 @JsonProperty( "interval" )   Integer interval,
                                 @JsonProperty( "deadband" )   Double deadband )
   {
      this.daqType = daqType;
      this.fieldsOfInterest = fieldsOfInterest;
      this.numericPrecision = numericPrecision;
      this.filterType = filterType == null ? DEFAULT_FILTER_TYPE : filterType;
      this.n = n == null ? DEFAULT_N : n;
      this.interval =  interval == null ? DEFAULT_INTERVAL : interval;
      this.deadband = deadband == null ? DEFAULT_DEADBAND : deadband;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @JsonIgnore
   public Optional<DaqType> getDaqType()
   {
      return daqType == null ? Optional.empty() : Optional.of( daqType );
   }

   @JsonIgnore
   public Optional<Set<String>> getFieldsOfInterest()
   {
      return fieldsOfInterest == null ? Optional.empty() : Optional.of( Set.of( fieldsOfInterest.split( ";" ) ) );
   }

   @JsonIgnore
   public Optional<Integer> getNumericPrecision()
   {
      return numericPrecision == null ? Optional.empty() : Optional.of( numericPrecision );
   }

   @JsonIgnore
   public FilterType getFilterType()
   {
      return filterType;
   }


   @JsonIgnore
   public int getN()
   {
      return n;
   }

   @JsonIgnore
   public int getInterval()
   {
      return interval;
   }

   @JsonIgnore
   public double getDeadband()
   {
      return deadband;
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
   }

   public enum DaqType
   {
      @JsonProperty( "poller" )    POLLER,
      @JsonProperty( "monitorer" ) MONITORER
   }
}
