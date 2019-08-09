/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the data acquisition mode for a channel. This can be a channel
 * that monitors the underlying control system variables, that polls them
 * at a predefined rate or which polls the last cached value received
 * when monitoring.
 */
public enum WicaDataAcquisitionMode
{

/*- Public attributes --------------------------------------------------------*/

   POLL             ("poll",true, false ),
   MONITOR          ("monitor",false, true ),
   POLL_AND_MONITOR ("poll-and-monitor",true, true );


/*- Private attributes -------------------------------------------------------*/

   private final String name;
   private final boolean doesPolling;
   private final boolean doesMonitoring;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaDataAcquisitionMode( String name, boolean doesPolling, boolean doesMonitoring )
   {
      this.name = name;
      this.doesPolling = doesPolling;
      this.doesMonitoring = doesMonitoring;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public boolean doesPolling()
   {
      return doesPolling;
   }
   public boolean doesMonitoring()
   {
      return doesMonitoring;
   }

   @Override
   public String toString()
   {
      return name;
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
