/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica.model.channel.WicaChannel;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class EpicsChannelMonitoringRequest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final EpicsChannelName epicsChannelName;
   private final WicaChannel publicationChannel;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelMonitoringRequest( WicaChannel wicaChannel )
   {
      this( EpicsChannelName.of( wicaChannel.getName().getControlSystemName() ), wicaChannel );
   }

   EpicsChannelMonitoringRequest( EpicsChannelName epicsChannelName, WicaChannel publicationChannel )
   {
      this.epicsChannelName = epicsChannelName;
      this.publicationChannel = publicationChannel;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public EpicsChannelName getEpicsChannelName()
   {
      return epicsChannelName;
   }

   public WicaChannel getPublicationChannel()
   {
      return publicationChannel;
   }

   @Override
   public String toString()
   {
      return "Monitor<" + epicsChannelName + '>';
   }

   // The EpicsChannelMonitoringService will not accept two requests which are equal.
   // The rule here establishes that the requests are considered the same if they
   // refer to the same EPICS channel name.
   @Override
   public boolean equals( Object o )
   {
      if ( this == o )
      {
         return true;
      }
      if ( o == null || getClass() != o.getClass() )
      {
         return false;
      }

      EpicsChannelMonitoringRequest that = (EpicsChannelMonitoringRequest) o;

      return epicsChannelName != null ? epicsChannelName.equals( that.epicsChannelName ) : that.epicsChannelName == null;
   }

   @Override
   public int hashCode()
   {
      return epicsChannelName != null ? epicsChannelName.hashCode() : 0;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
