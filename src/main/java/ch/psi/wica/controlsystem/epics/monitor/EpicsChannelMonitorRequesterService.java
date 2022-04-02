/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.monitor;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.WicaChannelStartMonitoringEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelStopMonitoringEvent;
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
 * requests which require starting or stopping the monitoring of EPICS
 * Process Variables (PV's) using Channel-Access (CA) protocol.
 */
@Service
@ThreadSafe
public class EpicsChannelMonitorRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitorRequesterService.class );
   private final EpicsChannelMonitorService epicsChannelMonitorService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelMonitorRequesterService( @Autowired EpicsChannelMonitorService epicsChannelMonitorService )
   {
      this.epicsChannelMonitorService = Validate.notNull( epicsChannelMonitorService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    *
    * @param event
    */
   @EventListener
   public void handleWicaChannelStartMonitoringEvent( WicaChannelStartMonitoringEvent event )
   {
      Validate.notNull( event);
      final WicaChannel wicaChannel = event.get();

      // This service will start monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( isSupportedProtocol( wicaChannel.getName() ) )
      {
         logger.trace( "Starting to monitor wica channel: '{}'", wicaChannel );
         startMonitoring( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring start monitoring request for wica channel: '{}'", wicaChannel );
      }
   }

   /**
    *
    * @param event
    */
   @EventListener
   public void handleWicaChannelStopMonitoringEvent( WicaChannelStopMonitoringEvent event )
   {
      Validate.notNull( event );
      final WicaChannel wicaChannel = event.get();

      // This service will stop monitoring Wica channels where the protocol is
      // not explicitly set, or where it is set to indicate that EPICS Channel
      // Access (CA) protocol should be used.
      if ( isSupportedProtocol( wicaChannel.getName() ) )
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

   /**
    * Returns a boolean, indicating whether the protocol associated with the
    * supplied WicaChannelName is supported.
    *
    * This service supports EPICS Channel-Access (CA) protocol. For channels
    * where the protocol is not explicitly stated CA is assumed to be the
    * default.
    *
    * @param wicaChannelName the channel whose protocol is to be examined.
    * @return the result, set true when the protocol is supported (= EPICS CA)}
    */
   private static boolean isSupportedProtocol( WicaChannelName wicaChannelName)
   {
      return ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA );
   }

   /**
    * Starts monitoring the specified wica channel.
    *
    * @param wicaChannel the channel to monitor.
    */
   private void startMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now start monitoring
      final var requestObject = new EpicsChannelMonitorRequest( wicaChannel );
      try
      {
         appLogger.trace( "Starting monitor: '{}'", requestObject );
         epicsChannelMonitorService.startMonitoring( requestObject );
      }
      catch( Exception ex)
      {
         logger.warn( "Failed to start monitoring data acquisition: '{}'", requestObject );
         throw ex;
      }
   }

   /**
    * Stops monitoring the specified wica channel.
    *
    * @param wicaChannel the channel which should no longer be monitored.
    */
   private void stopMonitoring( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now stop monitoring
      final var requestObject = new EpicsChannelMonitorRequest( wicaChannel );
      appLogger.trace( "Stopping monitor: '{}'", requestObject );
      epicsChannelMonitorService.stopMonitoring( requestObject );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
