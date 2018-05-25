/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import org.junit.Before;
import org.junit.Test;
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
public class MvcTests
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( MvcTests.class );

	@Autowired
	private MockMvc mockMvc;

   private String jsonNotificationBody;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Before
   public void buildJsonNotificationBody() throws IOException
   {
      jsonNotificationBody = new String( Files.readAllBytes( Paths.get( "src/test/resources/epics_channels.json") ), "UTF-8" ) ;
   }

	@Test
   public void testPostEpicsCaStream() throws Exception
   {
   	  final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams" ).content( jsonNotificationBody ).contentType( MediaType.APPLICATION_JSON_VALUE ).accept( MediaType.TEXT_PLAIN_VALUE );
   	  MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isOk() ).andReturn();
   	  logger.info( "Returned data was: '{}'", result.getResponse().getContentAsString() );

   	  assertEquals("0", result.getResponse().getContentAsString() );
	}


   @Test
   public void testGetEpicsCaStream() throws Exception
   {
      // Subscribe to a few channels
      final RequestBuilder rb1 = MockMvcRequestBuilders.post( "/ca/streams" ).content( jsonNotificationBody ).contentType( MediaType.APPLICATION_JSON_VALUE ).accept( MediaType.TEXT_PLAIN_VALUE );
      MvcResult result = mockMvc.perform( rb1 ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", result.getResponse().getContentAsString() );

      // Now monitor them
      final RequestBuilder rb2 = MockMvcRequestBuilders.get( "/ca/streams/0" ).accept( MediaType.TEXT_EVENT_STREAM_VALUE);

      MvcResult result1 = mockMvc.perform( rb2 ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", result1.getResponse().getContentAsString() );

      Thread.sleep( 5000L );

      MvcResult result2 = mockMvc.perform( rb2 ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data now is: '{}'", result2.getResponse().getContentAsString() );
      //assertEquals("0", result2.getResponse().getContentAsString() );
   }



   /*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
