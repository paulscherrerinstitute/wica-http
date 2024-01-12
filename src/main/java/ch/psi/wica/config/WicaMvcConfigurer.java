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

/**
 * Provides the functionality to configure MVC.
 */
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

   /**
    * Configures the MVC task executor.
    *
    * @param configurer the async support configurer.
    */
   @Override
   public void configureAsyncSupport( AsyncSupportConfigurer configurer )
   {
      logger.info( "Configuring Async Support..." );
      configurer.setTaskExecutor( getMvcTaskExecutor() );
      logger.info( "Async Support configuration completed.");
   }

/*- Private methods ----------------------------------------------------------*/

   private ThreadPoolTaskExecutor getMvcTaskExecutor()
   {
      logger.info( "Configuring MVC Task Executor...");
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setThreadNamePrefix( "my-mvc-task-executor-" );
      executor.setCorePoolSize( 5 );
      executor.setMaxPoolSize( 200 );
      executor.initialize();
      return executor;
   }

/*- Nested Classes -----------------------------------------------------------*/

}

