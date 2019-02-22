/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static java.time.temporal.ChronoUnit.MILLIS;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A filter that returns an output list with values taken from the input list
 * periodically according to the value's timestamp.
 */
@ThreadSafe
class WicaChannelValueFilterRateLimitingSampler implements WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );
   private final Duration samplingInterval;
   private LocalDateTime lastSampleTimestamp;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which returns an output list containing the
    * first input value and then subsequent values taken from the input
    * list after the specified minimum sampling interval.
    *
    * @param samplingIntervalInMillis - the minimum time duration between samples.
    */
   WicaChannelValueFilterRateLimitingSampler( long samplingIntervalInMillis )
   {
      Validate.isTrue(samplingIntervalInMillis > 0 );

      samplingInterval = Duration.of( samplingIntervalInMillis, MILLIS );
      this.lastSampleTimestamp = LONG_AGO;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> apply( List<WicaChannelValue> inputList )
   {
      final List<WicaChannelValue> outputList = new LinkedList<>();

      for ( WicaChannelValue inputValue : inputList )
      {
         if ( Duration.between( lastSampleTimestamp, inputValue.getWicaServerTimestamp() ).compareTo( samplingInterval) > 0 )
         {
            outputList.add( inputValue );
            lastSampleTimestamp = inputValue.getWicaServerTimestamp();
         }
      }
      return outputList;
   }

   @Override
   public String toString()
   {
      return "WicaChannelValueFilterRateLimitingSampler{" +
            "samplingInterval=" + samplingInterval +
            ", lastSampleTimestamp=" + lastSampleTimestamp +
            '}';
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
