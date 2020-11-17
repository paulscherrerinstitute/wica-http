/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
@AutoConfigureMockMvc
class WicaChannelGetControllerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
	private MockMvc mockMvc;

   @Value( "${wica.channel-get-timeout-interval-in-ms}")
   private int DEFAULT_GET_TIMEOUT;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testGetValueRequest_DefaultTimeout()
   {
      final String channelName = "XXXXX";
      final RequestBuilder getRequest = MockMvcRequestBuilders.get("/ca/channel/" + channelName + "?fieldsOfInterest=conn" )
                                                              .accept(MediaType.APPLICATION_JSON_VALUE );

      // Check that the method returns in less than the default timeout
      final int methodDefaultTimeout = DEFAULT_GET_TIMEOUT;
      final int guardTime = 200;
      final int testTimeoutInMillis = methodDefaultTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( getRequest )
               .andExpect( status().isOk() )
               .andExpect( content().contentTypeCompatibleWith( MediaType.APPLICATION_JSON_VALUE ) )
               .andDo( print() )
               .andExpect( content().string( containsString("\"conn\":false") ) )
               .andReturn();
      } );
   }

   @Test
   void testGetValueRequest_UserSpecifiedTimeout()
   {
      final String channelName = "XXXXX";
      final int userSpecifiedTimeout = 300;
      final RequestBuilder getRequest = MockMvcRequestBuilders.get("/ca/channel/" + channelName + "?timeout=" + userSpecifiedTimeout + "&fieldsOfInterest=conn" )
                                                              .accept( MediaType.APPLICATION_JSON_VALUE );

      // Check that the method returns in less than the user specified timeout
      final int guardTime = 200;
      final int testTimeoutInMillis = userSpecifiedTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( getRequest )
               .andExpect( status().isOk() )
               .andExpect( content().contentTypeCompatibleWith( MediaType.APPLICATION_JSON_VALUE ) )
               .andDo(print())
               .andExpect( content().string(containsString("\"conn\":false") ) )
               .andReturn();
      } );
   }

   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   @Disabled
   @Test
   void testGetValueRequest_HappyDay1()
   {
      final String channelName = "wica:test:db_ok";
      final RequestBuilder getRequest = MockMvcRequestBuilders.get("/ca/channel/" + channelName + "?fieldsOfInterest=conn" )
                                                              .accept( MediaType.APPLICATION_JSON_VALUE );

      // Check that the method returns in less than the default timeout
      final int methodDefaultTimeout = DEFAULT_GET_TIMEOUT;
      final int guardTime = 200;
      final int testTimeoutInMillis = methodDefaultTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( getRequest )
               .andExpect( status().isOk())
               .andExpect( content().contentTypeCompatibleWith( MediaType.APPLICATION_JSON_VALUE ) )
               .andDo( print())
               .andExpect( content().string( containsString("\"conn\":true") ) )
               .andReturn();
      } );
   }

   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   @Disabled
   @Test
   void testGetValueRequest_HappyDay_with_Added_TimestampCheck()
   {
      final LocalDateTime now = LocalDateTime.now();

      // Note: a more accurate timeAndDateNowString could be constructed by including seconds in the
      // matcher pattern. Thus: "yyyy-MM-dd'T'HH:mm:ss". But there is a higher chance this would fail
      // if the test is performed very close to the end of an second boundary.
      final String TRUNCATED_DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm";
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( TRUNCATED_DATETIME_FORMAT_PATTERN );
      final String truncatedTimeAndDateNow = now.format( formatter );

      final String channelName = "wica:test:db_ok";
      final RequestBuilder getRequest = MockMvcRequestBuilders.get("/ca/channel/" + channelName + "?fieldsOfInterest=ts" )
            .accept( MediaType.APPLICATION_JSON_VALUE );

      // Check that the method returns in less than the default timeout
      final int methodDefaultTimeout = DEFAULT_GET_TIMEOUT;
      final int guardTime = 200;
      final int testTimeoutInMillis = methodDefaultTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( getRequest )
               .andExpect( status().isOk())
               .andExpect( content().contentTypeCompatibleWith( MediaType.APPLICATION_JSON_VALUE ) )
               .andDo( print())
               .andExpect( content().string( containsString("\"ts\":" + "\"" + truncatedTimeAndDateNow ) ) )
               .andReturn();
      } );
   }

   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   @Disabled
   @Test
   void testGetMetadataRequest_HappyDay1()
   {
      final String channelName = "wica:test:db_ok";
      final RequestBuilder getRequest = MockMvcRequestBuilders.get("/ca/channel/metadata/" + channelName + "?fieldsOfInterest=type" )
            .accept( MediaType.APPLICATION_JSON_VALUE );

      // Check that the method returns in less than the default timeout
      final int methodDefaultTimeout = DEFAULT_GET_TIMEOUT;
      final int guardTime = 200;
      final int testTimeoutInMillis = methodDefaultTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( getRequest )
               .andExpect( status().isOk())
               .andExpect( content().contentTypeCompatibleWith( MediaType.APPLICATION_JSON_VALUE ) )
               .andDo( print())
               .andExpect( content().string( containsString("\"type\":\"REAL\"") ) )
               .andReturn();
      } );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
