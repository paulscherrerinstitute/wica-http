/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.channel.EpicsChannelEventPublisher;
import ch.psi.wica.model.app.StatisticsCollectionService;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service which establishes EPICS CA channels on user-specified channels of
 * interest, subsequently publishing changes in the channel's connection state
 * to interested consumers within the application.
 *
 * @implNote.
 * The current implementation uses PSI's CA EPICS client library to create a
 * single shared EPICS CA Context per class instance. The EPICS CA context and
 * all associated resources are disposed of when the service instance is closed.
 */
@ThreadSafe
public abstract class EpicsChannelManager implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelManager.class );

   private final String scope;
   private final Context caContext;
   private final Map<EpicsChannelName,Channel<Object>> channels;
   private final Map<EpicsChannelName,Integer> channelInterestMap;
   private final EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber;
   private final EpicsChannelEventPublisher epicsChannelEventPublisher;
   private final EpicsChannelStatistics statisticsCollector;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param scope a String which specifies the scope of this manager.
    * @param epicsChannelAccessContextSupplier an object which can be used to subscribe to connection state changes.
    * @param epicsChannelConnectionChangeSubscriber an object which can be used to subscribe to connection state changes.
    * @param epicsChannelEventPublisher an object which publishes events of interest to consumers within the application.
    * @param statisticsCollectionService an object which will collect the statistics associated with this class instance.
    */
   public EpicsChannelManager( String scope,
                               EpicsChannelAccessContextSupplier epicsChannelAccessContextSupplier,
                               EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber,
                               EpicsChannelEventPublisher epicsChannelEventPublisher,
                               StatisticsCollectionService statisticsCollectionService )
   {
      logger.debug( "'{}' - constructing new EpicsChannel instance...", this );

      Validate.notNull( epicsChannelAccessContextSupplier );
      this.scope = Validate.notNull( scope );
      this.epicsChannelConnectionChangeSubscriber = Validate.notNull( epicsChannelConnectionChangeSubscriber );
      this.epicsChannelEventPublisher = Validate.notNull( epicsChannelEventPublisher );


      this.channels = new ConcurrentHashMap<>();
      this.channelInterestMap = new ConcurrentHashMap<>();

      this.statisticsCollector = new EpicsChannelStatistics( scope, channels );
      statisticsCollectionService.addCollectable( statisticsCollector );

      caContext = epicsChannelAccessContextSupplier.getContextForScope( scope );
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public EpicsChannelStatistics getStatistics()
   {
      return statisticsCollector;
   }

   public void createChannel( EpicsChannelName epicsChannelName )
   {
      Validate.notNull( epicsChannelName );
      statisticsCollector.incrementCreateMonitoredChannelRequests();

      if ( channelInterestMap.containsKey( epicsChannelName ) )
      {
         final int currentInterestCount = channelInterestMap.get( epicsChannelName );
         final int newInterestCount = currentInterestCount + 1;
         logger.info( "Increasing interest level in control system channel named: '{}' to {}", epicsChannelName, newInterestCount );
         channelInterestMap.put( epicsChannelName, newInterestCount );
         return;
      }

      logger.debug( "'{}' - creating new channel...", epicsChannelName );
      final Channel<Object> caChannel = createChannel_( epicsChannelName );
      channels.put( epicsChannelName, caChannel );
      channelInterestMap.put( epicsChannelName, 1 );
   }

   public void removeChannel( EpicsChannelName epicsChannelName )
   {
      Validate.notNull( epicsChannelName );
      Validate.validState( channels.containsKey( epicsChannelName ) );
      Validate.validState( channelInterestMap.containsKey( epicsChannelName ) );
      Validate.validState(channelInterestMap.get( epicsChannelName ) > 0 );
      statisticsCollector.incrementRemoveMonitoredChannelRequests();

      final int currentInterestCount = channelInterestMap.get( epicsChannelName );
      final int newInterestCount = currentInterestCount - 1;
      logger.info( "Reducing interest level in control system channel named: '{}' to {}" , epicsChannelName, newInterestCount );
      channelInterestMap.put( epicsChannelName, newInterestCount );

      if ( newInterestCount == 0 )
      {
         logger.info( "Removing control system channel named: '{}'", epicsChannelName );
         channelInterestMap.remove( epicsChannelName );
         final Channel<Object> caChannel = channels.remove( epicsChannelName );
         caChannel.close();
         epicsChannelEventPublisher.publishChannelDisconnected( scope, caChannel );
      }
   }

   /**
    * Disposes of all resources associated with this class instance.
    */
   @Override
   public void close()
   {
      // Dispose of any references that are no longer required
      logger.debug( "'{}' - disposing resources...", this );

      // Note: closing the context automatically disposes of any open channels and monitors.
      caContext.close();
      channels.clear();

      logger.debug( "'{}' - resources disposed ok.", this );
   }

/*- Private methods ----------------------------------------------------------*/

   /**
    * Creates a new EPICS channel and starts publishing events describing
    * the channel's evolving connection state.
    *
    * The EPICS channel may or may not be online when this method is invoked.
    * The connection-state-change event will be published when the connection to
    * the remote IOC is eventually established. Subsequently, the value-change
    * event will be published to provide the latest value of the channel.
    *
    * @param epicsChannelName the request specification object.
    * @throws NullPointerException if the 'epicsChannelName' argument was null.
    */
   private Channel<Object> createChannel_( EpicsChannelName epicsChannelName )
   {
      Validate.notNull( epicsChannelName );

      logger.debug("'{}' - creating channel... ", epicsChannelName );
      final Channel<Object> caChannel;
      try
      {
         logger.debug("'{}' - creating channel of type 'generic'...", epicsChannelName );
         caChannel = caContext.createChannel( epicsChannelName.asString(), Object.class);
         logger.debug("'{}' - channel created ok.", epicsChannelName );

         // Synchronously add a connection listener before making any attempt to connect the channel.
         logger.debug("'{}' - adding connection change listener... ", epicsChannelName );
         epicsChannelConnectionChangeSubscriber.subscribe( caChannel, (conn) -> {
            if ( conn )
            {
               epicsChannelEventPublisher.publishChannelConnected( scope, caChannel );
            }
            else
            {
               epicsChannelEventPublisher.publishChannelDisconnected( scope, caChannel );
            }
         } );
         logger.debug("'{}' - connection change listener added ok.", epicsChannelName );

         logger.debug("'{}' - connecting asynchronously... ", epicsChannelName );
         caChannel.connectAsync().thenRunAsync( () -> {
            // Note the CA current (1.2.2) implementation of the CA library calls back
            // this code block using MULTIPLE threads taken from the so-called LeaderFollowersThreadPool.
            // By default this pool is configured for FIVE threads but where necessary this can be
            // increased by setting the system property shown below:
            // System.setProperty( "LeaderFollowersThreadPool.thread_pool_size", "50" );
            logger.debug("'{}' - asynchronous connect completed. Waiting for channel to come online.", epicsChannelName );
            epicsChannelEventPublisher.publishFirstConnected( scope, caChannel );
         } )
               .exceptionally( (ex) -> {
                  logger.warn( "'{}' - exception on channel, details were as follows: {}", this, ex.toString());
                  return null;
               } );
      }
      catch ( Exception ex )
      {
         logger.error("'{}' - exception whilst creating channel, details were as follows: {}", epicsChannelName, ex.toString() );
         throw ex;
      }

      logger.debug("'{}' - channel created ok.", epicsChannelName );
      return caChannel;
   }

/*- Nested Classes -----------------------------------------------------------*/

   @Service
   public static class EpicsPolledChannelManagerService extends EpicsChannelManager
   {
      /**
       * Constructs a new instance.
       *
       * @param epicsChannelAccessContextSupplier      an object which can be used to obtain a Channel-Access context.
       * @param epicsChannelConnectionChangeSubscriber an object which can be used to subscribe to connection state changes.
       * @param epicsChannelEventPublisher             an object which publishes events of interest to consumers within the application.
       * @param statisticsCollectionService            an object which will collect the statistics associated with this class instance.
       */
      public EpicsPolledChannelManagerService( @Autowired EpicsChannelAccessContextSupplier epicsChannelAccessContextSupplier,
                                               @Autowired EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber,
                                               @Autowired EpicsChannelEventPublisher epicsChannelEventPublisher,
                                               @Autowired StatisticsCollectionService statisticsCollectionService )
      {
         super( "polled", epicsChannelAccessContextSupplier, epicsChannelConnectionChangeSubscriber, epicsChannelEventPublisher, statisticsCollectionService );
      }
   }

   @Service
   public static class EpicsMonitoredChannelManagerService extends EpicsChannelManager
   {
      /**
       * Constructs a new instance.
       *
       * @param epicsChannelAccessContextSupplier      an object which can be used to obtain a Channel-Access context.
       * @param epicsChannelConnectionChangeSubscriber an object which can be used to subscribe to connection state changes.
       * @param epicsChannelEventPublisher             an object which publishes events of interest to consumers within the application.
       * @param statisticsCollectionService            an object which will collect the statistics associated with this class instance.
       */
      public EpicsMonitoredChannelManagerService( @Autowired EpicsChannelAccessContextSupplier epicsChannelAccessContextSupplier,
                                                  @Autowired EpicsChannelConnectionChangeSubscriber epicsChannelConnectionChangeSubscriber,
                                                  @Autowired EpicsChannelEventPublisher epicsChannelEventPublisher,
                                                  @Autowired StatisticsCollectionService statisticsCollectionService )
      {
         super( "monitored", epicsChannelAccessContextSupplier, epicsChannelConnectionChangeSubscriber, epicsChannelEventPublisher, statisticsCollectionService );
      }
   }

}
