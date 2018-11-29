/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Queue;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@JsonPropertyOrder( { "val", "stat", "sevr", "ts1", "ts2", "ts1-alt", "ts2-alt" } )
@Immutable
public class WicaChannelValue<T>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * The format which will be used when making JSON or String representations
    * of the times/dates in this class.
    */
   private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
   private static final ObjectMapper jsonObjectMapper = new Jackson2ObjectMapperBuilder().createXmlMapper( false ).build();

   static {
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      jsonObjectMapper.configure( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false );
   }

   private final T value;
   private final WicaChannelAlarmSeverity alarmSeverity;
   private final int alarmStatus;
   private final LocalDateTime wicaServerTimestamp;
   private final LocalDateTime epicsIocTimestamp;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance based on the supplied value, alarm information
    * and the server and channel timestamps.
    *
    * @param value the channel value. Can be NULL to indicate the channel is
    *        disconnected. When the channel is non-scalar the normal Java
    *        array representation should be supplied, thus: [ "abc", "def",
    *        "ghi" ] etc.
    *
    * @param alarmSeverity the alarm severity.
    * @param alarmStatus the alarm status.
    *
    * @param wicaServerTimestamp the timestamp obtained from the Wica Server
    *                            at the time the instance was created.
    *
    * @param epicsIocTimestamp the timestamp sent by the remote IOC
    */
   private WicaChannelValue( T value, WicaChannelAlarmSeverity alarmSeverity, int alarmStatus, LocalDateTime wicaServerTimestamp, LocalDateTime epicsIocTimestamp )
   {
      this.value = value;
      this.alarmSeverity = Validate.notNull( alarmSeverity );
      this.alarmStatus = alarmStatus;
      this.wicaServerTimestamp = Validate.notNull( wicaServerTimestamp );
      this.epicsIocTimestamp = Validate.notNull( epicsIocTimestamp );
   }

/*- Class methods ------------------------------------------------------------*/

   public static <T> WicaChannelValue<T> createChannelDisconnectedValue( LocalDateTime wicaServerTimestamp )
   {
      return new WicaChannelValue<>( null, WicaChannelAlarmSeverity.INVALID_ALARM, 0, wicaServerTimestamp, wicaServerTimestamp );
   }

   public static <T> WicaChannelValue<T> createChannelConnectedValue( T value, WicaChannelAlarmSeverity alarmSeverity, int alarmStatus, LocalDateTime wicaServerTimestamp, LocalDateTime epicsIocTimestamp )
   {
      return new WicaChannelValue<>( value, alarmSeverity, alarmStatus, wicaServerTimestamp, epicsIocTimestamp );
   }

   public static String convertMapToJsonRepresentation( Map<WicaChannelName, Queue<WicaChannelValue>> map )
   {
      try
      {
         final String result = jsonObjectMapper.writeValueAsString( map );
         return result;
      }
      catch ( JsonProcessingException ex )
      {
         return "error";
      }
   }

/*- Public methods -----------------------------------------------------------*/

   // This method is mainly to explicitly document the fact that if the value
   // is set to null this indicates that the EPICS channel that provides the
   // information source is not available.
   @JsonIgnore
   public boolean isChannelConnected()
   {
      return value == null;
   }

   @JsonProperty( "val" )
   public T getValue()
   {
      return value;
   }

   @JsonIgnore
   @JsonProperty( "stat")
   public int getAlarmStatus()
   {
      return alarmStatus;
   }

   @JsonProperty( "sevr")
   public int getAlarmSeverity()
   {
      return alarmSeverity.ordinal();
   }

   @JsonIgnore
   @JsonProperty( "ts1" )
   public long getWicaServerTimestamp()
   {
      return wicaServerTimestamp.atOffset( ZoneOffset.UTC ).toInstant().toEpochMilli();
   }

   @JsonIgnore
   @JsonProperty( "ts2" )
   public long getEpicsIocTimestamp()
   {
      return epicsIocTimestamp.atOffset( ZoneOffset.UTC ).toInstant().toEpochMilli();
   }

   @JsonIgnore
   @JsonProperty( "ts1-alt" )
   public LocalDateTime getWicaServerTimestampAlt()
   {
      return wicaServerTimestamp;
   }

   @JsonIgnore
   @JsonProperty( "ts2-alt" )
   public LocalDateTime getEpicsIocTimestampAlt()
   {
      return epicsIocTimestamp;
   }

/*- Private methods ----------------------------------------------------------*/

   @JsonIgnore
   public LocalDateTime getTimestamp()
   {
      return wicaServerTimestamp;
   }

/*- Nested Classes -----------------------------------------------------------*/

}
