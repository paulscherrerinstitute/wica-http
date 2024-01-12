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

   /**
    * Periodically sends a network request to the underlying control system asking it to provide updated
    * information on the dynamically-changing properties of the control point. The information that is
    * obtained is configurable but will usually include the current value of the control point. Additionally
    * it is possible to request the control point's timestamp and alarm state.
    * <p>
    * The received information is internally buffered by the Wica Server and sent out in batches as individual
    * Server-Sent-Event (SSE) messages within the overall Wica Stream. The size of the polled-value buffer is
    * configurable on the server by the system administrator. The rate at which the polled values are sent out
    * in batches is configurable by the user as a property of each stream.
    * <p>
    * Unlike values obtained by monitoring, values obtained by polling are NOT filterable. That's to say each
    * polled value will be directly represented in the Wica Stream's SSE update messages.
    * <p>
    * This mode allows the user to acquire information with the required update latency by configuration
    * of the appropriate polling interval (but with consequential increase in the network demand).
    * @see ch.psi.wica.model.stream.WicaStreamPropertiesDefaults#DEFAULT_POLLED_VALUE_FLUX_INTERVAL_IN_MILLIS
    */
   POLL ("poll",true, false, false, false, true ),

   /**
    * Sends a single network request to the underlying control system asking it to provide notification
    * of interesting changes to the dynamically-changing information associated with the control point.
    * <p>
    * Interesting changes will usually include changes to the control point's current value (possibly
    * outside certain defined limits) and additionally changes to the control point's alarm state.
    * <p>
    * Notified values are internally buffered on the Wica Server, optionally FILTERED (if an appropriate
    * rule has been defined), and published in batches as individual Server-Sent-Event (SSE) messages on
    * the Wica Stream.
    * <p>
    * The size of the internal monitored-value buffer is configured on the server by the system administrator.
    * The rate at which the monitored values are sent out in batches is configurable by the user as a
    * property of the stream.
    * <p>
    * This mode is economical with network usage whilst optimising the latency with which updated
    * information is posted to the Wica Stream.
    * @see ch.psi.wica.model.stream.WicaStreamPropertiesDefaults#DEFAULT_MONITORED_VALUE_FLUX_INTERVAL_IN_MILLIS
    */
   MONITOR ("monitor",false, true, true, false, false ),

   /**
    * Samples the dynamically-changing properties of the control point using a combination of POLL and MONITOR
    * modes. By selection of an appropriate polling interval this mode can guarantee that the Wica Stream
    * publishes a new value at least as often as every X milliseconds, seconds, minutes etc.
    * <p>
    * This mode can be useful for updating a graphical plot of control point values at a periodic rate
    * even when the value is not changing in the control system (that's to say when no monitor notifications
    * are being posted). Another use case is to periodically verify that the control system monitoring is
    * still working as expected (that's to say that the monitors on the control-points-of-interest have
    * not quietly died).
    *
    * @see WicaDataAcquisitionMode#POLL
    * @see WicaDataAcquisitionMode#MONITOR
    */
   POLL_AND_MONITOR  ("poll-and-monitor", true, true, true, false, true ),

   // TODO consider replacing this acquisition mode with a minimum-rate filter.
   /**
    * Sends a single network request to the underlying control system asking it to provide notification
    * of interesting changes to the dynamically-changing information associated with the control point.
    * Internally buffers the most recently received notification information but does NOT publish this
    * directly on the Wica Stream. Periodically samples the value that is buffered on the server, and
    * publishes it as part of the Wica Stream's polled value flux.
    */
   POLL_MONITOR ("poll-monitor", true, true, false, true, false );


   // TODO: Move this information into the control system support documentation. See EPICS.md
   // EPICS Implementation Notes on Channel Monitoring: Credit EPICS Record Reference Manual
   //
   // The ADEL and MDEL database field can bee used to determine when to send monitors placed on the VAL field.
   // The monitors are sent when the value field exceeds the last monitored field by the appropriate deadband. If
   // these fields have a value of zero, everytime the value changes, a monitor will be triggered; if they have
   // a value of -1, everytime the record is scanned, monitors are triggered. The ADEL field is used by archive
   // monitors and the MDEL field for all other types of monitors.

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

   @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
