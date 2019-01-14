/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static ch.psi.wica.model.WicaChannelType.REAL;
import static ch.psi.wica.model.WicaChannelType.REAL_ARRAY;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A WicaChannelValueMapper that returns an output list with all input values
 * represented in the output list.
 *
 * If the input value is of type WicaChannelType.REAL or WicaChannelType.REAL_ARRAY
 * then the returned value will (additionally) be precision limited to the specified
 * number of digits.
 */
@ThreadSafe
class WicaAllValuePrecisionLimitingChannelValueMapper implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaAllValuePrecisionLimitingChannelValueMapper.class);

   private final int numberOfDigits;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance based on the specified numberOfDigits.
    *
    * @param numberOfDigits
    */
   WicaAllValuePrecisionLimitingChannelValueMapper(int numberOfDigits )
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
            final WicaChannelValue convertedValue =  WicaChannelValue.createChannelValueConnected( currentValueAsDouble, numberOfDigits );
            outputList.add( convertedValue );
         }
         else if ( currentConnectedValue.getWicaChannelType() == REAL_ARRAY )
         {
            final double[] currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedRealArray) currentValue).getValue();
            final WicaChannelValue convertedValue =  WicaChannelValue.createChannelValueConnected( currentValueAsDouble, numberOfDigits );
            outputList.add( convertedValue );
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
