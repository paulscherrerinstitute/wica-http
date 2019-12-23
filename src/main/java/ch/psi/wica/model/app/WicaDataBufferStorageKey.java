/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.util.Objects;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaDataBufferStorageKey
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaChannel wicaChannel;
   private int storageKey;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaDataBufferStorageKey( WicaChannel wicaChannel, int storageKey )
   {
      this.wicaChannel = wicaChannel;
      this.storageKey = storageKey;
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaDataBufferStorageKey getPolledValueStorageKey( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Optimisation Note:
      // The storage key for polling a channel is based on the channel's name, the
      // channel's polling interval and the polling mode (network-based or monitor based).
      // This means that resources can be shared between different subscribers when/if
      // these parameters are aligned.
      final int hashCode = (Objects.hash( wicaChannel.getName(),
                                          wicaChannel.getProperties().getOptionalPollingIntervalInMillis(),
                                          wicaChannel.getProperties().getDataAcquisitionMode() ) );

      return new WicaDataBufferStorageKey( wicaChannel, hashCode );
   }

   public static WicaDataBufferStorageKey getMonitoredValueStorageKey( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Optimisation Note:
      // The storage key for monitoring a channel is based only on the channel's name.
      // This means that resources can be shared when multiple wica stream subscribers
      // share the same parameters.
      final int hashCode = (Objects.hash( wicaChannel.getName().getControlSystemName() ) );
      return new WicaDataBufferStorageKey( wicaChannel, hashCode );
   }

/*- Public methods -----------------------------------------------------------*/

   public WicaChannel getWicaChannel()
   {
      return wicaChannel;
   }

   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaDataBufferStorageKey) ) return false;
      WicaDataBufferStorageKey that = (WicaDataBufferStorageKey) o;
      return storageKey == that.storageKey;
   }

   @Override
   public int hashCode()
   {
      return Objects.hash( storageKey );
   }

/*- Protected methods --------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
