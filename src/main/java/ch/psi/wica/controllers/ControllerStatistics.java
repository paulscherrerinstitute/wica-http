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

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/


/**
 * Provides statistics related to the Wica Controllers.
 */
@ThreadSafe
public class ControllerStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String statisticsHeader;

   private final List<String> clientIpAddrList = Collections.synchronizedList( new ArrayList<>());
   private final AtomicInteger requests = new AtomicInteger(0);
   private final AtomicInteger replies = new AtomicInteger(0);
   private final AtomicInteger errors = new AtomicInteger(0);


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new instance with the specified header.
    *
    * @param statisticsHeader the header.
    */
   public ControllerStatistics( String statisticsHeader )
   {
      this.statisticsHeader = statisticsHeader;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( statisticsHeader, List.of( new StatisticsItem("- Requests", getRequests() ),
                                                        new StatisticsItem("- Replies", getReplies() ),
                                                        new StatisticsItem("- Errors", getErrors() ),
                                                        new StatisticsItem("- Clients", getClientIpAddrList() )
      ) );
   }

   @Override
   public void reset()
   {
      requests.set( 0 );
      replies.set( 0 );
      errors.set( 0 );
      clientIpAddrList.clear();
   }

/*- Package-access methods ---------------------------------------------------*/

   /**
    * Increments the count of requests.
    */
   void incrementRequests()
   {
      requests.incrementAndGet();
   }

   /**
    * Increments the count of replies.
    */
   void incrementReplies()
   {
      replies.incrementAndGet();
   }

   /**
    * Increments the count of errors.
    */
   void incrementErrors()
   {
      errors.incrementAndGet();
   }

   /**
    * Adds the supplied client IP to the list of collected IPs.
    *
    * @param clientIpAddr the IP to add.
    */
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
              .map( this::getFormattedHostNameFromIP)
              .distinct().toList();

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
