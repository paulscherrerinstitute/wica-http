/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class ServerStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final LocalDateTime serverStartTime;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public ServerStatistics()
   {
      this.serverStartTime = LocalDateTime.now();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<StatisticsEntry> getEntries()
   {
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      final String formattedTimeAndDateNow =  LocalDateTime.now().format(formatter );
      final String formattedServerStartTime = serverStartTime.format( formatter );
      final long serverUpTimeInSeconds= Duration.between( serverStartTime , LocalDateTime.now() ).getSeconds();
      final String formattedServerUpTime = String.format( "%d days, %02d hours, %02d minutes, %02d seconds",
                                                          ( serverUpTimeInSeconds / 86400 ),        // days
                                                          ( serverUpTimeInSeconds % 86400 ) / 3600, // hours
                                                          ( serverUpTimeInSeconds % 3600  )  / 60,  // minutes
                                                          ( serverUpTimeInSeconds % 60    ) );      // seconds

      return List.of(
            new StatisticsHeader( "SERVER:" ),
            new StatisticsItem( "Time Now", formattedTimeAndDateNow ),
            new StatisticsItem( "Server Started", formattedServerStartTime ),
            new StatisticsItem( "Server Uptime", formattedServerUpTime )
      );
   }

   @Override
   public void clearEntries()
   {
      // Nothing to do here
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
