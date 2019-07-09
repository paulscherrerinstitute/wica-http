/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.WicaApplication;
import ch.psi.wica.services.epics.EpicsChannelMonitorService;
import ch.psi.wica.services.stream.WicaStreamService;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
   private final EpicsChannelMonitorService epicsChannelMonitorService;
   private final WicaStreamService wicaStreamService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaAdminPageController( @Autowired WicaChannelGetController wicaChannelGetController,
                                   @Autowired WicaChannelPutController wicaChannelPutController,
                                   @Autowired WicaStreamCreateController wicaStreamCreateController,
                                   @Autowired WicaStreamDeleteController wicaStreamDeleteController,
                                   @Autowired WicaStreamGetController wicaStreamGetController,
                                   @Autowired WicaStreamService wicaStreamService,
                                   @Autowired EpicsChannelMonitorService epicsChannelMonitorService )
   {
      this.wicaChannelGetController = wicaChannelGetController;
      this.wicaChannelPutController = wicaChannelPutController;
      this.wicaStreamCreateController = wicaStreamCreateController;
      this.wicaStreamGetController = wicaStreamGetController;
      this.wicaStreamDeleteController = wicaStreamDeleteController;
      this.wicaStreamService = wicaStreamService;
      this.epicsChannelMonitorService = epicsChannelMonitorService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   // Leave the default MVC handling for this method. This means that the returned value will
   // be interpreted as a reference to a thymeleaf template.
   @GetMapping( value="/admin", produces = MediaType.TEXT_HTML_VALUE )
   public String getServiceConfigurationListAsHtml( Model viewModel )
   {
      logger.info("Received status GET /admin request" );

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
      serverStatisticsMap.put( "- WICA Stream Create Requests", String.valueOf( wicaStreamCreateController.getStatistics().getRequests() ) );
      serverStatisticsMap.put( "- WICA Stream Get Requests",    String.valueOf(  wicaStreamGetController.getStatistics().getRequests() ) );
      serverStatisticsMap.put( "- WICA Stream Delete Requests", String.valueOf(  wicaStreamDeleteController.getStatistics().getRequests() ) );
      serverStatisticsMap.put( "- WICA Channel Get Requests",   String.valueOf( wicaChannelGetController.getStatistics().getRequests() ) );
      serverStatisticsMap.put( "- WICA Channel Put Requests",   String.valueOf( wicaChannelPutController.getStatistics().getRequests() ) );
      serverStatisticsMap.put( ".  ", "" );

      serverStatisticsMap.put( "STREAM:", " " );
      serverStatisticsMap.put( "- WICA Streams Created",     String.valueOf( wicaStreamService.getStreamsCreated() ) );
      serverStatisticsMap.put( "- WICA Streams Deleted",     String.valueOf( wicaStreamService.getStreamsDeleted() ) );
      serverStatisticsMap.put( ".   ", "" );

      serverStatisticsMap.put( "CLIENTS:", " " );
      final List<String> clientList1 = wicaChannelGetController.getStatistics().getClients();
      final List<String> clientList2 = wicaChannelPutController.getStatistics().getClients();
      final List<String> clientList3 = wicaStreamCreateController.getStatistics().getClients();
      final List<String> clientList4 = wicaStreamDeleteController.getStatistics().getClients();
      final List<String> clientList5 = wicaStreamGetController.getStatistics().getClients();

      final List<String> allClients = Stream.of(clientList1, clientList2, clientList3, clientList4, clientList5 )
                                            .flatMap(Collection::stream)
                                            .distinct()
                                            .collect(Collectors.toList());

      serverStatisticsMap.put( "- Number of Distinct Clients", String.valueOf( allClients.size() ) );
      serverStatisticsMap.put( "- Client Addresses", String.valueOf( allClients ) );
      serverStatisticsMap.put( ".    ", "" );

      serverStatisticsMap.put( "EPICS:", " " );
      serverStatisticsMap.put( "- EPICS Channels Created",   String.valueOf( epicsChannelMonitorService.getChannelsCreatedCount()   ) );
      serverStatisticsMap.put( "- EPICS Channels Deleted",   String.valueOf( epicsChannelMonitorService.getChannelsDeletedCount()   ) );
      serverStatisticsMap.put( "- EPICS Channels Active",    String.valueOf( epicsChannelMonitorService.getChannelsActiveCount()    ) );
      serverStatisticsMap.put( "- EPICS Channels Connected", String.valueOf( epicsChannelMonitorService.getChannelsConnectedCount() ) );
      serverStatisticsMap.put( "- EPICS Monitors Active",    String.valueOf( epicsChannelMonitorService.getMonitorsConnectedCount() ) );
      serverStatisticsMap.put( ".     ", "" );

      viewModel.addAttribute("serverStatisticsMap", serverStatisticsMap );

      // Return reference to the template. Spring Boot will do the rest !
      final String templateFileName = "AdminPage";
      logger.info( "Returning reference to thymeleaf template: '{}'", templateFileName );
      return templateFileName;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

