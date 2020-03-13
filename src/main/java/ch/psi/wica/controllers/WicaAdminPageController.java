/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.EpicsChannelMonitoringService;
import ch.psi.wica.controlsystem.epics.EpicsChannelPollingService;
import ch.psi.wica.model.app.StatisticsCollectable;
import ch.psi.wica.model.app.StatisticsCollectionService;
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

import java.util.List;

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

   private final StatisticsCollectionService statisticsCollectionService;
   private final EpicsChannelMonitoringService epicsChannelMonitoringService;
   private final EpicsChannelPollingService epicsChannelPollingService;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaAdminPageController( @Autowired StatisticsCollectionService statisticsCollectionService,
                                   @Autowired EpicsChannelMonitoringService epicsChannelMonitoringService,
                                   @Autowired EpicsChannelPollingService epicsChannelPollingServiceService
   )
   {
      this.statisticsCollectionService = statisticsCollectionService;
      this.epicsChannelMonitoringService = epicsChannelMonitoringService;
      this.epicsChannelPollingService= epicsChannelPollingServiceService;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   // Leave the default MVC handling for this method. This means that the returned value will
   // be interpreted as a reference to a thymeleaf template.
   @GetMapping( value="/admin", produces = MediaType.TEXT_HTML_VALUE )
   public String getAdminPageAsHtml( Model viewModel )
   {
      logger.trace("Received status GET /admin request" );

      final List<StatisticsCollectable.Statistics> statisticsList = statisticsCollectionService.collect();

      viewModel.addAttribute("statisticsList", statisticsList );

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
      statisticsCollectionService.resetStatistics();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

