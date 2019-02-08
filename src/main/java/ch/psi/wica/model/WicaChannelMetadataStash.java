/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a store for the metadata received from one or more Wica channels.
 *
 * Unless explicitly stated otherwise in the javadoc all methods which
 * take object arguments will throw NullPointerException in the case
 * that a non null argument is passed.
 */
@ThreadSafe
public class WicaChannelMetadataStash
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Map<WicaChannelName, WicaChannelMetadata> stash;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new stash.
    */
   public WicaChannelMetadataStash()
   {
      stash = Collections.synchronizedMap( new HashMap<>() );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Updates the metadata associated with the specified channel.
    *
    * @param wicaChannelName the channel's name.
    * @param wicaChannelMetadata the channel's metadata.
    */
   public void put( WicaChannelName wicaChannelName, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull( wicaChannelName );
      Validate.notNull( wicaChannelMetadata );

      stash.put(wicaChannelName, wicaChannelMetadata );
   }

   /**
    * Gets the metadata for the specified channel (which must already exist
    * in the stash).
    *
    * @param wicaChannelName the channel of interest.
    * @return the channel's metadata.
    *
    * @throws IllegalStateException if the stash has no previously stored
    *         metadata for this channel.
    */
   public WicaChannelMetadata get( WicaChannelName wicaChannelName )
   {
      Validate.notNull( wicaChannelName );
      Validate.validState( stash.containsKey( wicaChannelName ), "no metadata for channel with name: ", wicaChannelName );
      return stash.get( wicaChannelName );
   }

   /**
    * Gets the metadata for the specified stream (whose channel metadata
    * must already exist in the stash).
    *
    * @param wicaChannels the channels of interest.
    * @return the stream's metadata.
    *
    * @throws IllegalStateException if the stash has no metadata for
    *         one or more channel's in the stream.
    */
   public Map<WicaChannelName,WicaChannelMetadata> get( Set<WicaChannel> wicaChannels )
   {
      Validate.notNull( wicaChannels );
      Validate.validState( wicaChannels.stream().anyMatch( c -> stash.containsKey( c.getName() ) ), "no metadata for one or more channels");

      return wicaChannels.stream().collect( Collectors.toMap( WicaChannel::getName, c -> stash.get(c.getName() ) ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
