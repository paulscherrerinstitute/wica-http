/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.ControlSystemName;
import ch.psi.wica.model.WicaChannelType;
import ch.psi.wica.model.WicaChannelValue;
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
class EpicsChannelValueChangeSubscriber
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelValueChangeSubscriber.class );
   private final WicaChannelValueBuilder wicaChannelValueBuilder;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance that will work with the specified value
    * builder object.
    *
    * @param wicaChannelValueBuilder the builder.
    */
   public EpicsChannelValueChangeSubscriber( @Autowired WicaChannelValueBuilder wicaChannelValueBuilder )
   {
      this.wicaChannelValueBuilder = Validate.notNull( wicaChannelValueBuilder );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Establishes a monitor on the supplied EPICS channel and registers a
    * handler which should subsequently be informed when the channel value
    * changes.
    *
    * This method operates synchronously and incurs the cost of a network
    * round trip to establish the monitor on the remote data source.
    *
    * Precondition: the supplied channel should already be connected.
    * Postcondition: the supplied channel will remain open.
    *
    * @param channel the EPICS channel.
    * @param valueChangeHandler the event consumer.
    * @return the EPICS monitor.
    *
    * @throws NullPointerException if the channel argument was null.
    * @throws IllegalStateException if the channel was not already connected.
    */
   Monitor<Timestamped<Object>> subscribe( Channel<Object> channel, Consumer<WicaChannelValue> valueChangeHandler )
   {
      Validate.notNull( channel );
      Validate.isTrue( channel.getConnectionState() == ConnectionState.CONNECTED );

      // Obtain the control system name for logging purposes.
      final ControlSystemName controlSystemName = ControlSystemName.of(channel.getName());

      // Establish a monitor on the most "dynamic" (= frequently changing)
      // properties of the channel.  These include the timestamp, the alarm
      // information and value itself. In EPICS Channel Access this requirement
      // is fulfilled by the DBR_TIME_xxx type which is supported in PSI's
      // CA library via the Metadata<Timestamped> class.
      logger.debug("'{}' - adding monitor...", controlSystemName );

      final Monitor<Timestamped<Object>> monitor = channel.addMonitor( Timestamped.class, wicaChannelValueObj -> {
         logger.trace("'{}' - publishing new value...", controlSystemName );
         final var wicaChannelValue = wicaChannelValueBuilder.build( controlSystemName, wicaChannelValueObj );
         valueChangeHandler.accept( wicaChannelValue );
         logger.trace("'{}' - new value published.", controlSystemName );
      } );
      logger.debug("'{}' - monitor added.", controlSystemName );
      return monitor;

   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
