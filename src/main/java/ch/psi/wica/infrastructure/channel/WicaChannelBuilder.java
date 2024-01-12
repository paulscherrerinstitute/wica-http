/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

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

   /**
    * Creates a new WicaChannelBuilder instance ready for further customisations.
    *
    * @return the instance.
    */
   public static WicaChannelBuilder create()
   {
      return new WicaChannelBuilder();
   }


/*- Public methods -----------------------------------------------------------*/

   /**
    * Creates a new WicaChannelBuilder instance with the specified channel name and empty properties.
    *
    * @param wicaChannelName the channel name.
    */
   public WicaChannelBuilder withChannelNameAndEmptyProperties( String wicaChannelName )
   {
      Validate.notNull( wicaChannelName,"The 'wicaChannelName' argument was null." );
      this.wicaChannelName = WicaChannelName.of( wicaChannelName );
      this.wicaChannelProperties = WicaChannelPropertiesBuilder.create().build();
      return this;
   }

   /**
    * Creates a new WicaChannelBuilder instance with the specified channel name and default properties.
    *
    * @param wicaChannelName the channel name.
    */
   public WicaChannelBuilder withChannelNameAndDefaultProperties( String wicaChannelName )
   {
      Validate.notNull( wicaChannelName,"The 'wicaChannelName' argument was null." );
      this.wicaChannelName = WicaChannelName.of( wicaChannelName );
      this.wicaChannelProperties = WicaChannelPropertiesBuilder.create().withDefaultProperties().build();
      return this;
   }

   /**
    * Creates a new WicaChannelBuilder instance with the specified channel name and properties.
    *
    * @param wicaChannelName the channel name.
    * @param wicaChannelProperties the channel properties.
    * @return the instance.
    */
   public WicaChannelBuilder withChannelNameAndProperties( String wicaChannelName, WicaChannelProperties wicaChannelProperties  )
   {
      Validate.notNull( wicaChannelName,"The 'wicaChannelName' argument was null." );
      this.wicaChannelName = WicaChannelName.of( wicaChannelName );
      this.wicaChannelProperties = Validate.notNull( wicaChannelProperties,"The 'wicaChannelProperties' argument was null." );
      return this;
   }

   /**
    * Builds the Wica Channel instance.
    *.
    * @return the constructed WicaChannel.
    */
   public WicaChannel build()
   {
      return new WicaChannel( wicaChannelName, wicaChannelProperties );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
