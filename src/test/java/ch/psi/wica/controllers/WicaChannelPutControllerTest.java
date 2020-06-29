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

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
@AutoConfigureMockMvc
class WicaChannelPutControllerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   @Autowired
	private MockMvc mockMvc;

   @Value( "${wica.channel-get-timeout-interval-in-ms}")
   private int DEFAULT_PUT_TIMEOUT;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Test
   void testPutRequest_DefaultTimeout()
   {
      final String channelName = "XXXXX";
      final RequestBuilder putRequest = MockMvcRequestBuilders.put("/ca/channel/" + channelName )
                                                              .contentType( MediaType.TEXT_PLAIN_VALUE )
                                                              .accept(MediaType.TEXT_PLAIN_VALUE )
                                                              .content( "25" );

      // Check that the method returns in less than the default timeout
      final int methodDefaultTimeout = DEFAULT_PUT_TIMEOUT;
      final int guardTime = 200;
      final int testTimeoutInMillis = methodDefaultTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( putRequest )
               .andExpect( status().is5xxServerError() )
               .andDo( print() )
               .andReturn();
      } );
   }

   @Test
   void testPutRequest_UserSpecifiedTimeout()
   {
      final String channelName = "XXXXX";
      final int userSpecifiedTimeout = 300;
      final RequestBuilder putRequest = MockMvcRequestBuilders.put("/ca/channel/" + channelName + "?timeout=" + userSpecifiedTimeout)
                                                              .contentType( MediaType.TEXT_PLAIN_VALUE )
                                                              .accept( MediaType.TEXT_PLAIN_VALUE )
                                                              .content( "25" );

      // Check that the method returns in less than the user specified timeout
      final int guardTime = 200;
      final int testTimeoutInMillis = userSpecifiedTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( putRequest )
               .andExpect( status().is5xxServerError() )
               .andDo( print() )
               .andReturn();
      } );
   }

   // By default this test is suppressed as it would create problems in the automatic
   // build system. The test should be enabled as required during pre-production testing.
   @Disabled
   @Test
   void testPutRequest_HappyDay()
   {
      final String channelName = "wica:test:db_ok";
      final RequestBuilder getRequest = MockMvcRequestBuilders.put("/ca/channel/" + channelName )
                                                              .contentType( MediaType.TEXT_PLAIN_VALUE )
                                                              .accept( MediaType.TEXT_PLAIN_VALUE )
                                                              .content( "25" );

      // Check that the method returns in less than the default timeout
      final int methodDefaultTimeout = DEFAULT_PUT_TIMEOUT;
      final int guardTime = 200;
      final int testTimeoutInMillis = methodDefaultTimeout + guardTime;
      assertTimeoutPreemptively( Duration.ofMillis( testTimeoutInMillis ), () -> {
         mockMvc.perform( getRequest )
               .andExpect( status().isOk())
               .andExpect( content().contentTypeCompatibleWith( MediaType.TEXT_PLAIN ) )
               .andDo( print() )
               .andReturn();
      } );
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
