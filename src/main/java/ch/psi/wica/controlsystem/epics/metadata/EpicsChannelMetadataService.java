/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.metadata;

/*- Imported packages --------------------------------------------------------*/

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
public class EpicsChannelMetadataService
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMetadataService.class );
   private final EpicsChannelMetadataPublisher epicsChannelMetadataPublisher;

   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param epicsChannelMetadataPublisher an object which can be used to get the channel metadata.
    */
   public EpicsChannelMetadataService( @Autowired EpicsChannelMetadataPublisher epicsChannelMetadataPublisher )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMetadataService instance...", this );
      this.epicsChannelMetadataPublisher = Validate.notNull( epicsChannelMetadataPublisher );

      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public EpicsChannelMetadataStatistics getStatistics()
   {
      return epicsChannelMetadataPublisher.getStatistics();
   }

   /**
    * Starts acquiring metadata for the EPICS control system channel specified
    * by the supplied request object.
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
   public void startDataAcquisition( EpicsChannelMetadataRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The metadata service was previously closed." );
      Validate.validState( ! epicsChannelMetadataPublisher.isRequestObjectRecognised( requestObject ), "The metadata request object is already active." );

      logger.info( "'{}' - starting to acquire metadata... ", requestObject );
      try
      {
         // The ordering below is important.
         // Tell the metadata publisher about the new request. This means that the most recent and future
         // connection events associated with the control system channel will get published to the relevant
         // wica publication channel.
        epicsChannelMetadataPublisher.addChannel( requestObject );
      }
      catch ( Exception ex )
      {
         logger.error( "'{}' - exception on channel, details were as follows: {}", requestObject, ex.toString() );
      }
      logger.info( "'{}' - monitor started ok.", requestObject );
   }

   /**
    * Stops acquiring metadata for the EPICS control system channel specified by the
    * supplied request object.
    *
    * @param requestObject the request specification object.
    * @throws NullPointerException if the 'requestObject' argument was null.
    * @throws IllegalStateException if this service was previously closed.
    * @throws IllegalStateException if the 'requestObject' was not recognised.
    */
   public void stopDataAcquisition( EpicsChannelMetadataRequest requestObject )
   {
      Validate.notNull( requestObject );
      Validate.validState( ! closed, "The monitoring service was previously closed." );
      Validate.validState( epicsChannelMetadataPublisher.isRequestObjectRecognised( requestObject ), "The metadata request object was not recognised." );

      logger.info("'{}' - stopping monitoring... ", requestObject );
      try
      {
         // Tell the metadata publisher that the monitor service is no longer interested in this channel.
         // This means that the publisher will immediately stop listening for control system connection
         // events associated with this channel and will no longer publish the metadata associated with
         // such changes.
         final var metadataRequestObject = new EpicsChannelMetadataRequest( requestObject.getEpicsChannelName(), requestObject.getPublicationChannel() );
         epicsChannelMetadataPublisher.removeChannel( metadataRequestObject );

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
      epicsChannelMetadataPublisher.removeAllChannels();
      this.getStatistics().reset();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

