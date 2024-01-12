/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import ch.psi.wica.model.channel.value.WicaChannelValueBuilder;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.data.Timestamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the functionality to build and return a WicaChannelValue object
 * using information obtained by querying a connected EPICS channel.
 */
@Immutable
@Component
public class EpicsChannelValueGetter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelValueGetter.class );
   private final WicaChannelValueCreator wicaChannelValueCreator;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance that will work with the specified value
    * builder object.
    *
    * @param wicaChannelValueCreator the builder.
    */
   public EpicsChannelValueGetter( @Autowired WicaChannelValueCreator wicaChannelValueCreator )
   {
      this.wicaChannelValueCreator = Validate.notNull( wicaChannelValueCreator, "The 'wicaChannelValueBuilder' argument is null." );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Performs a channel-access GET operation on the supplied EPICS channel
    * and returns a WicaChannelValue object which encapsulates the properties
    * of the channel which may change quickly (eg online/offline state,
    * instantaneous value, timestamp, alarm severity etc).
    * <p>
    * This method operates synchronously and potentially incurs the cost of
    * a network round trip to obtain the information from the remote data
    * source.
    * <p>
    * If the channel is already offline when this method is invoked then
    * the returned value will be returned immediately to indicate that the
    * channel is disconnected. However, if the channel goes offline
    * just at the moment it is queried then in the worst case this method
    * will incur the cost of the channel-access GET operation timeout,
    * which may be several seconds.
    * <p>
    * Precondition: the channel should have been connected at least once.
    * Postcondition: the state of the channel will remain unaffected.
    *
    * @param channel the EPICS channel to query.
    *
    * @param timeout the timeout to be applied when attempting to get the channel
    *     value from the underlying data source. If a timeout occurs the returned
    *     value will be WicaChannelMetadataUnknown.
    *
    * @param timeUnit the time units to be used.
    *
    * @return the object created as a result of the channel access GET
    *
    * @throws NullPointerException if the channel argument was null.
    * @throws IllegalStateException if the channel state was not as expected.
    *
    * @throws InterruptedException if the channel get operation was interrupted.
    * @throws TimeoutException if the channel get operation timed out.
    * @throws ExecutionException if the channel get operation through failed
    */
   public WicaChannelValue get( Channel<Object> channel, long timeout, TimeUnit timeUnit ) throws InterruptedException, TimeoutException, ExecutionException
   {
      Validate.notNull( channel, "The 'channel' argument is null." );
      Validate.notNull( timeUnit, "The 'timeUnit' argument is null." );
      Validate.validState( channel.getConnectionState() != ConnectionState.NEVER_CONNECTED, "Programming Error: the channel was never connected" );

      // Obtain the control system name for logging purposes.
      final ControlSystemName controlSystemName = ControlSystemName.of(channel.getName());

      // Optimisation: immediately return channel disconnected value if we know
      // already that the channel is offline.
      if ( channel.getConnectionState() != ConnectionState.CONNECTED )
      {
         logger.trace( "'{}' - channel is offline, returning disconnected value.", controlSystemName );
         return WicaChannelValueBuilder.createChannelValueDisconnected();
      }

      // Perform a channel GET request to obtain the properties of the channel which
      // may change quickly. Currently (2019-07-11) this includes the current value,
      // the timestamp the and alarm state.
      logger.trace( "'{}' - getting EPICS TIMESTAMPED data...", controlSystemName );
      final var completableFuture = channel.getAsync( Timestamped.class );
      final var metadataObj = completableFuture.get( timeout, timeUnit );
      final var timestampedObj = (Timestamped<Object>) metadataObj;
      logger.trace( "'{}' - EPICS TIMESTAMPED data received.", controlSystemName );

      // Now construct and return a wica value object using the timestamped object information.
      return wicaChannelValueCreator.build( controlSystemName, timestampedObj );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
