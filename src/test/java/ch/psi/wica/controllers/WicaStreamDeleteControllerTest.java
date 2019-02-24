/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaStreamId;
import ch.psi.wica.services.epics.EpicsChannelMonitorService;
import ch.psi.wica.services.epics.EpicsControlSystemMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
@AutoConfigureMockMvc
class WicaStreamDeleteControllerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamDeleteControllerTest.class );

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EpicsChannelMonitorService epicsChannelMonitorService;

   private String epicsChannelListOk;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void buildJsonNotificationBody() throws IOException
   {
      epicsChannelListOk = Files.readString( Paths.get("src/test/resources/epics/epics_channel_list_ok.json") );
      WicaStreamId.resetAllocationSequencer();
   }

   @Test
   void testDelete_RequestIsRejectedWhenStreamIdIsUnrecognised() throws Exception
   {
      final RequestBuilder rb = MockMvcRequestBuilders.delete( "/ca/streams/XXXXX" );
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }

   @Test
   void testDelete_RequestIsAccepted() throws Exception
   {
      // Send a POST request to create a stream with a list containing a couple of EPICS channels
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" )
                                                               .content( epicsChannelListOk )
                                                               .contentType(MediaType.APPLICATION_JSON_VALUE )
                                                               .accept(MediaType.TEXT_PLAIN_VALUE );

      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );
      assertEquals( 2, epicsChannelMonitorService.getChannelsActiveCount() );

      // Send a DELETE request to delete the stream we just created
      final RequestBuilder rb = MockMvcRequestBuilders.delete( "/ca/streams/0" );
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isOk() ).andReturn();
      assertEquals( 0, epicsChannelMonitorService.getChannelsActiveCount() );
      assertEquals( 2, epicsChannelMonitorService.getChannelsCreatedCount() );
      assertEquals( 2, epicsChannelMonitorService.getChannelsDeletedCount() );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
