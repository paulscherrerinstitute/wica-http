/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelValue;
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
    * Precondition: the channel should have been connected at least once.
    * Postcondition: the state of the channel will remain unaffected.
    *
    * @param channel the EPICS channel.
    * @return the wica value object
    *
    * @throws NullPointerException if the channel argument was null.
    * @throws IllegalStateException if the channel state was not as expected.
    */
   WicaChannelValue get( Channel<Object> channel  )
   {
      // Validate preconditions
      validateChannelConnectionState( channel );

      // Obtain the control system name for logging purposes.
      final ControlSystemName controlSystemName = ControlSystemName.of(channel.getName());

      // Perform a channel GET request to obtain the properties of the channel which
      // may change quickly. Currently (2019-07-11) this includes the current value,
      // the timestamp the and alarm state.
      logger.trace( "'{}' - getting EPICS TIMESTAMPED data...", controlSystemName );
      final Timestamped<Object> epicsTimestampedObject = channel.get( Timestamped.class );
      logger.trace( "'{}' - EPICS TIMESTAMPED data received.", controlSystemName ) ;

      // Now construct and return a wica value object using the timestamped object information.
      return wicaChannelValueBuilder.build( controlSystemName, epicsTimestampedObject );
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
