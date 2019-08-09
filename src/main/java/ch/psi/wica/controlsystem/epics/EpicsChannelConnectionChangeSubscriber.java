/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class EpicsChannelConnectionChangeSubscriber
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelConnectionChangeSubscriber.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Registers a handler which will publish connection changed events.
    *
    * This method operates synchronously but does NOT involve a network
    * round trip.
    *
    * Precondition: the supplied channel should never previously have
    *     been connected.
    * Postcondition: the state of the channel will remain unaffected.
    *
    * @param channel the EPICS channel.
    * @param connectionChangeHandler the event consumer.
    *
    * @throws NullPointerException if the channel argument was null.
    * @throws NullPointerException if the connectionChangeHandler argument was null.
    * @throws IllegalStateException if the channel was in an unexpected state on
    *    entry or exit.
    */
   void subscribe( Channel<Object> channel, Consumer<Boolean> connectionChangeHandler )
   {
      // Validate preconditions
      Validate.notNull( channel );
      Validate.notNull( connectionChangeHandler );
      Validate.isTrue( channel.getConnectionState() == ConnectionState.NEVER_CONNECTED, "Programming Error: The channel was not in the expected state (NEVER_CONNECTED)" );

      // Obtain the control system name for logging purposes.
      final ControlSystemName controlSystemName = ControlSystemName.of(channel.getName());

      logger.info("'{}' - adding connection change handler...", controlSystemName );
      channel.addConnectionListener(( chan, isConnected ) -> {

         logger.info("'{}' - publishing new connection state: '{}'", controlSystemName, isConnected );
         connectionChangeHandler.accept( isConnected );
         logger.info("'{}' - published new connection state ok.", controlSystemName );
      });

      logger.info("'{}' - connection change handler added.", controlSystemName );

      // Validate postconditions
      Validate.isTrue( channel.getConnectionState() == ConnectionState.NEVER_CONNECTED, "Programming Error: The channel was not in the expected state (NEVER_CONNECTED)" );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
