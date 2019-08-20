/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelAlarmSeverity;
import ch.psi.wica.model.channel.WicaChannelAlarmStatus;
import ch.psi.wica.model.channel.WicaChannelType;
import ch.psi.wica.model.channel.WicaChannelValue;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelValueMixins
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes: ----------------------------------------------------------*/

   /**
    * WicaChannelValueSerializerMixin
    */
   @JsonFilter( "WicaChannelDataFilter" )
   @JsonPropertyOrder({ "wsts", "conn", "type", "stat", "sevr", "ts", "val" })
   public static abstract class WicaChannelValueSerializerMixin extends WicaChannelValue
   {
      private WicaChannelValueSerializerMixin() { super( false, null); }

      @Override public abstract @JsonProperty( "wsts" )
      LocalDateTime getWicaServerTimestamp();
      @Override public abstract @JsonProperty( "conn" ) boolean isConnected();
   }

   /**
    * WicaChannelValueConnectedSerializerMixin
    */
   public static abstract class WicaChannelValueConnectedSerializerMixin extends WicaChannelValue.WicaChannelValueConnected
   {
      private WicaChannelValueConnectedSerializerMixin() { super( null, null, null, null); }

      @Override public abstract @JsonProperty( "type" )
      WicaChannelType getWicaChannelType();
      @Override public abstract @JsonProperty( "sevr" )
      WicaChannelAlarmSeverity getWicaAlarmSeverity();
      @Override public abstract @JsonProperty( "stat" )
      WicaChannelAlarmStatus getWicaChannelAlarmStatus();
      @Override public abstract @JsonProperty( "ts"   ) LocalDateTime getDataSourceTimestamp();
   }

   /**
    * WicaChannelValueDisconnectedSerializerMixin
    */
   public static abstract class WicaChannelValueDisconnectedSerializerMixin extends WicaChannelValue.WicaChannelValueDisconnected
   {
      private WicaChannelValueDisconnectedSerializerMixin() { super(); }
      @Override public abstract @JsonProperty( "val"  ) String getValue();
   }

   /**
    * WicaChannelValueConnectedStringSerializerMixin
    */
   public static abstract class WicaChannelValueConnectedStringSerializerMixin extends WicaChannelValue.WicaChannelValueConnectedString
   {
      private  WicaChannelValueConnectedStringSerializerMixin() { super(null, null, null, null ); }
      @Override public abstract @JsonProperty( "val"  ) String getValue();
   }

   /**
    * WicaChannelValueConnectedStringArraySerializerMixin
    */
   public static abstract class WicaChannelValueConnectedStringArraySerializerMixin extends WicaChannelValue.WicaChannelValueConnectedStringArray
   {
      private  WicaChannelValueConnectedStringArraySerializerMixin() { super(null, null, null, null ); }
      @Override public abstract @JsonProperty( "val" ) String[] getValue();
   }

   /**
    * WicaChannelValueConnectedIntegerSerializerMixin
    */
   public static abstract class WicaChannelValueConnectedIntegerSerializerMixin extends WicaChannelValue.WicaChannelValueConnectedInteger
   {
      private  WicaChannelValueConnectedIntegerSerializerMixin() { super(null, null, null, 0 ); }
      @Override public abstract @JsonProperty( "val" ) int getValue();
   }

   /**
    * WicaChannelValueConnectedIntegerArraySerializerMixin
    */
   public static abstract class WicaChannelValueConnectedIntegerArraySerializerMixin extends WicaChannelValue.WicaChannelValueConnectedIntegerArray
   {
      private  WicaChannelValueConnectedIntegerArraySerializerMixin() { super(null, null, null, null ); }
      @Override public abstract @JsonProperty( "val" ) int[] getValue();
   }

   /**
    * WicaChannelValueConnectedRealSerializerMixin
    */
   public static abstract class WicaChannelValueConnectedRealSerializerMixin extends WicaChannelValue.WicaChannelValueConnectedReal
   {
      private  WicaChannelValueConnectedRealSerializerMixin() { super( null, null, null, 0.0 ); }
      @Override public abstract @JsonProperty( "val" ) double getValue();
   }

   /**
    * WicaChannelValueConnectedRealArraySerializerMixin
    */
   public static abstract class WicaChannelValueConnectedRealArraySerializerMixin extends WicaChannelValue.WicaChannelValueConnectedRealArray
   {
      private  WicaChannelValueConnectedRealArraySerializerMixin() { super( null, null, null, null ); }
      @Override public abstract @JsonProperty( "val" ) double[] getValue();
   }

   /**
    * WicaChannelAlarmStatusMixin
    */
   public static abstract class WicaChannelAlarmStatusMixin extends WicaChannelAlarmStatus
   {
      private  WicaChannelAlarmStatusMixin() { super( 0 ); }
      @Override public abstract @JsonValue
      int getStatusCode();
   }

}
