/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * SpringBoot application for the web visualisation of points of interest within
 * a distributed control system.
 */
@SpringBootApplication
public class WicaApplication
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
	private static final Logger logger = LoggerFactory.getLogger( WicaApplication.class );
	private static final LocalDateTime serverStartTime = LocalDateTime.now();

	private final boolean testLoggingOnStartup;

/*- Main ---------------------------------------------------------------------*/

	/**
	 * Application entry point
	 *
	 * @param args the command line arguments.
	 */
	public static void main( String[] args )
	{
		logger.info( " Wica Application is starting...");
		try
		{
			SpringApplication.run( WicaApplication.class, args );
		}
		catch( Exception ex )
		{
			logger.error( "An Unexpected problem prevented the service from starting up." );
			logger.error( "Further details are as follows:\n\n " + ex.getMessage() );
		}
		logger.info ( "os.name is: {}", System.getProperty( "os.name" ) );
		logger.info ( "user.dir is: {}", System.getProperty( "user.dir" ) );
	}

	@EventListener( ApplicationReadyEvent.class)
	public void doSomethingAfterStartup()
	{
		logger.info( "Wica Application READY Event." );
		logger.info( "Wica Service: starting..." );
		logger.info( "Wica Application started. ");
		appLogger.info( "Wica Service: started.");

		// When enabled run some performance checks on the application logging.
		if ( testLoggingOnStartup )
		{
			this.testLogging("TRACE", 1000);
			this.testLogging("DEBUG", 1000);
			this.testLogging("INFO", 1000);
			this.testLogging("WARN", 1000);
			this.testLogging("ERROR", 1000);
			this.testLogging("APP", 1000);
		}
	}

	@EventListener( ApplicationFailedEvent.class)
	public void doSomethingOnFailure()
	{
		logger.info( "Wica Application FAILURE Event." );
		appLogger.info( "Wica Service: failure event.");

	}

	// With Java 11 / SpringBoot 2.1.6 the @PreDestroy method seems to be working again.
	// In previous Spring releases this got broken and one had to use an
	// approach using the DisposableBean interface.
	@PreDestroy
	public void doSomethingBeforeShutdown()
	{
		logger.info( "Wica Application DESTROY Event." );
		logger.info( "Wica Service: stopped." );
		appLogger.info( "Wica Service: stopped." );
	}


/*- Constructor --------------------------------------------------------------*/

	public WicaApplication( @Value("${wica.test-logging-on-startup}") boolean testLoggingOnStartup )
	{
		this.testLoggingOnStartup = testLoggingOnStartup;
	}


/*- Class methods ------------------------------------------------------------*/

	public static LocalDateTime getServerStartTime()
	{
		return serverStartTime;
	}


/*- Public methods -----------------------------------------------------------*/
/*- Package-level methods ----------------------------------------------------*/

	void testLogging( String logLevel, int testDurationInMillis )
	{
		log( "INFO", "Starting logging test..." );

		final StopWatch stopWatch = StopWatch.createStarted();
		long loopCounter = 0;
		while( stopWatch.getTime(TimeUnit.MILLISECONDS ) < testDurationInMillis  )
		{
			log( logLevel, "{}: Logging Test Iteration: {}: This is a test.", logLevel, loopCounter++ );
		}
		long elapsedTimeInMiliiseconds = stopWatch.getTime(TimeUnit.MILLISECONDS );

		logger.info( "Logging level = '{}': Managed to send {} messages in {}ms.", logLevel, String.format( "%,d",loopCounter ), elapsedTimeInMiliiseconds );
		log( "INFO", "Starting logging completed." );
	}


/*- Private methods ----------------------------------------------------------*/

	private void log( String logLevel, String format, Object ...args )
	{
		switch( logLevel )
		{
			case "TRACE":
				logger.trace( "trace");
				break;

			case "DEBUG":
				logger.debug( format, args );
				break;

			case "INFO":
				logger.info( format, args );
				break;

			case "WARN":
				logger.warn( format, args );
				break;

			case "ERROR":
				logger.error( format, args );
				break;

			case "APP":
				appLogger.info( format, args );
				break;

			default:
				logger.error( "Unrecognised log level" );
				break;
		}
	}

/*- Nested Classes -----------------------------------------------------------*/

}