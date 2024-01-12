/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
@Service
public class WicaChannelValueFilteringService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger(WicaChannelValueFilteringService.class );

   private final Map<WicaChannel,WicaChannelValueFilter> wicaChannelValueFilterMap;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelValueFilteringService()
   {
      this.wicaChannelValueFilterMap = new ConcurrentHashMap<>();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Filters the supplied list of channel values of according to the properties
    * defined for channel monitoring.
    *
    * @param wicaChannel object which provides access to the filtering properties.
    * @param wicaChannelValues the list of values to filter.
    * @return the filtered output.
    */
   public List<WicaChannelValue> filterValues( WicaChannel wicaChannel, List<WicaChannelValue> wicaChannelValues )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );
      Validate.notNull( wicaChannelValues, "The 'wicaChannelValues' argument is null." );

      final WicaChannelValueFilter filter;
      if ( wicaChannelValueFilterMap.containsKey( wicaChannel ) )
      {
         filter = wicaChannelValueFilterMap.get( wicaChannel );
      }
      else
      {
         filter = getFilterForChannel( wicaChannel );
         wicaChannelValueFilterMap.put( wicaChannel, filter );
      }

      return filter.apply( wicaChannelValues );
   }

   /**
    * Filters the supplied list of channel values directly using a last value filter.
    *
    * @param wicaChannelValues the list of values to filter.
    * @return the filtered output.
    */
   public List<WicaChannelValue> filterLastValues( List<WicaChannelValue> wicaChannelValues )
   {
      var filter = new WicaChannelValueLatestValueFilter( 1 );
      return filter.apply( wicaChannelValues);
   }

/*- Private methods ----------------------------------------------------------*/

   private WicaChannelValueFilter getFilterForChannel( WicaChannel wicaChannel )
   {
      final WicaChannelProperties wicaChannelProperties = wicaChannel.getProperties();
      final WicaFilterType filterType = wicaChannelProperties.getFilterType();

      final WicaChannelValueFilter filter;
      switch ( filterType )
      {
         case ALL_VALUE:
            logger.trace("Creating channel value filter for MONITORED channels with filterType='all-value'");
            filter = new WicaChannelValuePassEverythingFilter();
            break;

         case RATE_LIMITER:
            final int samplingInterval = wicaChannelProperties.getFilterSamplingIntervalInMillis();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='rate-limiter', samplingInterval='{}'", samplingInterval);
            filter = new WicaChannelValueRateLimitingFilter( samplingInterval );
            break;

         case LAST_N:
            final int numSamples = wicaChannelProperties.getFilterNumSamples();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='last-n', n='{}'", numSamples);
            filter = new WicaChannelValueLatestValueFilter( numSamples );
            break;

         case ONE_IN_M:
            final int cycleLength = wicaChannelProperties.getFilterCycleLength();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='one-in-m', m='{}'", cycleLength);
            filter = new WicaChannelValueFixedSamplingCycleFilter( cycleLength );
            break;

         case CHANGE_DETECTOR:
            final double deadband = wicaChannelProperties.getFilterDeadband();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='changes', deadband='{}'", deadband);
            filter = new WicaChannelValueChangeDetectingFilter( deadband );
            break;

         case AVERAGER:
            final int averagerNumberOfSamples = wicaChannelProperties.getFilterNumSamplesInAverage();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='averager', samples='{}'", averagerNumberOfSamples );
            filter = new WicaChannelValueAveragingFilter( averagerNumberOfSamples );
            break;

         default:
            logger.warn("The filterType parameter was not recognised. Using default (last-n) filter.");
            final int defaultMaxNumberOfSamples = 1;
            logger.trace("Creating channel value filter for MONITORED channels with filterType='last-n', n='{}'", defaultMaxNumberOfSamples);
            filter = new WicaChannelValueLatestValueFilter( defaultMaxNumberOfSamples );
            break;
      }
      return filter;
   }

/*- Nested Classes -----------------------------------------------------------*/

}
