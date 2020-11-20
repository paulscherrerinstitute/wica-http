/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica.model.channel.WicaChannel;
import org.apache.commons.lang3.Validate;

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
      this( EpicsChannelName.of( Validate.notNull( wicaChannel ).getName().getControlSystemName() ), wicaChannel );
   }

   EpicsChannelMonitoringRequest( EpicsChannelName epicsChannelName, WicaChannel publicationChannel )
   {
      this.epicsChannelName = Validate.notNull( epicsChannelName );
      this.publicationChannel = Validate.notNull( publicationChannel );
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

   // To enforce the efficient use of resources the EpicsChannelMonitoringService
   // will reject requests which would result in multiple monitors being placed
   // on the same EPICS channel. This is conveniently achieved by rejecting
   // requests which are "the same".
   //
   // The rule below establishes that requests are considered the same if they
   // refer to the same EPICS channel name.
   //
   // Note: to avoid internal conflicts the higher level components of the
   // application (currently WicaStreamMonitoredValueRequesterService and
   // WicareamMonitoredValueCollectorService) must be designed to ensure
   // that the constraint described above is not violated.

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

      return epicsChannelName.equals( that.epicsChannelName );
   }

   @Override
   public int hashCode()
   {
      return epicsChannelName.hashCode();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
