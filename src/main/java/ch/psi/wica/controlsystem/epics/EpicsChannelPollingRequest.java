/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica.model.channel.WicaChannel;

import java.util.concurrent.TimeUnit;

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
      this( EpicsChannelName.of( wicaChannel.getNameAsString() ), wicaChannel.getProperties().getPollingIntervalInMillis(), wicaChannel );
   }

   EpicsChannelPollingRequest( EpicsChannelName epicsChannelName, long pollingIntervalInMillis, WicaChannel publicationChannel )
   {
      this.epicsChannelName = epicsChannelName;
      this.pollingInterval = pollingIntervalInMillis;
      this.publicationChannel = publicationChannel;
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

   // The EpicsChannelPollingService will not accept two requests which are equal.
   // The rule here establishes that the requests are considered the same if they
   // refer to the same EPICS channel name AND the same polling interval.
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
      return epicsChannelName != null ? epicsChannelName.equals( that.epicsChannelName ) : that.epicsChannelName == null;
   }

   @Override
   public int hashCode()
   {
      int result = epicsChannelName != null ? epicsChannelName.hashCode() : 0;
      result = 31 * result + (int) (pollingInterval ^ (pollingInterval >>> 32));
      return result;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
