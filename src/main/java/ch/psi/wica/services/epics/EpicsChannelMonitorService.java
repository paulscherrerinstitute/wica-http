/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Context;
import org.epics.ca.Monitor;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a service to facilitate the monitoring of a single EPICS channel
 * and to subsequently inform interested third-parties of changes of interest.
 *
 * @implNote.
 * The current implementation creates a single Context per class instance.
 * The context is disposed of when the service instance is closed.
 */
@Service
public class EpicsChannelMonitorService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitorService.class );
   private final Context caContext;

   private static final List<Channel> channels = new ArrayList<>();
   private static final List<Monitor> monitors = new ArrayList<>();

   private final EpicsChannelMetadataPublisher epicsChannelMetadataPublisher;
   private final EpicsChannelValuePublisher epicsChannelValuePublisher;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public EpicsChannelMonitorService()
   {
      this( new EpicsChannelMetadataPublisher(), new EpicsChannelValuePublisher() );
   }

   public EpicsChannelMonitorService( @Autowired EpicsChannelMetadataPublisher epicsChannelMetadataPublisher,
                                      @Autowired EpicsChannelValuePublisher epicsChannelValuePublisher )
   {
      logger.debug( "'{}' - constructing new EpicsChannelMonitorService instance...", this );

      this.epicsChannelMetadataPublisher = epicsChannelMetadataPublisher;
      this.epicsChannelValuePublisher = epicsChannelValuePublisher;

      // Setup a context that does no buffering. This is good enough for most
      // status display purposes to humans.
      System.setProperty( "CA_MONITOR_NOTIFIER_IMPL", "BlockingQueueMultipleWorkerMonitorNotificationServiceImpl,16,10" );
      System.setProperty( "CA_DEBUG", "1" );

      caContext = new Context();
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Starts monitoring the specified EPICS channel and sets up a plan whereby
    * future changes to the state of the underlying channel will be informed to
    * the supplied connection-state-change and value-change handlers.
    *
    * The creation of the underlying EPICS monitor is performed asynchronously
    * so the invocation of this method does NOT incur the cost of a network
    * round trip.
    *
    * The supplied channel may or may not be available when this method is invoked.
    * The connection-state-change handler will receive it's first message when the
    * connection to the remote IOC is eventually established. Subsequently, the
    * value-change handler will receive notification of the initial value of the
    * channel.
    *
    * The supplied handlers will be potentially called back on multiple threads.
    *
    * @param wicaChannelName the name of the channel to be monitored.
    * @param connectionStateChangeHandler the handler to be informed of changes
    *        to the channel's connection state.
    * @param metadataChangeHandler the handler to be informed of metadata changes.
    * @param valueChangeHandler the handler to be informed of value changes.
    *
    */
    void startMonitoring( WicaChannelName wicaChannelName,
                          Consumer<Boolean> connectionStateChangeHandler,
                          Consumer<WicaChannelMetadata> metadataChangeHandler,
                          Consumer<WicaChannelValue> valueChangeHandler )
   {
      Validate.notNull(wicaChannelName);
      Validate.notNull( connectionStateChangeHandler );
      Validate.notNull( metadataChangeHandler );
      Validate.notNull( valueChangeHandler );

      logger.debug("'{}' - starting to monitor... ", wicaChannelName);

      try
      {
         logger.debug("'{}' - creating channel of type '{}'...", wicaChannelName, "generic" );

         final String epicsChannelName = EpicsConversionUtilities.getEpicsChannelName( wicaChannelName );
         logger.debug("'{}' - converted to EPICS channel name: '{}'...", wicaChannelName, epicsChannelName );

         final Channel<Object> channel = caContext.createChannel( epicsChannelName, Object.class ) ;
         channels.add( channel );

         logger.debug("'{}' - channel created ok.", wicaChannelName);

         logger.debug("'{}' - adding connection listener... ", wicaChannelName);
         channel.addConnectionListener( ( chan, isConnected ) -> connectionStateChangeHandler.accept( isConnected ) );
         logger.debug("'{}' - connection listener added ok.", wicaChannelName);

         logger.debug("'{}' - connecting asynchronously to... ", wicaChannelName);
         final CompletableFuture<Channel<Object>> completableFuture = channel.connectAsync();
         logger.debug("'{}' - asynchronous connect completed ok.", wicaChannelName);

         completableFuture.thenRunAsync( () -> {
            logger.debug("'{}' - channel connected ok.", wicaChannelName);
            epicsChannelMetadataPublisher.getAndPublishMetadata( channel, metadataChangeHandler );
            epicsChannelValuePublisher.getAndPublishValue( channel, valueChangeHandler );
            registerValueChangeHandler( channel, valueChangeHandler );
         } );
      }
      catch ( Exception ex )
      {
         logger.debug("'{}' - exception on channel, details were as follows: ", wicaChannelName, ex.toString() );
      }
   }

   /**
    * Stop monitoring the specified channel.
    *
    * @param wicaChannelName the channel which is no longer of interest.
    */
   void stopMonitoring( WicaChannelName wicaChannelName )
   {
      Validate.notNull(wicaChannelName);

      final boolean channelNameIsRecognised = channels.stream()
                                                      .anyMatch( c -> c.getName().equals(wicaChannelName.toString() ) );

      Validate.isTrue( channelNameIsRecognised, "channel name not recognised" );

      logger.debug("'{}' - stopping monitor... ", wicaChannelName);

      channels.stream()
              .filter( c -> c.getName().equals(wicaChannelName.toString() ) )
              .forEach( Channel::close );
   }

   @Override
   public void close()
   {
      logger.debug( "'{}' - disposing resources...", this );
      caContext.close();

      // Dispose of any references that are no longer required
      monitors.clear();
      channels.clear();

      logger.debug( "'{}' - resources disposed ok.", this );
   }

   /**
    * Returns the count of channels established by this class.
    *
    * @return the count
    */
   public static long getChannelCreationCount()
   {
      return channels.size();
   }

   /**
    * Returns the count of channels established by this class.
    *
    * @return the count
    */
   public static long getChannelConnectionCount()
   {
      return channels.stream()
                     .filter( c -> c.getConnectionState() == ConnectionState.CONNECTED )
                     .count();
   }

   /**
    * Returns the count of monitors established by this class.
    *
    * @return the count
    */
   public static long getMonitorCreationCount()
   {
      return monitors.size();
   }


/*- Private methods ----------------------------------------------------------*/

   /**
    * Establishes a monitor on the supplied EPICS channel and registers a
    * handler which should subsequently be informed when the channel value
    * changes.
    *
    * Precondition: the supplied channel should already be created
    *               and connected.
    * Postcondition: the supplied channel will remain open.
    *
    * @param channel the EPICS channel.
    * @param valueChangeHandler the event consumer.
    */
   private void registerValueChangeHandler( Channel<Object> channel, Consumer<WicaChannelValue> valueChangeHandler )
   {
      final String epicsChannelName = channel.getName();

      // Establish a monitor on the most "dynamic" (= frequently changing)
      // properties of the channel.  These include the timestamp, the alarm
      // information and value itself. In EPICS Channel Access this requirement
      // is fulfilled by the DBR_TIME_xxx type which is supported in PSI's
      // CA library via the Metadata<Timestamped> class.
      logger.debug("'{}' - adding monitor...", epicsChannelName);

      @SuppressWarnings( "unchecked" )
      final Monitor<Timestamped> monitor = channel.addMonitor( Timestamped.class, valueObj -> {
         logger.trace("'{}' - publishing new value...", epicsChannelName);
         epicsChannelValuePublisher.publishValue( epicsChannelName, valueObj, valueChangeHandler);
         logger.trace("'{}' - new value published.", epicsChannelName);
      } );
      logger.debug("'{}' - monitor added.", epicsChannelName );
      monitors.add( monitor );
   }


/*- Nested Classes -----------------------------------------------------------*/

}

