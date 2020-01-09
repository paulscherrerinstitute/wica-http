/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelType;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.epics.ca.Channel;
import org.epics.ca.ConnectionState;
import org.epics.ca.data.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides the functionality to build and return a WicaChannelMetadata object
 * using information obtained by querying a connected EPICS channel.
 */
@Immutable
@Component
class EpicsChannelMetadataGetter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelMetadataGetter.class );
   private final WicaChannelMetadataBuilder wicaChannelMetadataBuilder;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Returns a new instance that will work with the specified metadata
    * builder object.
    *
    * @param wicaChannelMetadataBuilder the builder.
    */
   public EpicsChannelMetadataGetter( @Autowired WicaChannelMetadataBuilder wicaChannelMetadataBuilder )
   {
      this.wicaChannelMetadataBuilder = Validate.notNull( wicaChannelMetadataBuilder );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Queries the supplied EPICS channel to return a WicaChannelMetadata object
    * which encapsulates the properties of the channel remain relatively fixed
    * (eg alarm limits, control limits, precision, engineering units... etc).
    *
    * This method operates synchronously and incurs the cost of a network
    * round trip to obtain the information fom the remote data source.
    *
    * Precondition: the channel should have been connected at least once.
    * Postcondition: the state of the channel will remain unaffected.
    *
    * @param channel the EPICS channel.
    * @return the wica metadata object.
    *
    * @throws NullPointerException if the channel argument was null.
    * @throws IllegalStateException if the channel state was not as expected.
    */
   public WicaChannelMetadata get( Channel<Object> channel )
   {
      // Validate preconditions
      validateChannelConnectionState( channel );

      // Obtain the control system name for logging purposes.
      final ControlSystemName controlSystemName = ControlSystemName.of( channel.getName() );

      // Note:
      // The code below uses a somewhat inefficient two-step approach to getting the channel
      // metadata because an attempt to get the CTRL information directly from a channel
      // whose type does not support it (here's looking at you STRING, STRING_ARRAY)
      // results in a protocol hang.

      // STEP 1:
      // For all channel types...
      // Perform an initial, simple, channel GET request to determine the data source type.
      // This operation is supported on all EPICS data types.
      logger.trace( "'{}' - getting first value...", controlSystemName);
      final Object firstGetObject = channel.get();

      getNativeType( channel );

      // Decode the channel type.
      final WicaChannelType wicaChannelType;
      try
      {
         wicaChannelType = WicaChannelType.getTypeFromPojo( firstGetObject );
         logger.trace( "'{}' - first value received was of type {}. ", controlSystemName, wicaChannelType );
      }
      catch( IllegalArgumentException ex)
      {
         logger.error( "'{}' - type was UNKNOWN (Programming Error)", controlSystemName );
         return WicaChannelMetadata.createUnknownInstance();
      }

      // For string types there is no further information to be obtained
      // so construct and return a metadata object immediately.
      if  ( ( wicaChannelType == WicaChannelType.STRING ) || (wicaChannelType == WicaChannelType.STRING_ARRAY ) )
      {
         return wicaChannelMetadataBuilder.build( controlSystemName, wicaChannelType, null );
      }

      // STEP 2:
      // For channel types which support it...
      // Perform a second, more complex, channel GET request to obtain the properties
      // of the channel which remain fixed. Currently (2019-07-11) this includes the
      // control and alarm information and engineering units.
      logger.trace( "'{}' - getting epics CTRL metadata...", controlSystemName );
      final Control<?,?> epicsControlObject = (Control<?,?>) channel.get( Control.class );
      logger.trace( "'{}' - EPICS CTRL metadata received.", controlSystemName) ;

      // Now construct and return a wica metadata object using the control object information.
      return wicaChannelMetadataBuilder.build( controlSystemName, wicaChannelType, epicsControlObject );
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

   private WicaChannelType getNativeType( Channel<Object> channel )
   {
      final Map<String,Object> props = channel.getProperties();

      return null;
   }

/*- Nested Classes -----------------------------------------------------------*/

}
