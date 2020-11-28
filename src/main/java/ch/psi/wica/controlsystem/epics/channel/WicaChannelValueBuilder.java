/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelAlarmSeverity;
import ch.psi.wica.model.channel.WicaChannelAlarmStatus;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.epics.ca.data.AlarmSeverity;
import org.epics.ca.data.AlarmStatus;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the functionality to build a WicaChannelValue object using
 * raw channel value data.
 */
@Immutable
@Component
public class WicaChannelValueBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaChannelValueBuilder.class );


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a WicaChannelValue object based on the supplied data obtained
    * from the EPICS channel.
    *
    * @param controlSystemName the name of the control system channel (needed
    *     for logging purposes only). Not Null.
    *
    * @param epicsValueObject the EPICS CA library Timestamped object. Not Null.
    *
    * @return the constructed value object.
    *
    * @throws NullPointerException if the controlSystemName argument was null.
    * @throws NullPointerException if the epicsValueObject argument was null.
    */
   public WicaChannelValue build( ControlSystemName controlSystemName, Timestamped<Object> epicsValueObject )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( epicsValueObject );

      // Decode the channel type.
      final EpicsChannelType epicsChannelType;
      try
      {
         epicsChannelType = EpicsChannelType.getTypeFromPojo( epicsValueObject.getValue() );
         logger.trace( "'{}' - value received was of EPICS type {}. ", controlSystemName, epicsChannelType );
      }
      catch( IllegalArgumentException ex)
      {
         logger.error( "'{}' - type was UNKNOWN (Programming Error)", controlSystemName );
         return WicaChannelValue.createChannelValueDisconnected();
      }
      return build( controlSystemName, epicsChannelType, epicsValueObject );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Returns a WicaChannelValue object based on the supplied data obtained
    * from the EPICS channel.
    *
    * @param controlSystemName the name of the control system channel (needed
    *     for logging purposes only). Not Null.
    *
    * @param epicsChannelType the channel type. Not Null.
    * @param epicsValueObject the EPICS CA library Timestamped object. Not Null.
    *
    * @return the constructed value object.
    */
   private WicaChannelValue build( ControlSystemName controlSystemName, EpicsChannelType epicsChannelType, Timestamped<Object> epicsValueObject )
   {
      final var wicaChannelAlarmSeverity = fromEpics (epicsValueObject.getAlarmSeverity() );
      final var wicaChannelAlarmStatus = fromEpics( epicsValueObject.getAlarmStatus() );
      final var wicaDataSourceTimestamp = getEpicsTimestamp( epicsValueObject.getSeconds(), epicsValueObject.getNanos() );

      logger.trace("'{}' - type is {}}.", controlSystemName, epicsChannelType );

      switch( epicsChannelType )
      {
         case STRING:
            final String strValue = (String) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedString( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, strValue );

         case STRING_ARRAY:
            final String[] strArrayValue = (String[]) epicsValueObject.getValue();
            return  WicaChannelValue.createChannelValueConnectedStringArray( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, strArrayValue );

         case BYTE:
            final byte byteValue = (Byte) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedInteger( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, byteValue );

         case BYTE_ARRAY:
            final byte[] byteArrayValue = (byte[]) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedIntegerArray( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, getIntArrayFromByteArray( byteArrayValue ) );

         case SHORT:
            final short shortValue = (Short) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedInteger( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, shortValue );

         case SHORT_ARRAY:
            final short[] shortArrayValue = (short[]) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedIntegerArray( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, getIntArrayFromShortArray( shortArrayValue ) );

         case INTEGER:
            final int intValue = (Integer) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedInteger( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, intValue );

         case INTEGER_ARRAY:
            final int[] intArrayValue = (int[]) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedIntegerArray( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, intArrayValue );

         case FLOAT:
            final float floatValue = (Float) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedReal( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, floatValue );

         case FLOAT_ARRAY:
            final float[] floatArrayValue = (float[]) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedRealArray( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, getDoubleArrayFromFloatArray( floatArrayValue ) );
            
         case DOUBLE:
            final double dblValue = (Double) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedReal( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, dblValue );

         case DOUBLE_ARRAY:
            final double[] dblArrayValue = (double[]) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnectedRealArray( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, dblArrayValue );

         default:
            logger.error( "'{}' - type is NOT SUPPORTED (Programming Error)", controlSystemName );
            return WicaChannelValue.createChannelValueDisconnected();
      }
   }

   private static WicaChannelAlarmSeverity fromEpics( AlarmSeverity caAlarmSeverity )
   {
      return WicaChannelAlarmSeverity.valueOf( caAlarmSeverity.toString() );
   }

   private static WicaChannelAlarmStatus fromEpics( AlarmStatus caAlarmStatus )
   {
      return WicaChannelAlarmStatus.of( caAlarmStatus.ordinal() );
   }

   // TODO: this ties the current location to PSI's site. Should be made configurable.
   private static LocalDateTime getEpicsTimestamp( long secondsPastEpicsEpoch, int nanoseconds )
   {
      final Instant instant = Instant.ofEpochSecond(secondsPastEpicsEpoch, nanoseconds );
      final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("Europe/Zurich") );
      return zonedDateTime.toLocalDateTime();
   }

   static private int[] getIntArrayFromByteArray( byte[] inputArray )
   {
      final int[] outputArray = new int[ inputArray.length ];
      for ( int i=0; i < inputArray.length; i++ )
      {
         outputArray[ i ] = inputArray[ i ];
      }
      return outputArray;
   }

   static private int[] getIntArrayFromShortArray( short[] inputArray )
   {
      final int[] outputArray = new int[ inputArray.length ];
      for ( int i=0; i < inputArray.length; i++ )
      {
         outputArray[ i ] = inputArray[ i ];
      }
      return outputArray;
   }

   static private double[] getDoubleArrayFromFloatArray( float[] inputArray )
   {
      final double[] outputArray = new double[ inputArray.length ];
      for ( int i=0; i < inputArray.length; i++ )
      {
         outputArray[ i ] = inputArray[ i ];
      }
      return outputArray;
   }


/*- Nested Classes -----------------------------------------------------------*/

}
