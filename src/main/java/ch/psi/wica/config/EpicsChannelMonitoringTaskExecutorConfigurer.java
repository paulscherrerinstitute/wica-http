/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.config;

/*- Imported packages --------------------------------------------------------*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Configuration
@EnableAsync
class EpicsChannelMonitoringTaskExecutorConfigurer
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(EpicsChannelMonitoringTaskExecutorConfigurer.class );

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Bean( name = "epicsChannelMonitoringTaskExecutor" )
   public Executor threadPoolTaskExecutor()
   {
      logger.info( "Configuring Async Support for EpicsChannelMonitoringService..." );
      final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setThreadNamePrefix( "my-epics-channel-monitoring-task-executor-" );
      executor.setCorePoolSize( 5 );
      executor.setMaxPoolSize( 50 );
      executor.initialize();
      logger.info( "Async Support for EpicsChannelMonitoringService configuration completed.");
      return executor;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

