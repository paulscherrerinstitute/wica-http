/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.ControlSystemName;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class EpicsChannelName extends ControlSystemName
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelName( String channelName )
   {
      super( channelName );
   }

/*- Class methods ------------------------------------------------------------*/

   public static EpicsChannelName of( String channelName )
   {
      return new EpicsChannelName( channelName );
   }

   public static EpicsChannelName of( ControlSystemName controlSystemName )
   {
      return of( controlSystemName.asString() );
   }

/*- Public methods -----------------------------------------------------------*/

   @Override
   public String asString()
   {
      return super.asString();
   }

   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
