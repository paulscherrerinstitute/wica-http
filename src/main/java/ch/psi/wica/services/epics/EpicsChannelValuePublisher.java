/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import net.jcip.annotations.Immutable;
import org.epics.ca.Channel;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Service
@Immutable
public class EpicsChannelValuePublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelValuePublisher.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
   
   /**
    * Obtains the widest possible set of information (eg alarm limits,
    * control limits, precision, units...) from the supplied EPICS channel,
    * creates a WicaChannelValue object from it and publishes the
    * information to the supplied Consumer.
    *
    * Precondition: the supplied channel should already be created
    *               and connected.
    *
    * Postcondition: the supplied channel will remain open.
    *
    * @param channel the EPICS channel.
    * @param valueChangeHandler the event consumer.
    */
   public void getAndPublishValue( Channel<Object> channel, Consumer<WicaChannelValue> valueChangeHandler  )
   {
      final String epicsChannelName = channel.getName();
      logger.debug("'{}' - getting first value...", epicsChannelName);
      final Object firstGet = channel.get();

      final WicaChannelType type = WicaChannelType.getTypeFromObject(firstGet);
      logger.debug("'{}' - first value received was of type {}. ", epicsChannelName, type);

      logger.debug("'{}' - getting epics TIMESTAMPED value data...", epicsChannelName);
      final Timestamped<Object> valueObj = channel.get(Timestamped.class);
      logger.debug("'{}' - EPICS TIMESTAMPED value data received.", epicsChannelName);

      publishValue( epicsChannelName, valueObj, valueChangeHandler );
   }
   
   public void publishValue( String epicsChannelName, Timestamped<Object> valueObj, Consumer<WicaChannelValue> valueChangeHandler  )
   {
      if ( ! WicaChannelType.isRecognisedType( valueObj.getValue() ) )
      {
         logger.trace("'{}' - the value received was of an unrecognised type. ", epicsChannelName );
         return;
      }


      final WicaChannelAlarmSeverity wicaChannelAlarmSeverity = EpicsConversionUtilities.fromEpics( valueObj.getAlarmSeverity() );
      final WicaChannelAlarmStatus wicaChannelAlarmStatus = EpicsConversionUtilities.fromEpics( valueObj.getAlarmStatus() );
      final LocalDateTime wicaDataSourceTimestamp  = EpicsConversionUtilities.getEpicsTimestamp( valueObj.getSeconds(), valueObj.getNanos() );

      switch( WicaChannelType.getTypeFromObject( valueObj.getValue() ) )
      {
         case REAL:
            logger.trace( "'{}' - value was DOUBLE.", epicsChannelName );
            publishTimestampedValueDouble( valueObj, wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, valueChangeHandler );
            return;

         case REAL_ARRAY:
            logger.trace( "'{}' - value was DOUBLE ARRAY.", epicsChannelName );
            publishTimestampedValueDoubleArray( valueObj, wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, valueChangeHandler );
            return;

         case INTEGER:
            logger.trace("'{}' - value was INTEGER.", epicsChannelName );
            publishTimestampedValueInteger( valueObj, wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, valueChangeHandler );
            return;

         case INTEGER_ARRAY:
            logger.trace("'{}' - value was INTEGER ARRAY.", epicsChannelName );
            publishTimestampedValueIntegerArray( valueObj, wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, valueChangeHandler );
            return;

         case STRING:
            logger.trace("'{}' - value was STRING.", epicsChannelName );
            publishTimestampedValueString( valueObj, wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, valueChangeHandler );
            return;

         case STRING_ARRAY:
            logger.trace("'{}' - value was STRING ARRAY.", epicsChannelName );
            publishTimestampedValueStringArray( valueObj, wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, valueChangeHandler );
            return;
      }
      logger.warn( "'{}' - first value was of an unsupported type", epicsChannelName );
   }

   

/*- Private methods ----------------------------------------------------------*/
   private void publishTimestampedValueDouble( Timestamped<Object> valueObj, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus,
                                               LocalDateTime wicaDataSourceTimestamp, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final Double value = (Double) valueObj.getValue();
      final WicaChannelValue wicaChannelValue = WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, value );
      valueChangeHandler.accept( wicaChannelValue );
   }
   
   private void publishTimestampedValueDoubleArray( Timestamped<Object> valueObj, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus,
                                                    LocalDateTime wicaDataSourceTimestamp, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final double[] value = (double[]) valueObj.getValue();
      final WicaChannelValue wicaChannelValue = WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, value );
      valueChangeHandler.accept( wicaChannelValue );
   }

   private void publishTimestampedValueInteger( Timestamped<Object> valueObj, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus,
                                               LocalDateTime wicaDataSourceTimestamp, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final Integer value = (Integer) valueObj.getValue();
      final WicaChannelValue wicaChannelValue = WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, value );
      valueChangeHandler.accept( wicaChannelValue );
   }

   private void publishTimestampedValueIntegerArray( Timestamped<Object> valueObj, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus,
                                                    LocalDateTime wicaDataSourceTimestamp, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final int[] value = (int[]) valueObj.getValue();
      final WicaChannelValue wicaChannelValue = WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, value );
      valueChangeHandler.accept( wicaChannelValue );
   }
   
   private void publishTimestampedValueString( Timestamped<Object> valueObj, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus,
                                               LocalDateTime wicaDataSourceTimestamp, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final String value = (String) valueObj.getValue();
      final WicaChannelValue wicaChannelValue = WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, value );
      valueChangeHandler.accept( wicaChannelValue );
   }
   private void publishTimestampedValueStringArray( Timestamped<Object> valueObj, WicaChannelAlarmSeverity wicaChannelAlarmSeverity, WicaChannelAlarmStatus wicaChannelAlarmStatus,
                                               LocalDateTime wicaDataSourceTimestamp, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final String[] value = (String[]) valueObj.getValue();
      final WicaChannelValue wicaChannelValue = WicaChannelValue.createChannelValueConnected( wicaChannelAlarmSeverity, wicaChannelAlarmStatus, wicaDataSourceTimestamp, value );
      valueChangeHandler.accept( wicaChannelValue );
   }

   

/*- Nested Classes -----------------------------------------------------------*/

}
