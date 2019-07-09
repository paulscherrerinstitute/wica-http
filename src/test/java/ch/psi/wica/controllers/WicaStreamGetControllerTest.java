/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

   private static final Logger logger = LoggerFactory.getLogger( WicaStreamGetControllerTest.class );

	@Autowired
	private MockMvc mockMvc;

   private String epicsChannelListOk;
   private String epicsChannelListOkWithShortenedHeartbeat;
   private String epicsChannelListOkCustomisedForStepVerifier;

   @Autowired
   private WicaStreamGetController wicaStreamGetController;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void buildJsonNotificationBody() throws IOException
   {
      epicsChannelListOk = Files.readString(Paths.get("src/test/resources/epics/epics_channel_list_ok.json") );
      epicsChannelListOkWithShortenedHeartbeat = Files.readString(Paths.get("src/test/resources/epics/epics_channel_list_ok_with_shortened_heartbeat.json") );
      epicsChannelListOkCustomisedForStepVerifier = Files.readString(Paths.get("src/test/resources/epics/epics_channel_list_ok_customised_for_step_verifier.json") );
   }

   @Test
   void testSendValidRequest_EventStreamReturnedOk() throws Exception
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
   void testSendValidRequest_EventStreamShouldIncludeHeartbeatSignal() throws Exception
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
   void testSendValidRequest_EventStreamIncludesExpectedEventIds() throws Exception
   {
      // Send a POST request with a list containing a couple of EPICS channels and a heartbeat interval of 2s
      final RequestBuilder postRequest = MockMvcRequestBuilders.post( "/ca/streams" )
            .content( epicsChannelListOkCustomisedForStepVerifier )
            .contentType( MediaType.APPLICATION_JSON_VALUE )
            .accept( MediaType.TEXT_PLAIN_VALUE );

      final MvcResult postRequestResult = mockMvc.perform( postRequest ).andDo( print()).andExpect( status().isOk() ).andReturn();
      final String id = postRequestResult.getResponse().getContentAsString();
      final ResponseEntity<Flux<ServerSentEvent<String>>> responseEntity = wicaStreamGetController.get( Optional.of( id ), null );
      final Flux<ServerSentEvent<String>> flux = responseEntity.getBody();

      StepVerifier.create( flux )
            .expectSubscription()
            .expectNextMatches( sse -> sseCommentContains( sse, "initial channel metadata" ) )
            .expectNextMatches( sse -> sseCommentContains( sse, "initial channel values" ) )
            .expectNextMatches( sse -> sseCommentContains( sse, "channel value changes" ) )
            .expectNextMatches( sse -> sseCommentContains( sse, "polled channel values" ) )
            .expectNextMatches( sse -> sseCommentContains( sse, "heartbeat" ) )
            .expectNextMatches( sse -> sseCommentContains( sse, "channel value changes" ) )
            .expectNextMatches( sse -> sseCommentContains( sse, "polled channel values" ) )
            .consumeNextWith( (x) -> deleteStream( id ) )
            .thenConsumeWhile( t -> sseCommentContains( t, "-") )
            .verifyComplete();
   }

   @Test
   void testSendInvalidRequestEmptyPathVariable_ShouldBeRejected() throws Exception
   {
      // Send the request with a null string as the content.
      final RequestBuilder rb = MockMvcRequestBuilders.get("/ca/streams/" + "" ).accept( MediaType.TEXT_EVENT_STREAM_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest() ).andReturn();

      // Now check all the expectations were satisfied.
      Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus() );
      final String responseHeader = result.getResponse().getHeader("X-WICA-ERROR" );
      assertNotNull( responseHeader );
      Assertions.assertEquals("WICA SERVER: The stream ID was empty/null.", responseHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      Assert.assertEquals("", content );
      logger.info( "Returned Content was: '{}'", content );
   }

   @Test
   void testSendInvalidRequestBlankPathVariable_ShouldBeRejected() throws Exception
   {
      // Send the request with a blank string as the content.
      final RequestBuilder rb = MockMvcRequestBuilders.get("/ca/streams/" + " " ).accept( MediaType.TEXT_EVENT_STREAM_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Now check all the expectations were satisfied.
      Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus() );
      final String responseHeader = result.getResponse().getHeader("X-WICA-ERROR" );
      assertNotNull( responseHeader );
      Assertions.assertEquals("WICA SERVER: The stream ID was blank.", responseHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      Assert.assertEquals("", content );
      logger.info( "Returned Content was: '{}'", content );
   }


   @Test
   void testSendInvalidRequestUnknownStreamId_ShouldBeRejected() throws Exception
   {
      // Send the request with an unknown stream ID.
      final RequestBuilder rb = MockMvcRequestBuilders.get("/ca/streams/" + "UnknownStreamId!" ).accept( MediaType.TEXT_EVENT_STREAM_VALUE );
      final MvcResult result = mockMvc.perform( rb ).andDo( print()).andExpect( status().isBadRequest()).andReturn();

      // Check that the X-WICA-ERROR is as expected
      final String errorHeader = result.getResponse().getHeader( "X-WICA-ERROR" );
      assertNotNull( errorHeader );
      Assert.assertEquals("WICA SERVER: The stream ID 'UnknownStreamId!' was not recognised.", errorHeader );

      // Check that the body content was empty as expected.
      final String content = result.getResponse().getContentAsString();
      Assert.assertEquals("", content );
      logger.info( "Returned Content was: '{}'", content );
   }


/*- Private methods ----------------------------------------------------------*/

   void deleteStream( String streamId )
   {
      final RequestBuilder deleteRequest = MockMvcRequestBuilders.delete( "/ca/streams/" + streamId );
      try
      {
         mockMvc.perform( deleteRequest ).andDo( print() ).andExpect( status().isOk() ).andReturn();
      }
      catch( Exception ex )
      {
         logger.warn( "Unexpected exception when deleting stream" );
      }
   }

   static boolean sseCommentContains( ServerSentEvent<String> sse, String str )
   {
      Validate.notNull(sse );
      Validate.notNull( sse.comment() );
      final boolean result = sse.comment().contains( str );
      logger.info( "Checking whether SSE comment: '{}' contains: '{}'. Result: {} ", sse.comment(),  str, result );
      return result;
   }

/*- Nested Classes -----------------------------------------------------------*/

}
