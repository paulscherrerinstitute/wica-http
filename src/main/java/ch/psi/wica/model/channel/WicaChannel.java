/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


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

   public WicaChannel()
   {
      this.wicaChannelName = null;
      this.wicaChannelProperties = null;
   }

   /**
    * Create a new WicaChannel with the specified name and properties.
    *
    * @param wicaChannelName the name of the channel.
    * @param wicaChannelProperties the properties of the channel.
    */
   public WicaChannel( WicaChannelName wicaChannelName, WicaChannelProperties wicaChannelProperties )
   {
      final Logger logger = LoggerFactory.getLogger(WicaChannel.class );
      logger.trace( "Creating new WicaChannel with name '{}' and channel properties '{}'.", wicaChannelName, wicaChannelProperties );
      this.wicaChannelName = wicaChannelName;
      this.wicaChannelProperties = wicaChannelProperties;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String getNameAsString()
   {
      return wicaChannelName == null ? "" : wicaChannelName.asString();
   }

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
            ", wicaChannelProperties=" + wicaChannelProperties +
            '}';
   }

   // Note: The WicaChannel class generates VALUE objects which are considered equal if the fields match.
   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaChannel) ) return false;
      WicaChannel that = (WicaChannel) o;
      return Objects.equals(wicaChannelName, that.wicaChannelName) &&
            Objects.equals(wicaChannelProperties, that.wicaChannelProperties);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(wicaChannelName, wicaChannelProperties);
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
