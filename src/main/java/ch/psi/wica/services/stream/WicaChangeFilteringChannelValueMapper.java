/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelType;
import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A WicaChannelValueMapper that writes a new value to the output list every time
 * the input signal makes a change whose absolute value exceeds a configured
 * deadband.
 *
 * The mapper only transfer values for types WicaChannelType.REAL and
 * WicaChannelType.INTEGER. All other value types in the input list will be
 * ignored and will NOT be transferred to the output list.
 */
@ThreadSafe
class WicaChangeFilteringChannelValueMapper implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaChangeFilteringChannelValueMapper.class);

   private final double deadband;
   private WicaChannelValue previousValue;

   /*- Main ---------------------------------------------------------------------*/
   /*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance based on the specified deadband.
    *
    * @param deadband
    */
   WicaChangeFilteringChannelValueMapper( double deadband )
   {
      Validate.isTrue( deadband > 0 );
      this.deadband = deadband;
      this.previousValue = WicaChannelValue.createChannelValueDisconnected();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> map( List<WicaChannelValue> inputList )
   {
      Validate.notNull( inputList );
      final List<WicaChannelValue> outputList = new LinkedList<>();

      for ( WicaChannelValue currentValue : inputList )
      {
         if ( ! previousValue.isConnected() || ( ! currentValue.isConnected() ) )
         {
            outputList.add( currentValue );
            previousValue = currentValue;
            continue;
         }

         final WicaChannelValue.WicaChannelValueConnected currentValueConnected = (WicaChannelValue.WicaChannelValueConnected) currentValue;
         final WicaChannelValue.WicaChannelValueConnected previousValueConnected = (WicaChannelValue.WicaChannelValueConnected) previousValue;
         if ( changeDetected( currentValueConnected, previousValueConnected ) )
         {
            outputList.add( currentValue );
         }
         previousValue = currentValue;
      }
      return outputList;
   }

   /*- Private methods ----------------------------------------------------------*/

   private boolean changeDetected( WicaChannelValue.WicaChannelValueConnected currentValue,
                                   WicaChannelValue.WicaChannelValueConnected previousValue )
   {
      Validate.notNull( currentValue );
      Validate.notNull( previousValue );

      switch ( currentValue.getWicaChannelType() )
      {
         case REAL:
            final double currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) currentValue).getValue();
            final double previousValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) previousValue).getValue();
            final double changeAsDouble = currentValueAsDouble - previousValueAsDouble;
            if ( Math.abs( changeAsDouble ) > deadband )
            {
               logger.info("Change significant. Previous Value: {}, CurrentValue: {}, Change: {} units", previousValueAsDouble, currentValueAsDouble, changeAsDouble );
               return true;
            }
            break;

         case INTEGER:
           final int currentValueAsInteger = ((WicaChannelValue.WicaChannelValueConnectedInteger) currentValue).getValue();
           final int previousValueAsInteger = ((WicaChannelValue.WicaChannelValueConnectedInteger) previousValue).getValue();
           final int changeAsInteger = currentValueAsInteger - previousValueAsInteger;
            if ( Math.abs( changeAsInteger) > deadband )
            {
               logger.info("Change significant. Previous Value: {}, CurrentValue: {}, Change: {} units", previousValueAsInteger, currentValueAsInteger, changeAsInteger );
               return true;
            }
            break;

         // All other types pass through directly without translation
         default:
            throw new IllegalArgumentException( "The input value was of an unexpected type: " + currentValue.getWicaChannelType() );
      }

      return false;
   }


/*- Nested Classes -----------------------------------------------------------*/

}
