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

   private final WicaChannel wicaChannel;
   private final int storageKey;

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
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );

      // Optimisation Note:
      // The storage key for saving data obtained by channel polling is based on ALL the properties
      // of the Wica Channel. This means that each Wica Channel is represented uniquely in the
      // polled value data stache.
      final int hashCode = (Objects.hash( wicaChannel ));
      return new WicaDataBufferStorageKey( wicaChannel, hashCode );
   }

   public static WicaDataBufferStorageKey getMonitoredValueStorageKey( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Optimisation Note:
      // The storage key for saving data obtained by channel monitoring is based on ALL the properties
      // of the Wica Channel. This means that each Wica Channel is represented uniquely in the
      // monitored value data stache.
      final int hashCode = (Objects.hash( wicaChannel ));
      return new WicaDataBufferStorageKey( wicaChannel, hashCode );
   }

   public static WicaDataBufferStorageKey getMetadataStorageKey( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument is null." );

      // Optimisation Note:
      // The storage key for saving metadata is based on ALL the properties of the Wica Channel.
      // This means that each Wica Channel is represented uniquely in the metadata stache.
      final int hashCode = (Objects.hash( wicaChannel ));
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
