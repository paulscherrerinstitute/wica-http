/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica;

/*- Imported packages --------------------------------------------------------*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * SpringBoot application for the web visualisation of points of interest within
 * a distributed control system.
 */
@SpringBootApplication
public class WicaApplication implements DisposableBean
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final Logger appLogger = LoggerFactory.getLogger("APP_LOGGER" );
	private static final Logger logger = LoggerFactory.getLogger( WicaApplication.class );
	private static final LocalDateTime serverStartTime = LocalDateTime.now();

/*- Main ---------------------------------------------------------------------*/

	/**
	 * Application entry point
	 *
	 * @param args the command line arguments (which are not currently used).
	 */
	public static void main( String[] args )
	{
		logger.info( " Git Wica Application is starting...");
		if ( args.length != 1 )
		{
			logger.error( args.length > 1 ? "Too many arguments" : "Too few arguments" );
			logger.info( "Usage: java  [{JVM_startup_args}] keystorePassword" );
			return;
		}

		final String keystorePassword = args[ 0 ];

		// The value set here will override any definitions in the application.properties file
		System.setProperty( "server.ssl.key-store-password", keystorePassword );
		logger.info( "The Keystore Password was provided and has been set." );

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
		logger.info(" Wica Application started. ");
		appLogger.info( "Wica Service: started");
	}

	@EventListener( ApplicationFailedEvent.class)
	public void doSomethingOnFailure()
	{
		logger.info( "Git Wica Application FAILURE Event." );
		appLogger.info( "Git Wica Service: failure event.");
	}

	// Previously the @PreDestroy annotation was used here. But this seems a mess under
	// the Java 9 JPMS. The module 'java.xml.ws.annotation' has been deprecated so attempts
	// to add it to the module path get flagged up in red. The approach now is to leverage
	// of the DisposableBean marker interface. Seems to work !
	public void destroy()
	{
		logger.info( "Wica Application DESTROY Event." );
		logger.info( "Wica Service: stopped." );
		appLogger.info( "Wica Service: stopped." );
	}


/*- Constructor --------------------------------------------------------------*/

	public WicaApplication() {}


/*- Class methods ------------------------------------------------------------*/

	public static LocalDateTime getServerStartTime()
	{
		return serverStartTime;
	}


/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}