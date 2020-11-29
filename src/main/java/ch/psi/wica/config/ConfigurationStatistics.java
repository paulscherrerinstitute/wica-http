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
   private final String corsAllowCredentials;
   private final String corsAllowedOrigins;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

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
                                   @Value( "${wica.cors-allow_credentials}" ) Boolean corsAllowCredentials,
                                   @Value( "${wica.cors-allowed-origins}" ) String corsAllowedOrigins )
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
      this.corsAllowCredentials = String.valueOf( corsAllowCredentials );
      this.corsAllowedOrigins = String.valueOf( corsAllowedOrigins );

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
                                                                      new StatisticsItem( "- wica.cors-allow-credentials",                          corsAllowCredentials ),
                                                                      new StatisticsItem( "- wica.cors-allowed-origins",                            corsAllowedOrigins ) ) );
   }

   @Override
   public void reset()
   {
      // Nothing to do here
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
