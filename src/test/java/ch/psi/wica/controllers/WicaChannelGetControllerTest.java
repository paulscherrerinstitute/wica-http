/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controllers;

/*- Imported packages --------------------------------------------------------*/

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
   void testGetRequest_DefaultTimeout()
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
               .andExpect( content().string(containsString("\"conn\":false") ) )
               .andReturn();
      } );
   }

   @Test
   void testGetRequest_UserSpecifiedTimeout()
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

   @Test
   void testGetRequest_HappyDay()
   {
      final String channelName = "test:db_ok";
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


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
