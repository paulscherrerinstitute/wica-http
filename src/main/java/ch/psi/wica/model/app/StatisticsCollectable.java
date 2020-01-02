/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/

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

   List<StatisticsEntry> getEntries();
   void clearEntries();

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   class StatisticsEntry {}

   class StatisticsHeader extends StatisticsEntry
   {
      private final String header;

      public StatisticsHeader( String header )
      {
         this.header = header;
      }

      public String getHeader()
      {
         return header;
      }

      public String toString()
      {
         return getHeader();
      }
   }

   class StatisticsItem extends StatisticsEntry
   {
      private final String item;
      private final String value;

      public StatisticsItem( String item, String value )
      {
         this.item = item;
         this.value = value;
      }

      public String getItem()
      {
         return item;
      }

      public String getValue()
      {
         return value;
      }

      public String toString()
      {
         return "- " + getItem() + ":" + getValue();
      }
   }

}