/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.epics.EpicsChannelMonitoringService;
import ch.psi.wica.model.stream.WicaStreamId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

// Need to rewire the service after each test since the epics
// channel monitoring service will have been used an unknown
// number of times before the test begins so we cannot know
// the state that would be returned by getChannelsCreatedCount
// and getChannelsDeleteCount.
@DirtiesContext( classMode= DirtiesContext.ClassMode.BEFORE_CLASS )
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
	private EpicsChannelMonitoringService epicsChannelMonitoringService;

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
      epicsChannelMonitoringService.getStatistics().reset();
   }

   @Test
   void testSendValidDeleteByPostRequest_RequestIsRejectedWhenStreamIdIsUnrecognised() throws Exception
   {
      final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams/delete" )
            .contentType(MediaType.TEXT_PLAIN_VALUE )
            .content( "XXXXX" );
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }

   @Test
   void testSendValidDeleteByDeleteRequest_RequestIsRejectedWhenStreamIdIsUnrecognised() throws Exception
   {
      final RequestBuilder rb = MockMvcRequestBuilders.delete( "/ca/streams/XXXXX" );
      mockMvc.perform( rb ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }

   @Test
   void testSendValidDeleteByPostRequest_RequestIsAccepted() throws Exception
   {
      // Send a POST request to create a stream with a list containing a couple of EPICS channels
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" )
            .content( epicsChannelListOk )
            .contentType(MediaType.APPLICATION_JSON_VALUE )
            .accept(MediaType.TEXT_PLAIN_VALUE );

      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "2") );

      // Send a DELETE request to delete the stream we just created.
      final RequestBuilder deleteRequest1 = MockMvcRequestBuilders.post( "/ca/streams/delete" )
            .contentType(MediaType.TEXT_PLAIN_VALUE )
            .content( "0" );
      mockMvc.perform( deleteRequest1 ).andDo( print() ).andExpect( status().isOk() ).andReturn();

      // Verify that initially the total channel count and number of stop requests will not change.
      // But after the resources have been disposed of N seconds later they will be.
      assertThat( epicsChannelMonitoringService.getStatistics().getStopRequests(), is( "0") );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "2") );
      Thread.sleep( 6000 );
      assertThat( epicsChannelMonitoringService.getStatistics().getStopRequests(), is( "2") );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0") );

      // Send a further DELETE request to delete the stream we created before. This time it should be rejected.
      final RequestBuilder deleteRequest2 = MockMvcRequestBuilders.post( "/ca/streams/delete" )
            .contentType(MediaType.TEXT_PLAIN_VALUE )
            .content( "0" );
      mockMvc.perform( deleteRequest2 ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }

   @Test
   void testSendValidDeleteByDeleteRequest_RequestIsAccepted() throws Exception
   {
      // Send a POST request to create a stream with a list containing a couple of EPICS channels
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" )
                                                               .content( epicsChannelListOk )
                                                               .contentType(MediaType.APPLICATION_JSON_VALUE )
                                                               .accept(MediaType.TEXT_PLAIN_VALUE );

      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      logger.info( "Returned data was: '{}'", postRequestResult.getResponse().getContentAsString() );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "2") );

      // Send a DELETE request to delete the stream we just created.
      final RequestBuilder deleteRequest1 = MockMvcRequestBuilders.delete( "/ca/streams/0" );
      mockMvc.perform( deleteRequest1 ).andDo( print() ).andExpect( status().isOk() ).andReturn();

      // Verify that initially the total channel count and number of stop requests will not change.
      // But after the resources have been disposed of N seconds later they will be.
      assertThat( epicsChannelMonitoringService.getStatistics().getStopRequests(), is( "0") );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "2") );
      Thread.sleep( 6000 );
      assertThat( epicsChannelMonitoringService.getStatistics().getStopRequests(), is( "2") );
      assertThat( epicsChannelMonitoringService.getStatistics().getTotalChannelCount(), is( "0") );

      // Send a further DELETE request to delete the stream we created before. This time it should be rejected.
      final RequestBuilder deleteRequest2 = MockMvcRequestBuilders.delete( "/ca/streams/0" );
      mockMvc.perform( deleteRequest2 ).andDo( print() ).andExpect( status().isBadRequest() ).andReturn();
   }

   @Test
   void testSendInvalidDeleteByPostRequestEmptyRequestBody_ShouldBeRejected() throws Exception
   {
      // Send the request with a null string as the content.
      final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams/delete" )
            .contentType(MediaType.TEXT_PLAIN_VALUE )
            .content( "" )
            .accept( MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Now check all the expectations were satisfied.
      Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus() );
      final String responseHeader = result.getResponse().getHeader("X-WICA-ERROR" );
      assertNotNull( responseHeader );
      Assertions.assertEquals("WICA SERVER: The stream ID was empty/null.", responseHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      assertEquals( "", content );
      logger.info( "Returned Content was: '{}'", content );
   }

   @Test
   void testSendInvalidDeleteByDeleteRequestEmptyPathVariable_ShouldBeRejected() throws Exception
   {
      // Send the request with a null string as the content.
      final RequestBuilder rb = MockMvcRequestBuilders.delete("/ca/streams/" + "" ).accept( MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Now check all the expectations were satisfied.
      Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus() );
      final String responseHeader = result.getResponse().getHeader("X-WICA-ERROR" );
      assertNotNull( responseHeader );
      Assertions.assertEquals("WICA SERVER: The stream ID was empty/null.", responseHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      assertEquals( "", content );
      logger.info( "Returned Content was: '{}'", content );
   }

   @Test
   void testSendInvalidDeleteByPostRequestBlankPathVariable_ShouldBeRejected() throws Exception
   {
      // Send the request with a blank string as the content.
      final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams/delete" )
            .contentType(MediaType.TEXT_PLAIN_VALUE )
            .content( " " )
            .accept( MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Now check all the expectations were satisfied.
      Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus() );
      final String responseHeader = result.getResponse().getHeader("X-WICA-ERROR" );
      assertNotNull( responseHeader );
      Assertions.assertEquals("WICA SERVER: The stream ID was blank.", responseHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      assertEquals( "", content );
      logger.info( "Returned Content was: '{}'", content );
   }

   @Test
   void testSendInvalidDeleteByDeleteRequestBlankPathVariable_ShouldBeRejected() throws Exception
   {
      // Send the request with a blank string as the content.
      final RequestBuilder rb = MockMvcRequestBuilders.delete("/ca/streams/" + " " ).accept( MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Now check all the expectations were satisfied.
      Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus() );
      final String responseHeader = result.getResponse().getHeader("X-WICA-ERROR" );
      assertNotNull( responseHeader );
      Assertions.assertEquals("WICA SERVER: The stream ID was blank.", responseHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      assertEquals( "", content );
      logger.info( "Returned Content was: '{}'", content );
   }

   @Test
   void testSendInvalidDeleteByPostRequestUnknownStreamId_ShouldBeRejected() throws Exception
   {
      // Send the request with an unknown stream ID.
      final RequestBuilder rb = MockMvcRequestBuilders.post( "/ca/streams/delete" )
            .contentType(MediaType.TEXT_PLAIN_VALUE )
            .content( "UnknownStreamId!" )
            .accept( MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Check that the X-WICA-ERROR is as expected
      final String errorHeader = result.getResponse().getHeader( "X-WICA-ERROR" );
      assertNotNull( errorHeader );
      assertEquals( "WICA SERVER: The stream ID 'UnknownStreamId!' was not recognised.", errorHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      assertEquals( "", content );
      logger.info( "Returned Content was: '{}'", content );
   }


   @Test
   void testSendInvalidDeleteByDeleteRequestUnknownStreamId_ShouldBeRejected() throws Exception
   {
      // Send the request with an unknown stream ID.
      final RequestBuilder rb = MockMvcRequestBuilders.delete("/ca/streams/" + "UnknownStreamId!" ).accept( MediaType.TEXT_PLAIN_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Check that the X-WICA-ERROR is as expected
      final String errorHeader = result.getResponse().getHeader( "X-WICA-ERROR" );
      assertNotNull( errorHeader );
      assertEquals( "WICA SERVER: The stream ID 'UnknownStreamId!' was not recognised.", errorHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      assertEquals( "", content );
      logger.info( "Returned Content was: '{}'", content );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
