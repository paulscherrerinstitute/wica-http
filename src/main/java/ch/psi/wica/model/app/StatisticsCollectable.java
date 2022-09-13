/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/

import java.util.List;

/*- Interface Declaration ----------------------------------------------------*/

/**
 * Provides a mechanism for collecting the statistics associated with various aspects of the Wica server.
 */
public interface StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the statistics associated with the collectable.
    *
    * @return the statistics.
    */
   Statistics get();

   /**
    * Resets the statistics.
    */
   void reset();

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   /**
    * Models some statistics that need to be collected.
    */
   record Statistics(String header, List<StatisticsItem> entries)
   {
      public Statistics(String header, List<StatisticsItem> entries)
      {
         this.header = header;
         this.entries = List.copyOf( entries );
      }

      @Override
      @SuppressWarnings("unused")
      public List<StatisticsItem> entries()
      {
         return entries;
      }
   }

   /**
    * Models a statistics item.
    */
   record StatisticsItem( String key, String value)
   {
      public String toString()
      {
            return "- " + key() + ":" + value();
      }
   }

}