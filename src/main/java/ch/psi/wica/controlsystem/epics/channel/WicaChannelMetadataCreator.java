/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.metadata.WicaChannelMetadata;
import ch.psi.wica.model.channel.metadata.WicaChannelMetadataBuilder;
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
public class WicaChannelMetadataCreator
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelMetadataCreator.class );
   
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
    * @param epicsChannelType the type of the EPICS channel.
    *
    * @param epicsMetadataObject the EPICS CA library Control object. Not Null
    *     except when channel is of type STRING or STRING_ARRAY.
    *
    * @return the constructed metadata object.
    */
   public WicaChannelMetadata build( ControlSystemName controlSystemName, EpicsChannelType epicsChannelType, Control<?,?>  epicsMetadataObject )
   {
      Validate.notNull( controlSystemName, "The 'controlSystemName' argument is null." );
      Validate.notNull( epicsChannelType, "The 'epicsChannelType' argument is null." );

      logger.trace( "'{}' - type is {}", controlSystemName, epicsChannelType );

       return switch ( epicsChannelType )
       {
           case STRING -> WicaChannelMetadataBuilder.createStringInstance();
           case STRING_ARRAY -> WicaChannelMetadataBuilder.createStringArrayInstance();
           case BYTE -> WicaChannelMetadataBuilder.createIntegerInstance( epicsMetadataObject.getUnits( ),
                   (byte) epicsMetadataObject.getUpperDisplay( ),
                   (byte) epicsMetadataObject.getLowerDisplay( ),
                   (byte) epicsMetadataObject.getUpperControl( ),
                   (byte) epicsMetadataObject.getLowerControl( ),
                   (byte) epicsMetadataObject.getUpperAlarm( ),
                   (byte) epicsMetadataObject.getLowerAlarm( ),
                   (byte) epicsMetadataObject.getUpperWarning( ),
                   (byte) epicsMetadataObject.getLowerWarning( ) );
           case SHORT -> WicaChannelMetadataBuilder.createIntegerInstance( epicsMetadataObject.getUnits( ),
                   (short) epicsMetadataObject.getUpperDisplay( ),
                   (short) epicsMetadataObject.getLowerDisplay( ),
                   (short) epicsMetadataObject.getUpperControl( ),
                   (short) epicsMetadataObject.getLowerControl( ),
                   (short) epicsMetadataObject.getUpperAlarm( ),
                   (short) epicsMetadataObject.getLowerAlarm( ),
                   (short) epicsMetadataObject.getUpperWarning( ),
                   (short) epicsMetadataObject.getLowerWarning( ) );
           case INTEGER -> WicaChannelMetadataBuilder.createIntegerInstance( epicsMetadataObject.getUnits( ),
                   (int) epicsMetadataObject.getUpperDisplay( ),
                   (int) epicsMetadataObject.getLowerDisplay( ),
                   (int) epicsMetadataObject.getUpperControl( ),
                   (int) epicsMetadataObject.getLowerControl( ),
                   (int) epicsMetadataObject.getUpperAlarm( ),
                   (int) epicsMetadataObject.getLowerAlarm( ),
                   (int) epicsMetadataObject.getUpperWarning( ),
                   (int) epicsMetadataObject.getLowerWarning( ) );
           case BYTE_ARRAY -> WicaChannelMetadataBuilder.createIntegerArrayInstance( epicsMetadataObject.getUnits( ),
                   (byte) epicsMetadataObject.getUpperDisplay( ),
                   (byte) epicsMetadataObject.getLowerDisplay( ),
                   (byte) epicsMetadataObject.getUpperControl( ),
                   (byte) epicsMetadataObject.getLowerControl( ),
                   (byte) epicsMetadataObject.getUpperAlarm( ),
                   (byte) epicsMetadataObject.getLowerAlarm( ),
                   (byte) epicsMetadataObject.getUpperWarning( ),
                   (byte) epicsMetadataObject.getLowerWarning( ) );
           case SHORT_ARRAY -> WicaChannelMetadataBuilder.createIntegerArrayInstance( epicsMetadataObject.getUnits( ),
                   (short) epicsMetadataObject.getUpperDisplay( ),
                   (short) epicsMetadataObject.getLowerDisplay( ),
                   (short) epicsMetadataObject.getUpperControl( ),
                   (short) epicsMetadataObject.getLowerControl( ),
                   (short) epicsMetadataObject.getUpperAlarm( ),
                   (short) epicsMetadataObject.getLowerAlarm( ),
                   (short) epicsMetadataObject.getUpperWarning( ),
                   (short) epicsMetadataObject.getLowerWarning( ) );
           case INTEGER_ARRAY -> WicaChannelMetadataBuilder.createIntegerArrayInstance( epicsMetadataObject.getUnits( ),
                   (int) epicsMetadataObject.getUpperDisplay( ),
                   (int) epicsMetadataObject.getLowerDisplay( ),
                   (int) epicsMetadataObject.getUpperControl( ),
                   (int) epicsMetadataObject.getLowerControl( ),
                   (int) epicsMetadataObject.getUpperAlarm( ),
                   (int) epicsMetadataObject.getLowerAlarm( ),
                   (int) epicsMetadataObject.getUpperWarning( ),
                   (int) epicsMetadataObject.getLowerWarning( ) );
           case FLOAT -> WicaChannelMetadataBuilder.createRealInstance( epicsMetadataObject.getUnits( ),
                   epicsMetadataObject.getPrecision( ),
                   (float) epicsMetadataObject.getUpperDisplay( ),
                   (float) epicsMetadataObject.getLowerDisplay( ),
                   (float) epicsMetadataObject.getUpperControl( ),
                   (float) epicsMetadataObject.getLowerControl( ),
                   (float) epicsMetadataObject.getUpperAlarm( ),
                   (float) epicsMetadataObject.getLowerAlarm( ),
                   (float) epicsMetadataObject.getUpperWarning( ),
                   (float) epicsMetadataObject.getLowerWarning( ) );
           case DOUBLE -> WicaChannelMetadataBuilder.createRealInstance( epicsMetadataObject.getUnits( ),
                   epicsMetadataObject.getPrecision( ),
                   (double) epicsMetadataObject.getUpperDisplay( ),
                   (double) epicsMetadataObject.getLowerDisplay( ),
                   (double) epicsMetadataObject.getUpperControl( ),
                   (double) epicsMetadataObject.getLowerControl( ),
                   (double) epicsMetadataObject.getUpperAlarm( ),
                   (double) epicsMetadataObject.getLowerAlarm( ),
                   (double) epicsMetadataObject.getUpperWarning( ),
                   (double) epicsMetadataObject.getLowerWarning( ) );
           case FLOAT_ARRAY -> WicaChannelMetadataBuilder.createRealArrayInstance( epicsMetadataObject.getUnits( ),
                   epicsMetadataObject.getPrecision( ),
                   (float) epicsMetadataObject.getUpperDisplay( ),
                   (float) epicsMetadataObject.getLowerDisplay( ),
                   (float) epicsMetadataObject.getUpperControl( ),
                   (float) epicsMetadataObject.getLowerControl( ),
                   (float) epicsMetadataObject.getUpperAlarm( ),
                   (float) epicsMetadataObject.getLowerAlarm( ),
                   (float) epicsMetadataObject.getUpperWarning( ),
                   (float) epicsMetadataObject.getLowerWarning( ) );
           case DOUBLE_ARRAY -> WicaChannelMetadataBuilder.createRealArrayInstance( epicsMetadataObject.getUnits( ),
                   epicsMetadataObject.getPrecision( ),
                   (double) epicsMetadataObject.getUpperDisplay( ),
                   (double) epicsMetadataObject.getLowerDisplay( ),
                   (double) epicsMetadataObject.getUpperControl( ),
                   (double) epicsMetadataObject.getLowerControl( ),
                   (double) epicsMetadataObject.getUpperAlarm( ),
                   (double) epicsMetadataObject.getLowerAlarm( ),
                   (double) epicsMetadataObject.getUpperWarning( ),
                   (double) epicsMetadataObject.getLowerWarning( ) );
       };
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
