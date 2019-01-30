/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

// Todo:
// Note this class is clunky. It should probably be refactored to perform
// automatic decoding directly into the classes of interest.

@Immutable
public class WicaStreamConfigurationDecoder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStreamConfigurationDecoder.class );

   private Map<String,String> streamPropertiesMap;

   private Set<WicaChannel> wicaChannels = new HashSet<>();

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
         logger.warn( "Failed to decode JSON channel configuration string '{}'", jsonInputString );
         logger.warn( "The detail of the exception message was '{}", ex.getMessage() );
         throw new IllegalArgumentException( "The JSON channel configuration string: '" + jsonInputString + "' was illegal", ex );
      }
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaStreamProperties getWicaStreamProperties()
   {
      return WicaStreamProperties.of( streamPropertiesMap );
   }

   public Set<WicaChannel> getWicaChannels()
   {
      return Collections.unmodifiableSet( wicaChannels );
   }


/*- Private methods ----------------------------------------------------------*/

   private void parse( String jsonInputString ) throws IOException
   {
      final ObjectMapper mapper = new ObjectMapper();
      final JsonNode rootNode = mapper.readTree(jsonInputString);

      if ( !rootNode.isObject() )
      {
         throw new IllegalArgumentException("The root node of the JSON configuration string was not a JSON Object.");
      }

      if ( rootNode.hasNonNull("props") )
      {
         this.streamPropertiesMap = decodeObject( rootNode.get( "props" ) );
      }
      else
      {
         this.streamPropertiesMap = new HashMap<>();
      }

      if ( ! rootNode.has("channels") )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string did not contain a field named 'channels'");
      }

      if ( ! rootNode.hasNonNull("channels") )
      {
         throw new IllegalArgumentException( "The root node of the JSON configuration string did not contain a value for field named 'channels'");
      }

      final JsonNode channelsObjectNode = rootNode.findValue( "channels" );
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
         final JsonNode strNode = channelNode.findValue("name");

         final WicaChannelName wicaChannelName = WicaChannelName.of(strNode.asText());

         final Map<String, String> propsMap;
         if ( channelNode.has("props") )
         {
            if ( channelNode.hasNonNull("props") )
            {
               propsMap = decodeObject( channelNode.get( "props" ) );
            }
            else
            {
               throw new IllegalArgumentException( "The JSON configuration string did not specify one or more property values (missing 'props' value field)" );
            }
         }
         else
         {
            propsMap = new HashMap<>();
         }
         this.wicaChannels.add( new WicaChannel( wicaChannelName, WicaChannelProperties.of( propsMap ) ) );
      }
   }

   private Map<String,String> decodeObject( JsonNode propsNode )
   {
      Validate.notNull( propsNode, "The JSON configuration string did not specify one or more property values (missing 'props' value field)" );

      final Map<String,String> propsMap = new HashMap<>();
      for( Iterator<String> it = propsNode.fieldNames(); it.hasNext();)
      {
         final String propName = it.next();
         if ( propsNode.hasNonNull( propName ) )
         {
            final JsonNode propValue = propsNode.get(propName);
            propsMap.put(propName, propValue.asText());
         }
         else
         {
            throw new IllegalArgumentException( "The JSON configuration string did not specify one or more property values (missing '" + propName + "' field)" );
         }
      }

      return propsMap;
   }


/*- Nested Classes -----------------------------------------------------------*/

}
