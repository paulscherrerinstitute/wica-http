/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.WicaDataBufferStorageKey;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStreamMetadataDataBuffer extends WicaStreamDataBuffer<WicaChannelMetadata>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamMetadataDataBuffer()
   {
      super( 1 );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Protected methods --------------------------------------------------------*/

   @Override
   protected WicaDataBufferStorageKey getStorageKey( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      return  WicaDataBufferStorageKey.getMonitoredValueStorageKey( wicaChannel );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
