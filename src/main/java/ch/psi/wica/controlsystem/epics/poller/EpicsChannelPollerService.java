/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.poller;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelManager;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service which schedules the polling of user-specified EPICS channels of
 * interest, subsequently publishing the results of each poll operation to
 * interested consumers within the application.
 */
@Service
@ThreadSafe
public class EpicsChannelPollerService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelPollerService.class );
   private final EpicsChannelManager epicsChannelManager;
   private final EpicsChannelPollerPublisher epicsChannelPollerPublisher;

   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param epicsChannelManager an object which can be used to get the channel.
    * @param epicsChannelPollerPublisher an object which can be used to get the channel value.
    */
   public EpicsChannelPollerService( @Autowired EpicsChannelManager.EpicsPolledChannelManagerService epicsChannelManager,
                                     @Autowired EpicsChannelPollerPublisher epicsChannelPollerPublisher )
   {
      logger.debug( "'{}' - constructing new EpicsChannelPollerService instance...", this );

      this.epicsChannelManager = Validate.notNull( epicsChannelManager );
      this.epicsChannelPollerPublisher = Validate.notNull( epicsChannelPollerPublisher );

      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the statistics associated with this service.
    *
    * @return the statistics.
    */
   public EpicsChannelPollerStatistics getStatistics()
   {
      return epicsChannelPollerPublisher.getStatistics();
   }

   /**
    * Starts polling the EPICS control system channel according to the
    * parameters in the supplied request object.
    * <p>
    * The creation of the underlying EPICS poller is performed asynchronously
    * so the invocation of this method does NOT incur the cost of a network
    * round trip.
    * <p>
    * The EPICS channel may or may not be online when this method is invoked.
    * The connection-state-change event will be published when the connection to
    * the remote IOC is eventually established. Subsequently, the value-change
    * event updates will be published on each periodic polling cycle to provide
    * the latest value of the channel.
    *
    * @param requestObject the request specification object.
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was already active.
    */
   public void startPolling( EpicsChannelPollerRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The monitoring service was previously closed." );
      Validate.validState( ! epicsChannelPollerPublisher.isRequestObjectRecognised( requestObject ), "The request object is already active." );

      logger.info( "'{}' - starting to poll... ", requestObject );
      try
      {
         // The ordering below is important.
         // 1. Tell the poller publisher about the new request. This means that a new poller scheduler
         //    will be created to periodically poll the control system channel and to publish the results
         //    to the relevant wica publication channel.
         epicsChannelPollerPublisher.addChannel( requestObject );

         // 2. Tell the channel manager to create a new channel for polling purposes.
         epicsChannelManager.createChannel( requestObject.getEpicsChannelName() );
      }
      catch ( Exception ex )
      {
         logger.error( "'{}' - exception on channel, details were as follows: {}", requestObject, ex.toString() );
      }
      logger.info( "'{}' - poller started ok.", requestObject );
   }

   /**
    * Stops polling the EPICS control system channel specified by the supplied request object.
    *
    * @param requestObject the request specification object.
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was not recognised.
    */
   public void stopPolling( EpicsChannelPollerRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The polling service was previously closed." );
      Validate.validState( epicsChannelPollerPublisher.isRequestObjectRecognised( requestObject ), "The request object was not recognised." );

      logger.info( "'{}' - stopping poller... ", requestObject );
      try
      {
         // The ordering below is NOT important.
         // 1. Tell the poller publisher that the poller service is no longer interested in this channel.
         //    This means that the publisher will immediately stop publishing the value changes associated
         //    with polling updates.
         epicsChannelPollerPublisher.removeChannel( requestObject );

         // 3. Tell the channel manager that the poller service is no longer interested in this channel.
         //    This may or may not result in the underlying channel being destroyed (depending on whether
         //    any other clients are interested in the channel).
         epicsChannelManager.removeChannel( requestObject.getEpicsChannelName() );
      }
      catch ( Exception ex )
      {
         logger.error( "'{}' - exception on channel, details were as follows: {}", requestObject, ex.toString() );
      }
      logger.info( "'{}' - poller stopped ok.", requestObject );
   }

   /**
    * Closes the service.
    */
   public void close()
   {
      // Set a flag to prevent further usage
      closed = true;
      this.epicsChannelPollerPublisher.close();
      this.getStatistics().reset();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

