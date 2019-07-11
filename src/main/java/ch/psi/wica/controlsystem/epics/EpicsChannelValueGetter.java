/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the functionality to build and return a WicaChannelValue object
 * using information obtained by querying a connected EPICS channel.
 */
@Immutable
@Component
class EpicsChannelValueGetter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelValueGetter.class );
   private final WicaChannelValueBuilder wicaChannelValueBuilder;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance that will work with the specified value
    * builder object.
    *
    * @param wicaChannelValueBuilder the builder.
    */
   public EpicsChannelValueGetter( @Autowired WicaChannelValueBuilder wicaChannelValueBuilder )
   {
      this.wicaChannelValueBuilder = Validate.notNull( wicaChannelValueBuilder );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Queries the supplied EPICS channel to return a WicaChannelValue object
    * which encapsulates the properties of the channel which may change quickly
    * (eg instantaneous value, timestamp and alarm severity etc).
    *
    * This method operates synchronously and incurs the cost of a network
    * round trip to obtain the information fom the remote data source.
    *
    * Precondition: the supplied channel should already be connected.
    * Postcondition: the supplied channel will remain open.
    *
    * @param channel the EPICS channel.
    * @return the wica value object
    *
    * @throws NullPointerException if the channel argument was null.
    * @throws IllegalStateException if the channel was not already connected.
    */
   WicaChannelValue get( Channel<Object> channel  )
   {
      Validate.notNull( channel );
      Validate.isTrue( channel.getConnectionState() == ConnectionState.CONNECTED );

      // Obtain the control system name for logging purposes.
      final ControlSystemName controlSystemName = ControlSystemName.of(channel.getName());

      // Perform a channel GET request to obtain the properties of the channel which
      // may change quickly. Currently (2019-07-11) this includes the current value,
      // the timestamp the and alarm state.
      logger.debug( "'{}' - getting epics TIMESTAMPED metadata...", controlSystemName );
      final Timestamped<Object> epicsTimestampedObject = channel.get( Timestamped.class );
      logger.debug( "'{}' - EPICS TIMESTAMPED metadata received.", controlSystemName) ;

      // Now construct and return a wica value object using the timestamped object information.
      return wicaChannelValueBuilder.build( controlSystemName, epicsTimestampedObject );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
