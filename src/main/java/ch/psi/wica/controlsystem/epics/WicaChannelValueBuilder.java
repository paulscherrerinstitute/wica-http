/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelAlarmSeverity;
import ch.psi.wica.model.channel.WicaChannelAlarmStatus;
import ch.psi.wica.model.channel.WicaChannelType;
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
class WicaChannelValueBuilder
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
   WicaChannelValue build( ControlSystemName controlSystemName, Timestamped<Object> epicsValueObject )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( epicsValueObject );

      // Decode the channel type.
      final WicaChannelType wicaChannelType;
      try
      {
         wicaChannelType = WicaChannelType.getTypeFromPojo( epicsValueObject.getValue() );
         logger.trace( "'{}' - value received was of type {}. ", controlSystemName, wicaChannelType );
      }
      catch( IllegalArgumentException ex)
      {
         logger.error( "'{}' - type was UNKNOWN (Programming Error)", controlSystemName );
         return WicaChannelValue.createChannelValueDisconnected();
      }
      return build( controlSystemName, wicaChannelType, epicsValueObject );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Returns a WicaChannelValue object based on the supplied data obtained
    * from the EPICS channel.
    *
    * @param controlSystemName the name of the control system channel (needed
    *     for logging purposes only). Not Null.
    *
    * @param wicaChannelType the channel type. Not Null.
    * @param epicsValueObject the EPICS CA library Timestamped object. Not Null.
    *
    * @return the constructed value object.
    */
   private WicaChannelValue build( ControlSystemName controlSystemName, WicaChannelType wicaChannelType, Timestamped<Object> epicsValueObject )
   {
      final var wicaChannelAlarmSeverity = fromEpics (epicsValueObject.getAlarmSeverity() );
      final var wicaChannelAlarmStatus = fromEpics( epicsValueObject.getAlarmStatus() );
      final var wicaDataSourceTimestamp = getEpicsTimestamp( epicsValueObject.getSeconds(), epicsValueObject.getNanos() );

      switch( wicaChannelType )
      {
         case STRING:
            logger.trace("'{}' - type is STRING.", controlSystemName );
            final String strValue = (String) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, strValue );

         case STRING_ARRAY:
            logger.trace("'{}' - type is STRING ARRAY.", controlSystemName );
            final String[] strArrayValue = (String[]) epicsValueObject.getValue();
            return  WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, strArrayValue );

         case INTEGER:
            logger.trace("'{}' - type is INTEGER.", controlSystemName );
            final Integer intValue = (Integer) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, intValue );

         case INTEGER_ARRAY:
            logger.trace("'{}' - type is INTEGER ARRAY.", controlSystemName );
            final int[] intArrayValue = (int[]) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, intArrayValue );

         case REAL:
            logger.trace( "'{}' - type is DOUBLE.", controlSystemName );
            final Double dblValue = (Double) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, dblValue );

         case REAL_ARRAY:
            logger.trace( "'{}' - type is DOUBLE ARRAY.", controlSystemName );
            final double[] dblArrayValue = (double[]) epicsValueObject.getValue();
            return WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, dblArrayValue );

         default:
            logger.error( "'{}' - type is NOT SUPPORTED (Programming Error)", controlSystemName );
            return WicaChannelValue.createChannelValueDisconnected();
      }
   }

   private WicaChannelAlarmSeverity fromEpics( AlarmSeverity caAlarmSeverity )
   {
      return WicaChannelAlarmSeverity.valueOf( caAlarmSeverity.toString() );
   }

   private WicaChannelAlarmStatus fromEpics( AlarmStatus caAlarmStatus )
   {
      return WicaChannelAlarmStatus.of( caAlarmStatus.ordinal() );
   }

   // TODO: this ties the current location to PSI's site. Should be made configurable.
   private LocalDateTime getEpicsTimestamp( long secondsPastEpicsEpoch, int nanoseconds )
   {
      final Instant instant = Instant.ofEpochSecond(secondsPastEpicsEpoch, nanoseconds );
      final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("Europe/Zurich") );
      return zonedDateTime.toLocalDateTime();
   }


   /*- Nested Classes -----------------------------------------------------------*/

}
