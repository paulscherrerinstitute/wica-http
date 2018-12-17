/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelType;
import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static ch.psi.wica.model.WicaChannelType.REAL;


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
class WicaPrecisionLimitingChannelValueMapper implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaPrecisionLimitingChannelValueMapper.class);

   private final int numberOfDigits;

   /*- Main ---------------------------------------------------------------------*/
   /*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance based on the specified numberOfDigits.
    *
    * @param numberOfDigits
    */
   WicaPrecisionLimitingChannelValueMapper( int numberOfDigits )
   {
      Validate.isTrue( numberOfDigits >= 0 );
      this.numberOfDigits = numberOfDigits;
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
         if ( ! currentValue.isConnected() )
         {
            outputList.add( currentValue );
            continue;
         }

         final WicaChannelValue.WicaChannelValueConnected currentConnectedValue = (WicaChannelValue.WicaChannelValueConnected) currentValue;
         if ( currentConnectedValue.getWicaChannelType() == REAL )
         {
            final double currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) currentValue).getValue();
            final String formatString = "%." + numberOfDigits  + "f";
            final String convertedToFixedLengthStringValue = String.format( formatString, currentValueAsDouble );
            outputList.add( WicaChannelValue.createChannelValueConnected( convertedToFixedLengthStringValue ) );
         }
         else
         {
            outputList.add( currentValue );
         }
      }
      return outputList;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
