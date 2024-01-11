/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A filter that writes a new value to the output list every time the input
 * signal makes a change whose absolute value exceeds the configured deadband.
 *
 * The filter only operates on values for types WicaChannelType.REAL and
 * WicaChannelType.INTEGER. All other value types in the input list
 * will be passed through unaffected.
 *
 * Where the values in the input list indicate that the data source is offline
 * then this will result in the transfer of the offline values to the output list.
 * When the data source eventually comes back online then the first online value
 * is also transferred.
 *
 * In the unlikely situation where successive values from the input list are of
 * different types the first value of the changed type is always transferred
 * to the output list.
 *
 */
@ThreadSafe
class WicaChannelValueChangeDetectingFilter implements WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaChannelValueChangeDetectingFilter.class);

   private final double deadband;
   private WicaChannelValue previousValue;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance based on the specified deadband.
    *
    * @param deadband defines the absolute change in the input value which must
    *     occur in order for the new value to be transferred from the input list
    *     to the output list.
    */
   WicaChannelValueChangeDetectingFilter(double deadband )
   {
      Validate.isTrue( deadband > 0 );
      this.deadband = deadband;
      this.previousValue = WicaChannelValue.createChannelValueDisconnected();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> apply( List<WicaChannelValue> inputList )
   {
      Validate.notNull( inputList, "The 'inputList' argument is null." );
      final List<WicaChannelValue> outputList = new LinkedList<>();

      for ( WicaChannelValue currentValue : inputList )
      {
         // If the current value is offline then transfer the offline value
         // to the output list
         if ( ( ! currentValue.isConnected() ) )
         {
            outputList.add( currentValue );
            previousValue = currentValue;
            continue;
         }

         // If the current and previous values indicates that the data source has
         // just come online then transfer the new value to the output list
         if ( ( ! previousValue.isConnected() ) && ( currentValue.isConnected() ) )
         {
            outputList.add( currentValue );
            previousValue = currentValue;
            continue;
         }

         final WicaChannelValue.WicaChannelValueConnected currentValueConnected = (WicaChannelValue.WicaChannelValueConnected) currentValue;
         final WicaChannelValue.WicaChannelValueConnected previousValueConnected = (WicaChannelValue.WicaChannelValueConnected) previousValue;
         if ( isChangeDetected( currentValueConnected, previousValueConnected ) )
         {
            outputList.add( currentValue );
         }
         previousValue = currentValue;
      }
      return outputList;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueChangeDetectingFilter{" +
              "deadband=" + deadband +
              '}';
   }

/*- Private methods ----------------------------------------------------------*/

   private boolean isChangeDetected( WicaChannelValue.WicaChannelValueConnected currentValue,
                                     WicaChannelValue.WicaChannelValueConnected previousValue )
   {
      Validate.notNull( currentValue, "The 'currentValue' argument is null." );
      Validate.notNull( previousValue, "The 'previousValue' argument is null." );

      // Handle the unusual situation where successive values in the input list are of
      // different types. In this case transfer the new value.
      if ( currentValue.getWicaChannelType() != previousValue.getWicaChannelType() )
      {
         return true;
      }

      switch ( currentValue.getWicaChannelType() )
      {
         case REAL:
            final double currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) currentValue).getValue();
            final double previousValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) previousValue).getValue();
            final double changeAsDouble = currentValueAsDouble - previousValueAsDouble;
            if ( Math.abs( changeAsDouble ) > deadband )
            {
               logger.trace("Change significant. Previous Value: {}, CurrentValue: {}, Change: {} units", previousValueAsDouble, currentValueAsDouble, changeAsDouble );
               return true;
            }
            break;

         case INTEGER:
           final int currentValueAsInteger = ((WicaChannelValue.WicaChannelValueConnectedInteger) currentValue).getValue();
           final int previousValueAsInteger = ((WicaChannelValue.WicaChannelValueConnectedInteger) previousValue).getValue();
           final int changeAsInteger = currentValueAsInteger - previousValueAsInteger;
            if ( Math.abs( changeAsInteger) > deadband )
            {
               logger.trace("Change significant. Previous Value: {}, CurrentValue: {}, Change: {} units", previousValueAsInteger, currentValueAsInteger, changeAsInteger );
               return true;
            }
            break;

         // All other types are passed through unchanged
         default:
            return true;
      }

      return false;
   }


/*- Nested Classes -----------------------------------------------------------*/

}
