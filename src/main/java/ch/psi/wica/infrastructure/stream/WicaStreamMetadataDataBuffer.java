/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelName;
import net.jcip.annotations.ThreadSafe;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStreamMetadataDataBuffer extends WicaStreamDataBuffer<ControlSystemName, WicaChannelMetadata>
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
   protected ControlSystemName getKeyFromChannelName( WicaChannelName wicaChannelName )
   {
      return  wicaChannelName.getControlSystemName();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
