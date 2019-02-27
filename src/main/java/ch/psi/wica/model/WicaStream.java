/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@ThreadSafe
public class WicaStream
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final LocalDateTime LONG_AGO = LocalDateTime.of( 1961,8,25,0,0 );
   private LocalDateTime lastPublicationTime = LONG_AGO;

   private final WicaStreamId wicaStreamId;
   private final WicaStreamProperties wicaStreamProperties;
   private final Set<WicaChannel> wicaChannels;

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

      // Output some diagnostic information to the log.
      final Logger logger = LoggerFactory.getLogger(WicaStream.class);
      logger.info("Created new WicaStream with properties as follows: '{}'", this );
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

   /**
    * Returns the timestamp for the last time the values from the
    * stream were published.
    *
    * @return the timestamp.
    */
   public LocalDateTime getLastPublicationTime()
   {
      return lastPublicationTime;
   }

   /**
    * Sets with the current time the timestamp which indicates the last time
    * the stream was published.
    */
   public void updateLastPublicationTime()
   {
      lastPublicationTime = LocalDateTime.now();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
