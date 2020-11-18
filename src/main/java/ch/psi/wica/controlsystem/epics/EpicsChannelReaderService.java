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
 * A service which offers the possibility to get the metadata or value
 * associated with an EPICS channel.
 *
 * @implNote.
 * The current implementation uses PSI's CA EPICS client library to create a
 * single shared EPICS CA Context per class instance. The EPICS CA context and
 * all associated resources are disposed of when the service instance is closed.
 */
@Service
@ThreadSafe
public class EpicsChannelReaderService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelReaderService.class );
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
    * @param epicsCaLibraryDebugLevel the CA library debug level.
    * @param epicsChannelMetadataGetter an object that can get and build the returned metadata.
    * @param epicsChannelValueGetter an object that can get and build the returned value.
    */
   public EpicsChannelReaderService( @Value( "${wica.epics-ca-library-debug-level}" ) int epicsCaLibraryDebugLevel,
                                     @Autowired EpicsChannelMetadataGetter epicsChannelMetadataGetter,
                                     @Autowired EpicsChannelValueGetter epicsChannelValueGetter )
   {
      logger.debug( "'{}' - constructing new EpicsChannelGetAndPutService instance...", this );

      this.epicsChannelMetadataGetter = Validate.notNull( epicsChannelMetadataGetter );
      this.epicsChannelValueGetter = Validate.notNull( epicsChannelValueGetter );

      logger.info( "Creating CA context for WicaChannelService..." );

      // Setup a context that uses the debug message log level defined in the configuration file.
      // Note: the EPICS_CA_MAX_ARRAY_BYTES property should not be required, since unlimited
      // size is the default behaviour of the library.
      // System.setProperty( ProtocolConfiguration.PropertyNames.EPICS_CA_MAX_ARRAY_BYTES.toString(), "1000000");
      final Properties properties = new Properties();
      properties.setProperty( LibraryConfiguration.PropertyNames.CA_LIBRARY_LOG_LEVEL.toString(), String.valueOf( epicsCaLibraryDebugLevel ) );

      this.caContext = new Context( properties );

      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the metadata associated with a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous GET operation.
    *
    * @param epicsChannelName the name of the channel.
    *
    * @param timeout the timeout to be applied when attempting to get the channel
    *     metadata from the underlying data source. If a timeout occurs the returned
    *     value will be WicaChannelMetadataUnknown.
    *
    * @param timeUnit the time units to be used.
    *
    * @return the metadata.
    */
   public WicaChannelMetadata readChannelMetadata( EpicsChannelName epicsChannelName, long timeout, TimeUnit timeUnit )
   {
      Validate.notNull( epicsChannelName );
      Validate.notNull( timeUnit );
      Validate.isTrue( timeout > 0 );
      Validate.validState( ! closed, "The service was previously closed and can no longer be used." );

      final String channelName = epicsChannelName.asString();
      logger.info( "'{}' - Reading channel metadata...", channelName );
      try( final Channel<Object> caChannel = this.caContext.createChannel( epicsChannelName.asString(), Object.class ) )
      {
         caChannel.connectAsync().get( timeout, timeUnit);

         logger.info( "'{}' - Getting channel metadata...", channelName );
         final var result = epicsChannelMetadataGetter.get( caChannel );
         logger.info( "'{}' - OK: channel metadata obtained.", channelName );
         return result;
      }
      catch ( Throwable th )
      {
         logger.info( "'{}' - ERROR: Exception whilst getting channel metadata. Details: '{}'.", channelName, th.getMessage() );
         return WicaChannelMetadata.createUnknownInstance();
      }
   }

   /**
    * Returns the value of a channel.
    *
    * This method incurs the network cost of establishing a channel to the remote
    * data source and performing a synchronous GET operation.
    *
    * @param epicsChannelName the name of the channel.
    *
    * @param timeout the timeout to be applied when attempting to get the channel
    *     value from the underlying data source. If a timeout occurs the returned
    *     value will be WicaChannelValueDisconnected.
    *
    * @param timeUnit the time units to be used.
    *
    * @return the value.
    */
   public WicaChannelValue readChannelValue( EpicsChannelName epicsChannelName, long timeout, TimeUnit timeUnit )
   {
      Validate.notNull( epicsChannelName );
      Validate.notNull( timeUnit );
      Validate.isTrue( timeout > 0 );
      Validate.validState( ! closed, "The service was previously closed and can no longer be used." );

      // Create a new autocloseable channel.
      final String channelName = epicsChannelName.asString();
      logger.info( "'{}' - Reading channel value...", channelName );
      try( final Channel<Object> caChannel = this.caContext.createChannel( epicsChannelName.asString(), Object.class ) )
      {
         caChannel.connectAsync().get( timeout, timeUnit);

         logger.info( "'{}' - Getting channel value...", channelName );
         final var result = epicsChannelValueGetter.get( caChannel );
         logger.info( "'{}' - OK: channel value obtained.", channelName );
         return result;
      }
      catch ( Throwable th )
      {
         logger.info( "'{}' - ERROR: Exception whilst getting channel value. Details: '{}'.", channelName, th.getMessage() );
         return WicaChannelValue.createChannelValueDisconnected();
      }
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
