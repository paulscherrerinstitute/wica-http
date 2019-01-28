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
 * A WicaChannelValueMapper that returns an output list with values taken from
 * the input list periodically according to the value's timestamp.
 */
@ThreadSafe
class WicaChannelValueMapperRateLimitingSampler implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );
   private Duration minimumSampleGap;
   private LocalDateTime lastSampleTimestamp;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance which returns an output list containing the
    * first input value and then subsequent values taken from the input
    * list after the specified minimum sampling interval.
    *
    * @param minimumSampleGapInMilliseconds - the minimum time duration between samples.
    */
   WicaChannelValueMapperRateLimitingSampler( long minimumSampleGapInMilliseconds )
   {
      Validate.isTrue(minimumSampleGapInMilliseconds > 0 );

      minimumSampleGap = Duration.of( minimumSampleGapInMilliseconds, MILLIS );
      this.lastSampleTimestamp = LONG_AGO;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<WicaChannelValue> map( List<WicaChannelValue> inputList )
   {
      final List<WicaChannelValue> outputList = new LinkedList<>();

      for ( WicaChannelValue inputValue : inputList )
      {
         if ( Duration.between( lastSampleTimestamp, inputValue.getWicaServerTimestamp() ).compareTo(minimumSampleGap) > 0 )
         {
            outputList.add( inputValue );
            lastSampleTimestamp = inputValue.getWicaServerTimestamp();
         }
      }
      return outputList;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
