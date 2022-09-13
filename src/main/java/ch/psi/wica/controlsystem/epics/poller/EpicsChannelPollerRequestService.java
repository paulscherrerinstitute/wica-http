/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.poller;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.WicaChannelStartPollingEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelStopPollingEvent;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for listening and responding to application event
 * requests which require starting or stopping the polling of EPICS
 * Process Variables (PV's) using Channel-Access (CA) protocol.
 */
@Service
@ThreadSafe
public class EpicsChannelPollerRequestService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger( EpicsChannelPollerRequestService.class );
   private final EpicsChannelPollerService epicsChannelPollerService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new instance.
    *
    * @param epicsChannelPollerService the polling service.
    */
   EpicsChannelPollerRequestService( @Autowired EpicsChannelPollerService epicsChannelPollerService )
   {
      this.epicsChannelPollerService = Validate.notNull( epicsChannelPollerService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Handles requests to start polling the specified Wica Channel.
    *
    * @param event contains the channel to start polling.
    */
   @EventListener
   public void handleWicaChannelStartPollingEvent( WicaChannelStartPollingEvent event )
   {
      Validate.notNull( event );
      final WicaChannel wicaChannel = event.get();

      // Note: this service only handles direct, network-based polling of a remote IOC
      // using the EPICS Channel-Access (CA) protocol. All other requests will be silently ignored.
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannel.getProperties().getDataAcquisitionMode();
      if ( ! wicaDataAcquisitionMode.doesNetworkPolling() )
      {
         return;
      }

      // This service will start polling Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( isSupportedProtocol( wicaChannel.getName() ) )
      {
         final int pollingIntervalInMillis = wicaChannel.getProperties().getPollingIntervalInMillis();
         logger.trace("Starting to poll wica channel: '{}', periodically every {} ms.", wicaChannel, pollingIntervalInMillis );
         startPolling( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring start poll request for wica channel: '{}'", wicaChannel );
      }
   }

   /**
    * Handles requests to stop polling the specified Wica Channel.
    *
    * @param event contains the channel to stop polling.
    */
   @EventListener
   public void handleWicaChannelStopPollingEvent( WicaChannelStopPollingEvent event )
   {
      Validate.notNull( event);
      final WicaChannel wicaChannel = event.get();

      // Note: this service only handles direct, network-based polling of a remote IOC
      // using the EPICS Channel-Access (CA) protocol. All other requests will be silently ignored.
      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannel.getProperties().getDataAcquisitionMode();
      if ( ! wicaDataAcquisitionMode.doesNetworkPolling() )
      {
         return;
      }

      // This service will stop polling Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( isSupportedProtocol( wicaChannel.getName() ) )
      {
         logger.trace( "Stopping polling wica channel: '{}'", wicaChannel);
         stopPolling( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring stop poll request for wica channel: '{}'", wicaChannel);
      }
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Returns a boolean, indicating whether the protocol associated with the
    * supplied WicaChannelName is supported.
    * <p>
    * This service supports EPICS Channel-Access (CA) protocol. For channels
    * where the protocol is not explicitly stated CA is assumed to be the
    * default.
    *
    * @param wicaChannelName the channel whose protocol is to be examined.
    * @return the result, set true when the protocol is supported (= EPICS CA)}
    */
   private static boolean isSupportedProtocol( WicaChannelName wicaChannelName )
   {
      return ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA );
   }

   /**
    * Starts polling the specified Wica Channel.
    *
    * @param wicaChannel the channel to poll.
    */
   private void startPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now start polling
      final var requestObject = new EpicsChannelPollerRequest( wicaChannel );
      try
      {
         appLogger.trace( "Starting poller: '{}'", requestObject );
         epicsChannelPollerService.startPolling( requestObject );
      }
      catch( Exception ex)
      {
         logger.warn( "Failed to start monitoring data acquisition: '{}'", requestObject );
         throw ex;
      }
   }

   /**
    * Stops polling the specified Wica Channel.
    *
    * @param wicaChannel the channel which should no longer be polled.
    */
   private void stopPolling( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now stop polling
      final var requestObject = new EpicsChannelPollerRequest( wicaChannel );
      appLogger.trace( "Stopping poller: '{}'", requestObject );
      epicsChannelPollerService.stopPolling( requestObject );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
