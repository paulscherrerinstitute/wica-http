/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelName;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.stream.WicaStreamProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaStreamConfigurationDecoder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Set<WicaChannel> wicaChannels = new HashSet<>();
   private WicaStreamProperties wicaStreamProperties;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaStreamConfigurationDecoder( String jsonInputString )
   {
      Validate.notEmpty( jsonInputString );
      try
      {
         parse( jsonInputString );
      }
      catch ( IOException ex )
      {
         final Logger logger = LoggerFactory.getLogger(WicaStreamConfigurationDecoder.class);
         logger.warn("Failed to decode JSON channel configuration string '{}'", jsonInputString );
         logger.warn("The detail of the exception message was '{}", ex.getMessage() );
         throw new IllegalArgumentException( "The JSON channel configuration string: '" + jsonInputString + "' was illegal", ex );
      }
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaStreamProperties getWicaStreamProperties()
   {
      return wicaStreamProperties;
   }

   public Set<WicaChannel> getWicaChannels()
   {
      return Collections.unmodifiableSet( wicaChannels );
   }


/*- Private methods ----------------------------------------------------------*/

   private void parse( String jsonInputString ) throws IOException
   {
      final ObjectMapper mapper = new ObjectMapper();
      final JsonNode rootNode = mapper.readTree( jsonInputString );

      if ( !rootNode.isObject() )
      {
         throw new IllegalArgumentException("The root node of the JSON configuration string was not a JSON Object.");
      }

      if ( rootNode.hasNonNull("props") )
      {
         final JsonNode propsNode = rootNode.get( "props" );
         if ( propsNode.isContainerNode() )
         {
            this.wicaStreamProperties = mapper.treeToValue( propsNode, WicaStreamProperties.class) ;
         }
         else
         {
            throw new IllegalArgumentException( "The 'props' field in the root node of the JSON configuration string was not a container value." );
         }
      }
      else
      {
         this.wicaStreamProperties = mapper.readValue("{}" , WicaStreamProperties.class );
      }

      if ( ! rootNode.has("channels") )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string did not contain a field named 'channels'");
      }

      if ( ! rootNode.hasNonNull("channels") )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string did not contain a value for field named 'channels'");
      }

      final JsonNode channelsObjectNode = rootNode.get( "channels" );
      if ( ! channelsObjectNode.isArray() )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string contained a field named 'channels', but it wasn't an array" );
      }

      final JsonNode channelArrayNode = rootNode.get( "channels" );
      for (final JsonNode channelNode: channelArrayNode )
      {
         if ( !channelNode.has("name") )
         {
            throw new IllegalArgumentException( "The JSON configuration string did not specify the name of one or more channels (missing 'name' field)");
         }

         if ( !channelNode.hasNonNull("name") )
         {
            throw new IllegalArgumentException( "The JSON configuration string did not contain a valid value for one or more channel 'name' fields.");
         }

         final JsonNode strNode = channelNode.get("name");
         final WicaChannelName wicaChannelName = WicaChannelName.of( strNode.asText() );

         final WicaChannelProperties wicaChannelProperties;
         if ( channelNode.has("props") )
         {
            final JsonNode propsNode = channelNode.get( "props" );
            if ( propsNode.isContainerNode() )
            {
               wicaChannelProperties = mapper.treeToValue( propsNode, WicaChannelProperties.class);
            }
            else
            {
               throw new IllegalArgumentException( "The 'props' field in one or more channel nodes of the JSON configuration string was not a container value." );
            }
         }
         else
         {
            wicaChannelProperties = mapper.readValue("{}" , WicaChannelProperties.class );
         }
         this.wicaChannels.add( WicaChannel.createFromNameAndProperties( wicaChannelName, wicaChannelProperties ) );
      }
   }

/*- Nested Classes -----------------------------------------------------------*/

}
