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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@JsonPropertyOrder( { "val", "alarm", "ts" } )
@Immutable
public class EpicsChannelValue<T>
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
   private final EpicsChannelAlarmSeverity alarmSeverity;
   private final int alarmStatus;
   private final LocalDateTime wicaServerTimestamp;
   private final LocalDateTime epicsIocTimestamp;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance based on the supplied string representations
    * of the current value, the current alarm status and the supplied timestamp.
    *
    * @param value the string representation of the channel value. When
    *        the channel is non-scalar the normal Java representation should
    *        be supplied, thus: [ "abc", "def", "ghi" ]
    *
    * @param alarmSeverity the alarm severity
    *
    * @param wicaServerTimestamp the timestamp obtained from the Wica Server
    *                            at the time the instance was created.
    *
    * @param epicsIocTimestamp the timestamp sent by the remote IOC
    */
   public EpicsChannelValue( T value, EpicsChannelAlarmSeverity alarmSeverity, int alarmStatus, LocalDateTime wicaServerTimestamp, LocalDateTime epicsIocTimestamp )
   {
      this.value = Validate.notNull( value );
      this.alarmSeverity = Validate.notNull( alarmSeverity );
      this.alarmStatus = alarmStatus;
      this.wicaServerTimestamp = Validate.notNull( wicaServerTimestamp );
      this.epicsIocTimestamp = Validate.notNull( epicsIocTimestamp );
   }

/*- Class methods ------------------------------------------------------------*/

   public static String convertMapToJsonRepresentation( Map<EpicsChannelName,EpicsChannelValue> map )
   {
      try
      {
         return jsonObjectMapper.writeValueAsString( map );
      }
      catch ( JsonProcessingException ex )
      {
         return "error";
      }
   }

/*- Public methods -----------------------------------------------------------*/

   @JsonProperty( "val" )
   public T getValue()
   {
      return value;
   }

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

   @JsonProperty( "ts1" )
   public long getWicaServerTimestamp()
   {
      return wicaServerTimestamp.atOffset( ZoneOffset.UTC ).toInstant().toEpochMilli();
   }

   @JsonProperty( "ts2" )
   public long getEpicsIocTimestamp()
   {
      return epicsIocTimestamp.atOffset( ZoneOffset.UTC ).toInstant().toEpochMilli();
   }

/*- Private methods ----------------------------------------------------------*/

   @JsonIgnore
   public LocalDateTime getTimestamp()
   {
      return wicaServerTimestamp;
   }

   /*- Nested Classes -----------------------------------------------------------*/

}
