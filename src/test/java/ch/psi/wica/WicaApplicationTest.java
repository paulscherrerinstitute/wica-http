/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaApplicationTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaApplicationTest.class );

   @Autowired
   WicaApplication wicaApplication;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void contextLoads() throws Exception
   {
      // checks that the environment starts up correctly !
   }

   @Test
   void testGetServerStartTime()
   {
      logger.info( "Hello !" );
       final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
       assertNotNull( formatter );
       assertNotNull( WicaApplication.getServerStartTime() );
       WicaApplication.getServerStartTime().format( formatter );
   }

   @CsvSource( { "DEBUG,10",
                 "DEBUG,100",
                 "DEBUG,1000",
                 "TRACE,10",
                 "TRACE,100",
                 "TRACE,1000",
                 "INFO,1000" })

   @ParameterizedTest
   void testLogging( String logLevel, int durationInMillis )
   {
      wicaApplication.testLogging( logLevel, durationInMillis );
   }


/*- Private methods ----------------------------------------------------------*/

   private void mylog( String logLevel, String format, Object ...args )
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

         default:
            logger.error( "Unrecognised log level" );
            break;
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
