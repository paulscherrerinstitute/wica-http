/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.infrastructure.stream.WicaStreamBuilder;
import ch.psi.wica.model.app.WicaDataAcquisitionMode;
import ch.psi.wica.model.app.WicaFilterType;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.channel.WicaChannelValue;
import ch.psi.wica.model.stream.WicaStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@DirtiesContext( classMode= DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD )
@SpringBootTest
class WicaStreamPolledValueRequesterServiceTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
   private WicaStreamPolledValueCollectorService wicaStreamPolledValueCollectorService;

   @Autowired
   private WicaStreamPolledValueRequesterService service;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testStartPolling_wicaStreamPolledValueCollectorServiceInitialisedOk()
   {
      final String myWicaChannelName = "simon:counter:01";
      final WicaChannelProperties wicaChannelProperties = WicaChannelPropertiesBuilder.create()
            .withDataAcquisitionMode( WicaDataAcquisitionMode.POLL )
            .withFilterType(WicaFilterType.ALL_VALUE )
            .withPollingIntervalInMillis( 200 )
            .build();

      final WicaChannel myWicaChannel = WicaChannelBuilder.create().withChannelNameAndProperties( myWicaChannelName, wicaChannelProperties ).build();

      final WicaStream wicaStream = WicaStreamBuilder.create()
            .withId( "myStream" )
            .withChannel( myWicaChannel )
            .build();

      final Map<WicaChannel, List<WicaChannelValue>> preFirstValueMap = wicaStreamPolledValueCollectorService.get( wicaStream, LocalDateTime.MIN );
      assertThat( preFirstValueMap.size(), is( 0 ) );

      service.startPolling( wicaStream );

      final Map<WicaChannel, List<WicaChannelValue>> firstValueMap = wicaStreamPolledValueCollectorService.get( wicaStream, LocalDateTime.MIN  );
      assertThat( firstValueMap.size(), is( 1 ) );
      assertThat( firstValueMap.containsKey( myWicaChannel ), is( true ) );
      assertThat( firstValueMap.get( myWicaChannel ).get( 0 ).isConnected(), is( false ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
