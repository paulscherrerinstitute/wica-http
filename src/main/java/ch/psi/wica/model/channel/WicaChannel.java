/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannel
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelName wicaChannelName;
   private final WicaChannelProperties wicaChannelProperties;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Create a new WicaChannel with the specified name and properties.
    *
    * @param wicaChannelName the name of the channel.
    * @param wicaChannelProperties the properties of the channel.
    */
   private WicaChannel( WicaChannelName wicaChannelName, WicaChannelProperties wicaChannelProperties )
   {
      final Logger logger = LoggerFactory.getLogger(WicaChannel.class );
      logger.trace( "Creating new WicaChannel with name {} and channel properties {}.", wicaChannelName, wicaChannelProperties );
      this.wicaChannelName = wicaChannelName;
      this.wicaChannelProperties = wicaChannelProperties;
   }

/*- Class methods ------------------------------------------------------------*/

   /**
    * Returns an instance with default channel properties based on the name
    * supplied as a string.
    *
    * @param wicaChannelNameAsString the string representation of the name.
    * @return the instance.
    */
   public static WicaChannel createFromName( String wicaChannelNameAsString )
   {
      return createFromName( WicaChannelName.of( wicaChannelNameAsString )) ;
   }

   /**
    * Returns an instance with default channel properties based on the name
    * supplied as an object.
    *
    * @param wicaChannelName the channel name.
    * @return the instance.
    */
   public static WicaChannel createFromName( WicaChannelName wicaChannelName )
   {
      return new WicaChannel( wicaChannelName, WicaChannelProperties.createDefaultInstance() ) ;
   }

   /**
    * Returns an instance with the specified properties based on the name
    * supplied as a string.
    *
    * @param wicaChannelNameAsString the string representation of the name.
    * @return the instance.
    */
   public static WicaChannel createFromNameAndProperties( String wicaChannelNameAsString, WicaChannelProperties wicaChannelProperties )
   {
      return createFromNameAndProperties( WicaChannelName.of( wicaChannelNameAsString), wicaChannelProperties );
   }

   /**
    * Returns an instance with the specified properties based on the name
    * supplied as a string.
    *
    * @param wicaChannelName the channel name.
    * @return the instance.
    */
   public static WicaChannel createFromNameAndProperties( WicaChannelName wicaChannelName, WicaChannelProperties wicaChannelProperties )
   {
      return new WicaChannel( wicaChannelName, wicaChannelProperties ) ;
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
   public String toString()
   {
      return "WicaChannel{" +
            "wicaChannelName=" + wicaChannelName +
            '}';
   }

   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaChannel) ) return false;
      WicaChannel that = (WicaChannel) o;
      return Objects.equals(wicaChannelName, that.wicaChannelName);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(wicaChannelName);
   }

   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
