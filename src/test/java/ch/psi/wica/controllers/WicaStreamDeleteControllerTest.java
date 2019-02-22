/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaStreamId;
import ch.psi.wica.services.epics.EpicsChannelMonitorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WicaStreamDeleteControllerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamDeleteControllerTest.class );

	@Autowired
	private MockMvc mockMvc;

   private String epicsChannelListOk;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   // TODO for some reason this test is still being run under junit 4. So
   // the @before annotation is required and @BeforeEach does nothing
   @Before
   @BeforeEach
   public void buildJsonNotificationBody() throws IOException
   {
      epicsChannelListOk = new String(Files.readAllBytes(Paths.get("src/test/resources/epics/epics_channel_list_ok.json") ), StandardCharsets.UTF_8) ;
      WicaStreamId.resetAllocationSequencer();
   }

   @Test
   public void testDelete_RequestIsRejectedWhenStreamIdIsUnrecognised() throws Exception
   {
      final RequestBuilder rb = MockMvcRequestBuilders.delete( "/ca/streams/XXXXX" );
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }

   @Test
   public void testDelete_RequestIsAccepted() throws Exception
   {
      // Send a POST request to create a stream with a list containing a couple of EPICS channels
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" )
                                                               .content( epicsChannelListOk )
                                                               .contentType(MediaType.APPLICATION_JSON_VALUE )
                                                               .accept(MediaType.TEXT_PLAIN_VALUE );

      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );
      assertEquals( 2, EpicsChannelMonitorService.getChannelsCreatedCount() );

      // Send a DELETE request to delete the stream we just created
      final RequestBuilder rb = MockMvcRequestBuilders.delete( "/ca/streams/0" );
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isOk() ).andReturn();
      assertEquals( 2, EpicsChannelMonitorService.getChannelsCreatedCount() );
   }


   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
