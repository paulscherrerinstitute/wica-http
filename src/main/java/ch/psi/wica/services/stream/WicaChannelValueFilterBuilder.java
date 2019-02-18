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
    * Returns a filter based on the default channel properties.
    *
    * @return the filter
    */
   public static WicaChannelValueFilter createDefault()
   {
      final WicaChannelProperties wicaChannelProperties = new WicaChannelProperties();
      return WicaChannelValueFilterBuilder.createFilterForMonitoredChannels(wicaChannelProperties );
   }

   /**
    * Returns a filter suitable for channels whose data acquisition mode includes monitoring.
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
            logger.info("Creating channel filter with filterType='allValueSampler'");
            filter = new WicaChannelValueFilterAllValueSampler();
            break;

         case RATE_LIMITER:
            final int minSampleGap = wicaChannelProperties.getFilterMinSampleGapInMillis();
            logger.info("Creating channel filter with filterType='rate-limiter', minSampleGap='{}'", minSampleGap );
            filter = new WicaChannelValueFilterRateLimitingSampler( minSampleGap );
            break;

         case ONE_IN_N:
            final int cycleLength = wicaChannelProperties.getFilterCycleLength();
            logger.info("Creating channel filter with filterType='one-in-n', n='{}'", cycleLength );
            filter =  new WicaChannelValueFilterFixedCycleSampler( cycleLength );
            break;

         case LAST_N:
            final int numSamples = wicaChannelProperties.getFilterNumSamples();
            logger.info("Creating channel filter with filterType='last-n', n='{}'", numSamples );
            filter =  new WicaChannelValueFilterLatestValueSampler( numSamples );
            break;

         case CHANGE_FILTERER:
            final double deadband = wicaChannelProperties.getFilterDeadband();
            logger.info("Creating channel filter with filterType='change-filterer', deadband='{}'", deadband );
            filter =  new WicaChannelValueFilterChangeFilteringSampler(deadband );
            break;

         default:
            logger.warn("The filterType parameter was not recognised. Using default (last-n) filter.");
            final int defaultMaxNumberOfSamples = 1;
            logger.info("Creating channel filter with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
            filter = new WicaChannelValueFilterLatestValueSampler( defaultMaxNumberOfSamples );
            break;
      }

      return filter;
   }

   /**
    * Returns a filter suitable for channels whose data acquisition mode includes polling.
    *
    * @param wicaChannelProperties the channel properties object
    *
    * @return the filter.
    */
   public static WicaChannelValueFilter createFilterForPolledChannels( WicaStreamProperties wicaStreamProperties, WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaStreamProperties );
      Validate.notNull( wicaChannelProperties );

      final int valuePollFluxIntervalInMillis = wicaStreamProperties.getValuePollFluxIntervalInMillis();
      final int channelPollingIntervalInMillis = wicaChannelProperties.getPollingIntervalInMillis();
      final int cycleLength = channelPollingIntervalInMillis / valuePollFluxIntervalInMillis;
      logger.info("Creating channel filter with filterType='one-in-n', n='{}'", cycleLength );
      return new WicaChannelValueFilterFixedCycleSampler( cycleLength );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
