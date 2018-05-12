/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica2.controllers;

/*- Imported packages --------------------------------------------------------*/

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Controller
class HtmlControllerDemoPage
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @GetMapping( value="/html", produces = MediaType.TEXT_HTML_VALUE )
   public String getServiceConfigurationListAsHtml()
   {
      return "DemoPage";
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

