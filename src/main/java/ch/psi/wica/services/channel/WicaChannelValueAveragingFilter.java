/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.util.LinkedList;
import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A filter that writes a new value to the output list based on the
 * average of the previous X samples provided.
 *
 * The filter only operates on values for types WicaChannelType.REAL and
 * WicaChannelType.INTEGER. All other value types in the input list
 * will be ignored and will not contribute to the averaging result.
 *
 * The output of the filter will be of the same numeric type as the
 * input values passed into it. In the unusual situation where both
 * WicaChannelType.REAL and WicaChannelType.INTEGER values are supplied
 * the output will be of the widest type (WicaChannelType.REAL).
 *
 * If the input list contains an insufficient number of numeric samples
 * to calculate an average then the output result will be empty. Any
 * supplied values will be accumulated internally and will be used
 * to contribute to the averaging result the next time the filter's
 * apply method is invoked.
 *
 * If any of the values in the input list indicate that the data
 * source is
 * offline then the averaging result will be a single offline value.
 */
@ThreadSafe
class WicaChannelValueAveragingFilter implements WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int numberOfSamplesInAverage;

   private double sum;
   private int numberOfReceivedOfflineSamples;
   private int numberOfReceivedIntegerSamples;
   private int numberOfReceivedDoubleSamples;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance that calculates its output based on
    * the arithmetic mean of the specified number of samples.
    *
    * @param numberOfSamplesInAverage the number of samples to be used to
    *        calculate each output value.
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
         processValue( currentValue );
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

   @Override
   public String toString()
   {
      return "WicaChannelValueAveragingFilter{" +
              "numberOfSamplesInAverage=" + numberOfSamplesInAverage +
              '}';
   }

/*- Private methods ----------------------------------------------------------*/

   private int getNumberOfReceivedSamples()
   {
      return numberOfReceivedOfflineSamples + numberOfReceivedDoubleSamples + numberOfReceivedIntegerSamples;
   }

   private void processValue( WicaChannelValue currentValue )
   {
      // If the current value is offline then increment the count of
      // received offline samples without accumulating anything in the
      // sum. When the expected number of samples has been received
      // the averaging result will be presented as an offline value.
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

         case SHORT:
            final short currentValueAsShort = ((WicaChannelValue.WicaChannelValueConnectedShort) currentValue).getValue();
            sum += currentValueAsShort;
            numberOfReceivedIntegerSamples++;
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
      // If any of the samples in the average was offline then present the
      // averaging result as an offline value.
      if ( numberOfReceivedOfflineSamples > 0 )
      {
         outputList.add( WicaChannelValue.createChannelValueDisconnected() );
      }

      // If there is at least one double sample then present the averaging result
      // as a double.
      else if ( numberOfReceivedDoubleSamples > 0 )
      {
         final double average = sum / getNumberOfReceivedSamples();
         outputList.add( WicaChannelValue.createChannelValueConnected( average ) );
      }

      // If all the input samples are integers then present the averaging result
      // as an integer, using rounding where necessary.
      else
      {
         final int average = (int) Math.round( sum / getNumberOfReceivedSamples() );
         outputList.add( WicaChannelValue.createChannelValueConnected( average ) );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
