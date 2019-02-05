/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/
import ch.psi.wica.model.WicaChannel;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelProperties;
import ch.psi.wica.model.WicaStream;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public class WicaChannelDataFieldsOfInterestSupplier implements  WicaChannelValueMapSerializer.FieldsOfInterestSupplier
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(WicaStreamConfigurationDecoder.class );
   private final WicaStream wicaStream;

   private final Map<WicaChannelName, Set<String>> map = new HashMap<>();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelDataFieldsOfInterestSupplier( WicaStream wicaStream )
   {
      this.wicaStream = Validate.notNull(wicaStream );
      addWicaStreamDefaultValues();
      addWicaChannelPropertiesOverrides();
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public Set<String> supplyForChannelNamed( WicaChannelName wicaChannelName)
   {
      return map.get( wicaChannelName );
   }



/*- Private methods ----------------------------------------------------------*/

   private void addWicaStreamDefaultValues()
   {
      final var wicaStreamProperties = wicaStream.getWicaStreamProperties();

      final boolean includeAlarmInfo = wicaStreamProperties.hasProperty( "includeAlarmInfo" )
            && wicaStreamProperties.getPropertyValue( "includeAlarmInfo" ).equals( "true" );
      logger.info( "includeAlarmInfo is : '{}'", includeAlarmInfo );

      final boolean includeTimestamp = wicaStreamProperties.hasProperty( "includeTimestamp" )
            && wicaStreamProperties.getPropertyValue( "includeTimestamp" ).equals( "true" );
      logger.info( "includeTimestamp is : '{}'", includeTimestamp );

      final Set<String> wicaChannelValueFieldSelectors = includeAlarmInfo ?
            ( includeTimestamp ? Set.of( "val", "sevr", "ts" ) : Set.of( "val", "sevr" ) ) :
            ( includeTimestamp ? Set.of( "val", "ts" ) : Set.of( "val" ) );

      logger.info( "Default fields selected for value serialization are '{}'", wicaChannelValueFieldSelectors );

      map.keySet().forEach( (c) -> map.put( c, wicaChannelValueFieldSelectors ) );
   }

   private void addWicaChannelPropertiesOverrides()
   {
      //wicaStream.getWicaChannels().stream().map( c -> c.getName(), c -> c. ).
   }


//
//   private Map<WicaChannelName,Set<String>> getValueFieldMap()
//   {
//      final Map<WicaChannelName,Set<String>> outputMap = new HashMap<>();
//      channelMap.keySet().forEach( c -> {
//         final WicaChannel wicaChannel = this.channelMap.get(c );
//         final WicaChannelProperties props = wicaChannel.getProperties();
//         final String fieldSpecifierString = props.hasProperty( "fields" ) ? props.getPropertyValue("fields") : "val;sevr";
//         final Set<String> fieldSerialisationSelectorSet = buildFieldSerialisationSelectorSet(fieldSpecifierString );
//         outputMap.put( c, fieldSerialisationSelectorSet );
//      } );
//
//      return outputMap;
//   }


   static private Set<String> buildFieldSerialisationSelectorSet( String fieldSerialsationSelectorString )
   {
      final String[] arr = fieldSerialsationSelectorString.split(";");
      return Set.of( arr );
   }

   /*- Nested Classes -----------------------------------------------------------*/

}
