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

   private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   private final LocalDateTime serverStartTime;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public ServerStatistics()
   {
      this.serverStartTime = LocalDateTime.now();
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String getTimeAndDateNow()
   {
      return LocalDateTime.now().format( FORMATTER );
   }

   public String getServerStartTime()
   {
      return serverStartTime.format( FORMATTER );
   }

   public long getUpTimeInSeconds()
   {
      return Duration.between( serverStartTime , LocalDateTime.now() ).getSeconds();
   }

   @Override
   public Statistics get()
   {
      final long serverUpTimeInSeconds = getUpTimeInSeconds();
      final String formattedServerUpTime = String.format( "%d days, %02d hours, %02d minutes, %02d seconds",
                                                          ( serverUpTimeInSeconds / 86400 ),        // days
                                                          ( serverUpTimeInSeconds % 86400 ) / 3600, // hours
                                                          ( serverUpTimeInSeconds % 3600  )  / 60,  // minutes
                                                          ( serverUpTimeInSeconds % 60    ) );      // seconds

      return new Statistics( "SERVER", List.of( new StatisticsItem( "- Time Now", getTimeAndDateNow() ),
                                                        new StatisticsItem( "- Server Started", getServerStartTime() ),
                                                        new StatisticsItem( "- Server Uptime", formattedServerUpTime ) ) );
   }

   @Override
   public void clearEntries()
   {
      // Nothing to do here
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
