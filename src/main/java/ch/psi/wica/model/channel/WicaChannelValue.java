/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/


/**
 * Represents the abstract root of a typed hierarchy of objects which reflect a
 * control point's instantaneous state.
 * <p>
 * These include, typically, whether the channel is online or offline, the
 * raw value and timestamp obtained when the channel was last read out, and
 * whether an alarm or warning condition exists.
 */
@Immutable
public abstract class WicaChannelValue extends WicaChannelData
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final boolean connected;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValue( WicaChannelType wicaChannelType, LocalDateTime wicaServerTimestamp, boolean connected )
   {
      super( wicaChannelType, wicaServerTimestamp);
      this.connected = connected;
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelValue createChannelValueDisconnected()
   {
      return new WicaChannelValue.WicaChannelValueDisconnected();
   }

   public static WicaChannelValue.WicaChannelValueConnectedReal createChannelValueConnected( double value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedReal(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedReal createChannelValueConnected( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, double value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedReal(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedRealArray createChannelValueConnected( double[] value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedRealArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedRealArray createChannelValueConnected( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, double[] value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedRealArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedInteger createChannelValueConnected( int value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedInteger(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedInteger createChannelValueConnected( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, int value  )
   {
      return new WicaChannelValue.WicaChannelValueConnectedInteger(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedIntegerArray createChannelValueConnected( int[] value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedIntegerArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedIntegerArray  createChannelValueConnected( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, int[] value  )
   {
      return new WicaChannelValue.WicaChannelValueConnectedIntegerArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedString createChannelValueConnected( String value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedString(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedString createChannelValueConnected( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, String value  )
   {
      return new WicaChannelValue.WicaChannelValueConnectedString(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedStringArray createChannelValueConnected( String[] value )
   {
      return new WicaChannelValue.WicaChannelValueConnectedStringArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValue.WicaChannelValueConnectedStringArray createChannelValueConnected( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, String[] value  )
   {
      return new WicaChannelValue.WicaChannelValueConnectedStringArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

/*- Public methods -----------------------------------------------------------*/

   public boolean isConnected()
   {
      return connected;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

/*- Nested Class: WicaChannelValueDisconnected -------------------------------*/

   public static class WicaChannelValueDisconnected extends WicaChannelValue
   {
      private final String val;
      public String getValue()
      {
         return val;
      }

      public WicaChannelValueDisconnected()
      {
         super( WicaChannelType.UNKNOWN, LocalDateTime.now(), false );
         this.val = null;
      }
   }

/*- Nested Class: WicaChannelValueConnected ----------------------------------*/

   public static abstract class WicaChannelValueConnected extends WicaChannelValue
   {
      private final WicaChannelType wicaChannelType;
      private final WicaChannelAlarmSeverity wicaChannelAlarmSeverity;
      private final WicaChannelAlarmStatus wicaChannelAlarmStatus;
      private final LocalDateTime dataSourceTimestamp;

      public WicaChannelType getWicaChannelType()
      {
         return wicaChannelType;
      }
      public WicaChannelAlarmSeverity getWicaAlarmSeverity()
      {
         return wicaChannelAlarmSeverity;
      }
      public WicaChannelAlarmStatus getWicaChannelAlarmStatus()
      {
         return wicaChannelAlarmStatus;
      }
      public LocalDateTime getDataSourceTimestamp()
      {
         return dataSourceTimestamp;
      }

      public WicaChannelValueConnected( WicaChannelType wicaChannelType, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp )
      {
         super( wicaChannelType, LocalDateTime.now(), true );
         this.wicaChannelType = Validate.notNull( wicaChannelType, "wicaChannelType cannot be null " );
         this.wicaChannelAlarmSeverity = Validate.notNull( wicaChannelAlarmSeverity, "wicaAlarmSeverity cannot be null " );
         this.wicaChannelAlarmStatus = Validate.notNull( wicaChannelAlarmStatus, "wicaAlarmStatus cannot be null " );
         this.dataSourceTimestamp = Validate.notNull( dataSourceTimestamp,"dataSourceTimestamp cannot be null " );
      }
   }

/*- Nested Class: WicaChannelValueConnectedReal -----------------------------*/

   public static class WicaChannelValueConnectedReal extends WicaChannelValueConnected
   {
      private final double value;
      public double getValue()
      {
         return value;
      }

      public WicaChannelValueConnectedReal( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, double value )
      {
         super( WicaChannelType.REAL, alarmSeverity, alarmStatus, dataSourceTimestamp );
         this.value = value;
      }
   }

/*- Nested Class: WicaChannelValueConnectedRealArray ------------------------*/

   public static class WicaChannelValueConnectedRealArray extends WicaChannelValueConnected
   {
      private final double[] value;
      public double[] getValue()
      {
         return value;
      }

      public WicaChannelValueConnectedRealArray( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, double[] value )
      {
         super( WicaChannelType.REAL_ARRAY, alarmSeverity, alarmStatus, dataSourceTimestamp );
         this.value = value;
      }
   }

/*- Nested Class: WicaChannelValueConnectedInteger --------------------------*/

   public static class WicaChannelValueConnectedInteger extends WicaChannelValueConnected
   {
      private final int value;
      public int getValue()
      {
         return value;
      }

      public WicaChannelValueConnectedInteger( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, int value )
      {
         super( WicaChannelType.INTEGER, alarmSeverity, alarmStatus, dataSourceTimestamp);
         this.value = value;
      }
   }

/*- Nested Class: WicaChannelValueConnectedIntegerArray ---------------------*/

   public static class WicaChannelValueConnectedIntegerArray extends WicaChannelValueConnected
   {
      private final int[] value;
      public int[] getValue()
      {
         return value;
      }

      public WicaChannelValueConnectedIntegerArray( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, int[] value )
      {
         super( WicaChannelType.INTEGER_ARRAY, alarmSeverity, alarmStatus, dataSourceTimestamp);
         this.value = value;
      }
   }

/*- Nested Class: WicaChannelValueConnectedString ---------------------------*/

   public static class WicaChannelValueConnectedString extends WicaChannelValueConnected
   {
      private final String value;
      public String getValue()
      {
         return value;
      }

      public WicaChannelValueConnectedString( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, String value )
      {
         super( WicaChannelType.STRING, alarmSeverity, alarmStatus, dataSourceTimestamp);
         this.value = value;
      }
   }

/*- Nested Class: WicaChannelValueConnectedStringArray ----------------------*/

   public static class WicaChannelValueConnectedStringArray extends WicaChannelValueConnected
   {
      private final String[] value;
      public String[] getValue()
      {
         return value;
      }

      public WicaChannelValueConnectedStringArray( WicaChannelAlarmSeverity alarmSeverity, WicaChannelAlarmStatus alarmStatus, LocalDateTime dataSourceTimestamp, String[] value )
      {
         super( WicaChannelType.STRING_ARRAY, alarmSeverity, alarmStatus, dataSourceTimestamp);
         this.value = value;
      }
   }

}