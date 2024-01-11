/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.io;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelAccessContextSupplier;
import ch.psi.wica.controlsystem.epics.channel.EpicsChannelName;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

/**
 * A service which offers the possibility to write to a String value to an
 * EPICS channel.
 *
 * @implNote.
 * The current implementation uses PSI's CA EPICS client library to obtain a
 * single shared EPICS CA Context per class instance. The EPICS CA context and
 * all associated resources are disposed of when the service instance is closed.
 */
@Service
@ThreadSafe
public class EpicsChannelWriterService implements AutoCloseable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelWriterService.class );
   private final Context caContext;
   private boolean closed = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance.
    *
    * @param epicsChannelAccessContextSupplier an object which can be used to obtain a Channel-Access context.
    */
   public EpicsChannelWriterService( @Autowired EpicsChannelAccessContextSupplier epicsChannelAccessContextSupplier)
   {
      logger.debug( "'{}' - constructing new EpicsChannelWriterService instance...", this );

      logger.info( "Getting CA context for EpicsChannelWriterService..." );
      this.caContext = epicsChannelAccessContextSupplier.getContextForScope( "io" );

      logger.debug( "'{}' - service instance constructed ok.", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

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
   public boolean writeStringValue( EpicsChannelName epicsChannelName, String channelValue, long timeout, TimeUnit timeUnit )
   {
      Validate.notNull( epicsChannelName, "The 'epicsChannelName' argument is null." );
      Validate.notNull( channelValue, "The 'channelValue' argument is null." );
      Validate.notNull( timeUnit, "The 'timeUnit' argument is null." );
      Validate.isTrue( timeout > 0 );
      Validate.validState( ! closed, "The service was previously closed and can no longer be used." );

      // Create a new autocloseable channel.
      final String channelName = epicsChannelName.asString();
      logger.info( "'{}' - Setting channel value to '{}' ...", channelName, channelValue );
      logger.info( "'{}' - Creating channel...", channelName );
      try( Channel<String> caChannel = caContext.createChannel( channelName, String.class ) )
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
         logger.info( "'{}' - ERROR: Exception whilst putting channel value. Details: '{}'", channelName, ex.getMessage() );
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
