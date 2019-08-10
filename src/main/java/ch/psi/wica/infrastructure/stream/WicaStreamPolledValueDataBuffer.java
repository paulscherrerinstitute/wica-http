/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStreamPolledValueDataBuffer extends WicaStreamDataBuffer<WicaChannel, WicaChannelValue>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamPolledValueDataBuffer( int bufferSize  )
   {
      super( bufferSize );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Protected methods --------------------------------------------------------*/

   @Override
   protected WicaChannel getStorageKeyForChannel( WicaChannel wicaChannel )
   {
      return wicaChannel;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
