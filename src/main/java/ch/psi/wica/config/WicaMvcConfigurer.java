/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.config;

/*- Imported packages --------------------------------------------------------*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Configuration
class WicaMvcConfigurer implements WebMvcConfigurer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaMvcConfigurer.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public void configureAsyncSupport( AsyncSupportConfigurer configurer )
   {
      logger.info( "Configuring Async Support..." );
      configurer.setTaskExecutor( mvcTaskExecutor() );
      logger.info( "Async Support configuration completed.");
   }

/*- Private methods ----------------------------------------------------------*/

   private ThreadPoolTaskExecutor mvcTaskExecutor()
   {
      logger.info( "Configuring MVC Task Executor...");
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setThreadNamePrefix("my-mvc-task-executor-");
      executor.setCorePoolSize( 5 );
      executor.setMaxPoolSize( 200 );
      return executor;
   }

/*- Nested Classes -----------------------------------------------------------*/

}

