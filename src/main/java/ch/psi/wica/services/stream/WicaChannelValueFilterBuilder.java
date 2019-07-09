/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
import ch.psi.wica.model.WicaStreamProperties;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueFilterBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaChannelValueFilterBuilder.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a filter suitable for channels whose data acquisition mode includes monitoring.
    *
    * The filter will be chosen according to the properties configured in the wica channel
    * properties object.
    *
    * @param wicaChannelProperties the channel properties object.
    * @return the filter.
    */
   public static WicaChannelValueFilter createFilterForMonitoredChannels( WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaChannelProperties );

      final WicaChannelValueFilter filter;
      switch ( wicaChannelProperties.getFilterType() )
      {
         case ALL_VALUE:
            logger.trace("Creating channel value filter for MONITORED channels with filterType='allValueSampler'");
            filter = new WicaChannelValueFilterAllValueSampler();
            break;

         case RATE_LIMITER:
            final int samplingInterval = wicaChannelProperties.getFilterSamplingIntervalInMillis();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='rate-limiter', samplingInterval='{}'", samplingInterval );
            filter = new WicaChannelValueFilterRateLimitingSampler( samplingInterval );
            break;

         case LAST_N:
            final int numSamples = wicaChannelProperties.getFilterNumSamples();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='last-n', n='{}'", numSamples );
            filter = new WicaChannelValueFilterLatestValueSampler( numSamples );
            break;

         case ONE_IN_M:
            final int cycleLength = wicaChannelProperties.getFilterCycleLength();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='one-in-m', m='{}'", cycleLength );
            filter = new WicaChannelValueFilterFixedCycleSampler( cycleLength );
            break;

         case CHANGE_FILTERER:
            final double deadband = wicaChannelProperties.getFilterDeadband();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='changes', deadband='{}'", deadband );
            filter = new WicaChannelValueFilterChangeFilteringSampler(deadband );
            break;

         default:
            logger.warn("The filterType parameter was not recognised. Using default (last-n) filter.");
            final int defaultMaxNumberOfSamples = 1;
            logger.trace("Creating channel value filter for MONITORED channels with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
            filter = new WicaChannelValueFilterLatestValueSampler( defaultMaxNumberOfSamples );
            break;
      }

      return filter;
   }


   /**
    * Returns a filter suitable for a channels whose data acquisition mode includes polling.
    *
    * The filter that will be returned will be a fixed cycle ("one-in-m") sampler
    * whose cycle length (ie m) will be configured according to the configured
    * sampling ratio (taken either from the stream or channel properties).
    *
    * @param wicaStreamProperties the stream properties object.
    * @param wicaChannelProperties the channel properties object.
    * @return the filter.
    */
   public static WicaChannelValueFilter createFilterForPolledChannels( WicaStreamProperties wicaStreamProperties,
                                                                       WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaStreamProperties );
      Validate.notNull( wicaChannelProperties );

      // Extract the ratio of number of channel polls per acquired sample
      final int cycleLength = wicaChannelProperties.getPolledValueSampleRatio().isPresent() ?
            wicaChannelProperties.getPolledValueSampleRatio().get() :
            wicaStreamProperties.getPolledValueSampleRatio();

      // Return the filter
      logger.trace("Creating channel value filter for POLLED channels with filterType='one-in-m', m='{}'", cycleLength );
      return new WicaChannelValueFilterFixedCycleSampler( cycleLength );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
