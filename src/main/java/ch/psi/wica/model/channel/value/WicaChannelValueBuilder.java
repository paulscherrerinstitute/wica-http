/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.value;

/*- Imported packages --------------------------------------------------------*/

import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelValueBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelValueDisconnected createChannelValueDisconnected()
   {
      return new WicaChannelValueDisconnected();
   }

   public static WicaChannelValueConnectedReal createChannelValueConnectedReal( double value )
   {
      return new WicaChannelValueConnectedReal( WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValueConnectedReal createChannelValueConnectedReal( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, double value )
   {
      return new WicaChannelValueConnectedReal(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValueConnectedRealArray createChannelValueConnectedRealArray( double[] value )
   {
      return new WicaChannelValueConnectedRealArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValueConnectedRealArray createChannelValueConnectedRealArray( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, double[] value )
   {
      return new WicaChannelValueConnectedRealArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValueConnectedInteger createChannelValueConnectedInteger( int value )
   {
      return new WicaChannelValueConnectedInteger(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValueConnectedInteger createChannelValueConnectedInteger( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, int value  )
   {
      return new WicaChannelValueConnectedInteger(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValueConnectedIntegerArray createChannelValueConnectedIntegerArray( int[] value )
   {
      return new WicaChannelValueConnectedIntegerArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValueConnectedIntegerArray  createChannelValueConnectedIntegerArray( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, int[] value  )
   {
      return new WicaChannelValueConnectedIntegerArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValueConnectedString createChannelValueConnectedString( String value )
   {
      return new WicaChannelValueConnectedString(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValueConnectedString createChannelValueConnectedString( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, String value  )
   {
      return new WicaChannelValueConnectedString(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   public static WicaChannelValueConnectedStringArray createChannelValueConnectedStringArray( String[] value )
   {
      return new WicaChannelValueConnectedStringArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   public static WicaChannelValueConnectedStringArray createChannelValueConnectedStringArray( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, String[] value  )
   {
      return new WicaChannelValueConnectedStringArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}