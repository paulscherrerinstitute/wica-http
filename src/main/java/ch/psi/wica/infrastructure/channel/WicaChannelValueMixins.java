/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelAlarmSeverity;
import ch.psi.wica.model.channel.WicaChannelAlarmStatus;
import ch.psi.wica.model.channel.WicaChannelType;
import ch.psi.wica.model.channel.WicaChannelValue;
import com.fasterxml.jackson.annotation.JsonProperty;
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

/*- Nested Class: WicaChannelValueSerializerMixin ----------------------------*/

   public static abstract class WicaChannelValueSerializerMixin extends WicaChannelValue
   {
      // Dummy: only required to detect signature override errors.
      private WicaChannelValueSerializerMixin() { super( null, null, false ); }

      @Override public abstract @JsonProperty( "conn" ) boolean isConnected();
   }

/*- Nested Class: WicaChannelValueConnectedSerializerMixin --------------------*/

   public static abstract class WicaChannelValueConnectedSerializerMixin extends WicaChannelValue.WicaChannelValueConnected
   {
      // Dummy: only required to detect signature override errors.
      private WicaChannelValueConnectedSerializerMixin() { super( null, null, null, null); }

      @Override public abstract @JsonProperty( "type" )
      WicaChannelType getWicaChannelType();
      @Override public abstract @JsonProperty( "sevr" )
      WicaChannelAlarmSeverity getWicaAlarmSeverity();
      @Override public abstract @JsonProperty( "stat" )
      WicaChannelAlarmStatus getWicaChannelAlarmStatus();
      @Override public abstract @JsonProperty( "ts"   ) LocalDateTime getDataSourceTimestamp();
   }

/*- Nested Class: WicaChannelValueDisconnectedSerializerMixin -----------------*/

   public static abstract class WicaChannelValueDisconnectedSerializerMixin extends WicaChannelValue.WicaChannelValueDisconnected
   {
      // Dummy: only required to detect signature override errors.
      private WicaChannelValueDisconnectedSerializerMixin() { super(); }

      // Mappings: start here.
      @Override public abstract @JsonProperty( "val"  ) String getValue();
   }


/*- Nested Class: WicaChannelValueConnectedStringSerializerMixin --------------*/

   public static abstract class WicaChannelValueConnectedStringSerializerMixin extends WicaChannelValue.WicaChannelValueConnectedString
   {
      // Dummy: only required to detect signature override errors.
      private  WicaChannelValueConnectedStringSerializerMixin() { super(null, null, null, null ); }

      // Mappings: start here.
      @Override public abstract @JsonProperty( "val"  ) String getValue();
   }


/*- Nested Class: WicaChannelValueConnectedStringArraySerializerMixin ----------*/

   public static abstract class WicaChannelValueConnectedStringArraySerializerMixin extends WicaChannelValue.WicaChannelValueConnectedStringArray
   {
      // Dummy: only required to detect signature override errors.
      private  WicaChannelValueConnectedStringArraySerializerMixin() { super(null, null, null, null ); }

      // Mappings: start here.
      @Override public abstract @JsonProperty( "val" ) String[] getValue();
   }


/*- Nested Class: WicaChannelValueConnectedIntegerSerializerMixin --------------*/

   public static abstract class WicaChannelValueConnectedIntegerSerializerMixin extends WicaChannelValue.WicaChannelValueConnectedInteger
   {
      // Dummy: only required to detect signature override errors.
      private  WicaChannelValueConnectedIntegerSerializerMixin() { super(null, null, null, 0 ); }

      // Mappings: start here.
      @Override public abstract @JsonProperty( "val" ) int getValue();
   }


/*- Nested Class: WicaChannelValueConnectedIntegerArraySerializerMixin ---------*/

   public static abstract class WicaChannelValueConnectedIntegerArraySerializerMixin extends WicaChannelValue.WicaChannelValueConnectedIntegerArray
   {
      // Dummy: only required to detect signature override errors.
      private  WicaChannelValueConnectedIntegerArraySerializerMixin() { super(null, null, null, null ); }

      // Mappings: start here.
      @Override public abstract @JsonProperty( "val" ) int[] getValue();
   }


/*- Nested Class: WicaChannelValueConnectedRealSerializerMixin -----------------*/

   public static abstract class WicaChannelValueConnectedRealSerializerMixin extends WicaChannelValue.WicaChannelValueConnectedReal
   {
      // Dummy: only required to detect signature override errors.
      private  WicaChannelValueConnectedRealSerializerMixin() { super( null, null, null, 0.0 ); }

      // Mappings: start here.
      @Override public abstract @JsonProperty( "val" ) double getValue();
   }


/*- Nested Class: WicaChannelValueConnectedRealArraySerializerMixin ------------*/

   public static abstract class WicaChannelValueConnectedRealArraySerializerMixin extends WicaChannelValue.WicaChannelValueConnectedRealArray
   {
      // Dummy: only required to detect signature override errors.
      private  WicaChannelValueConnectedRealArraySerializerMixin() { super( null, null, null, null ); }

      // Mappings: start here.
      @Override public abstract @JsonProperty( "val" ) double[] getValue();
   }

/*- Nested Class: WicaChannelAlarmStatusMixin ----------------------------------*/

   public static abstract class WicaChannelAlarmStatusMixin extends WicaChannelAlarmStatus
   {
      // Dummy: only required to detect signature override errors.
      private  WicaChannelAlarmStatusMixin() { super( 0 ); }

      // Mappings: start here.
      @Override public abstract @JsonValue
      int getStatusCode();
   }

}
