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

   private final boolean allowCredentials;
   private final String allowedOrigins;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaCorsConfigurer( @Value( "${wica.cors_allow-credentials}" ) Boolean allowCredentials,
                              @Value( "${wica.cors_allowed-origins}" ) String allowedOrigins )
   {
      this.allowCredentials = allowCredentials;
      this.allowedOrigins = allowedOrigins;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public void addCorsMappings( CorsRegistry registry )
   {
      logger.info( "Configuring CORS... [allowCredentials='{}', allowedOrigins='{}']", allowCredentials, allowedOrigins );
      registry.addMapping("/**")
              .allowCredentials( this.allowCredentials )
              .allowedOrigins( this.allowedOrigins );
      logger.info( "CORS configuration completed.");
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

