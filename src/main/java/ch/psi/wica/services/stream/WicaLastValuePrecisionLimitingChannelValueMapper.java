/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ch.psi.wica.model.WicaChannelType.REAL;
import static ch.psi.wica.model.WicaChannelType.REAL_ARRAY;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A WicaChannelValueMapper that transfers only the last value from the input
 * list to the output list.
 *
 * If the input value is of type WicaChannelType.REAL or WicaChannelType.REAL_ARRAY
 * then the returned value will (additionally) be precision limited to the specified
 * number of digits.
 */
@Immutable
class WicaLastValuePrecisionLimitingChannelValueMapper implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaLastValuePrecisionLimitingChannelValueMapper.class);

   private final int numberOfDigits;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaLastValuePrecisionLimitingChannelValueMapper(int numberOfDigits )
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
      return ( inputList.size() == 0 ) ? List.of() : List.of( mapOneValue( inputList.get( inputList.size() - 1 ), numberOfDigits ) );
   }


/*- Private methods ----------------------------------------------------------*/

   private static WicaChannelValue mapOneValue( WicaChannelValue inputValue, int numberOfDigits )
   {
      Validate.notNull( inputValue );

      if ( ! inputValue.isConnected() )
      {
         return inputValue;
      }

      final WicaChannelValue.WicaChannelValueConnected currentConnectedValue = (WicaChannelValue.WicaChannelValueConnected) inputValue;
      if ( currentConnectedValue.getWicaChannelType() == REAL )
      {
         final double currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) inputValue).getValue();
         return WicaChannelValue.createChannelValueConnected( currentValueAsDouble, numberOfDigits );
      }
      else if ( currentConnectedValue.getWicaChannelType() == REAL_ARRAY )
      {
         final double[] currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedRealArray) inputValue).getValue();
         return  WicaChannelValue.createChannelValueConnected( currentValueAsDouble, numberOfDigits );
      }

      return inputValue;
   }

/*- Nested Classes -----------------------------------------------------------*/

}
