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
 * the previous N samples provided.
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

   private final int numberOfSamplesInAverage;

   private double sum;
   private int numberOfReceivedOfflineSamples;
   private int numberOfReceivedIntegerSamples;
   private int numberOfReceivedDoubleSamples;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance that calculates its average based on
    * the specified number of samples.
    *
    * @param numberOfSamplesInAverage - the number of samples to be used to
    *        calculate the output values.
    */
   WicaChannelValueAveragingFilter( int numberOfSamplesInAverage )
   {
      Validate.isTrue( numberOfSamplesInAverage > 0 );
      this.numberOfSamplesInAverage = numberOfSamplesInAverage;
      this.sum = 0;
      this.numberOfReceivedOfflineSamples = 0;
      this.numberOfReceivedIntegerSamples = 0;
      this.numberOfReceivedDoubleSamples = 0;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> apply( List<WicaChannelValue> inputList )
   {
      Validate.notNull( inputList );

      final List<WicaChannelValue> outputList = new LinkedList<>();
      for ( WicaChannelValue currentValue : inputList )
      {
         accumulate( currentValue );
         if ( getNumberOfReceivedSamples() == numberOfSamplesInAverage )
         {
            saveAverageInOutputList( outputList );
            sum = 0.0;
            numberOfReceivedOfflineSamples = 0;
            numberOfReceivedIntegerSamples = 0;
            numberOfReceivedDoubleSamples = 0;
         }
      }
      return outputList;
   }

/*- Private methods ----------------------------------------------------------*/

   private int getNumberOfReceivedSamples()
   {
      return numberOfReceivedOfflineSamples + numberOfReceivedDoubleSamples + numberOfReceivedIntegerSamples;
   }

   private void accumulate( WicaChannelValue currentValue )
   {
      // If the current value is offline then return an averaging
      // result which indicates the channel was offline.
      if ( ! currentValue.isConnected() )
      {
         numberOfReceivedOfflineSamples++;
         return;
      }

      final WicaChannelValue.WicaChannelValueConnected connectedValue = (WicaChannelValue.WicaChannelValueConnected) currentValue;
      switch ( connectedValue.getWicaChannelType() )
      {
         case REAL:
            final double currentValueAsDouble = ((WicaChannelValue.WicaChannelValueConnectedReal) connectedValue).getValue();
            sum += currentValueAsDouble;
            numberOfReceivedDoubleSamples++;
            break;

         case INTEGER:
            final int currentValueAsInteger = ((WicaChannelValue.WicaChannelValueConnectedInteger) currentValue).getValue();
            sum += currentValueAsInteger;
            numberOfReceivedIntegerSamples++;
            break;
      }
   }

   private void saveAverageInOutputList( List<WicaChannelValue> outputList )
   {
      // If any of the samples in the average was offline then the averaging
      // result is also an offline value.
      if ( numberOfReceivedOfflineSamples > 0 )
      {
         outputList.add( WicaChannelValue.createChannelValueDisconnected() );
      }

      // If there is at least one double sample present then return
      // a result of the wider type.
      else if ( numberOfReceivedDoubleSamples > 0 )
      {
         final double average = sum / getNumberOfReceivedSamples();
         outputList.add( WicaChannelValue.createChannelValueConnected( average ) );
      }

      // If all the input samples are integers then return the rounded
      // integer result.
      else
      {
         final int average = (int) Math.round( sum / getNumberOfReceivedSamples() );
         outputList.add( WicaChannelValue.createChannelValueConnected( average ) );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
