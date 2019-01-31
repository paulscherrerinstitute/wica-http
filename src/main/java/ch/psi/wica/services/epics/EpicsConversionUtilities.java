/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelAlarmSeverity;
import ch.psi.wica.model.WicaChannelAlarmStatus;
import ch.psi.wica.model.WicaChannelName;
import org.epics.ca.data.AlarmSeverity;
import org.epics.ca.data.AlarmStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class EpicsConversionUtilities
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   // TODO this is a kludge which allows a channel to be used multiple times
   // in the same stream. It breaks the Principle of Least Surprise and
   // should be refactored into something less intuitive.
   private static final String EPICS_END_OF_STRING_MARKER = "##";

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/

   static String getEpicsChannelName( WicaChannelName wicaChannelName )
   {
      final String[] tokens = wicaChannelName.toString().split( EPICS_END_OF_STRING_MARKER, 2 );
      return tokens[0];
   }

   static WicaChannelAlarmSeverity fromEpics( AlarmSeverity caAlarmSeverity )
   {
      return WicaChannelAlarmSeverity.valueOf( caAlarmSeverity.toString() );
   }


   static WicaChannelAlarmStatus fromEpics( AlarmStatus caAlarmStatus )
   {
      return WicaChannelAlarmStatus.of( caAlarmStatus.ordinal() );
   }

   // TODO: this ties the current location to PSI's site. Should be made configurable.
   static LocalDateTime getEpicsTimestamp( long secondsPastEpicsEpoch, int nanoseconds )
   {
      final Instant instant = Instant.ofEpochSecond(secondsPastEpicsEpoch, nanoseconds );
      final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("Europe/Zurich") );
      return zonedDateTime.toLocalDateTime();
   }



/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
