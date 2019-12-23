/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class ControllerStatisticsCollector
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   private final List<String> clients = Collections.synchronizedList(new ArrayList<>());
   private final AtomicInteger requests = new AtomicInteger(0);
   private final AtomicInteger replies = new AtomicInteger(0);
   private final AtomicInteger errors = new AtomicInteger(0);


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public String getRequests()
   {
      return String.valueOf(requests.get());
   }
   public String getReplies()
   {
      return String.valueOf(replies.get());
   }
   public String getErrors()
   {
      return String.valueOf(errors.get());
   }
   public void incrementRequests()
   {
      requests.incrementAndGet();
   }
   public void incrementReplies()
   {
      replies.incrementAndGet();
   }
   public void incrementErrors()
   {
      errors.incrementAndGet();
   }
   public List<String> getClients()
   {
      return clients;
   }

   public void addClient( String client )
   {
      clients.add( client );
   }

   public void reset()
   {
      requests.set( 0 );
      replies.set( 0 );
      errors.set( 0 );
      clients.clear();
   }

   public String getSummary()
   {
      return getRequests() + "/"  + getReplies() + "/" + getErrors();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}