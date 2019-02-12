/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.services.stream.WicaChannelValueFilter;
import ch.psi.wica.services.stream.WicaChannelValueFilterBuilder;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannel implements WicaChannelValueFilter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaChannel.class );

   private final WicaChannelName wicaChannelName;
   private final WicaChannelProperties wicaChannelProperties;
   private final WicaChannelValueFilter wicaChannelValueFilter;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannel( WicaChannelName wicaChannelName )
   {
      logger.info( "Creating new wicaChannelValueFilter with DEFAULT properties." );

      this.wicaChannelName = wicaChannelName;
      this.wicaChannelProperties = new WicaChannelProperties();
      this.wicaChannelValueFilter = WicaChannelValueFilterBuilder.createDefault();
   }

   public WicaChannel( WicaChannelName wicaChannelName, WicaChannelProperties wicaChannelProperties )
   {
      logger.info( "Creating new wicaChannelValueFilter with properties '{}'", wicaChannelProperties );

      this.wicaChannelName = wicaChannelName;
      this.wicaChannelProperties = wicaChannelProperties;
      this.wicaChannelValueFilter = WicaChannelValueFilterBuilder.createFromChannelProperties( wicaChannelProperties );
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannel of( String wicaChannelName )
   {
      return new WicaChannel( WicaChannelName.of( wicaChannelName ) );
   }

   public static WicaChannel of( WicaChannelName wicaChannelName )
   {
      return new WicaChannel( wicaChannelName );
   }

/*- Public methods -----------------------------------------------------------*/

   public WicaChannelName getName()
   {
      return wicaChannelName;
   }

   public WicaChannelProperties getProperties()
   {
      return wicaChannelProperties;
   }

   @Override
   public List<WicaChannelValue> apply( List<WicaChannelValue> inputList )
   {
      return wicaChannelValueFilter.apply(inputList );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
