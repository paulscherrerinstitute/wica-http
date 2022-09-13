/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the EPICS-specific implementation of a control system name.
 */
public class EpicsChannelName extends ControlSystemName
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new instance.
    *
    * @param channelName the name.
    */
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

   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
