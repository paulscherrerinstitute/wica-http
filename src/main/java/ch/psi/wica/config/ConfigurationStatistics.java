/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.config;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.StatisticsCollectable;
import net.jcip.annotations.ThreadSafe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides statistics related to the configuration of this Wica Server.
 */
@Component
@ThreadSafe
public class ConfigurationStatistics implements StatisticsCollectable
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final String testLoggingOnStartup;
   private final String epicsCaLibraryMonitorNotifierImpl;
   private final String epicsCaLibraryDebugLevel;
   private final String channelResourceReleaseIntervalInSecs;
   private final String channelPublishMonitorRestarts;
   private final String channelPublishPollerRestarts;
   private final String channelPublishChannelValueInitialState;
   private final String channelPublishChannelMetadataInitialState;
   private final String channelMonitoredValueBufferSize;
   private final String channelPolledValueBufferSize;
   private final String channelGetTimeoutIntervalInMs;
   private final String channelGetNumericScale;
   private final String channelGetValueDefaultFieldsOfInterest;
   private final String channelGetMetadataDefaultFieldsOfInterest;
   private final String channelPutTimeoutIntervalInMs;
   private final String streamQuoteNumericStrings;
   private final String streamMetadataFieldsOfInterest;
   private final String corsAllowedOriginPatterns;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

    /**
    * Creates a new instance based on the supplied configuration values.
    *
    * @param testLoggingOnStartup whether to perform logging test on startup.
    * @param epicsCaLibraryMonitorNotifierImpl the CA library configuration for channel monitors.
    * @param epicsCaLibraryDebugLevel the CA library debug level.
    * @param channelResourceReleaseIntervalInSecs the period for releasing resources that are no longer in use.
    * @param channelPublishMonitorRestarts policy for publishing monitor restarts.
    * @param channelPublishPollerRestarts policy for publishin poller restarts.
    * @param channelMonitoredValueBufferSize the number of values that can be held in the monitored value buffer.
    * @param channelPublishChannelValueInitialState whether a channel's initial value will be published as DISCONNECTED when a channel is first created.
    * @param channelPublishChannelMetadataInitialState whether a channel's initial metadata will be published as UNKNOWN when a channel is first created.
    * @param channelPolledValueBufferSize the number of values that can be held in the control system polled value buffer before older values start getting thrown away.
    * @param channelGetTimeoutIntervalInMs the default timeout in milliseconds to be applied when getting the current value of a wica channel.
    * @param channelGetNumericScale the default number of digits after the decimal point when getting the current value of a wica channel.
    * @param channelGetValueDefaultFieldsOfInterest semicolon separated list specifying the default names of the fields that will be returned when getting the current value of a wica channel.
    * @param channelGetMetadataDefaultFieldsOfInterest semicolon separated list specifying the default names of the fields that will be returned when getting the metadata associated with a wica channel.
    * @param channelPutTimeoutIntervalInMs the default timeout in milliseconds to be applied when putting a new value to a wica channel.
    * @param streamQuoteNumericStrings whether strict JSON compliance should be used when serializing NaN and Infinity values (=true) or whether JSON5 serialization compliance is acceptable (=false).
    * @param streamMetadataFieldsOfInterest the fields of interest that should be serialized when sending the channel metadata.
    * @param corsAllowedOriginPatterns which origin patterns must be present in the http request header in order for a request to be accepted.
    */
   public ConfigurationStatistics( @Value( "${wica.test-logging-on-startup}" ) Boolean testLoggingOnStartup,
                                   @Value( "${wica.epics-ca-library-monitor-notifier-impl}" ) String epicsCaLibraryMonitorNotifierImpl,
                                   @Value( "${wica.epics-ca-library-debug-level}" ) Integer epicsCaLibraryDebugLevel,
                                   @Value( "${wica.channel-resource-release-interval-in-secs}") Integer channelResourceReleaseIntervalInSecs,
                                   @Value( "${wica.channel-publish-monitor-restarts}") Boolean channelPublishMonitorRestarts,
                                   @Value( "${wica.channel-publish-poller-restarts}") Boolean channelPublishPollerRestarts,
                                   @Value( "${wica.channel-monitored-value-buffer-size}" ) Integer channelMonitoredValueBufferSize,
                                   @Value( "${wica.channel-publish-channel-value-initial-state}") Boolean channelPublishChannelValueInitialState,
                                   @Value( "${wica.channel-publish-channel-metadata-initial-state}") Boolean channelPublishChannelMetadataInitialState,
                                   @Value( "${wica.channel-polled-value-buffer-size}" ) Integer channelPolledValueBufferSize,
                                   @Value( "${wica.channel-get-timeout-interval-in-ms}" ) Integer channelGetTimeoutIntervalInMs,
                                   @Value( "${wica.channel-get-numeric-scale}" ) Integer channelGetNumericScale,
                                   @Value( "${wica.channel-get-value-default-fields-of-interest}" ) String channelGetValueDefaultFieldsOfInterest,
                                   @Value( "${wica.channel-get-metadata-default-fields-of-interest}" ) String channelGetMetadataDefaultFieldsOfInterest,
                                   @Value( "${wica.channel-put-timeout-interval-in-ms}" ) Integer channelPutTimeoutIntervalInMs,
                                   @Value( "${wica.stream-quote-numeric-strings}" ) Boolean streamQuoteNumericStrings,
                                   @Value( "${wica.stream-metadata-fields-of-interest}" ) String streamMetadataFieldsOfInterest,
                                   @Value( "${wica.cors-allowed-origin-patterns}" ) String corsAllowedOriginPatterns )
   {
      this.testLoggingOnStartup = String.valueOf( testLoggingOnStartup );
      this.epicsCaLibraryMonitorNotifierImpl = epicsCaLibraryMonitorNotifierImpl;
      this.epicsCaLibraryDebugLevel = String.valueOf( epicsCaLibraryDebugLevel );
      this.channelResourceReleaseIntervalInSecs = String.valueOf( channelResourceReleaseIntervalInSecs );
      this.channelPublishMonitorRestarts = String.valueOf( channelPublishMonitorRestarts );
      this.channelPublishPollerRestarts = String.valueOf( channelPublishPollerRestarts );
      this.channelPublishChannelValueInitialState = String.valueOf( channelPublishChannelValueInitialState );
      this.channelPublishChannelMetadataInitialState = String.valueOf( channelPublishChannelMetadataInitialState );
      this.channelMonitoredValueBufferSize = String.valueOf( channelMonitoredValueBufferSize );
      this.channelPolledValueBufferSize = String.valueOf( channelPolledValueBufferSize );
      this.channelGetTimeoutIntervalInMs = String.valueOf( channelGetTimeoutIntervalInMs );
      this.channelGetNumericScale = String.valueOf( channelGetNumericScale );
      this.channelGetValueDefaultFieldsOfInterest = channelGetValueDefaultFieldsOfInterest;
      this.channelGetMetadataDefaultFieldsOfInterest = channelGetMetadataDefaultFieldsOfInterest;
      this.channelPutTimeoutIntervalInMs = String.valueOf( channelPutTimeoutIntervalInMs );
      this.streamQuoteNumericStrings = String.valueOf( streamQuoteNumericStrings );
      this.streamMetadataFieldsOfInterest = streamMetadataFieldsOfInterest;
      this.corsAllowedOriginPatterns = String.valueOf( corsAllowedOriginPatterns );

   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Statistics get()
   {
      return new Statistics( "SERVER CONFIGURATION", List.of( new StatisticsItem( "- wica.test-logging-on-startup",                         testLoggingOnStartup ),
                                                                      new StatisticsItem( "- wica.epics-ca-library-monitor-notifier-impl",          epicsCaLibraryMonitorNotifierImpl ),
                                                                      new StatisticsItem( "- wica.epics-ca-library-debug-level",                    epicsCaLibraryDebugLevel ),
                                                                      new StatisticsItem( "- wica.channel-resource-release-interval-in-secs",       channelResourceReleaseIntervalInSecs ),
                                                                      new StatisticsItem( "- wica.channel-publish-monitor-restarts",                channelPublishMonitorRestarts ),
                                                                      new StatisticsItem( "- wica.channel-publish-poller-restarts",                 channelPublishPollerRestarts ),
                                                                      new StatisticsItem( "- wica.channel-publish-channel-value-initial-state",     channelPublishChannelValueInitialState ),
                                                                      new StatisticsItem( "- wica.channel-publish-channel-metadata-initial-state",  channelPublishChannelMetadataInitialState ),
                                                                      new StatisticsItem( "- wica.channel-monitored-value-buffer-size",             channelMonitoredValueBufferSize ),
                                                                      new StatisticsItem( "- wica.channel-polled-value-buffer-size",                channelPolledValueBufferSize ),
                                                                      new StatisticsItem( "- wica.channel-get-timeout-interval-in-ms",              channelGetTimeoutIntervalInMs ),
                                                                      new StatisticsItem( "- wica.channel-get-numeric-scale",                       channelGetNumericScale ),
                                                                      new StatisticsItem( "- wica.channel-get-value-default-fields-of-interest",    channelGetValueDefaultFieldsOfInterest ),
                                                                      new StatisticsItem( "- wica.channel-get-metadata-default-fields-of-interest", channelGetMetadataDefaultFieldsOfInterest ),
                                                                      new StatisticsItem( "- wica.channel-put-timeout-interval-in-ms",              channelPutTimeoutIntervalInMs ),
                                                                      new StatisticsItem( "- wica.stream-quote-numeric-strings",                    streamQuoteNumericStrings ),
                                                                      new StatisticsItem( "- wica.stream-metadata-fields-of-interest",              streamMetadataFieldsOfInterest ),
                                                                      new StatisticsItem( "- wica.cors-allowed-origin-patterns",                    corsAllowedOriginPatterns ) ) );
   }

   @Override
   public void reset()
   {
      // Nothing to do here
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
