/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.config;

/*- Imported packages --------------------------------------------------------*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Configuration
class WicaCorsConfigurer implements WebMvcConfigurer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaCorsConfigurer.class );
   private final String allowedOriginPatterns;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaCorsConfigurer( @Value( "${wica.cors-allowed-origin-patterns}" ) String allowedOriginPatterns )
   {
      this.allowedOriginPatterns = allowedOriginPatterns;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public void addCorsMappings( CorsRegistry registry )
   {
      logger.info( "Configuring CORS... [allowedOriginPatterns='{}']", allowedOriginPatterns );
      registry.addMapping("/**")
              .allowCredentials( true )
              .allowedHeaders( "*" )
              .exposedHeaders( "*" )
              .allowedMethods( "OPTIONS", "GET", "PUT", "POST", "DELETE"  )
              .allowedOriginPatterns( this.allowedOriginPatterns )
              .maxAge( 1880 );
      logger.info( "CORS configuration completed.");
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

