/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a store for the metadata received from one or more control
 * system channels.
 *
 * Unless explicitly stated otherwise in the javadoc all methods which
 * take object arguments will throw NullPointerException in the case
 * that a non null argument is passed.
 */
@Service
@ThreadSafe
public class WicaChannelMetadataStash
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * Stash of names in the controls system and their most recently
    * obtained metadata.
    */
   private final Map<ControlSystemName, WicaChannelMetadata> stash;


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
    * @param controlSystemName the name as known in the control system.
    * @param wicaChannelMetadata the channel's metadata.
    */
   public void put( ControlSystemName controlSystemName, WicaChannelMetadata wicaChannelMetadata )
   {
      Validate.notNull( controlSystemName );
      Validate.notNull( wicaChannelMetadata );

      stash.put( controlSystemName, wicaChannelMetadata );
   }

   /**
    * Gets the metadata for the specified stream (whose channel metadata must already exist in the stash).
    *
    * @param wicaChannels the channels of interest.
    * @return the stream's metadata.
    *
    * @throws IllegalStateException if the stash has no metadata for one or more channel's in the stream.
    */
   public Map<WicaChannelName,WicaChannelMetadata> get( Set<WicaChannel> wicaChannels )
   {
      Validate.notNull( wicaChannels );
      Validate.validState( wicaChannels.stream().anyMatch( c -> stash.containsKey( c.getName().getControlSystemName() ) ), "no metadata for one or more channels");

      return wicaChannels.stream().collect( Collectors.toMap( WicaChannel::getName, c -> stash.get( c.getName().getControlSystemName() ) ) );
   }

   /**
    * Gets the metadata for the specified channel (which must already exist in the stash).
    *
    * @param controlSystemName the name as known in the control system.
    * @return the channel's metadata.
    *
    * @throws IllegalStateException if the stash has no previously stored
    *         metadata for this channel.
    */
   @SuppressWarnings( "unused" )
   public WicaChannelMetadata get( ControlSystemName controlSystemName )
   {
      Validate.notNull( controlSystemName );
      Validate.validState( stash.containsKey( controlSystemName ), "no metadata for channel with name: ", controlSystemName );
      return stash.get( controlSystemName );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
