/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/


import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
@Immutable
public class WicaStreamConfigurationDecoder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaStream decode( String jsonInputString )
   {
      Validate.notNull( jsonInputString, "The JSON input string was null." );
      Validate.notEmpty( jsonInputString, "The JSON input string was empty." );
      Validate.notBlank( jsonInputString, "The JSON input string was blank." );
      try
      {
         return parse( jsonInputString );
      }
      catch ( IOException ex )
      {
         final Logger logger = LoggerFactory.getLogger(WicaStreamConfigurationDecoder.class);
         logger.warn("Failed to decode JSON channel configuration string '{}'", jsonInputString );
         logger.warn("The detail of the exception message was '{}", ex.getMessage() );
         throw new IllegalArgumentException( "The JSON channel configuration string: '" + jsonInputString + "' was invalid.", ex );
      }
   }

/*- Private methods ----------------------------------------------------------*/

   private WicaStream parse( String jsonInputString ) throws IOException
   {
      final ObjectMapper mapper = new ObjectMapper();
      mapper.enable( JsonParser.Feature.STRICT_DUPLICATE_DETECTION );
      final JsonNode rootNode = mapper.readTree( jsonInputString );

      if ( !rootNode.isObject() )
      {
         throw new IllegalArgumentException("The root node of the JSON configuration string was not a JSON Object.");
      }

      final WicaStreamProperties wicaStreamProperties;
      if ( rootNode.hasNonNull("props") )
      {
         final JsonNode propsNode = rootNode.get( "props" );
         if ( propsNode.isContainerNode() )
         {
            final String wicaStreamPropertiesString = propsNode.toString();
            final WicaStreamProperties wicaStreamPropertiesFromJson = WicaStreamSerializer.readFromJson( wicaStreamPropertiesString, WicaStreamProperties.class );
            wicaStreamProperties = WicaStreamPropertiesBuilder.create().withDefaultProperties().withStreamProperties( wicaStreamPropertiesFromJson ).build();
         }
         else
         {
            throw new IllegalArgumentException( "The 'props' field in the root node of the JSON configuration string was not a container value." );
         }
      }
      else
      {
         wicaStreamProperties = WicaStreamPropertiesBuilder.create().withDefaultProperties().build();
      }

      if ( ! rootNode.has("channels") )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string did not contain a field named 'channels'.");
      }

      if ( ! rootNode.hasNonNull("channels") )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string did not contain a value for field named 'channels'.");
      }

      final JsonNode channelsObjectNode = rootNode.get( "channels" );
      if ( ! channelsObjectNode.isArray() )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string contained a field named 'channels', but it wasn't an array." );
      }

      WicaStreamBuilder wicaStreamBuilder = WicaStreamBuilder.create().withStreamProperties( wicaStreamProperties );

      final JsonNode channelArrayNode = rootNode.get( "channels" );
      for (final JsonNode channelNode: channelArrayNode )
      {
         if ( !channelNode.has("name") )
         {
            throw new IllegalArgumentException( "The JSON configuration string did not specify the name of one or more channels (missing 'name' field).");
         }

         if ( !channelNode.hasNonNull("name") )
         {
            throw new IllegalArgumentException( "The JSON configuration string did not contain a valid value for one or more channel 'name' fields.");
         }

         final String wicaChannelName = channelNode.get("name").textValue();
         if ( channelNode.has("props") )
         {
            final JsonNode propsNode = channelNode.get( "props" );
            if ( propsNode.isContainerNode() )
            {

               final WicaChannelProperties wicaChannelPropertiesFromJson = WicaStreamSerializer.readFromJson( propsNode.toString(), WicaChannelProperties.class );
               wicaStreamBuilder = wicaStreamBuilder.withChannelNameAndCombinedProperties( wicaChannelName, wicaChannelPropertiesFromJson );
           }
            else
            {
               throw new IllegalArgumentException( "The 'props' field in one or more channel nodes of the JSON configuration string was not a container value." );
            }
         }
         else
         {
            wicaStreamBuilder = wicaStreamBuilder.withChannelNameAndStreamProperties( wicaChannelName );
         }
      }

      return wicaStreamBuilder.build();
   }

/*- Nested Classes -----------------------------------------------------------*/

}
