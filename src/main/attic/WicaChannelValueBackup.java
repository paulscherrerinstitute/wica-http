/*- Package Declaration ------------------------------------------------------*/

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelAlarmSeverity;
import ch.psi.wica.model.WicaChannelType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@JsonPropertyOrder( { "conn", "type", "val", "stat", "sevr", "ts", "ts1", "ts2", "ts1-alt", "ts2-alt" } )
@Immutable
public class WicaChannelValueBackup<T>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final boolean connected;
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
    * @param connected whether the value represents a situation where the
    *        connection to the underlying data source was available or not.
    *
    * @param value the channel value. This can be any scalar or non-scalar
    *        type.
    *
    *
    *              When the channel is non-scalar the normal
    *        Java array representation should be supplied, thus: [ "abc", "def",
    *        "ghi" ] etc. Set to NULL when the channel's state is unconnected.
    *
    * @param alarmSeverity the alarm severity. Set to INVALID_ALARM when
    *        the channel's state is unconnected.
    *
    * @param alarmStatus the alarm status. Set to zero when the channel's
    *        state is unconnected.
    *
    * @param wicaServerTimestamp the timestamp obtained from the Wica Server
    *        at the time the instance was created.
    *
    * @param epicsIocTimestamp the timestamp sent by the remote IOC at the
    *        time the instance. When the channel's state is unconnected
    *        set to the time the instance was created.
    */
   private WicaChannelValueBackup( boolean connected, T value, WicaChannelAlarmSeverity alarmSeverity, int alarmStatus, LocalDateTime wicaServerTimestamp, LocalDateTime epicsIocTimestamp )
   {
      this.connected = connected;
      this.value = Validate.notNull( value );
      this.alarmSeverity = Validate.notNull( alarmSeverity );
      this.alarmStatus = alarmStatus;
      this.wicaServerTimestamp = Validate.notNull( wicaServerTimestamp );
      this.epicsIocTimestamp = Validate.notNull( epicsIocTimestamp );
   }

/*- Class methods ------------------------------------------------------------*/

   public static <T> WicaChannelValueBackup<T> createChannelDisconnectedValue()
   {
      return new WicaChannelValueBackup<>(false, null, WicaChannelAlarmSeverity.INVALID_ALARM, 0, LocalDateTime.now(), LocalDateTime.now() );
   }

   public static <T> WicaChannelValueBackup<T> createChannelConnectedValue( T value, WicaChannelAlarmSeverity alarmSeverity, int alarmStatus, LocalDateTime wicaServerTimestamp, LocalDateTime epicsIocTimestamp )
   {
      return new WicaChannelValueBackup<>(true, value, alarmSeverity, alarmStatus, wicaServerTimestamp, epicsIocTimestamp );
   }

/*- Public methods -----------------------------------------------------------*/

   @JsonView( {ConnectedView.class, DisconnectedView.class } )
   @JsonIgnore
   @JsonProperty( "conn" )
   public boolean isConnected()
   {
      return connected;
   }


   @JsonIgnore
   @JsonView( {ConnectedView.class, DisconnectedView.class } )
   @JsonProperty( "ts1" )
   public long getWicaServerTimestamp()
   {
      return wicaServerTimestamp.atOffset( ZoneOffset.UTC ).toInstant().toEpochMilli();
   }

   @JsonIgnore
   @JsonView( {ConnectedView.class, DisconnectedView.class } )
   @JsonProperty( "ts2" )
   public long getEpicsIocTimestamp()
   {
      return epicsIocTimestamp.atOffset( ZoneOffset.UTC ).toInstant().toEpochMilli();
   }

   @JsonIgnore
   @JsonView( {ConnectedView.class, DisconnectedView.class } )
   @JsonProperty( "ts1-alt" )
   public LocalDateTime getWicaServerTimestampAlt()
   {
      return wicaServerTimestamp;
   }

   //   @JsonIgnore
   @JsonView( {ConnectedView.class, DisconnectedView.class } )
   @JsonProperty( "ts" )
   public LocalDateTime getEpicsIocTimestampAlt()
   {
      return epicsIocTimestamp;
   }

   @JsonIgnore
   @JsonView( {ConnectedView.class, DisconnectedView.class } )
   public LocalDateTime getTimestamp()
   {
      return wicaServerTimestamp;
   }

   @JsonIgnore
   @JsonView( {ConnectedView.class } )
   @JsonProperty( "type" )
   public WicaChannelType getType()
   {
      return isConnected() ? WicaChannelType.getTypeFromObject (value ) : WicaChannelType.INVALID;
   }

   @JsonView( ConnectedView.class )
   @JsonProperty( "val" )
   public T getValue()
   {
      return value;
   }

   @JsonIgnore
   @JsonView( ConnectedView.class )
   @JsonProperty( "stat")
   public int getAlarmStatus()
   {
      return alarmStatus;
   }

   @JsonView( ConnectedView.class )
   @JsonProperty( "sevr")
   public int getAlarmSeverity()
   {
      return alarmSeverity.ordinal();
   }


   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( o == null || getClass() != o.getClass() ) return false;
      WicaChannelValueBackup<?> that = (WicaChannelValueBackup<?>) o;
      return alarmStatus == that.alarmStatus &&
            Objects.equals(value, that.value) &&
            alarmSeverity == that.alarmSeverity &&
            Objects.equals(wicaServerTimestamp, that.wicaServerTimestamp) &&
            Objects.equals(epicsIocTimestamp, that.epicsIocTimestamp);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(value, alarmSeverity, alarmStatus, wicaServerTimestamp, epicsIocTimestamp);
   }

   @JsonIgnore
   @Override
   public String toString()
   {
      return "WicaChannelValue{" +
            "connected=" + isConnected() +
            ", type=" + getType() +
            ", value=" + getValue() +
            ", alarmSeverity=" + getAlarmSeverity() +
            ", alarmStatus=" + getAlarmStatus() +
            ", wicaServerTimestamp=" + getWicaServerTimestamp() +
            ", epicsIocTimestamp=" + getEpicsIocTimestamp() +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   /**
    * Marker interface to indicate whether a field should be serialised
    * when the underlying channel is connected.
    */
   public static class ConnectedView  {}

   /**
    * Marker interface to indicate whether a field should be serialised
    * when the underlying channel is disconnected.
    */
   public static class DisconnectedView {}
}
