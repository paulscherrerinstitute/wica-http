/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.WicaChannelStartMetadataDataAcquisitionEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelStopMetadataDataAcquisitionEvent;
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
 * requests which require starting or stopping the acquisition of EPICS
 * metadata (= the slowly changing properties) associated with Wica 
 * channels.
 */
@Service
@ThreadSafe
public class EpicsChannelMetadataRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMetadataRequesterService.class );
   private final EpicsChannelMetadataService epicsChannelMetadataService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   EpicsChannelMetadataRequesterService( @Autowired EpicsChannelMetadataService epicsChannelMetadataService )
   {
      this.epicsChannelMetadataService = Validate.notNull( epicsChannelMetadataService );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @EventListener
   public void handleWicaChannelStartMetadataDataAcquisitionEvent( WicaChannelStartMetadataDataAcquisitionEvent event )
   {
      Validate.notNull( event );
      final WicaChannel wicaChannel = event.get();

      // This service will start data acquisition for the Wica channels where the
      // protocol is not explicitly set, or where it is set to indicate that EPICS
      // Channel-Access (CA) protocol should be used.
      if ( isSupportedProtocol( wicaChannel.getName() ) )
      {
         logger.trace( "Starting metadata data acquisition for wica channel: '{}'.", wicaChannel );
         startMetadataDataAcquisition( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring start metadata data acquisition request for wica channel: '{}'", wicaChannel );
      }
   }

   @EventListener
   public void handleWicaChannelStopMetadataDataAcquisitionEvent( WicaChannelStopMetadataDataAcquisitionEvent event )
   {
      Validate.notNull( event);
      final WicaChannel wicaChannel = event.get();

      // This service will stop data acquisition for the Wica channels where the
      // protocol is not explicitly set, or where it is set to indicate that EPICS
      // Channel-Access (CA) protocol should be used.
      if ( isSupportedProtocol( wicaChannel.getName() ) )
      {
         logger.trace( "Stopping metadata data acquisition for wica channel: '{}'", wicaChannel);
         stopMetadataDataAcquisition( wicaChannel );
      }
      else
      {
         logger.trace( "Ignoring stop metadata data acquisition request for wica channel: '{}'", wicaChannel);
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
   private static boolean isSupportedProtocol( WicaChannelName wicaChannelName )
   {
      return ( wicaChannelName.getProtocol().isEmpty() ) || ( wicaChannelName.getProtocol().get() == WicaChannelName.Protocol.CA );
   }

   /**
    * Starts metadata data acquisition for the specified wica channel.
    *
    * @param wicaChannel the channel to poll.
    */
   private void startMetadataDataAcquisition( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now start polling
      final var requestObject = new EpicsChannelMetadataRequest( wicaChannel );
      appLogger.trace( "Starting metadata data acquisition: '{}'", requestObject );
      try
      {
         epicsChannelMetadataService.startDataAcquisition( requestObject );
      }
      catch( Exception ex)
      {
         logger.warn( "Failed to start metadata data acquisition: '{}'", requestObject );
         throw ex;
      }
   }

   /**
    * Stops metadata data acquisition for the specified wica channel.
    *
    * @param wicaChannel the channel which should no longer be polled.
    */
   private void stopMetadataDataAcquisition( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel );

      // Now stop polling
      final var requestObject = new EpicsChannelMetadataRequest( wicaChannel );
      appLogger.trace( "Stopping metadata data acquisition: '{}'", requestObject );
      epicsChannelMetadataService.stopDataAcquisition( requestObject );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
