/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannel;
import ch.psi.wica.model.WicaChannelProperties;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelValueMapperBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger logger = LoggerFactory.getLogger( WicaChannelValueMapperBuilder.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public static WicaChannelValueMapper createDefault()
   {
       logger.info( "Creating wicaChannelValueMapper for filterType=DEFAULT (=lastValue)" );
       return new WicaLastValueChannelValueMapper();
   }

   public static WicaChannelValueMapper createFromChannelProperties( WicaChannelProperties wicaChannelProperties )
   {

      // Return the default mapper if filterType nnot defined
      if ( ! wicaChannelProperties.hasProperty( "filterType" ) )
      {
         return createDefault();
      }

      switch ( wicaChannelProperties.getPropertyValue( "filterType" ) )
      {
         case "allValue":
         {
            logger.info( "Creating wicaChannelValueMapper for filterType=allValue" );
            return new WicaAllValueChannelValueMapper();
         }

         case "prec":
         {
            logger.info( "Creating wicaChannelValueMapper for filterType=precision" );
            Validate.isTrue( wicaChannelProperties.hasProperty("digits") );
            final int numberOfDigits = Integer.parseInt( wicaChannelProperties.getPropertyValue( "digits" ) );
            return new WicaPrecisionLimitingChannelValueMapper( numberOfDigits );
         }

         case "periodic":
         {
            logger.info( "Creating wicaChannelValueMapper for filterType=periodic" );
            Validate.isTrue( wicaChannelProperties.hasProperty("interval") );
            final int samplingInterval = Integer.parseInt( wicaChannelProperties.getPropertyValue( "interval" ) );
            return new WicaPeriodicSamplingChannelValueMapper( samplingInterval );
         }

         case "lastValue":
         {
            logger.info( "Creating wicaChannelValueMapper for filterType=lastValue" );
            return new WicaLastValueChannelValueMapper();
         }

         case "changes":
         {
            logger.info( "Creating wicaChannelValueMapper for filterType=changes" );
            Validate.isTrue( wicaChannelProperties.hasProperty(( "deadband") ) );
            final double deadband = Double.parseDouble( wicaChannelProperties.getPropertyValue(( "deadband" ) ) );
            return new WicaChangeFilteringChannelValueMapper( deadband );
         }

         default:
            logger.info( "Creating wicaChannelValueMapper for filterType=lastValue" );
            return new WicaLastValueChannelValueMapper();
      }

   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
