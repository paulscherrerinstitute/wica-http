/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaStreamId;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WicaStreamGetControllerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamGetControllerTest.class );

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
      epicsChannelListOk = Files.readString(Paths.get("src/test/resources/epics/epics_channel_list_ok.json") );
      WicaStreamId.resetAllocationSequencer();
   }


   @Test
   public void testGet_RequestReturnsEventStream() throws Exception
   {
      // Send a POST request with a list containing a couple of EPICS channels
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" ).content(epicsChannelListOk).contentType(MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );

      // Send a GET request to subscribe to the stream we just created
      final RequestBuilder getRequest = MockMvcRequestBuilders.get( "/ca/streams/0" ).accept( MediaType.TEXT_EVENT_STREAM_VALUE);
      final MvcResult getRequestResult = mockMvc.perform( getRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", getRequestResult.getResponse().getContentAsString() );

      // Check that the ContentType in the response indicated we are now subscribed to an event stream
      final String contentType = getRequestResult.getResponse().getContentType();
      logger.info( "Returned ContentType was: '{}'", contentType );
      assertEquals("text/event-stream;charset=UTF-8", contentType );
   }

   @Test
   public void testGet_RequestEventStreamIncludesHeartbeatSignal() throws Exception
   {
      // Send a POST request with a list containing a couple of EPICS channels
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" ).content( epicsChannelListOk ).contentType(MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );

      // Send a GET request to subscribe to the stream we just created
      final RequestBuilder getRequest = MockMvcRequestBuilders.get( "/ca/streams/0" ).accept( MediaType.TEXT_EVENT_STREAM_VALUE );
      final int heartbeatIntervalInMilliseconds = 11_000;
      mockMvc.perform( getRequest )
             .andExpect( status().isOk() )
             .andExpect( content().contentType( "text/event-stream;charset=UTF-8" ) )
             .andDo( l -> Thread.sleep( heartbeatIntervalInMilliseconds + 1000 ) )
             .andDo( print() )
             .andExpect( content().string( containsString( "id:0" ) ) )
             .andExpect( content().string( containsString( "heartbeat" ) ) )
             .andReturn();
   }

   @Test
   public void testGet_RequestIsRejectedWhenStreamIdIsUnrecognised() throws Exception
   {
      final RequestBuilder rb = MockMvcRequestBuilders.get( "/ca/streams/XXXXX" ).accept( MediaType.TEXT_EVENT_STREAM_VALUE);
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
