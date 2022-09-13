/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;
import org.epics.ca.Context;
import org.epics.ca.impl.LibraryConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Supplies an EPICS Channel Access context based on the configured application
 * properties.
 */
@Component
public class EpicsChannelAccessContextSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String epicsCaLibraryMonitorNotifierImpl;
   private final int epicsCaLibraryDebugLevel;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance.
    *
    * @param epicsCaLibraryMonitorNotifierImpl the CA library monitor notifier configuration.
    * @param epicsCaLibraryDebugLevel          the CA library debug level.
    */
   public EpicsChannelAccessContextSupplier( @Value( "${wica.epics-ca-library-monitor-notifier-impl}") String epicsCaLibraryMonitorNotifierImpl,
                                             @Value( "${wica.epics-ca-library-debug-level}") int epicsCaLibraryDebugLevel )
   {
      this.epicsCaLibraryMonitorNotifierImpl = Validate.notNull( epicsCaLibraryMonitorNotifierImpl );
      this.epicsCaLibraryDebugLevel = epicsCaLibraryDebugLevel;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public Context getContextForScope( String scope )
   {
      final Context context;
      switch ( scope )
      {
         case "monitored":
            context = getContextForMonitoredChannelScope();
            break;

         case "polled":
            context = getContextForPolledChannelScope();
            break;

         case "io":
            context = getContextForIoChannelScope();
            break;

         default:
            throw new UnsupportedOperationException( "The context scope was not recognised." );
      }
      return context;
   }

/*- Private methods ----------------------------------------------------------*/

   private Context getContextForMonitoredChannelScope()
   {
      return getSharedChannelAccessContext();
   }

   private Context getContextForPolledChannelScope()
   {
      return getSharedChannelAccessContext();
   }

   private Context getContextForIoChannelScope()
   {
      return getSharedChannelAccessContext();
   }

   private Context getSharedChannelAccessContext()
   {
      final Properties properties = new Properties();
      properties.setProperty( LibraryConfiguration.PropertyNames.CA_MONITOR_NOTIFIER_IMPL.toString(), this.epicsCaLibraryMonitorNotifierImpl );
      properties.setProperty( LibraryConfiguration.PropertyNames.CA_LIBRARY_LOG_LEVEL.toString(), String.valueOf( this.epicsCaLibraryDebugLevel ) );

      //System.setProperty( "EPICS_CA_ADDR_LIST", "192.168.0.46:5064" );
      //System.setProperty( "EPICS_CA_ADDR_LIST", "129.129.145.206:5064" );
      //System.setProperty( "EPICS_CA_ADDR_LIST", "proscan-cagw:5062" );

      return new Context( properties );
   }


/*- Nested Classes -----------------------------------------------------------*/

}
