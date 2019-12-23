/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelStartMonitoringEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopMonitoringEvent;
import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
public class EpicsMonitoredValueRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger(EpicsMonitoredValueRequesterService.class );

   private final EpicsChannelMonitoringService epicsChannelMonitoringService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsMonitoredValueRequesterService( EpicsChannelMonitoringService epicsChannelMonitoringService )
   {
      this.epicsChannelMonitoringService = Validate.notNull (epicsChannelMonitoringService);
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartMonitoringEvent( WicaChannelStartMonitoringEvent wicaChannelStartMonitoringEvent )
   {
      Validate.notNull( wicaChannelStartMonitoringEvent);
      final WicaChannel wicaChannel = wicaChannelStartMonitoringEvent.get();

      // This service will start monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( ( isMonitorable( wicaChannel.getName() )) )
      {
         logger.trace( "Starting to monitor wica channel: '{}'", wicaChannel );
         startMonitoring( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring start monitoring request for wica channel: '{}'", wicaChannel );
      }
   }

   @EventListener
   public void handleWicaChannelStopMonitoringEvent( WicaChannelStopMonitoringEvent wicaChannelStopMonitoringEvent )
   {
      Validate.notNull( wicaChannelStopMonitoringEvent);
      final WicaChannel wicaChannel = wicaChannelStopMonitoringEvent.get();

      // This service will stop monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( isMonitorable( wicaChannel.getName() ) )
      {
         logger.trace( "Stopping monitoring wica channel named: '{}'", wicaChannel );
         stopMonitoring( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring stop monitor request for wica channel: '{}'", wicaChannel );
      }
   }

/*- Private methods ----------------------------------------------------------*/

   private boolean isMonitorable( WicaChannelName wicaChannelName)
   {
      return ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA );
   }

   /**
    * Starts monitoring the control system channel with the specified name.
    *
    * @param wicaChannel the name of the channel to monitor.
    */
   private void startMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      final ControlSystemName controlSystemName = wicaChannel.getName().getControlSystemName();

      appLogger.trace( "EPICS channel subscribe: '{}'", controlSystemName.asString() );
      logger.trace( "Subscribing to new control system channel named: '{}'", controlSystemName.asString() );

      // Now start monitoring
      epicsChannelMonitoringService.startMonitoring( wicaChannel );
   }

   /**
    * Stops monitoring the control system channel with the specified name.
    *
    * @param wicaChannel the name of the channel which is no longer of interest.
    */
   private void stopMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );
      final ControlSystemName controlSystemName = wicaChannel.getName().getControlSystemName();

      appLogger.info( "EPICS channel unsubscribe: '{}'", controlSystemName.asString() );
      logger.trace( "Unsubscribing from control system channel named: '{}'", controlSystemName.asString() );

      epicsChannelMonitoringService.stopMonitoring( wicaChannel );
   }


/*- Nested Classes -----------------------------------------------------------*/

}
