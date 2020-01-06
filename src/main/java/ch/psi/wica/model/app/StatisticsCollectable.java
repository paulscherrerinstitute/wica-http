/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/

import java.util.ArrayList;
import java.util.List;

/*- Interface Declaration ----------------------------------------------------*/

public interface StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   Statistics get();
   void clearEntries();

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   class Statistics
   {
      private final String header;
      private final List<StatisticsItem> entries;

      public Statistics( String header, List<StatisticsItem> entries )
      {
         this.header = header;
         this.entries = List.copyOf( entries );
      }

      public String getHeader()
      {
         return header;
      }

      public List<StatisticsItem> getEntries()
      {
         return entries;
      }
   }

   class StatisticsItem
   {
      private final String key;
      private final String value;

      public StatisticsItem( String key, String value )
      {
         this.key = key;
         this.value = value;
      }

      public String getKey()
      {
         return key;
      }

      public String getValue()
      {
         return value;
      }

      public String toString()
      {
         return "- " + getKey() + ":" + getValue();
      }
   }

}