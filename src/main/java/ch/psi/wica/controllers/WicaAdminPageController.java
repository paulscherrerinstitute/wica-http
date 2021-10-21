/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.channel.EpicsChannelManager;
import ch.psi.wica.controlsystem.epics.monitor.EpicsChannelMonitorPublisher;
import ch.psi.wica.controlsystem.epics.poller.EpicsChannelPollerPublisher;
import ch.psi.wica.model.app.StatisticsCollectable;
import ch.psi.wica.model.app.StatisticsCollectionService;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
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
   private final EpicsChannelMonitorPublisher epicsChannelMonitorPublisher;
   private final EpicsChannelPollerPublisher epicsChannelPollerPublisher;
   private final EpicsChannelManager epicsMonitoredChannelManager;
   private final EpicsChannelManager epicsPolledChannelManager;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaAdminPageController( @Autowired StatisticsCollectionService statisticsCollectionService,
                                   @Autowired EpicsChannelMonitorPublisher epicsChannelMonitorPublisher,
                                   @Autowired EpicsChannelPollerPublisher epicsChannelPollerPublisher,
                                   @Autowired EpicsChannelManager.EpicsMonitoredChannelManagerService epicsMonitoredChannelManager,
                                   @Autowired EpicsChannelManager.EpicsPolledChannelManagerService epicsPolledChannelManager)
   {
      this.statisticsCollectionService = statisticsCollectionService;
      this.epicsChannelMonitorPublisher = epicsChannelMonitorPublisher;
      this.epicsChannelPollerPublisher= epicsChannelPollerPublisher;
      this.epicsMonitoredChannelManager = epicsMonitoredChannelManager;
      this.epicsPolledChannelManager = epicsPolledChannelManager;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @RequestMapping("/ilkcs/**")
   public ResponseEntity<?> mirrorRestIlkCs( @RequestBody(required = false) String body,
                                             HttpMethod method, HttpServletRequest request, HttpServletResponse response)
         throws URISyntaxException
   {
      String requestUrl = request.getRequestURI();

      URI uri = new URI( "https", null, "hipa-ilk.psi.ch", 10443, null, null, null);
      uri = UriComponentsBuilder.fromUri( uri)
            .path( requestUrl.substring( 6 ) )
            .query(request.getQueryString())
            .build(true).toUri();

      HttpHeaders headers = new HttpHeaders();
      Enumeration<String> headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
         String headerName = headerNames.nextElement();
         headers.set(headerName, request.getHeader(headerName));
      }

      HttpEntity<String> httpEntity = new HttpEntity<>( body, headers);
      RestTemplate restTemplate = new RestTemplate();
      try {
         return restTemplate.exchange(uri, method, httpEntity, String.class);
      } catch( HttpStatusCodeException e) {
         logger.warn( e.getMessage() );
         return ResponseEntity.status(e.getRawStatusCode())
               .headers(e.getResponseHeaders())
               .body(e.getResponseBodyAsString());
      } catch( RuntimeException ex )
      {
         logger.warn( ex.getMessage() );
         return ResponseEntity.badRequest().build();
      }
   }

   @RequestMapping("/ilkdb/**")
   public ResponseEntity<?> mirrorRestIlkDb( @RequestBody(required = false) String body,
                                             HttpMethod method, HttpServletRequest request, HttpServletResponse response)
         throws URISyntaxException
   {
      String requestUrl = request.getRequestURI();

      URI uri = new URI( "https", null, "hipa-ilk.psi.ch", 9443, null, null, null);
      uri = UriComponentsBuilder.fromUri( uri)
            .path( requestUrl.substring( 6 ) )
            .query(request.getQueryString())
            .build(true).toUri();

      HttpHeaders headers = new HttpHeaders();
      Enumeration<String> headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
         String headerName = headerNames.nextElement();
         headers.set(headerName, request.getHeader(headerName));
      }

      HttpEntity<String> httpEntity = new HttpEntity<>( body, headers);
      RestTemplate restTemplate = new RestTemplate();
      try {
         return restTemplate.exchange(uri, method, httpEntity, String.class);
      } catch( HttpStatusCodeException e) {
         logger.warn( e.getMessage() );
         return ResponseEntity.status(e.getRawStatusCode())
               .headers(e.getResponseHeaders())
               .body(e.getResponseBodyAsString());
      } catch( RuntimeException ex )
      {
         logger.warn( ex.getMessage() );
         return ResponseEntity.badRequest().build();
      }
   }


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
      final List<String> channelNames = epicsChannelMonitorPublisher.getStatistics().getChannelNames();
      return new ResponseEntity<>( channelNames, HttpStatus.OK );
   }

   @GetMapping( value="/channel-monitors/unconn" )
   public ResponseEntity<List<String>> getUnconnectedChannelMonitors()
   {
      final List<String> unconnectedChannelNames = epicsMonitoredChannelManager.getStatistics().getUnconnectedChannels();
      return new ResponseEntity<>( unconnectedChannelNames, HttpStatus.OK );
   }

   @GetMapping( value="/channel-pollers/all", produces = MediaType.APPLICATION_JSON_VALUE )
   public ResponseEntity<List<String>> getAllPolledChannels()
   {
      final List<String> channelNames = epicsChannelPollerPublisher.getStatistics().getChannelNames();
      return new ResponseEntity<>( channelNames, HttpStatus.OK );
   }

   @GetMapping( value="/channel-pollers/unconn" )
   public ResponseEntity<List<String>> getUnconnectedChannelPollers()
   {
      final List<String> unconnectedChannelNames = epicsPolledChannelManager.getStatistics().getUnconnectedChannels();
      return new ResponseEntity<>( unconnectedChannelNames, HttpStatus.OK );
   }

   @PutMapping( value="/statistics" )
   public void resetStatistics()
   {
      statisticsCollectionService.resetStatistics();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

