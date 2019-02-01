/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelType;
import net.jcip.annotations.Immutable;
import org.epics.ca.Channel;
import org.epics.ca.data.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Service
@Immutable
public class EpicsChannelMetadataPublisher
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMetadataPublisher.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Obtains the widest possible set of information (eg alarm limits,
    * control limits, precision, units...) from the supplied EPICS channel,
    * creates a WicaChannelMetadata object from it and publishes the
    * information to the supplied Consumer.
    *
    * Precondition: the supplied channel should already be created
    *               and connected.
    *
    * Postcondition: the supplied channel will remain open.
    *
    * @param channel the EPICS channel.
    * @param metadataChangeHandler the event consumer.
    */
   public void getAndPublishMetadata( Channel<Object> channel, Consumer<WicaChannelMetadata> metadataChangeHandler  )
   {
      final String wicaChannelName = channel.getName();
      logger.debug("'{}' - getting first value...", wicaChannelName);
      final Object firstGet = channel.get();

      final WicaChannelType type = WicaChannelType.getTypeFromObject(firstGet );
      logger.debug("'{}' - first value received was of type {}. ", wicaChannelName, type );

      switch( type )
      {
         case STRING:
            logger.debug("'{}' - first value was STRING.", wicaChannelName);
            publishMetadataString(metadataChangeHandler );
            return;

         case STRING_ARRAY:
            logger.debug("'{}' - first value was STRING ARRAY.", wicaChannelName);
            publishMetadataStringArray( metadataChangeHandler );
            return;
      }

      logger.debug( "'{}' - getting epics CTRL metadata...", wicaChannelName);
      final Object metadataObject = channel.get( Control.class );
      logger.debug( "'{}' - EPICS CTRL metadata received.", wicaChannelName);

      switch( type )
      {
         case INTEGER:
            logger.debug("'{}' - first value was INTEGER.", wicaChannelName);
            publishMetadataInteger((Control<?,?>) metadataObject, metadataChangeHandler );
            return;

         case INTEGER_ARRAY:
            logger.debug("'{}' - first value was INTEGER ARRAY.", wicaChannelName);
            publishMetadataIntegerArray((Control<?,?>) metadataObject, metadataChangeHandler );
            return;

         case REAL:
            logger.debug( "'{}' - first value was DOUBLE.", wicaChannelName);
            publishMetadataReal((Control<?,?>) metadataObject, metadataChangeHandler );
            return;

         case REAL_ARRAY:
            logger.debug( "'{}' - first value was DOUBLE ARRAY.", wicaChannelName);
            publishMetadataRealArray((Control<?,?>) metadataObject, metadataChangeHandler );
            return;
      }
      logger.warn( "'{}' - first value was of an unsupported type", wicaChannelName);
   }

   private void publishMetadataString( Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createStringInstance();
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private void publishMetadataStringArray( Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createStringArrayInstance();
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishMetadataInteger( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int upperDisplay = (int) metadataObject.getUpperDisplay();
      final int lowerDisplay = (int) metadataObject.getLowerDisplay();
      final int upperControl = (int) metadataObject.getUpperControl();
      final int lowerControl = (int) metadataObject.getLowerControl();
      final int upperAlarm   = (int) metadataObject.getUpperAlarm();
      final int lowerAlarm   = (int) metadataObject.getLowerAlarm();
      final int upperWarning = (int) metadataObject.getUpperWarning();
      final int lowerWarning = (int) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createIntegerInstance( units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishMetadataIntegerArray( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int upperDisplay = (int) metadataObject.getUpperDisplay();
      final int lowerDisplay = (int) metadataObject.getLowerDisplay();
      final int upperControl = (int) metadataObject.getUpperControl();
      final int lowerControl = (int) metadataObject.getLowerControl();
      final int upperAlarm   = (int) metadataObject.getUpperAlarm();
      final int lowerAlarm   = (int) metadataObject.getLowerAlarm();
      final int upperWarning = (int) metadataObject.getUpperWarning();
      final int lowerWarning = (int) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createIntegerArrayInstance( units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishMetadataReal( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int precision   = metadataObject.getPrecision();
      final double upperDisplay = (double) metadataObject.getUpperDisplay();
      final double lowerDisplay = (double) metadataObject.getLowerDisplay();
      final double upperControl = (double) metadataObject.getUpperControl();
      final double lowerControl = (double) metadataObject.getLowerControl();
      final double upperAlarm   = (double) metadataObject.getUpperAlarm();
      final double lowerAlarm   = (double) metadataObject.getLowerAlarm();
      final double upperWarning = (double) metadataObject.getUpperWarning();
      final double lowerWarning = (double) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createRealInstance( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }

   private <T,ST> void publishMetadataRealArray( Control<T,ST> metadataObject, Consumer<WicaChannelMetadata> metadataChangeHandler )
   {
      final String units = metadataObject.getUnits();
      final int precision   = metadataObject.getPrecision();
      final double upperDisplay = (double) metadataObject.getUpperDisplay();
      final double lowerDisplay = (double) metadataObject.getLowerDisplay();
      final double upperControl = (double) metadataObject.getUpperControl();
      final double lowerControl = (double) metadataObject.getLowerControl();
      final double upperAlarm   = (double) metadataObject.getUpperAlarm();
      final double lowerAlarm   = (double) metadataObject.getLowerAlarm();
      final double upperWarning = (double) metadataObject.getUpperWarning();
      final double lowerWarning = (double) metadataObject.getLowerWarning();

      final WicaChannelMetadata wicaChannelMetadata = WicaChannelMetadata.createRealArrayInstance(units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      metadataChangeHandler.accept(wicaChannelMetadata);
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
