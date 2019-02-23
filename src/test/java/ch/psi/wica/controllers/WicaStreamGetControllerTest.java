/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
@AutoConfigureMockMvc
class WicaStreamGetControllerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamGetControllerTest.class );

	@Autowired
	private MockMvc mockMvc;

   private String epicsChannelListOk;
   private String epicsChannelListOkWithShortenedHeartbeat;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void buildJsonNotificationBody() throws IOException
   {
      epicsChannelListOk = Files.readString(Paths.get("src/test/resources/epics/epics_channel_list_ok.json") );
      epicsChannelListOkWithShortenedHeartbeat = Files.readString(Paths.get("src/test/resources/epics/epics_channel_list_ok_with_shortened_heartbeat.json") );
   }

   @Test
   void testGet_RequestReturnsEventStream() throws Exception
   {
      // Send a POST request with a list containing a couple of EPICS channels
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" ).content(epicsChannelListOk).contentType(MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );
      final int id = Integer.parseInt( postRequestResult.getResponse().getContentAsString() );

      // Send a GET request to subscribe to the stream we just created
      final RequestBuilder getRequest = MockMvcRequestBuilders.get( "/ca/streams/" + id ).accept( MediaType.TEXT_EVENT_STREAM_VALUE);
      final MvcResult getRequestResult = mockMvc.perform( getRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", getRequestResult.getResponse().getContentAsString() );

      // Check that the ContentType in the response indicated we are now subscribed to an event stream
      final String contentType = getRequestResult.getResponse().getContentType();
      logger.info( "Returned ContentType was: '{}'", contentType );
      assertEquals("text/event-stream;charset=UTF-8", contentType );
   }

   @Test
   void testGet_RequestEventStreamIncludesHeartbeatSignal() throws Exception
   {
      // Send a POST request with a list containing a couple of EPICS channels and a heartbeat interval of 2s
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" )
                                                               .content( epicsChannelListOkWithShortenedHeartbeat )
                                                               .contentType( MediaType.APPLICATION_JSON_VALUE )
                                                               .accept( MediaType.TEXT_PLAIN_VALUE );

      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );
      final int id = Integer.parseInt( postRequestResult.getResponse().getContentAsString() );

      // Send a GET request to subscribe to the stream we just created
      final RequestBuilder getRequest = MockMvcRequestBuilders.get( "/ca/streams/" + id ).accept( MediaType.TEXT_EVENT_STREAM_VALUE );
      final int heartbeatIntervalInMilliseconds = 2_000;
      mockMvc.perform( getRequest )
             .andExpect( status().isOk() )
             .andExpect( content().contentType( "text/event-stream;charset=UTF-8" ) )
             .andDo( l -> Thread.sleep( heartbeatIntervalInMilliseconds + 1000 ) )
             .andDo( print() )
             .andExpect( content().string( containsString( "id:" ) ) )
             .andExpect( content().string( containsString( "heartbeat" ) ) )
             .andReturn();
   }

   @Test
   void testGet_RequestIsRejectedWhenStreamIdIsUnrecognised() throws Exception
   {
      final RequestBuilder rb = MockMvcRequestBuilders.get( "/ca/streams/XXXXX" ).accept( MediaType.TEXT_EVENT_STREAM_VALUE);
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
