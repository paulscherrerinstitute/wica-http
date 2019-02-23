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
public class WicaChannel
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaChannel.class );
   private final WicaChannelName wicaChannelName;
   private final WicaChannelProperties wicaChannelProperties;
   private final WicaChannelValueFilter wicaChannelValueFilterForMonitoredChannels;
   private final WicaChannelValueFilter wicaChannelValueFilterForPolledChannels;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Create a new WicaChannel with the specified name and the specified stream properties.
    *
    * @param wicaChannelName the name of the channel.
    * @param wicaStreamProperties the properties of the stream with which this channel is associated.
    */
   public WicaChannel( WicaChannelName wicaChannelName, WicaStreamProperties wicaStreamProperties )
   {
      this( wicaChannelName, wicaStreamProperties, new WicaChannelProperties() );
   }

   /**
    * Create a new WicaChannel with the specified name, the specified stream properties and
    * the specified channel properties.
    *
    * @param wicaChannelName the name of the channel.
    * @param wicaStreamProperties the properties of the stream with which this channel is associated.
    * @param wicaChannelProperties the properties of this channel.
    */
   public WicaChannel( WicaChannelName wicaChannelName, WicaStreamProperties wicaStreamProperties, WicaChannelProperties wicaChannelProperties )
   {
      logger.info( "Creating new WicaChannel with name {}, stream properties {} and channel properties {}.", wicaChannelName,
                    wicaStreamProperties, wicaChannelProperties );

      this.wicaChannelName = wicaChannelName;
      this.wicaChannelProperties = wicaChannelProperties;
      this.wicaChannelValueFilterForMonitoredChannels = WicaChannelValueFilterBuilder.createFilterForMonitoredChannels( wicaChannelProperties );
      this.wicaChannelValueFilterForPolledChannels = WicaChannelValueFilterBuilder.createFilterForPolledChannels( wicaStreamProperties,
                                                                                                                  wicaChannelProperties );
   }

/*- Class methods ------------------------------------------------------------*/

   /**
    * Returns a constructed channel from a string with default properties.
    *
    * @param wicaChannelName the channel's name.
    * @return the channel.
    */
   public static WicaChannel of( String wicaChannelName )
   {
      return new WicaChannel( WicaChannelName.of( wicaChannelName ), new WicaStreamProperties() );
   }

   /**
    * Returns a constructed channel from a channel name with default properties.
    *
    * @param wicaChannelName the channel's name.
    * @return the channel.
    */
   public static WicaChannel of( WicaChannelName wicaChannelName )
   {
      return new WicaChannel( wicaChannelName, new WicaStreamProperties() );
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

   public List<WicaChannelValue> applyFilterForPolledChannels( List<WicaChannelValue> inputList )
   {
      return wicaChannelValueFilterForPolledChannels.apply(inputList );
   }

   public List<WicaChannelValue> applyFilterForMonitoredChannels( List<WicaChannelValue> inputList )
   {
      return wicaChannelValueFilterForMonitoredChannels.apply(inputList );
   }

   @Override
   public String toString()
   {
      return "WicaChannel{" +
            "wicaChannelName=" + wicaChannelName +
            '}';
   }
   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
