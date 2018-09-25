/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.WicaApplication;
import ch.psi.wica.model.WicaStreamId;
import ch.psi.wica.services.EpicsChannelMonitorService;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Returns an HTML Page which provides general information together with links
 * to the other administration pages.
 */
@ThreadSafe
@RequestMapping("/")
@Controller
class WicaMainPageController
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaMainPageController.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/

/*- Public methods -----------------------------------------------------------*/

   // Leave the default MVC handling for this method. This means that the returned value will
   // be interpreted as a reference to a thymeleaf template.
   @GetMapping( value="/", produces = MediaType.TEXT_HTML_VALUE )
   public String getServiceConfigurationListAsHtml( Model viewModel )
   {
      logger.info("Received status GET / request" );


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

      serverStatisticsMap.put( "Last Updated",      formattedTimeAndDateNow );
      serverStatisticsMap.put( "Server Started",    formattedServerStartTime );
      serverStatisticsMap.put( "Server Uptime",     formattedServerUpTime );

      serverStatisticsMap.put( "WICA Streams Created",     String.valueOf(WicaStreamId.getCreationCount() ) );
      serverStatisticsMap.put( "EPICS Channels Created",   String.valueOf( EpicsChannelMonitorService.getChannelCreationCount()   ) );
      serverStatisticsMap.put( "EPICS Channels Connected", String.valueOf( EpicsChannelMonitorService.getChannelConnectionCount() ) );
      serverStatisticsMap.put( "EPICS Monitors Created",   String.valueOf( EpicsChannelMonitorService.getMonitorCreationCount()   ) );


      viewModel.addAttribute("serverStatisticsMap", serverStatisticsMap );

      // Return reference to the template. Spring Boot will do the rest !
      final String templateFileName = "MainPage";
      logger.info( "Returning reference to thymeleaf template: '{}'", templateFileName );
      return templateFileName;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

