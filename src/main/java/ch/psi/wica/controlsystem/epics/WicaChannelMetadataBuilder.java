/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelType;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.epics.ca.data.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the functionality to build a WicaChannelMetadata object using
 * raw channel value data.
 */
@Immutable
@Component
class WicaChannelMetadataBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaChannelMetadataBuilder.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a WicaChannelMetadata object based on the supplied data obtained
    * from the EPICS channel.
    *
    * @param controlSystemName the name of the control system channel (needed
    *     for logging purposes only). Not Null.
    *
    * @param wicaChannelType the channel type. Not Null.
    *
    * @param epicsMetadataObject the EPICS CA library Control object. Not Null
    *     except when channel is of type STRING or STRING_ARRAY.
    *
    * @return the constructed metadata object.
    */
   WicaChannelMetadata build( ControlSystemName controlSystemName, WicaChannelType wicaChannelType, Control<?,?>  epicsMetadataObject )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( wicaChannelType );

      if ( ( wicaChannelType != WicaChannelType.STRING ) && (wicaChannelType != WicaChannelType.STRING_ARRAY ) )
      {
         Validate.notNull( epicsMetadataObject );
      }

      switch( wicaChannelType )
      {
         case STRING:
            logger.trace("'{}' - type is STRING.", controlSystemName );
            return getMetadataString();

         case STRING_ARRAY:
            logger.trace("'{}' - type is STRING ARRAY.", controlSystemName );
            return getMetadataStringArray();

         case INTEGER:
            logger.trace("'{}' - type is INTEGER.", controlSystemName );
            return getMetadataInteger( epicsMetadataObject );

         case INTEGER_ARRAY:
            logger.trace("'{}' - type is INTEGER ARRAY.", controlSystemName );
            return getMetadataIntegerArray( epicsMetadataObject );

         case REAL:
            logger.trace( "'{}' - type is DOUBLE.", controlSystemName );
            return getMetadataReal( epicsMetadataObject );

         case REAL_ARRAY:
            logger.trace( "'{}' - type is DOUBLE ARRAY.", controlSystemName );
            return getMetadataRealArray( epicsMetadataObject );

         default:
            logger.error( "'{}' - type is NOT SUPPORTED (Programming Error)", controlSystemName );
            return WicaChannelMetadata.createUnknownInstance();
      }
   }


/*- Private methods ----------------------------------------------------------*/

   private WicaChannelMetadata getMetadataString()
   {
      return WicaChannelMetadata.createStringInstance();
   }

   private WicaChannelMetadata getMetadataStringArray()
   {
      return WicaChannelMetadata.createStringArrayInstance();
   }

   private <T,ST> WicaChannelMetadata getMetadataInteger( Control<T,ST> metadataObject )
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

      return WicaChannelMetadata.createIntegerInstance( units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   private <T,ST> WicaChannelMetadata getMetadataIntegerArray( Control<T,ST> metadataObject )
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

      return  WicaChannelMetadata.createIntegerArrayInstance( units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   private <T,ST> WicaChannelMetadata getMetadataReal( Control<T,ST> metadataObject )
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
      return WicaChannelMetadata.createRealInstance( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   private <T,ST> WicaChannelMetadata getMetadataRealArray( Control<T,ST> metadataObject )
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
      return WicaChannelMetadata.createRealArrayInstance(units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
