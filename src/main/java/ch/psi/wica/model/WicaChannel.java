/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.services.stream.WicaChannelValueMapper;
import ch.psi.wica.services.stream.WicaChannelValueMapperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannel implements WicaChannelValueMapper
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaChannel.class );

   private WicaChannelName wicaChannelName;
   private WicaChannelProperties wicaChannelProperties;
   private WicaChannelValueMapper wicaChannelValueMapper;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannel( WicaChannelName wicaChannelName )
   {
      logger.info( "Creating new wicaChannelValueMapper with DEFAULT properties." );

      this.wicaChannelName = wicaChannelName;
      this.wicaChannelProperties = WicaChannelProperties.ofEmpty();
      this.wicaChannelValueMapper = WicaChannelValueMapperBuilder.createDefault();
   }

   public WicaChannel( WicaChannelName wicaChannelName, WicaChannelProperties wicaChannelProperties )
   {
      logger.info( "Creating new wicaChannelValueMapper with properties '{}'", wicaChannelProperties );

      this.wicaChannelName = wicaChannelName;
      this.wicaChannelProperties = wicaChannelProperties;
      this.wicaChannelValueMapper = WicaChannelValueMapperBuilder.createFromChannelProperties( wicaChannelProperties );
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
   public List<WicaChannelValue> map( List<WicaChannelValue> inputList )
   {
      return wicaChannelValueMapper.map( inputList );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
