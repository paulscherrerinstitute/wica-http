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

   /**
    * Returns a new instance of a channel whose value is DISCONNECTED.
    *
    * @return the new instance.
    */
   public static WicaChannelValueDisconnected createChannelValueDisconnected()
   {
      return new WicaChannelValueDisconnected();
   }

   /**
    * Returns a new instance of a connected channel whose value is REAL.
    *
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedReal createChannelValueConnectedReal( double value )
   {
      return new WicaChannelValueConnectedReal( WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   /**
    * Returns a new instance of a connected channel whose value is REAL.
    *
    * @param wicaChannelAlarmSeverity the alarm severity.
    * @param wicaChannelAlarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedReal createChannelValueConnectedReal( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, double value )
   {
      return new WicaChannelValueConnectedReal(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   /**
    * Returns a new instance of a connected channel whose value type is a REAL_ARRAY.
    *
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedRealArray createChannelValueConnectedRealArray( double[] value )
   {
      return new WicaChannelValueConnectedRealArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   /**
    * Returns a new instance of a connected channel whose value is a REAL_ARRAY.
    *
    * @param wicaChannelAlarmSeverity the alarm severity.
    * @param wicaChannelAlarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedRealArray createChannelValueConnectedRealArray( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, double[] value )
   {
      return new WicaChannelValueConnectedRealArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   /**
    * Returns a new instance of a connected channel whose value is an INTEGER.
    *
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedInteger createChannelValueConnectedInteger( int value )
   {
      return new WicaChannelValueConnectedInteger(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   /**
    * Returns a new instance of a connected channel whose value is an INTEGER.
    *
    * @param wicaChannelAlarmSeverity the alarm severity.
    * @param wicaChannelAlarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedInteger createChannelValueConnectedInteger( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, int value  )
   {
      return new WicaChannelValueConnectedInteger(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   /**
    * Returns a new instance of a connected channel whose value is an INTEGER_ARRAY.
    *
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedIntegerArray createChannelValueConnectedIntegerArray( int[] value )
   {
      return new WicaChannelValueConnectedIntegerArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   /**
    * Returns a new instance of a connected channel whose value is an INTEGER_ARRAY.
    *
    * @param wicaChannelAlarmSeverity the alarm severity.
    * @param wicaChannelAlarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedIntegerArray  createChannelValueConnectedIntegerArray( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, int[] value  )
   {
      return new WicaChannelValueConnectedIntegerArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   /**
    * Returns a new instance of a connected channel whose value is a STRING.
    *
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedString createChannelValueConnectedString( String value )
   {
      return new WicaChannelValueConnectedString(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   /**
    * Returns a new instance of a connected channel whose value is a STRING.
    *
    * @param wicaChannelAlarmSeverity the alarm severity.
    * @param wicaChannelAlarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedString createChannelValueConnectedString( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, String value  )
   {
      return new WicaChannelValueConnectedString(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

   /**
    * Returns a new instance of a connected channel whose value is a STRING_ARRAY.
    *
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedStringArray createChannelValueConnectedStringArray( String[] value )
   {
      return new WicaChannelValueConnectedStringArray(WicaChannelAlarmSeverity.NO_ALARM, WicaChannelAlarmStatus.ofNoError(), LocalDateTime.now(), value );
   }

   /**
    * Returns a new instance of a connected channel whose value is a STRING_ARRAY.
    *
    * @param wicaChannelAlarmSeverity the alarm severity.
    * @param wicaChannelAlarmStatus the alarm status.
    * @param dataSourceTimestamp the data source timestamp.
    * @param value the value.
    * @return the new instance.
    */
   public static WicaChannelValueConnectedStringArray createChannelValueConnectedStringArray( WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus, LocalDateTime dataSourceTimestamp, String[] value  )
   {
      return new WicaChannelValueConnectedStringArray(wicaChannelAlarmSeverity, wicaChannelAlarmStatus, dataSourceTimestamp, value );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}