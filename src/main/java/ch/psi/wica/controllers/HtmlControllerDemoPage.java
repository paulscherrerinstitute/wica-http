/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Returns an HTML Page which provides general information together with links
 * to the other administration pages.
 */
@ThreadSafe
@RequestMapping("/")
@Controller
class HtmlControllerDemoPage
{

   /*- Public attributes --------------------------------------------------------*/
   /*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( HtmlControllerDemoPage.class );

   /*- Main ---------------------------------------------------------------------*/
   /*- Constructor --------------------------------------------------------------*/
   /*- Class methods ------------------------------------------------------------*/
   /*- Public methods -----------------------------------------------------------*/

   // Leave the default MVC handling for this method. This means that the returned value will
   // be interpreted as a reference to a thymeleaf template.
   @GetMapping( value="/", produces = MediaType.TEXT_HTML_VALUE )
   public String getServiceConfigurationListAsHtml( Model viewModel )
   {
      logger.info("Received status GET / request" );

      // Return reference to the template. Spring Boot will do the rest !
      final String templateFileName = "IndexPage";
      logger.info( "Returning reference to thymeleaf template: '{}'", templateFileName );
      return templateFileName;
   }

   /*- Private methods ----------------------------------------------------------*/
   /*- Nested Classes -----------------------------------------------------------*/

}

