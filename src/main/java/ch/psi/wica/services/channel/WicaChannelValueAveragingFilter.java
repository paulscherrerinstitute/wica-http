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
 * A filter that writes a new value to the output list based on the average of
 * the samples provided.
 *
 * The filter only operates on values for types WicaChannelType.REAL and
 * WicaChannelType.INTEGER. All other value types in the input list
 * will be ignored and will not contribute numerically to the result.
 *
 * The output of the filter will be of the same numeric type as the
 * input values passed into it. In the unusual situation where both
 * WicaChannelType.REAL and WicaChannelType.INTEGER values are supplied
 * the output will be of the widest type (WicaChannelType.REAL).
 *
 * If the input list contains no valid numeric values then the output
 * result will be empty.
 *
 * If any of the values in the input list indicate that the data source is
 * offline then the averaging result will be a single offline value.
 */
@ThreadSafe
class WicaChannelValueAveragingFilter implements WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaChannelValueAveragingFilter.class);

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    */
   WicaChannelValueAveragingFilter()
   {
      // nothing to do here yet
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> apply( List<WicaChannelValue> inputList )
   {
      Validate.notNull( inputList );

      final List<WicaChannelValue> outputList = new LinkedList<>();
      double sum = 0.0;
      int numberOfIntegerSamples = 0;
      int numberOfDoubleSamples = 0;
      for ( WicaChannelValue currentValue : inputList )
      {
         // If the current value is offline then return an averaging
         // result which indicates the channel was offline.
         if ( ! currentValue.isConnected() )
         {
            outputList.add( currentValue );
            return outputList;
         }

         final WicaChannelValue.WicaChannelValueConnected connectedValue = (WicaChannelValue.WicaChannelValueConnected) currentValue;
         switch ( connectedValue.getWicaChannelType() )
         {
            case REAL:
               final double currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) connectedValue).getValue();
               sum += currentValueAsDouble;
               numberOfDoubleSamples++;
               break;

            case INTEGER:
               final int currentValueAsInteger = ((WicaChannelValue.WicaChannelValueConnectedInteger) currentValue).getValue();
               sum += currentValueAsInteger;
               numberOfIntegerSamples++;
               break;

            // All other types are passed through unchanged
            default:
               continue;
         }
      }

      final int numberOfSamples = numberOfDoubleSamples + numberOfIntegerSamples;
      if ( numberOfSamples > 0 )
      {
         // If there is at least one double sample present then return
         // a result of the wider type.
         if ( numberOfDoubleSamples > 0 )
         {
            final double average = sum / numberOfSamples;
            outputList.add(WicaChannelValue.createChannelValueConnected( average ) );
         }
         // If all the input samples are integers then return the rounded
         // integer result
         else
         {
            final int average = (int) Math.round( sum / numberOfSamples );
            outputList.add(WicaChannelValue.createChannelValueConnected( average ) );
         }
      }
      return outputList;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
