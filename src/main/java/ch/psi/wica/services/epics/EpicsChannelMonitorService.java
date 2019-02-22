/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.ControlSystemName;
import ch.psi.wica.model.WicaChannelMetadata;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * A service for establishing a channel access monitor on a single EPICS
 * channel and for notifying service consumers of any received changes
 * of value or changes to the state of the underlying connection.
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

   private static final Map<ControlSystemName,Channel> channels = new HashMap<>();
   private static final Map<ControlSystemName,Monitor> monitors = new HashMap<>();

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
    * @param epicsChannelName the name of the channel to be monitored.
    * @param connectionStateChangeHandler the handler to be informed of changes
    *        to the channel's connection state.
    * @param metadataChangeHandler the handler to be informed of metadata changes.
    * @param valueChangeHandler the handler to be informed of value changes.
    * @throws NullPointerException if any of the input arguments were null.
    *
    */
    void startMonitoring( EpicsChannelName epicsChannelName,
                          Consumer<Boolean> connectionStateChangeHandler,
                          Consumer<WicaChannelMetadata> metadataChangeHandler,
                          Consumer<WicaChannelValue> valueChangeHandler )
   {
      Validate.notNull( epicsChannelName);
      Validate.notNull( connectionStateChangeHandler );
      Validate.notNull( metadataChangeHandler );
      Validate.notNull( valueChangeHandler );
      Validate.isTrue( ! channels.containsKey( epicsChannelName ),"channel name already exists" );

      logger.debug("'{}' - starting to monitor... ", epicsChannelName);

      try
      {
         logger.debug("'{}' - creating channel of type '{}'...", epicsChannelName, "generic" );

         final Channel<Object> channel = caContext.createChannel(epicsChannelName.toString(), Object.class ) ;
         channels.put( epicsChannelName, channel );

         logger.debug("'{}' - channel created ok.", epicsChannelName);

         logger.debug("'{}' - adding connection listener... ", epicsChannelName);
         channel.addConnectionListener( ( chan, isConnected ) -> connectionStateChangeHandler.accept( isConnected ) );
         logger.debug("'{}' - connection listener added ok.", epicsChannelName);

         logger.debug("'{}' - connecting asynchronously to... ", epicsChannelName);
         final CompletableFuture<Channel<Object>> completableFuture = channel.connectAsync();
         logger.debug("'{}' - asynchronous connect completed ok.", epicsChannelName);

         completableFuture.thenRunAsync( () -> {
            logger.debug("'{}' - channel connected ok.", epicsChannelName);
            epicsChannelMetadataPublisher.getAndPublishMetadata( channel, metadataChangeHandler );
            epicsChannelValuePublisher.getAndPublishValue( channel, valueChangeHandler );
            registerValueChangeHandler( channel, valueChangeHandler );
         } );
      }
      catch ( Exception ex )
      {
         logger.debug("'{}' - exception on channel, details were as follows: ", epicsChannelName, ex.toString() );
      }
   }

   /**
    * Stop monitoring the specified channel.
    *
    * @param epicsChannelName the channel which is no longer of interest.
    */
   void stopMonitoring( EpicsChannelName epicsChannelName )
   {
      Validate.notNull( epicsChannelName );
      Validate.isTrue( channels.containsKey( epicsChannelName ), "channel name not recognised" );

      logger.debug("'{}' - stopping monitor... ", epicsChannelName);
      channels.get( epicsChannelName ).close();
   }

   /**
    * Disposes of all resources associated with this class instance.
    */
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
    * Returns the count of the channels created by this class instance.
    *
    * A channel is "created" every time the startMonitoring method is invoked.
    * A channel is "deleted" every time the stop monitoring method is invoked.
    * All channels are "deleted" every time the class instance is closed.
    *
    * @return the count
    */
   public static long getChannelsCreatedCount()
   {
      return channels.size();
   }

   /**
    * Returns the count of channels known to this class instance which are
    * connected to the underlying data source.
    *
    * A channel is "connected" asynchronously, every time a previous call to the
    * startMonitoring method has subsequently resulted in a successful transaction
    * with the underlying CA data source. If a channel is available online this
    * will occur (typically) within a few milliseconds. If not it may not take
    * seconds/minutes/hours/weeks/days.
    *
    * @return the count
    */
   public static long getChannelsConnectedCount()
   {
      return channels.values()
                     .stream()
                     .filter( c -> c.getConnectionState() == ConnectionState.CONNECTED )
                     .count();
   }

   /**
    * Returns the count of monitors established by this class.
    *
    * @return the count
    */
   public static long getMonitorsConnectedCount()
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
      final ControlSystemName controlSystemName = ControlSystemName.of( channel.getName() );

      // Establish a monitor on the most "dynamic" (= frequently changing)
      // properties of the channel.  These include the timestamp, the alarm
      // information and value itself. In EPICS Channel Access this requirement
      // is fulfilled by the DBR_TIME_xxx type which is supported in PSI's
      // CA library via the Metadata<Timestamped> class.
      logger.debug("'{}' - adding monitor...", controlSystemName);

      @SuppressWarnings( "unchecked" )
      final Monitor<Timestamped> monitor = channel.addMonitor( Timestamped.class, valueObj -> {
         logger.trace("'{}' - publishing new value...", controlSystemName );
         epicsChannelValuePublisher.publishValue( controlSystemName, valueObj, valueChangeHandler);
         logger.trace("'{}' - new value published.", controlSystemName );
      } );
      logger.debug("'{}' - monitor added.", controlSystemName );
      monitors.put( controlSystemName, monitor );
   }


/*- Nested Classes -----------------------------------------------------------*/

}

