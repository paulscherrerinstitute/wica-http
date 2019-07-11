/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.ControlSystemName;

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
   public boolean equals( Object o )
   {
      return super.equals(o);
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
