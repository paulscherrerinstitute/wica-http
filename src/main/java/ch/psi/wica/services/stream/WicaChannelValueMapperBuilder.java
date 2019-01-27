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
         precisionMapper = new WicaPrecisionLimitingChannelValueMapper( numberOfDigits );
      }
      else
      {
         precisionMapper = new WicaPrecisionLimitingChannelValueMapper( DEFAULT_PRECISION );
      }

      final WicaChannelValueMapper filteringMapper;
      if ( wicaChannelProperties.hasProperty( "filterType" ) )
      {
         final String filterType = wicaChannelProperties.getPropertyValue("filterType" );
         switch ( filterType )
         {
            case "allValue":
               logger.info("Creating filtering mapper with filterType='allValue'");
               filteringMapper = new WicaAllValueChannelValueMapper();
               break;

            case "periodic":
               Validate.isTrue(wicaChannelProperties.hasProperty("ms"));
               final int samplingInterval = Integer.parseInt(wicaChannelProperties.getPropertyValue("ms") );
               logger.info("Creating filtering mapper with filterType='periodic', ms='{}'", samplingInterval );
               filteringMapper = new WicaRateLimitedSamplingChannelValueMapper(samplingInterval);
               break;

            case "1-in-n":
               Validate.isTrue(wicaChannelProperties.hasProperty("n"));
               final int samplingCycleLength = Integer.parseInt(wicaChannelProperties.getPropertyValue("n") );
               logger.info("Creating filtering mapper with filterType='1-in-n', n='{}'", samplingCycleLength );
               filteringMapper =  new WicaDiscreteSamplingChannelValueMapper( samplingCycleLength );
               break;

            case "last-n":
               Validate.isTrue( wicaChannelProperties.hasProperty("n"));
               final int maxNumberOfSamples = Integer.parseInt(wicaChannelProperties.getPropertyValue("n") );
               logger.info("Creating filtering mapper with filterType='last-n', n='{}'", maxNumberOfSamples );
               filteringMapper =  new WicaLatestValueChannelValueMapper( maxNumberOfSamples );
               break;

            case "changes":
               logger.info("Creating filtering mapper with filterType=changes");
               Validate.isTrue(wicaChannelProperties.hasProperty(("deadband")));
               final double deadband = Double.parseDouble( wicaChannelProperties.getPropertyValue(( "deadband" )));
               logger.info("Creating filtering mapper with filterType='changes', deadband='{}'", deadband );
               filteringMapper =  new WicaChangeFilteringChannelValueMapper( deadband );
               break;

            default:
               logger.warn("The filterType parameter was not recognised. Using default (last-n) mapper.");
               final int defaultMaxNumberOfSamples = 1;
               logger.info("Creating filtering mapper with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
               filteringMapper = new WicaLatestValueChannelValueMapper( defaultMaxNumberOfSamples );
               break;
         }
      }
      else
      {
         logger.warn( "The filterType parameter was not specified. Using default (last-n) mapper." );
         final int defaultMaxNumberOfSamples = 1;
         logger.info("Creating filtering mapper with filterType='last-n', n='{}'", defaultMaxNumberOfSamples );
         filteringMapper = new WicaLatestValueChannelValueMapper( defaultMaxNumberOfSamples );
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
