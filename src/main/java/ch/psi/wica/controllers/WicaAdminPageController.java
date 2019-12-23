/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.WicaApplication;
import ch.psi.wica.controlsystem.epics.EpicsChannelMonitoringService;
import ch.psi.wica.controlsystem.epics.EpicsChannelPollingService;
import ch.psi.wica.services.stream.WicaStreamLifecycleService;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Returns an HTML Page which provides general information together with links
 * to the other administration pages.
 */
@ThreadSafe
@Controller
class WicaAdminPageController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaAdminPageController.class );

   private final WicaChannelGetController wicaChannelGetController;
   private final WicaChannelPutController wicaChannelPutController;
   private final WicaStreamCreateController wicaStreamCreateController;
   private final WicaStreamDeleteController wicaStreamDeleteController;
   private final WicaStreamGetController wicaStreamGetController;
   private final EpicsChannelMonitoringService epicsChannelMonitoringService;
   private final EpicsChannelPollingService epicsChannelPollingService;
   private final WicaStreamLifecycleService wicaStreamLifecycleService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaAdminPageController( @Autowired WicaChannelGetController wicaChannelGetController,
                                   @Autowired WicaChannelPutController wicaChannelPutController,
                                   @Autowired WicaStreamCreateController wicaStreamCreateController,
                                   @Autowired WicaStreamDeleteController wicaStreamDeleteController,
                                   @Autowired WicaStreamGetController wicaStreamGetController,
                                   @Autowired WicaStreamLifecycleService wicaStreamLifecycleService,
                                   @Autowired EpicsChannelMonitoringService epicsChannelMonitoringService,
                                   @Autowired EpicsChannelPollingService epicsChannelPollingServiceService
   )
   {
      this.wicaChannelGetController = wicaChannelGetController;
      this.wicaChannelPutController = wicaChannelPutController;
      this.wicaStreamCreateController = wicaStreamCreateController;
      this.wicaStreamGetController = wicaStreamGetController;
      this.wicaStreamDeleteController = wicaStreamDeleteController;
      this.wicaStreamLifecycleService = wicaStreamLifecycleService;
      this.epicsChannelMonitoringService = epicsChannelMonitoringService;
      this.epicsChannelPollingService= epicsChannelPollingServiceService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   // Leave the default MVC handling for this method. This means that the returned value will
   // be interpreted as a reference to a thymeleaf template.
   @GetMapping( value="/admin", produces = MediaType.TEXT_HTML_VALUE )
   public String getServiceConfigurationListAsHtml( Model viewModel )
   {
      logger.trace("Received status GET /admin request" );

      // Add some server statistics
      final Map<String,String> serverStatisticsMap = new LinkedHashMap<>();
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      final String formattedServerStartTime = WicaApplication.getServerStartTime().format(formatter );
      final String formattedTimeAndDateNow =  LocalDateTime.now().format(formatter );
      final long serverUpTimeInSeconds= Duration.between( WicaApplication.getServerStartTime() , LocalDateTime.now() ).getSeconds();

      final String formattedServerUpTime = String.format( "%d days, %02d hours, %02d minutes, %02d seconds",
                                                          ( serverUpTimeInSeconds / 86400 ),        // days
                                                          ( serverUpTimeInSeconds % 86400 ) / 3600, // hours
                                                          ( serverUpTimeInSeconds % 3600  )  / 60,  // minutes
                                                          ( serverUpTimeInSeconds % 60    ) );      // seconds

      serverStatisticsMap.put( "SERVER:", " " );
      serverStatisticsMap.put( "- Last Updated",      formattedTimeAndDateNow );
      serverStatisticsMap.put( "- Server Started",    formattedServerStartTime );
      serverStatisticsMap.put( "- Server Uptime",     formattedServerUpTime );
      serverStatisticsMap.put( ". ", "" );

      serverStatisticsMap.put( "CONTROLLERS:", " " );
      serverStatisticsMap.put( "- WICA Stream CREATE Requests/Replies/Error",  String.valueOf( wicaStreamCreateController.getStatistics().getSummary() ) );
      serverStatisticsMap.put( "- WICA Stream GET Requests/Replies/Errors",    String.valueOf( wicaStreamGetController.getStatistics().getSummary() ) );
      serverStatisticsMap.put( "- WICA Stream DELETE Requests/Replies/Errors", String.valueOf( wicaStreamDeleteController.getStatistics().getSummary() ) );
      serverStatisticsMap.put( "- WICA Channel GET Requests/Replies/Erroros",  String.valueOf( wicaChannelGetController.getStatistics().getSummary() ) );
      serverStatisticsMap.put( "- WICA Channel PUT Requests/Replies/Errors",   String.valueOf( wicaChannelPutController.getStatistics().getSummary() ) );
      serverStatisticsMap.put( ".  ", "" );

      serverStatisticsMap.put( "STREAM:", " " );
      serverStatisticsMap.put( "- WICA Streams Created", String.valueOf( wicaStreamLifecycleService.getStreamsCreated() ) );
      serverStatisticsMap.put( "- WICA Streams Deleted", String.valueOf( wicaStreamLifecycleService.getStreamsDeleted() ) );
      serverStatisticsMap.put( ".   ", "" );

      serverStatisticsMap.put( "CLIENTS:", " " );
      final List<String> clientList1 = wicaChannelGetController.getStatistics().getClients();
      final List<String> clientList2 = wicaChannelPutController.getStatistics().getClients();
      final List<String> clientList3 = wicaStreamCreateController.getStatistics().getClients();
      final List<String> clientList4 = wicaStreamDeleteController.getStatistics().getClients();
      final List<String> clientList5 = wicaStreamGetController.getStatistics().getClients();

      final List<String> allClientIPs = Stream.of(clientList1, clientList2, clientList3, clientList4, clientList5 )
                                            .flatMap(Collection::stream)
                                            .distinct()
                                            .collect(Collectors.toList());

      final List<String> allClients = allClientIPs.stream().map( this::getFormattedHostNameFromIP ).collect(Collectors.toList() );

      serverStatisticsMap.put( "- Number of Distinct Clients", String.valueOf( allClients.size() ) );
      serverStatisticsMap.put( "- Client Addresses", String.valueOf( allClients ) );
      serverStatisticsMap.put( ".    ", "" );

      final var monitorStatistics = epicsChannelMonitoringService.getStatistics();
      serverStatisticsMap.put( "EPICS Monitoring Service:", " " );
      serverStatisticsMap.put( "- Start Monitor Requests",   monitorStatistics.getStartRequests() );
      serverStatisticsMap.put( "- Stop Monitor Requests",  monitorStatistics.getStopRequests() );
      serverStatisticsMap.put( "- Errors",  monitorStatistics.getErrors() );
      serverStatisticsMap.put( "- EPICS Channels: Total",     monitorStatistics.getTotalChannelCount() );
      serverStatisticsMap.put( "- EPICS Channels: Connected", monitorStatistics.getConnectedChannelCount() );
      serverStatisticsMap.put( "- EPICS Monitors: Total",     monitorStatistics.getTotalMonitorCount() );
      serverStatisticsMap.put( ".     ", "" );

      final var pollerStatistics = epicsChannelPollingService.getStatistics();
      serverStatisticsMap.put( "EPICS Polling Service:", " " );
      serverStatisticsMap.put( "- Start Polling Requests", pollerStatistics.getStartRequests() );
      serverStatisticsMap.put( "- Stop Polling Requests", pollerStatistics.getStopRequests() );
      serverStatisticsMap.put( "- Total Instances", pollerStatistics.getTotalPollerCount() );
      serverStatisticsMap.put( "- Cancelled Instances",  pollerStatistics.getCancelledPollerCount());
      serverStatisticsMap.put( "- Completed Instances",  pollerStatistics.getCompletedPollerCount() );
      serverStatisticsMap.put( ".      ", "" );

      viewModel.addAttribute("serverStatisticsMap", serverStatisticsMap );

      // Return reference to the template. Spring Boot will do the rest !
      final String templateFileName = "AdminPage";
      logger.trace( "Returning reference to thymeleaf template: '{}'", templateFileName );
      return templateFileName;
   }

   @GetMapping( value="/channel-monitors/all", produces = MediaType.APPLICATION_JSON_VALUE )
   public ResponseEntity<List<String>> getAllMonitoredChannels()
   {
      final List<String> channelNames = epicsChannelMonitoringService.getStatistics().getChannelNames();
      return new ResponseEntity<>( channelNames, HttpStatus.OK );
   }

   @GetMapping( value="/channel-monitors/unconn" )
   public ResponseEntity<List<String>> getUnconnectedChannels()
   {
      final List<String> unconnectedChannelNames = epicsChannelMonitoringService.getStatistics().getUnconnectedChannelNames();
      return new ResponseEntity<>( unconnectedChannelNames, HttpStatus.OK );
   }

   @GetMapping( value="/channel-pollers/all", produces = MediaType.APPLICATION_JSON_VALUE )
   public ResponseEntity<List<String>> getAllPolledChannels()
   {
      final List<String> channelNames = epicsChannelPollingService.getStatistics().getChannelNames();
      return new ResponseEntity<>( channelNames, HttpStatus.OK );
   }

   @PutMapping( value="/statistics" )
   public void resetStatistics()
   {
      wicaChannelGetController.resetStatistics();
      wicaChannelPutController.resetStatistics();
      wicaStreamCreateController.resetStatistics();
      wicaStreamDeleteController.resetStatistics();
      wicaStreamGetController.resetStatistics();
   }


/*- Private methods ----------------------------------------------------------*/

   private String getFormattedHostNameFromIP( String ipAddress )
   {
      if ( ipAddress.isEmpty() )
      {
         return "";
      }

      try
      {
         final InetAddress inetAddress = InetAddress.getByName( ipAddress );
         return ipAddress + " ("  + inetAddress.getCanonicalHostName() + ")";
      }
      catch( Exception ex )
      {
         return ipAddress;
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}

