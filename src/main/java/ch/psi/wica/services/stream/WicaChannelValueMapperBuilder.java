/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelProperties;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueMapperBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaChannelValueMapperBuilder.class );

   private static final int DEFAULT_PRECISION = 6;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a channel value mapper based on the supplied channel properties object.
    *
    * @param wicaChannelProperties the channel properties obhject.
    * @return the returned mapper.
    */
   public static WicaChannelValueMapper createFromChannelProperties( WicaChannelProperties wicaChannelProperties )
   {
      Validate.notNull( wicaChannelProperties );

      if ( wicaChannelProperties.hasProperty( "filterType" ) )
      {
         final String filterType = wicaChannelProperties.getPropertyValue("filterType" );
         switch ( filterType )
         {
            case "allValue":
            {
               logger.info("Creating wicaChannelValueMapper for filterType=allValue");
               return new WicaAllValueChannelValueMapper();
            }

           case "periodic":
            {
               logger.info("Creating wicaChannelValueMapper for filterType=periodic");
               Validate.isTrue(wicaChannelProperties.hasProperty("interval"));
               final int samplingInterval = Integer.parseInt(wicaChannelProperties.getPropertyValue("interval"));
               return new WicaPeriodicSamplingChannelValueMapper(samplingInterval);
            }

            case "changes":
            {
               logger.info("Creating wicaChannelValueMapper for filterType=changes");
               Validate.isTrue(wicaChannelProperties.hasProperty(("deadband")));
               final double deadband = Double.parseDouble(wicaChannelProperties.getPropertyValue(("deadband")));
               return new WicaChangeFilteringChannelValueMapper(deadband);
            }

            case "precision":
            {
               logger.info("Creating wicaChannelValueMapper for filterType=precision");
               Validate.isTrue(wicaChannelProperties.hasProperty("digits"));
               final int numberOfDigits = Integer.parseInt(wicaChannelProperties.getPropertyValue("digits"));
               return new WicaAllValuePrecisionLimitingChannelValueMapper(numberOfDigits);
            }

            default:
               logger.warn("The filterType parameter was not recognised. Using default mapper.");
               return createDefault( wicaChannelProperties );
         }
      }
      else
      {
         logger.warn( "The filterType parameter was not specified. Using default mapper." );
         return createDefault( wicaChannelProperties );
      }
   }

   /**
    * Returns the DEFAULT channel value mapper.
    *
    * @return the returned mapper.
    */
   public static WicaChannelValueMapper createDefault()
   {
      final WicaChannelProperties defaultProps = WicaChannelProperties.of( Map.of("prec", "6" ) );
      return createDefault( defaultProps );
   }

   /**
    * Returns the DEFAULT channel value mapper, modified, optionally by the
    * supplied properties object.
    *
    * @implNote
    *
    * The current implementation:
    * - transfers only the last value from the input list.
    * - precision limits WicaChannelType.REAL and WicaChannelType.REAL_ARRAY values.
    * - transfers all other values unchanged.
    */
   public static WicaChannelValueMapper createDefault( WicaChannelProperties wicaChannelProperties )
   {
      logger.info( "Creating default wicaChannelValueMapper" );
      if(  wicaChannelProperties.hasProperty("prec") )
      {
         final int numberOfDigits = Integer.parseInt(wicaChannelProperties.getPropertyValue("prec"));
         logger.info( "Precison property was found and set to {} digits.", numberOfDigits );
         return new WicaLastValuePrecisionLimitingChannelValueMapper( numberOfDigits );
      }
      else
      {
         logger.info( "Precison property NOT found. Set to default value of {} digits", DEFAULT_PRECISION );
         return new WicaLastValuePrecisionLimitingChannelValueMapper( DEFAULT_PRECISION );
      }
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

// TODO: clean up this failed attempt to dynamically build a processing chain
//       based on the channel properties...

//   public enum WicaChannelValueMappers
//   {
//      SAMPLE_ALL_VALUES( "allValue", WicaAllValueChannelValueMapper.class ),
//      SAMPLE_LAST_VALUE( "allValue", WicaAllValueChannelValueMapper.class ),
//      SAMPLE_PERIODIC  ( "periodic", WicaPeriodicSamplingChannelValueMapper.class ),
//      FILTER_CHANGES   ( "changes",  WicaChangeFilteringChannelValueMapper.class),
//      LIMIT_PRECISION  ( "prec",     WicaAllValuePrecisionLimitingChannelValueMapper.class );
//
//      private String propertyName;
//      private Class mapper;
//
//      WicaChannelValueMappers( String propertyName, Class mapperClass  )
//      {
//         this.propertyName = propertyName;
//         this.mapper = mapper;
//      }
//
//      public boolean isKnownProperty( String property )
//      {
//         WicaChannelValueMappers.valueOf()
//
//      }
//   }


}
