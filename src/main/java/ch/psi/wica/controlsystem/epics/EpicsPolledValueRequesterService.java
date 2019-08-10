/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.*;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


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

   private final Logger logger = LoggerFactory.getLogger(EpicsPolledValueRequesterService.class );

   private final ApplicationEventPublisher applicationEventPublisher;
   private final EpicsChannelPollingService epicsChannelPollingService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsPolledValueRequesterService( ApplicationEventPublisher applicationEventPublisher,
                                     EpicsChannelPollingService epicsChannelPollingService )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
      this.epicsChannelPollingService = Validate.notNull( epicsChannelPollingService );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartPollingEvent( WicaChannelStartPollingEvent wicaChannelStartPollingEvent )
   {
      Validate.notNull( wicaChannelStartPollingEvent );
      final WicaChannel wicaChannel = wicaChannelStartPollingEvent.get();
      final int pollingIntervalInMillis = wicaChannelStartPollingEvent.getPollingIntervalInMillis();

      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannelStartPollingEvent.getWicaDataAcquisitionMode();
      if ( wicaDataAcquisitionMode != WicaDataAcquisitionMode.POLL )
      {
         logger.trace( "Ignoring start poll request for wica channel: '{}'", wicaChannel );
         return;
      }

      // This service will start polling Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      final WicaChannelName wicaChannelName = wicaChannel.getName();
      if ( ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA ) )
      {
         logger.trace( "Starting to poll wica channel: '{}'", wicaChannel );
         startPolling( wicaChannel, pollingIntervalInMillis );
      }
      else
      {
         logger.trace( "Ignoring start poll request for wica channel: '{}'", wicaChannel );
      }
   }

   @EventListener
   public void handleWicaChannelStopPollingEvent( WicaChannelStopPollingEvent wicaChannelStopPollingEvent )
   {
      Validate.notNull( wicaChannelStopPollingEvent);
      final WicaChannel wicaChannel = wicaChannelStopPollingEvent.get();

      final WicaDataAcquisitionMode wicaDataAcquisitionMode = wicaChannelStopPollingEvent.getWicaDataAcquisitionMode();
      if ( wicaDataAcquisitionMode != WicaDataAcquisitionMode.POLL )
      {
         logger.trace( "Ignoring stop poll request for wica channel: '{}'", wicaChannel );
         return;
      }

      // This service will stop polling Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      final WicaChannelName wicaChannelName = wicaChannel.getName();
      if ( ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA ) )
      {
         logger.trace( "Stopping polling wica channel: '{}'", wicaChannel );
         stopPolling( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring stop poll request for wica channel: '{}'", wicaChannel );
      }
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Starts polling the specified wica channel.
    *
    * @param wicaChannel the channel to poll.
    * @param pollingIntervalInMillis the polling interval.
    */
   private void startPolling( WicaChannel wicaChannel, int pollingIntervalInMillis )
   {
      Validate.notNull( wicaChannel );

      // Define the handlers to be informed of interesting changes to the channel
      final Consumer<WicaChannelValue> valueUpdateHandler = v -> handlePolledValueUpdate( wicaChannel, v );

      // Now start polling
      epicsChannelPollingService.startPolling( EpicsChannelName.of( wicaChannel.getName().getControlSystemName() ), pollingIntervalInMillis, valueUpdateHandler );
   }

   /**
    * Stops polling the specified wica channel.
    *
    * @param wicaChannel the name of the channel which is no longer of interest.
    */
   private void stopPolling( WicaChannel wicaChannel  )
   {
      Validate.notNull( wicaChannel );
      epicsChannelPollingService.stopPolling( EpicsChannelName.of( wicaChannel.getName().getControlSystemName() ) );
   }

   /**
    * Handles a value update published by the EPICS channel poller.
    *
    * @param wicaChannel the name of the channel whose value has changed.
    * @param wicaChannelValue the new value.
    */
   private void handlePolledValueUpdate( WicaChannel wicaChannel, WicaChannelValue wicaChannelValue )
   {
      Validate.notNull( wicaChannel, "The 'wicaChannel' argument was null");
      Validate.notNull( wicaChannelValue, "The 'wicaChannelValue' argument was null");

      logger.trace("'{}' - value changed to: '{}'", wicaChannel, wicaChannelValue );
      applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannel, wicaChannelValue ) );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
