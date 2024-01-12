/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.monitor;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.WicaChannelValueCreator;
import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.Monitor;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the functionality to monitor the specified EPICS channel and
 * to publish updating channel value information to listening consumers.
 */
@Immutable
@Component
class EpicsChannelMonitorSubscriber
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMonitorSubscriber.class );
   private final WicaChannelValueCreator wicaChannelValueCreator;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance that will work with the specified value
    * builder object.
    *
    * @param wicaChannelValueCreator the builder.
    */
   public EpicsChannelMonitorSubscriber( @Autowired WicaChannelValueCreator wicaChannelValueCreator )
   {
      this.wicaChannelValueCreator = Validate.notNull( wicaChannelValueCreator, "The 'wicaChannelValueCreator' argument is null." );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Establishes a monitor on the supplied EPICS channel and registers a
    * handler which should subsequently be informed when the channel value
    * changes.
    * <p>
    * This method operates synchronously and incurs the cost of a network
    * round trip to establish the monitor on the remote data source.
    * <p>
    * Precondition: the channel should have been connected at least once.
    * Postcondition: the state of the channel will remain unaffected.
    *
    * @param channel the EPICS channel.
    * @param valueChangeHandler the event consumer.
    *
    * @throws NullPointerException if the channel argument was null.
    * @throws IllegalStateException if the channel state was not as expected.
    */
   void subscribe( Channel<Object> channel, Consumer<WicaChannelValue> valueChangeHandler )
   {
      // Validate preconditions
      validateChannelConnectionState( channel );

      // Obtain the control system name for logging purposes.
      final ControlSystemName controlSystemName = ControlSystemName.of(channel.getName());

      // Establish a monitor on the most "dynamic" (= frequently changing)
      // properties of the channel.  These include the timestamp, the alarm
      // information and value itself. In EPICS Channel Access this requirement
      // is fulfilled by the DBR_TIME_xxx type which is supported in PSI's
      // CA library via the Metadata<Timestamped> class.
      logger.info("'{}' - adding monitor...", controlSystemName );

      @SuppressWarnings( "unused" )
      final Monitor<Timestamped<Object>> monitor = channel.addMonitor( Timestamped.class, epicsTimestampedObject -> {
         logger.trace("'{}' - publishing new value...", controlSystemName );
         final var wicaChannelValue = wicaChannelValueCreator.build( controlSystemName, epicsTimestampedObject );
         valueChangeHandler.accept( wicaChannelValue );
         logger.trace("'{}' - new value published.", controlSystemName );
      } );
      logger.info("'{}' - monitor added.", controlSystemName );
   }

/*- Private methods ----------------------------------------------------------*/

   private void validateChannelConnectionState( Channel<Object> channel )
   {
      if ( channel == null )
      {
         throw new NullPointerException( "Programming Error: the channel argument was null." );
      }

      final ConnectionState connectionState = channel.getConnectionState();
      final boolean invalidState = ( connectionState == ConnectionState.NEVER_CONNECTED );
      if ( invalidState )
      {
         throw new IllegalStateException( "Programming Error: the channel was in an unexpected connection state: '" + connectionState + "'." );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
