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
import org.springframework.http.HttpStatus;
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
class WicaStreamCreateControllerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamCreateControllerTest.class );

	@Autowired
	private MockMvc mockMvc;

   private String epicsChannelListOk1;
   private String epicsChannelListOk2;
   private String epicsChannelListEmpty;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void beforeEach() throws IOException
   {
      epicsChannelListOk1 = Files.readString(Paths.get("src/test/resources/epics/epics_channel_list_ok.json") );
      epicsChannelListOk2 = Files.readString( Paths.get("src/test/resources/epics/epics_channel_list_ok2.json") );
      epicsChannelListEmpty = Files.readString( Paths.get("src/test/resources/epics/epics_channel_list_empty.json") );
   }

	@Test
   void testPost_RequestIsProcessedNormallyWhenEpicsChannelListOk() throws Exception
   {
      // Send a POST request with a list containing a couple of EPICS channels
      final RequestBuilder rb1 = MockMvcRequestBuilders.post( "/ca/streams" ).content( epicsChannelListOk1 ).contentType(MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
   	final MvcResult response1 = mockMvc.perform( rb1 ).andDo( print() ).andExpect( status().isOk() ).andReturn();

   	// Check that the status code was ok
   	final int statusCode = response1.getResponse().getStatus();
      logger.info( "Response status code was: '{}'", statusCode  );
      assertEquals( HttpStatus.OK.value(), statusCode );

      // Check that the ContentType in the response is just normal text
      final String contentType = response1.getResponse().getContentType();
   	logger.info( "Returned ContentType was: '{}'", contentType );
      assertEquals("text/plain;charset=UTF-8", contentType );

      // Check that the first stream that was allocated was Stream with id "0"
      final String content1 = response1.getResponse().getContentAsString();
      logger.info( "Returned Content was: '{}'", content1 );
      assertEquals( "0", content1 );

      // Now make a second request and check that the second stream which was allocated had id "1"
      final RequestBuilder rb2 = MockMvcRequestBuilders.post( "/ca/streams" ).content( epicsChannelListOk2 ).contentType( MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
      final MvcResult response2 = mockMvc.perform( rb2 ).andDo( print() ).andExpect( status().isOk() ).andReturn();
      final String content2 = response2.getResponse().getContentAsString();
      logger.info( "Returned Content was: '{}'", content2 );
      assertEquals("1", content2 );
   }

   @Test
   void testPost_RequestIsRejectedWhenEpicsChannelListIsEmpty() throws Exception
   {
      // Send a POST request with an empty EPICS channel list
      final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams" ).content(epicsChannelListEmpty).contentType(MediaType.APPLICATION_JSON_VALUE ).accept(MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest() ).andReturn();
      final String content = result.getResponse().getContentAsString();
      logger.info( "Returned Content was: '{}'", content );
      assertEquals("The JSON configuration string did not define any channels.", content );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
