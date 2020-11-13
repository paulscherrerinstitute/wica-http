/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelValue;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.impl.LibraryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A service which offers the possibility to get or put the values of an
 * EPICS channel.
 *
 * @implNote.
 * The current implementation uses PSI's CA EPICS client library to create a
 * single shared EPICS CA Context per class instance. The EPICS CA context and
 * all associated resources are disposed of when the service instance is closed.
 */
@Service
@ThreadSafe
public class EpicsChannelGetAndPutService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelGetAndPutService.class );
   private final Context caContext;
   private final EpicsChannelValueGetter epicsChannelValueGetter;
   private final EpicsChannelMetadataGetter epicsChannelMetadataGetter;
   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance that will read and/or write information from
    * EPICS channels of interest using the supplied value getter.
    *
    * @param epicsCaLibraryMonitorNotifierImpl the CA library monitor notifier configuration.
    * @param epicsCaLibraryDebugLevel          the CA library debug level.
    * @param epicsChannelValueGetter           an object that can get and build the returned value.
    */
   public EpicsChannelGetAndPutService( @Value( "${wica.epics-ca-library-monitor-notifier-impl}" ) String epicsCaLibraryMonitorNotifierImpl,
                                        @Value( "${wica.epics-ca-library-debug-level}" ) int epicsCaLibraryDebugLevel,
                                        @Autowired EpicsChannelMetadataGetter epicsChannelMetadataGetter,
                                        @Autowired EpicsChannelValueGetter epicsChannelValueGetter )
   {
      logger.debug( "'{}' - constructing new EpicsChannelGetAndPutService instance...", this );

      this.epicsChannelMetadataGetter = Validate.notNull( epicsChannelMetadataGetter );
      this.epicsChannelValueGetter = Validate.notNull( epicsChannelValueGetter );

      logger.info( "Creating CA context for WicaChannelService..." );

      // Setup a context that uses the monitor notification policy and debug
      // message log level defined in the configuration file.
      // Note: the EPICS_CA_MAX_ARRAY_BYTES property should not be required, since unlimited
      // size is the defaulty behaviour of the library.
      // System.setProperty( ProtocolConfiguration.PropertyNames.EPICS_CA_MAX_ARRAY_BYTES.toString(), "1000000");
      final Properties properties = new Properties();
      properties.setProperty( LibraryConfiguration.PropertyNames.CA_MONITOR_NOTIFIER_IMPL.toString(), epicsCaLibraryMonitorNotifierImpl );
      properties.setProperty( LibraryConfiguration.PropertyNames.CA_LIBRARY_LOG_LEVEL.toString(), String.valueOf( epicsCaLibraryDebugLevel ) );

      caContext = new Context( properties );
      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Gets the metadata associated with a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous GET operation.
    *
    * @param epicsChannelName the name of the channel.
    *
    * @param timeout the timeout to be applied when attempting to get the channel
    *     metadata from the underlying data source. If a timeout occurs the returned
    *     value will be WicaChannelMetadataUnknown.
    * @param timeUnit the time units to be used.
    * @return the metadata.
    */
   public WicaChannelMetadata getChannelMetadata( EpicsChannelName epicsChannelName, long timeout, TimeUnit timeUnit )
   {
      Validate.notNull( epicsChannelName );
      Validate.notNull( timeUnit );
      Validate.isTrue( timeout > 0 );
      Validate.validState( ! closed, "The service was previously closed and can no longer be used." );

      // Create a new autocloseable channel.
      final String channelName = epicsChannelName.asString();
      logger.info( "'{}' - Getting channel metadata...", channelName );
      logger.info( "'{}' - Creating channel...", channelName );
      try( Channel<Object> caChannel = caContext.createChannel(channelName, Object.class ) )
      {
         logger.info( "'{}' - OK: channel created.", channelName );
         logger.info( "'{}' - Connecting channel with timeout {} {}...", channelName, timeout, timeUnit );
         caChannel.connectAsync().get( timeout, timeUnit );
         logger.info( "'{}' - OK: channel connected.", channelName );

         logger.info( "'{}' - Getting channel metadata...", channelName );
         final var result = epicsChannelMetadataGetter.get( caChannel );
         logger.info( "'{}' - OK: channel metadata obtained.", channelName );
         return result;
      }
      catch ( Throwable ex )
      {
         logger.info( "'{}' - ERROR: Exception whilst getting channel metadata. Details: '{}'", channelName, ex.getMessage() );
         return WicaChannelMetadata.createUnknownInstance();
      }
   }

   /**
    * Gets the value of a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous GET operation.
    *
    * @param epicsChannelName the name of the channel.
    *
    * @param timeout the timeout to be applied when attempting to get the channel
    *     value from the underlying data source. If a timeout occurs the returned
    *     value will be WicaChannelValueDisconnected.
    * @param timeUnit the time units to be used.
    * @return the value.
    */
   public WicaChannelValue getChannelValue( EpicsChannelName epicsChannelName, long timeout, TimeUnit timeUnit )
   {
      Validate.notNull( epicsChannelName );
      Validate.notNull( timeUnit );
      Validate.isTrue( timeout > 0 );
      Validate.validState( ! closed, "The service was previously closed and can no longer be used." );

      // Create a new autocloseable channel.
      final String channelName = epicsChannelName.asString();
      logger.info( "'{}' - Getting channel value...", channelName );
      logger.info( "'{}' - Creating channel...", channelName );
      try( Channel<Object> caChannel = caContext.createChannel(channelName, Object.class ) )
      {
         logger.info( "'{}' - OK: channel created.", channelName );
         logger.info( "'{}' - Connecting channel with timeout {} {}...", channelName, timeout, timeUnit );
         caChannel.connectAsync().get( timeout, timeUnit );
         logger.info( "'{}' - OK: channel connected.", channelName );
         logger.info( "'{}' - Getting channel value...", channelName );
         final var result = epicsChannelValueGetter.get( caChannel );
         logger.info( "'{}' - OK: channel value obtained.", channelName );
         return result;
      }
      catch ( Throwable ex )
      {
         logger.info( "'{}' - ERROR: Exception whilst getting channel value. Details: '{}'", channelName, ex.getMessage() );
         return WicaChannelValue.createChannelValueDisconnected();
      }
   }

   /**
    * Sets the value of a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous (= confirmed) PUT operation.
    *
    * @param epicsChannelName the channel name.
    * @param channelValue the channel value.
    * @param timeout the timeout to be applied when attempting to put the channel
    *     value to the underlying data source. If a timeout occurs the returned
    *     value will be false.
    * @param timeUnit the time units to be used.
    * @return boolean set true when the put completed successfully.
    * @throws NullPointerException if any of the reference object arguments were null.
    */
   public boolean putChannelValue( EpicsChannelName epicsChannelName, String channelValue, long timeout, TimeUnit timeUnit )
   {
      Validate.notNull( epicsChannelName );
      Validate.notNull( channelValue );
      Validate.notNull( timeUnit );
      Validate.isTrue( timeout > 0 );
      Validate.validState( ! closed, "The service was previously closed and can no longer be used." );

      // Create a new autocloseable channel.
      final String channelName = epicsChannelName.asString();
      logger.info( "'{}' - Setting channel value to '{}' ...", channelName, channelValue );
      logger.info( "'{}' - Creating channel...", channelName );
      try( Channel<String> caChannel = caContext.createChannel(channelName, String.class ) )
      {
         logger.info( "'{}' - OK: channel created.", channelName );

         logger.info( "'{}' - Connecting channel with timeout {} {}...", channelName, timeout, timeUnit );
         caChannel.connectAsync().get( timeout, timeUnit );
         logger.info( "'{}' - OK: channel connected.", channelName );

         logger.info( "'{}' - Putting to channel with timeout {} {}...", channelName, timeout, timeUnit );
         caChannel.putAsync( channelValue ).get( timeout, timeUnit );
         logger.info( "'{}' - OK: Channel PUT completed.", channelName );
      }
      catch ( Throwable ex )
      {
         logger.info( "'{}' - ERROR: Exception whilst getting channel value. Details: '{}'", channelName, ex.getMessage() );
         return false;
      }

      return true;
   }

   /**
    * Disposes of all resources associated with this class instance.
    */
   @Override
   public void close()
   {
      // Set a flag to prevent further usage
      closed = true;

      // Dispose of any references that are no longer required
      logger.debug( "'{}' - disposing resources...", this );

      // Note: closing the context disposes of any open channels.
      caContext.close();

      logger.debug( "'{}' - resources disposed ok.", this );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
