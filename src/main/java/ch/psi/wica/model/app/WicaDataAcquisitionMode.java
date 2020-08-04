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

   POLL              ("poll",true, false, false, false, true ),
   MONITOR           ("monitor",false, true, true, false, false ),
   POLL_AND_MONITOR  ("poll-and-monitor", true, true, false, false, true ),
   POLL_MONITOR      ("poll-monitor", true, true, false,true, false );

/*- Private attributes -------------------------------------------------------*/

   private final String name;
   private final boolean doesPolling;
   private final boolean doesMonitoring;
   private final boolean doesMonitorPublication;
   private final boolean doesMonitorPolling;
   private final boolean doesNetworkPolling;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaDataAcquisitionMode( String name, boolean doesPolling, boolean doesMonitoring, boolean doesMonitorPublication, boolean doesMonitorPolling, boolean doesNetworkPolling )
   {
      this.name = name;
      this.doesPolling = doesPolling;
      this.doesMonitoring = doesMonitoring;
      this.doesMonitorPublication = doesMonitorPublication;
      this.doesMonitorPolling = doesMonitorPolling;
      this.doesNetworkPolling = doesNetworkPolling;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public boolean doesPolling()
   {
      return doesPolling;
   }

   public boolean doesMonitorPublication()
   {
      return doesMonitorPublication;
   }
   public boolean doesMonitoring()
   {
      return doesMonitoring;
   }
   public boolean doesMonitorPolling()
   {
      return doesMonitorPolling;
   }

   public boolean doesNetworkPolling()
   {
      return doesNetworkPolling;
   }

   @Override
   public String toString()
   {
      return name;
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
