/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
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
      return WicaChannelValueFilterBuilder.createFromChannelProperties( wicaChannelProperties );
   }

   /**
    * Returns a filter based on the supplied channel properties object.
    *
    * @param wicaChannelProperties the channel properties object.
    * @return the filter.
    */
   public static WicaChannelValueFilter createFromChannelProperties( WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaChannelProperties );

      final WicaChannelValueFilter filter;
      switch ( wicaChannelProperties.getFilterType() )
      {
         case ALL_VALUE:
            logger.info("Creating channel filter with filterType='all-value'");
            filter = new WicaChannelValueFilterAllValueSampler();
            break;

         case RATE_LIMITER:
            final int sampleGap = wicaChannelProperties.getInterval();
            logger.info("Creating channel filter with filterType='rate-limiter', interval='{}'", sampleGap );
            filter = new WicaChannelValueFilterRateLimitingSampler( sampleGap );
            break;

         case ONE_IN_N:
            final int cycleLength = wicaChannelProperties.getN();
            logger.info("Creating channel filter with filterType='one-in-n', n='{}'", cycleLength );
            filter =  new WicaChannelValueFilterFixedCycleSampler( cycleLength );
            break;

         case LAST_N:
            final int numSamples = wicaChannelProperties.getN();
            logger.info("Creating channel filter with filterType='last-n', n='{}'", numSamples );
            filter =  new WicaChannelValueFilterLatestValueSampler( numSamples );
            break;

         case CHANGE_FILTERER:
            final double deadband = wicaChannelProperties.getDeadband();
            logger.info("Creating channel filter with filterType='change-filterer', deadband='{}'", deadband );
            filter =  new WicaChannelValueFilterChangeFilteringSampler(deadband );
            break;

         default:
            logger.warn("The filterType parameter was not recognised. Using default (last-n) filter.");
            final int defaultMaxNumberOfSamples = 1;
            logger.info("Creating channel filter with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
            filter = new WicaChannelValueFilterLatestValueSampler(defaultMaxNumberOfSamples );
            break;
      }

      return filter;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
