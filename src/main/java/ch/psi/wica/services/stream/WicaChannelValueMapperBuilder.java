/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
import ch.psi.wica.model.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueMapperBuilder implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaChannelValueMapperBuilder.class );
   private static final int DEFAULT_PRECISION = 6;

   private final WicaChannelValueMapper precisionMapper;
   private final WicaChannelValueMapper filteringMapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns an instance based on the supplier mappers.
    *
    * @param filteringMapper the filtering mapper that will be applied to the initial input list.
    * @param precisionMapper the precision mapper that is to be applied to the final result.
    */
   private WicaChannelValueMapperBuilder( WicaChannelValueMapper filteringMapper, WicaChannelValueMapper precisionMapper )
   {
      this.precisionMapper = precisionMapper;
      this.filteringMapper = filteringMapper;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public static WicaChannelValueMapperBuilder createDefault()
   {
      WicaChannelProperties wicaChannelProperties = WicaChannelProperties.ofEmpty();
      return WicaChannelValueMapperBuilder.createFromChannelProperties( wicaChannelProperties );
   }


   /**
    * Returns a channel value mapper based on the supplied channel properties object.
    *
    * @param wicaChannelProperties the channel properties object.
    * @return the returned mapper.
    */
   public static WicaChannelValueMapperBuilder createFromChannelProperties( WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaChannelProperties );

      final WicaChannelValueMapper precisionMapper;
      if ( wicaChannelProperties.hasProperty( "prec" ) )
      {
         final int numberOfDigits = Integer.parseInt( wicaChannelProperties.getPropertyValue("prec") );
         logger.info( "Creating precision mapper with prec='{}'", numberOfDigits );
         precisionMapper = new WicaChannelValueMapperPrecisionLimitingSampler(numberOfDigits );
      }
      else
      {
         precisionMapper = new WicaChannelValueMapperPrecisionLimitingSampler(DEFAULT_PRECISION );
      }

      final WicaChannelValueMapper filteringMapper;
      if ( wicaChannelProperties.hasProperty( "filterType" ) )
      {
         final String filterType = wicaChannelProperties.getPropertyValue("filterType" );
         switch ( filterType )
         {
            case "all-value":
               logger.info("Creating channel filter with filterType='all-value'");
               filteringMapper = new WicaChannelValueMapperAllValueSampler();
               break;

            case "rate-limiter":
               Validate.isTrue(wicaChannelProperties.hasProperty("interval"));
               final int sampleGap = Integer.parseInt(wicaChannelProperties.getPropertyValue("interval") );
               logger.info("Creating channel filter with filterType='rate-limiter', interval='{}'", sampleGap );
               filteringMapper = new WicaChannelValueMapperRateLimitingSampler(sampleGap);
               break;

            case "one-in-n":
               Validate.isTrue(wicaChannelProperties.hasProperty("n"));
               final int cycleLength = Integer.parseInt(wicaChannelProperties.getPropertyValue("n") );
               logger.info("Creating channel filter with filterType='one-in-n', n='{}'", cycleLength );
               filteringMapper =  new WicaChannelValueMapperFixedCycleSampler( cycleLength );
               break;

            case "last-n":
               Validate.isTrue( wicaChannelProperties.hasProperty("n"));
               final int numSamples = Integer.parseInt(wicaChannelProperties.getPropertyValue("n") );
               logger.info("Creating channel filter with filterType='last-n', n='{}'", numSamples );
               filteringMapper =  new WicaChannelValueMapperLatestValueSampler( numSamples );
               break;

            case "change-filterer":
               Validate.isTrue(wicaChannelProperties.hasProperty(("deadband")));
               final double deadband = Double.parseDouble( wicaChannelProperties.getPropertyValue(( "deadband" )));
               logger.info("Creating channel filter with filterType='change-filterer', deadband='{}'", deadband );
               filteringMapper =  new WicaChannelValueMapperChangeFilteringSampler(deadband );
               break;

            default:
               logger.warn("The filterType parameter was not recognised. Using default (last-n) filter.");
               final int defaultMaxNumberOfSamples = 1;
               logger.info("Creating channel filter with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
               filteringMapper = new WicaChannelValueMapperLatestValueSampler(defaultMaxNumberOfSamples );
               break;
         }
      }
      else
      {
         logger.warn( "The filterType parameter was not specified. Using default (last-n) filter." );
         final int defaultMaxNumberOfSamples = 1;
         logger.info("Creating channel filter with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
         filteringMapper = new WicaChannelValueMapperLatestValueSampler(defaultMaxNumberOfSamples );
      }

      return new WicaChannelValueMapperBuilder( precisionMapper, filteringMapper );
   }

   /**
    * @inheritDoc
    */
   @Override
   public List<WicaChannelValue> map( List<WicaChannelValue> inputList )
   {
      return precisionMapper.map( filteringMapper.map( inputList ) );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
