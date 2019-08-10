/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStartPollingEvent;
import ch.psi.wica.controlsystem.event.WicaChannelStopPollingEvent;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service for starting and stopping the control system monitoring
 * for a WicaStream.
 */
@Service
@ThreadSafe
public class WicaStreamPolledValueRequesterService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamPolledValueRequesterService.class );
   private final ApplicationEventPublisher applicationEventPublisher;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param applicationEventPublisher reference to the application publisher
    *    which will be used to publish the channels that are to be monitored
    *    or which are no longer of interest.
    */
   WicaStreamPolledValueRequesterService( @Autowired ApplicationEventPublisher applicationEventPublisher )
   {
      this.applicationEventPublisher = Validate.notNull( applicationEventPublisher );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Package-level methods ----------------------------------------------------*/

   /**
    * Starts polling the specified stream.
    *
    * @param wicaStream the stream to monitor.
    */
   void startPolling( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      wicaStream.getWicaChannels()
         .stream()
         .filter( c -> c.getProperties().getDataAcquisitionMode().doesPolling() )
         .filter( c -> c.getProperties().getOptionalPollingIntervalInMillis().isPresent() )
         .filter( c -> c.getProperties().getOptionalPollingIntervalInMillis().get() > 0  )
         .forEach( c -> startPollingChannel( c, c.getProperties().getDataAcquisitionMode(), c.getProperties().getPollingIntervalInMillis() ) );
   }

   /**
    * Stops polling the specified stream.
    *
    * @param wicaStream the stream that is no longer of interest.
    */
   void stopPolling( WicaStream wicaStream )
   {
      Validate.notNull( wicaStream );
      wicaStream.getWicaChannels()
         .stream()
         .filter( c -> c.getProperties().getDataAcquisitionMode().doesPolling() )
         .filter( c -> c.getProperties().getOptionalPollingIntervalInMillis().isPresent() )
         .filter( c -> c.getProperties().getOptionalPollingIntervalInMillis().get() > 0  )
         .forEach( c -> stopPollingChannel( c, c.getProperties().getDataAcquisitionMode() ) );
   }

/*- Private methods ----------------------------------------------------------*/

   private void startPollingChannel( WicaChannel wicaChannel, WicaDataAcquisitionMode wicaDataAcquisitionMode, int pollingIntervalInMillis )
   {
      logger.trace( "Request to start polling wica channel: '{}'", wicaChannel );
      Validate.notNull( wicaChannel );

      // Set the initial state for the polled value stashes.
      // TODO: resolve polled channel use of metadata
      applicationEventPublisher.publishEvent( new WicaChannelPolledValueUpdateEvent( wicaChannel, WicaChannelValue.createChannelValueDisconnected() ));

      // Now start polling
      applicationEventPublisher.publishEvent( new WicaChannelStartPollingEvent( wicaChannel, wicaDataAcquisitionMode, pollingIntervalInMillis ) );
   }

   private void stopPollingChannel( WicaChannel wicaChannel, WicaDataAcquisitionMode wicaDataAcquisitionMode )
   {
      logger.trace( "Request to stop polling wica channel: '{}'", wicaChannel );
      applicationEventPublisher.publishEvent( new WicaChannelStopPollingEvent( wicaChannel, wicaDataAcquisitionMode ) );
   }

/*- Nested Classes -----------------------------------------------------------*/

}
