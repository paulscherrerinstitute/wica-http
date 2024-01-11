/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SuppressWarnings( "WeakerAccess" )
public class WicaChannelBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaChannelName wicaChannelName;
   private WicaChannelProperties wicaChannelProperties;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   // Private to force use of the create factory method.
   private WicaChannelBuilder() {}

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelBuilder create()
   {
      return new WicaChannelBuilder();
   }


/*- Public methods -----------------------------------------------------------*/

   public WicaChannelBuilder withChannelNameAndEmptyProperties( String wicaChannelName )
   {
      Validate.notNull( wicaChannelName,"The 'wicaChannelName' argument was null." );
      this.wicaChannelName = WicaChannelName.of( wicaChannelName );
      this.wicaChannelProperties = WicaChannelPropertiesBuilder.create().build();
      return this;
   }

   public WicaChannelBuilder withChannelNameAndDefaultProperties( String wicaChannelName )
   {
      Validate.notNull( wicaChannelName,"The 'wicaChannelName' argument was null." );
      this.wicaChannelName = WicaChannelName.of( wicaChannelName );
      this.wicaChannelProperties = WicaChannelPropertiesBuilder.create().withDefaultProperties().build();
      return this;
   }

   public WicaChannelBuilder withChannelNameAndProperties( String wicaChannelName, WicaChannelProperties wicaChannelProperties  )
   {
      Validate.notNull( wicaChannelName,"The 'wicaChannelName' argument was null." );
      this.wicaChannelName = WicaChannelName.of( wicaChannelName );
      this.wicaChannelProperties = Validate.notNull( wicaChannelProperties,"The 'wicaChannelProperties' argument was null." );
      return this;
   }

   public WicaChannel build()
   {
      return new WicaChannel( wicaChannelName, wicaChannelProperties );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
