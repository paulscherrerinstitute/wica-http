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
import org.springframework.http.HttpStatus;
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
public class MvcTests
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( MvcTests.class );

	@Autowired
	private MockMvc mockMvc;

   private String epicsChannelListOk;
   private String epicsChannelListEmpty;

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
      epicsChannelListEmpty = new String(Files.readAllBytes(Paths.get("src/test/resources/epics/epics_channel_list_empty.json") ), StandardCharsets.UTF_8) ;
      WicaStreamId.resetAllocationSequencer();
   }

	@Test
   public void testPost_RequestIsProcessedNormallyWhenEpicsChannelListOk() throws Exception
   {
      // Send a POST request with a list containing a couple of EPICS channels
      final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams" ).content( epicsChannelListOk ).contentType(MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
   	final MvcResult result1 = mockMvc.perform( rb ).andDo( print()).andExpect( status().isOk() ).andReturn();

   	// Check that the status code was ok
   	final int statusCode = result1.getResponse().getStatus();
      logger.info( "Response status code was: '{}'", statusCode  );
      assertEquals( HttpStatus.OK.value(), statusCode );

      // Check that the ContentType in the response is just normal text
      final String contentType = result1.getResponse().getContentType();
   	logger.info( "Returned ContentType was: '{}'", contentType );
      assertEquals("text/plain;charset=UTF-8", contentType );

      // Check that the first stream that was allocated was Stream with id "0"
      final String content1 = result1.getResponse().getContentAsString();
      logger.info( "Returned Content was: '{}'", content1 );
      assertEquals( "0", content1 );
      //assertEquals( "0", content1 );

      // Now make a second request and check that the second stream which was allocated had id "1"
      final MvcResult result2 = mockMvc.perform( rb ).andDo( print()).andExpect( status().isOk() ).andReturn();
      final String content2 = result2.getResponse().getContentAsString();
      logger.info( "Returned Content was: '{}'", content2 );
      assertEquals("1", content2 );
   }

   @Test
   public void testPost_RequestIsRejectedWhenEpicsChannelListIsEmpty() throws Exception
   {
      // Send a POST request with an empty EPICS channel list
      final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams" ).content(epicsChannelListEmpty).contentType(MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest() ).andReturn();
      final String content = result.getResponse().getContentAsString();
      logger.info( "Returned Content was: '{}'", content );
      assertEquals("The JSON configuration string did not define any channels.", content );
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
      final int heartbeatIntervalInMilliseconds = 15_000;
      mockMvc.perform( getRequest )
             .andExpect( status().isOk() )
             .andExpect( content().contentType( "text/event-stream;charset=UTF-8" ) )
             .andDo( l -> Thread.sleep( heartbeatIntervalInMilliseconds + 5000 ) )
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
