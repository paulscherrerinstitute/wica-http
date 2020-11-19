/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelStartPollingEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopPollingEvent;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for listening and responding to application event
 * requests which require starting or stopping the monitoring of EPICS
 * Process Variables (PV's) using Channel Access protocol.
 *
 * Additionally supports the handling of requests for polling EPICS
 * channels at a fixed rate.
 */
@Service
@ThreadSafe
public class EpicsPolledValueRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger(EpicsPolledValueRequesterService.class );
   private final EpicsChannelPollingService epicsChannelPollingService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsPolledValueRequesterService( EpicsChannelPollingService epicsChannelPollingService )
   {
      this.epicsChannelPollingService = Validate.notNull( epicsChannelPollingService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartPollingEvent( WicaChannelStartPollingEvent wicaChannelStartPollingEvent )
   {
      Validate.notNull( wicaChannelStartPollingEvent );
      final WicaChannel wicaChannel = wicaChannelStartPollingEvent.get();

      // Note: this service only handles direct, network-based polling of a remote IOC
      // using the EPICS channel access protocol. All other requests will be silently ignored.
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannel.getProperties().getDataAcquisitionMode();
      if ( wicaDataAcquisitionMode.doesNetworkPolling() )
      {
         // This service will start polling Wica channels where the protocol is
         // not explicitly set, or where it is set to indicate that EPICS Channel
         // Access (CA) protocol should be used.
         final WicaChannelName wicaChannelName = wicaChannel.getName();
         if ( (wicaChannelName.getProtocol().isEmpty()) || (wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA) )
         {
            final int pollingIntervalInMillis = wicaChannel.getProperties().getPollingIntervalInMillis();
            logger.trace("Starting to poll wica channel: '{}', periodically every {} ms.", wicaChannel, pollingIntervalInMillis );
            startPolling ( wicaChannel );
         }
         else
         {
            logger.trace("Ignoring start poll request for wica channel: '{}'", wicaChannel);
         }
      }
   }

   @EventListener
   public void handleWicaChannelStopPollingEvent( WicaChannelStopPollingEvent wicaChannelStopPollingEvent )
   {
      Validate.notNull( wicaChannelStopPollingEvent);
      final WicaChannel wicaChannel = wicaChannelStopPollingEvent.get();

      // Note: this service only handles direct, network-based polling of a remote IOC
      // using the EPICS channel access protocol. All other requests will be silently ignored.
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannel.getProperties().getDataAcquisitionMode();
      if ( wicaDataAcquisitionMode.doesNetworkPolling() )
      {
         // This service will stop polling Wica channels where the protocol is
         // not explicitly set, or where it is set to indicate that EPICS Channel
         // Access (CA) protocol should be used.
         final WicaChannelName wicaChannelName = wicaChannel.getName();
         if ( (wicaChannelName.getProtocol().isEmpty()) || (wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA) )
         {
            logger.trace("Stopping polling wica channel: '{}'", wicaChannel);
            stopPolling( wicaChannel );
         }
         else
         {
            logger.trace("Ignoring stop poll request for wica channel: '{}'", wicaChannel);
         }
      }
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Starts polling the specified wica channel.
    *
    * @param wicaChannel the channel to poll.
    */
   private void startPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now start polling
      final var requestObject = new EpicsChannelPollingRequest( wicaChannel );
      appLogger.trace( "Starting poller: '{}'", requestObject );
      epicsChannelPollingService.startPolling( requestObject );
   }

   /**
    * Stops polling the specified wica channel.
    *
    * @param wicaChannel the channel which should no longer be polled.
    */
   private void stopPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now stop polling
      final var requestObject = new EpicsChannelPollingRequest( wicaChannel );
      appLogger.trace( "Stopping poller: '{}'", requestObject );
      epicsChannelPollingService.stopPolling( requestObject );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
