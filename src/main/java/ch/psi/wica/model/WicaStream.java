/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.*;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStream
{

/*- Public attributes --------------------------------------------------------*/

   private static final Set<String> STREAM_DEFAULT_FIELDS_OF_INTEREST = Set.of("val", "sevr" );
   private static final int STREAM_DEFAULT_NUMERIC_PRECISION = 6;
   private static final boolean STREAM_QUOTE_NUMERIC_STRINGS = false;

//   private static final WicaStreamPropertiesOld STREAM_DEFAULT_PROPERTIES = WicaStreamPropertiesOld.of(Map.of("fields", "val;sevr",
//                                                                                                              "heartbeatFluxInterval", "10000",
//                                                                                                              "channelValueUpdateFluxInterval", "100" ) );

/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaStream.class );

   private final WicaStreamId wicaStreamId;
   private final WicaStreamProperties wicaStreamProperties;
   private final Set<WicaChannel> wicaChannels;
 //  private final WicaChannelMetadataMapSerializer wicaChannelMetadataMapSerializer;
  // private final WicaChannelValueMapSerializer wicaChannelValueMapSerializer;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new stream with no special stream properties.
    *
    * @param wicaStreamId the stream's id.
    * @param wicaChannels the stream's channels.
    */
   public WicaStream( WicaStreamId wicaStreamId, Set<WicaChannel> wicaChannels )
   {
      this( wicaStreamId, new WicaStreamProperties(), wicaChannels );
   }

   public WicaStream( WicaStreamId wicaStreamId,
                      WicaStreamProperties wicaStreamProperties,
                      Set<WicaChannel> wicaChannels )
   {
      // Capture and Validate all parameters
      this.wicaStreamId = Validate.notNull( wicaStreamId );
      this.wicaStreamProperties = Validate.notNull( wicaStreamProperties );
      this.wicaChannels = Validate.notNull( wicaChannels );

      // Build an object that can return the required numeric precision for serializing wica stream floating point values.
//      final var wicaChannelValueNumericScaleSupplier = new WicaChannelDataNumericScaleSupplier( wicaStreamProperties, wicaChannels, STREAM_DEFAULT_NUMERIC_PRECISION );

      // Build an object that can return information the required data fields for serializing wica channel values.
//      final var wicaChannelValueFieldsOfInterestSupplier = new WicaChannelDataFieldsOfInterestSupplier( wicaStreamProperties, wicaChannels, STREAM_DEFAULT_FIELDS_OF_INTEREST);

      // Build an object that can serialize channel values to a JSON String.
//      this.wicaChannelValueMapSerializer = new WicaChannelValueMapSerializer( wicaChannelValueFieldsOfInterestSupplier, wicaChannelValueNumericScaleSupplier, STREAM_QUOTE_NUMERIC_STRINGS );

      // Build an object that can serialize channel metadata to a JSON String.
//      logger.info( "Fields selected for metadata serialization are '{}'. Numeric precision is '{}'. Numeric string quoting is: '{}'",
//          "ALL", STREAM_DEFAULT_NUMERIC_PRECISION, STREAM_QUOTE_NUMERIC_STRINGS );
//
//      this.wicaChannelMetadataMapSerializer = new WicaChannelMetadataMapSerializer( c -> Set.of(), c -> STREAM_DEFAULT_NUMERIC_PRECISION,
//           STREAM_QUOTE_NUMERIC_STRINGS );

      // Output some diagnostic information to the log.
      logger.info( "Created new WicaStream with properties as follows: '{}'", this );
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns the stream's id object.
    * @return the object.
    */
   public WicaStreamId getWicaStreamId()
   {
      return wicaStreamId;
   }

   /**
    * Returns the stream's properties object.
    * @return the object.
    */
   public WicaStreamProperties getWicaStreamProperties()
   {
      return wicaStreamProperties;
   }

   /**
    * Returns the stream's channels.
    * @return the object.
    */
   public Set<WicaChannel> getWicaChannels()
   {
      return wicaChannels;
   }


//   public WicaChannelMetadataMapSerializer getWicaChannelMetadataMapSerializer()
//   {
//      return wicaChannelMetadataMapSerializer;
//   }
//
//   public WicaChannelValueMapSerializer getWicaChannelValueMapSerializer()
//   {
//      return wicaChannelValueMapSerializer;
//   }

   /**
    * Returns the stream's string representation.
    *
    * @return the representation
    */
   @Override
   public String toString()
   {
      return "WicaStream{" +
             "wicaStreamId=" + wicaStreamId +
             ", wicaStreamProperties=" + getWicaStreamProperties() +
             ", wicaChannels=" + getWicaChannels() +
      "}'";
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
