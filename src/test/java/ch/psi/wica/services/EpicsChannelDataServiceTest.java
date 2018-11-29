/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelAlarmSeverity;
import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;



/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class EpicsChannelDataServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( EpicsChannelDataServiceTest.class );

   private EpicsChannelMonitorService monitorServiceMock;
   private EpicsChannelDataService dataService;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/


   @Before()
   void setup()
   {
      monitorServiceMock = Mockito.mock( EpicsChannelMonitorService.class );
      dataService = new EpicsChannelDataService( monitorServiceMock );

   }

   @Test
   void testConstructor_ThrowsNpeWhenArgumentNull()
   {
      assertThrows( NullPointerException.class, () -> {
         new EpicsChannelDataService(null);
      } );
   }

//   @Test
//   void testConstructor_ThrowsIAEWhenArgumentEmpty()
//   {
//      assertThrows( IllegalArgumentException.class, () -> {
//         new EpicsChannelDataService( "" );
//      } );
//   }

//   public void startMonitoring( WicaChannelName wicaChannelName, Consumer<Boolean> connectionStateChangeHandler,
//                                Consumer<WicaChannelMetadata> metadataChangeHandler, Consumer<WicaChannelValue> valueChangeHandler  )

   @Test
   void testX()
   {
//      Consumer<Boolean> connectionStateChangeHandler,
//      Consumer<WicaChannelMetadata> metadataChangeHandler
//      Consumer<WicaChannelValue> valueChangeHandler
//
      final WicaChannelName myChannel = new WicaChannelName( "simon:counter:01" );


      dataService.startMonitoring( myChannel );
      Mockito.verify( monitorServiceMock, times(1)).startMonitoring( any(), any(), any(), any());


      // epicsChannelMonitorService.startMonitoring( channelName, stateChangedHandler, metadataChangedHandler, valueChangedHandler );

      final ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
