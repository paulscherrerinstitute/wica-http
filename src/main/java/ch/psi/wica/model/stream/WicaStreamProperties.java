/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SuppressWarnings( { "WeakerAccess", "unused" } )
@JsonIgnoreProperties()
@Immutable
public class
WicaStreamProperties extends WicaChannelProperties
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


/*- Private attributes -------------------------------------------------------*/

   private static WicaStreamProperties DEFAULT_INSTANCE = new Builder().build();

   private static final Logger logger = LoggerFactory.getLogger(WicaChannelProperties.class);
   private static final ObjectMapper objectMapper = new ObjectMapper();

   private final Integer heartbeatFluxIntervalInMillis;
   private final Integer changedValueFluxIntervalInMillis;
   private final Integer polledValueFluxIntervalInMillis;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaStreamProperties( Integer heartbeatFluxIntervalInMillis,
                                 Integer changedValueFluxIntervalInMillis,
                                 Integer polledValueFluxIntervalInMillis )
   {
      super();
      this.heartbeatFluxIntervalInMillis = (heartbeatFluxIntervalInMillis == null) ? DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS : heartbeatFluxIntervalInMillis;
      this.changedValueFluxIntervalInMillis = (heartbeatFluxIntervalInMillis == null) ? DEFAULT_CHANGED_VALUE_FLUX_INTERVAL_IN_MILLIS : changedValueFluxIntervalInMillis;
      this.polledValueFluxIntervalInMillis = (heartbeatFluxIntervalInMillis == null) ? DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS : polledValueFluxIntervalInMillis;
   }

   private WicaStreamProperties( @JsonProperty( "heartbeat" ) Integer heartbeatFluxIntervalInMillis,
                                 @JsonProperty( "changeint" ) Integer changedValueFluxIntervalInMillis,
                                 @JsonProperty( "pollint" ) Integer polledValueFluxIntervalInMillis,
                                 @JsonProperty( "daqmode" ) DataAcquisitionMode channelDataAcquisitionMode,
                                 @JsonProperty( "pollratio" ) Integer channelPolledValueSamplingRatio,
                                 @JsonProperty( "fields" ) String channelFieldsOfInterest,
                                 @JsonProperty( "prec" ) Integer channelNumericPrecision,
                                 @JsonProperty( "filter" ) FilterType channelFilterType,
                                 @JsonProperty( "n" ) Integer channelFilterNumSamples,
                                 @JsonProperty( "m" ) Integer channelFilterCycleLength,
                                 @JsonProperty( "interval" ) Integer channelFilterSamplingIntervalInMillis,
                                 @JsonProperty( "deadband" ) Double channelFilterDeadband )
                           {
      super( channelDataAcquisitionMode,
             channelPolledValueSamplingRatio,
             channelFieldsOfInterest,
             channelNumericPrecision,
             channelFilterType,
             channelFilterNumSamples,
             channelFilterCycleLength,
             channelFilterSamplingIntervalInMillis,
             channelFilterDeadband);

      this.heartbeatFluxIntervalInMillis = captureOrDefault( heartbeatFluxIntervalInMillis, DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS );
      this.changedValueFluxIntervalInMillis = captureOrDefault( changedValueFluxIntervalInMillis, DEFAULT_CHANGED_VALUE_FLUX_INTERVAL_IN_MILLIS );
      this.polledValueFluxIntervalInMillis = captureOrDefault( polledValueFluxIntervalInMillis, DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS );
   }

/*- Class methods ------------------------------------------------------------*/

   /**
    * Returns an instance constructed from the information provided in the JSON
    * string descriptor.
    * <p>
    * The following keys define the properties that are unique to the stream.
    *
    * <code>heartbeat</code>
    * <code>changeint</code>
    * <code>pollint</code>
    * <p>
    * The following keys define the default properties for the channels in
    * the stream when not explicitly specified in the optional channel
    * properties descriptor:
    *
    * <code>daqmode</code>
    * <code>pollratio</code>
    * <code>fields</code>
    * <code>prec</code>
    * <code>filter</code>
    * <code>n</code>
    * <code>m</code>
    * <code>interval</code>
    * <code>deadband</code>
    *
    * @param jsonStringSpecifier the JSON string representation containing the property
    *                            keys and values.
    * @return the instance.
    * @throws IllegalArgumentException if the string specifier syntax was invalid.
    */
   public static WicaStreamProperties of( String jsonStringSpecifier )
   {
      try
      {
         return objectMapper.readValue(jsonStringSpecifier, WicaStreamProperties.class);
      }
      catch ( Exception ex )
      {
         final String msg = "The input string: '" + jsonStringSpecifier + "' was not a valid descriptor for the properties of a wica stream.";
         logger.warn(msg);
         logger.warn("The underlying exception cause was: '{}'.", ex.getMessage());
         throw new IllegalArgumentException(msg);
      }
   }

   /**
    * Returns an instance with default properties.
    *
    * @return the instance.
    */
   public static WicaStreamProperties createDefaultInstance()
   {
      return DEFAULT_INSTANCE;
   }


/*- Public methods -----------------------------------------------------------*/

   @JsonIgnore
   public Integer getHeartbeatFluxIntervalInMillis()
   {
      return heartbeatFluxIntervalInMillis;
   }

   @JsonIgnore
   public Integer getChangedValueFluxIntervalInMillis()
   {
      return changedValueFluxIntervalInMillis;
   }

   @JsonIgnore
   public Integer getPolledValueFluxIntervalInMillis()
   {
      return polledValueFluxIntervalInMillis;
   }

   // Implemented to simplify testing
   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaStreamProperties) ) return false;
      if ( !super.equals(o) ) return false;
      WicaStreamProperties that = (WicaStreamProperties) o;
      return Objects.equals(heartbeatFluxIntervalInMillis, that.heartbeatFluxIntervalInMillis) &&
            Objects.equals(changedValueFluxIntervalInMillis, that.changedValueFluxIntervalInMillis) &&
            Objects.equals(polledValueFluxIntervalInMillis, that.polledValueFluxIntervalInMillis);
   }

   // Implemented to simplify testing
   @Override
   public int hashCode()
   {
      return Objects.hash(super.hashCode(), heartbeatFluxIntervalInMillis, changedValueFluxIntervalInMillis, polledValueFluxIntervalInMillis);
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   @SuppressWarnings( "unused" )
   public static class Builder extends ChannelPropertyBuilder
   {
      private int heartbeatFluxIntervalInMillis = DEFAULT_HEARTBEAT_FLUX_INTERVAL_IN_MILLIS;
      private int changedValueFluxIntervalInMillis = DEFAULT_CHANGED_VALUE_FLUX_INTERVAL_IN_MILLIS;
      private int polledValueFluxIntervalInMillis = DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS;

      public Builder withHeartbeatFluxInterval( int heartbeatFluxIntervalInMillis )
      {
         this.heartbeatFluxIntervalInMillis = heartbeatFluxIntervalInMillis;
         return this;
      }

      public Builder withChangedValueFluxInterval( int changedValueFluxIntervalInMillis )
      {
         this.changedValueFluxIntervalInMillis = changedValueFluxIntervalInMillis;
         return this;
      }

      public Builder withPolledValueFluxInterval( int polledValueFluxIntervalInMillis )
      {
         this.polledValueFluxIntervalInMillis = polledValueFluxIntervalInMillis;
         return this;
      }

      public WicaStreamProperties build()
      {
         return new WicaStreamProperties( heartbeatFluxIntervalInMillis,
                                          changedValueFluxIntervalInMillis,
                                          polledValueFluxIntervalInMillis );
      }

   }
}
