/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStreamLifecycleStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String statisticsHeader;
   private final AtomicInteger streamsCreated = new AtomicInteger(0);
   private final AtomicInteger streamsDeleted = new AtomicInteger(0);



/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamLifecycleStatistics( String statisticsHeader )
   {
      this.statisticsHeader = statisticsHeader;
   }


/*- Class methods ------------------------------------------------------------*/

/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( statisticsHeader, List.of( new StatisticsItem("- Streams Created", getStreamsCreated() ),
                                                        new StatisticsItem("- Streams Deleted", getStreamsDeleted() ) )
      );
   }

   @Override
   public void reset()
   {
      streamsCreated.set( 0 );
      streamsDeleted.set( 0 );
   }

/*- Package-access methods ---------------------------------------------------*/

   void incrementStreamsCreated()
   {
      streamsCreated.incrementAndGet();
   }
   void incrementStreamsDeleted()
   {
      streamsDeleted.incrementAndGet();
   }

/*- Private methods ----------------------------------------------------------*/

   private String getStreamsCreated()
   {
      return String.valueOf(streamsCreated.get());
   }

   private String getStreamsDeleted()
   {
      return String.valueOf(streamsDeleted.get());
   }


/*- Nested Classes -----------------------------------------------------------*/

}
