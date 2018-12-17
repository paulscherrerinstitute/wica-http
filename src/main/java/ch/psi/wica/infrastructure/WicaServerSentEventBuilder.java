/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaStreamId;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.codec.ServerSentEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public enum WicaServerSentEventBuilder
{
   EV_WICA_SERVER_HEARTBEAT      ( "ev-wica-server-heartbeat", "server heartbeat"      ),
   EV_WICA_CHANNEL_METADATA      ( "ev-wica-channel-metadata", "channel metadata"      ),
   EV_WICA_CHANNEL_VALUE_ALLDATA ( "ev-wica-channel-value",    "channel values"        ),
   EV_WICA_CHANNEL_VALUE_CHANGES ( "ev-wica-channel-value",    "channel value changes" );


/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   /**
    * The format which will be used when making String representations
    * of the times/dates in this class as used in the SSE comment field.
    */
   private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
   private final String event;
   private final String comment;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaServerSentEventBuilder( String event, String comment )
   {
      this.event = Validate.notBlank( event );
      this.comment = Validate.notBlank( comment );
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Returns a Wica ServerSent Event customised with the supplied WicaStream id
    * and String data payload.
    *
    * @param id the WicaStreamId
    * @param dataString the String data
    * @return the generated SSE.
    */
   public ServerSentEvent<String> build( WicaStreamId id, String dataString )
   {
      Validate.notNull( id );
      Validate.notNull( dataString,"The valueMap field was null ");

      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( DATETIME_FORMAT_PATTERN );
      final String formattedTimeAndDateNow =  LocalDateTime.now().format(formatter );
      return ServerSentEvent.builder( dataString )
            .id( id.asString() )
            .comment( formattedTimeAndDateNow + " - " + this.comment )
            .event( this.event )
            .build();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
