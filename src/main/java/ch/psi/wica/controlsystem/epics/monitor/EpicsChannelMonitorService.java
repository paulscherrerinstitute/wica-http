/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.monitor;

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
 * A service which establishes EPICS CA monitors on user-specified channels of
 * interest, subsequently publishing the information from each monitor update
 * to interested consumers within the application.
 */
@Service
@ThreadSafe
public class EpicsChannelMonitorService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitorService.class );
   private final EpicsChannelManager epicsChannelManager;
   private final EpicsChannelMonitorPublisher epicsChannelMonitorPublisher;

   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param epicsChannelManager an object which can be used to get the channel.
    * @param epicsChannelMonitorPublisher an object which can be used to get the channel value.
    */
   public EpicsChannelMonitorService( @Autowired EpicsChannelManager.EpicsMonitoredChannelManagerService epicsChannelManager,
                                      @Autowired EpicsChannelMonitorPublisher epicsChannelMonitorPublisher )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );

      this.epicsChannelManager = Validate.notNull( epicsChannelManager );
      this.epicsChannelMonitorPublisher = Validate.notNull( epicsChannelMonitorPublisher );

      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the statistics associated with this service.
    *
    * @return the statistics.
    */
   public EpicsChannelMonitorStatistics getStatistics()
   {
      return epicsChannelMonitorPublisher.getStatistics();
   }

   /**
    * Starts monitoring the EPICS control system channel according to the
    * parameters in the supplied request object.
    * <p>
    * The creation of the underlying EPICS monitor is performed asynchronously
    * so the invocation of this method does NOT incur the cost of a network
    * round trip.
    * <p>
    * The EPICS channel may or may not be online when this method is invoked.
    * The connection-state-change event will be published when the connection to
    * the remote IOC is eventually established. Subsequently, the value-change
    * event will be published to provide the latest value of the channel.
    *
    * @param requestObject the request specification object.
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was already active.
    */
   public void startMonitoring( EpicsChannelMonitorRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The monitoring service was previously closed." );
      Validate.validState( ! epicsChannelMonitorPublisher.isRequestObjectRecognised( requestObject ), "The request object is already active." );

      logger.info( "'{}' - starting to monitor... ", requestObject );
      try
      {
         // The ordering below is important.
         // 1. Tell the monitor publisher about the new request. This means that the most recent and future
         //    monitor updates associated with the control system channel will get published to the relevant
         //    wica publication channel.
         epicsChannelMonitorPublisher.addChannel( requestObject );

         // 2. Tell the channel manager to create a new channel for monitoring purposes.
         epicsChannelManager.createChannel( requestObject.getEpicsChannelName() );
      }
      catch ( Exception ex )
      {
         logger.error( "'{}' - exception on channel, details were as follows: {}", requestObject, ex.toString() );
      }
      logger.info( "'{}' - monitor started ok.", requestObject );
   }

   /**
    * Stops monitoring the EPICS control system channel specified by the supplied request
    * object.
    *
    * @param requestObject the request specification object.
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was not recognised.
    */
   public void stopMonitoring( EpicsChannelMonitorRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The monitoring service was previously closed." );
      Validate.validState( epicsChannelMonitorPublisher.isRequestObjectRecognised( requestObject ), "The request object was not recognised." );

      logger.info("'{}' - stopping monitoring... ", requestObject );
      try
      {
         // The ordering below is NOT important.
         // 1. Tell the monitor publisher that the monitor service is no longer interested in this channel.
         //    This means that the publisher will immediately stop publishing the value changes associated
         //    with monitor updates.
         epicsChannelMonitorPublisher.removeChannel( requestObject );

         // 2. Tell the channel manager that the monitor service is no longer interested in this channel.
         //    This may or may not result in the underlying channel being destroyed (depending on whether
         //    any other clients are interested in the channel).
         epicsChannelManager.removeChannel( requestObject.getEpicsChannelName() );
      }
      catch ( Exception ex )
      {
         logger.error("'{}' - exception on channel, details were as follows: {}", requestObject, ex.toString() );
      }
      logger.info("'{}' - monitor stopped ok.", requestObject );
   }

   /**
    * Closes the service.
    */
   public void close()
   {
      // Set a flag to prevent further usage
      closed = true;
      epicsChannelMonitorPublisher.removeAllChannels();
      this.getStatistics().reset();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

