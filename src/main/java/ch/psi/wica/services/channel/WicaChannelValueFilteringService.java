/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStreamProperties;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
@Service
public class WicaChannelValueFilteringService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger(WicaChannelValueFilteringService.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
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
   public List<WicaChannelValue> filterMonitoredValues( WicaChannel wicaChannel, List<WicaChannelValue> wicaChannelValues )
   {
      final var notifiedValuesFilter = this.getMonitoredChannelsFilter(wicaChannel.getProperties() );
      return notifiedValuesFilter.apply( wicaChannelValues );
   }

   /**
    * Filters the supplied list of channel values of according to the properties defined
    * for channel polling.
    *
    * @param wicaChannel  object which provides access to the filtering properties.
    * @param wicaChannelValues the list of values to filter.
    * @return the filtered output.
    */
   public List<WicaChannelValue> filterPolledValues( WicaChannel wicaChannel, List<WicaChannelValue> wicaChannelValues )
   {
      final var polledValuesFilter = WicaChannelValueFilteringService.getPolledChannelsFilter( wicaChannel.getProperties() );
      return polledValuesFilter.apply( wicaChannelValues );

   }


/*- Private methods ----------------------------------------------------------*/

   /**
    * Returns a filter suitable for channels whose data acquisition mode includes monitoring.
    *
    * The filter will be chosen according to the properties configured in the wica channel
    * properties object.
    *
    * @param wicaChannelProperties the channel properties object.
    * @return the filter.
    */
   private WicaChannelValueFilter getMonitoredChannelsFilter( WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaChannelProperties );

      final WicaChannelValueFilter filter;
      switch ( wicaChannelProperties.getFilterType() )
      {
         case ALL_VALUE:
            logger.trace("Creating channel value filter for MONITORED channels with filterType='allValueSampler'");
            filter = new WicaChannelValuePassEverythingFilter();
            break;

         case RATE_LIMITER:
            final int samplingInterval = wicaChannelProperties.getFilterSamplingIntervalInMillis();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='rate-limiter', samplingInterval='{}'", samplingInterval );
            filter = new WicaChannelValueRateLimitingFilter(samplingInterval );
            break;

         case LAST_N:
            final int numSamples = wicaChannelProperties.getFilterNumSamples();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='last-n', n='{}'", numSamples );
            filter = new WicaChannelValueLatestValueFilter(numSamples );
            break;

         case ONE_IN_M:
            final int cycleLength = wicaChannelProperties.getFilterCycleLength();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='one-in-m', m='{}'", cycleLength );
            filter = new WicaChannelValueFixedSamplingCycleFilter( cycleLength );
            break;

         case CHANGE_FILTERER:
            final double deadband = wicaChannelProperties.getFilterDeadband();
            logger.trace("Creating channel value filter for MONITORED channels with filterType='changes', deadband='{}'", deadband );
            filter = new WicaChannelValueNoiseRejectionFilter(deadband );
            break;

         default:
            logger.warn("The filterType parameter was not recognised. Using default (last-n) filter.");
            final int defaultMaxNumberOfSamples = 1;
            logger.trace("Creating channel value filter for MONITORED channels with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
            filter = new WicaChannelValueLatestValueFilter(defaultMaxNumberOfSamples );
            break;
      }

      return filter;
   }


   /**
    * Returns a filter suitable for a channels whose data acquisition mode includes polling.
    *
    * The filter that will be returned will be a fixed cycle ("one-in-m") sampler
    * whose cycle length (ie m) will be configured according to the configured
    * sampling ratio (taken either from the channel properties).
    *
    * @param wicaChannelProperties the channel properties object.
    * @return the filter.
    */
   private static WicaChannelValueFilter getPolledChannelsFilter( WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaChannelProperties );

      // Extract the ratio of number of channel polls per acquired sample
      final int cycleLength = wicaChannelProperties.getPolledValueSampleRatio();

      // Return the filter
      logger.trace("Creating channel value filter for POLLED channels with filterType='one-in-m', m='{}'", cycleLength );
      return new WicaChannelValueFixedSamplingCycleFilter( cycleLength );
   }


/*- Nested Classes -----------------------------------------------------------*/

}
