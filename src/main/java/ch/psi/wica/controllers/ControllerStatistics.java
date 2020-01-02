/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class ControllerStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String statisticsHeader;

   private final List<String> clientIpAddrList = Collections.synchronizedList(new ArrayList<>());
   private final AtomicInteger requests = new AtomicInteger(0);
   private final AtomicInteger replies = new AtomicInteger(0);
   private final AtomicInteger errors = new AtomicInteger(0);


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public ControllerStatistics( String statisticsHeader )
   {
      this.statisticsHeader = statisticsHeader;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public List<StatisticsEntry> getEntries()
   {
      return List.of(
            new StatisticsHeader( statisticsHeader ),
            new StatisticsItem("Requests", getRequests() ),
            new StatisticsItem("Replies", getReplies() ),
            new StatisticsItem("Errors", getErrors() ),
            new StatisticsItem("Clients", getClientIpAddrList() )
      );
   }

   @Override
   public void clearEntries()
   {
      requests.set( 0 );
      replies.set( 0 );
      errors.set( 0 );
      clientIpAddrList.clear();
   }

/*- Package-access methods ---------------------------------------------------*/

   void incrementRequests()
   {
      requests.incrementAndGet();
   }
   void incrementReplies()
   {
      replies.incrementAndGet();
   }
   void incrementErrors()
   {
      errors.incrementAndGet();
   }
   void addClientIpAddr( String clientIpAddr )
   {
      clientIpAddrList.add(clientIpAddr );
   }

/*- Private methods ----------------------------------------------------------*/

   private String getRequests()
   {
      return String.valueOf( requests.get());
   }

   private String getReplies()
   {
      return String.valueOf( replies.get());
   }

   private String getErrors()
   {
      return String.valueOf( errors.get());
   }

   private String getClientIpAddrList()
   {
      final List<String> clients = clientIpAddrList.stream()
            .map( this::getFormattedHostNameFromIP )
            .collect( Collectors.toList() );

      return clients.toString();
   }


   private String getFormattedHostNameFromIP( String ipAddress )
   {
      if ( ipAddress.isEmpty() )
      {
         return "";
      }

      try
      {
         final InetAddress inetAddress = InetAddress.getByName(ipAddress );
         return ipAddress + " ("  + inetAddress.getCanonicalHostName() + ")";
      }
      catch( Exception ex )
      {
         return ipAddress;
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
