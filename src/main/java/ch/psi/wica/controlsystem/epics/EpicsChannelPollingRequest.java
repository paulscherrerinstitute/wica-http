/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import org.apache.commons.lang3.Validate;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class EpicsChannelPollingRequest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final EpicsChannelName epicsChannelName;
   private final long pollingInterval;
   private final WicaChannel publicationChannel;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelPollingRequest( WicaChannel wicaChannel )
   {
      this( EpicsChannelName.of( Validate.notNull( wicaChannel.getName().getControlSystemName() ) ), wicaChannel.getProperties().getPollingIntervalInMillis(), wicaChannel );
   }

   EpicsChannelPollingRequest( EpicsChannelName epicsChannelName, long pollingIntervalInMillis, WicaChannel publicationChannel )
   {
      this.epicsChannelName = Validate.notNull( epicsChannelName );
      this.pollingInterval = pollingIntervalInMillis;
      this.publicationChannel = Validate.notNull( publicationChannel );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public EpicsChannelName getEpicsChannelName()
   {
      return epicsChannelName;
   }

   public long getPollingInterval()
   {
      return pollingInterval;
   }

   public WicaChannel getPublicationChannel()
   {
      return publicationChannel;
   }

   @Override
   public String toString()
   {
      return "Poller<" + epicsChannelName + "," + pollingInterval + '>';
   }

   // To enforce the efficient use of resources the EpicsChannelPollingService
   // will reject requests which would result in multiple pollers operating at
   // the same polling rates being placed on the same EPICS channel. This is
   // conveniently achieved by rejecting requests which are "the same".
   //
   // The rule below establishes that polling requests are considered the same
   // if they refer to the same EPICS channel name operating at the same polling
   // interval.
   //
   // Note: to avoid internal conflicts the higher level components of the
   // application (currently WicaStreamPolledValueRequesterService and
   // WicaStreamPolledValueCollectorService) must be designed to ensure
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

      EpicsChannelPollingRequest that = (EpicsChannelPollingRequest) o;

      if ( pollingInterval != that.pollingInterval )
      {
         return false;
      }
      return epicsChannelName.equals( that.epicsChannelName );
   }

   @Override
   public int hashCode()
   {
      int result = epicsChannelName.hashCode();
      result = 31 * result + (int) (pollingInterval ^ (pollingInterval >>> 32));
      return result;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
