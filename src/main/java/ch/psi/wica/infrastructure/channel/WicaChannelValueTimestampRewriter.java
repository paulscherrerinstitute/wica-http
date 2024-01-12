/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.value.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
public class WicaChannelValueTimestampRewriter
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public WicaChannelValue rewrite( WicaChannelValue valueObj, LocalDateTime newTimeStamp )
   {
      final WicaChannelValueConnected connectedValue;
      if ( valueObj.isConnected() )
      {
         connectedValue = ((WicaChannelValueConnected) valueObj);
      }
      else
      {
         return WicaChannelValueBuilder.createChannelValueDisconnected();
      }

      final WicaChannelAlarmStatus alarmStatus = connectedValue.getWicaChannelAlarmStatus();
      final WicaChannelAlarmSeverity alarmSeverity = connectedValue.getWicaAlarmSeverity();

       return switch ( connectedValue.getWicaChannelType( ) )
       {
           case REAL -> {
               final double dblValue = ( (WicaChannelValueConnectedReal) connectedValue ).getValue( );
               yield WicaChannelValueBuilder.createChannelValueConnectedReal( alarmSeverity, alarmStatus, newTimeStamp, dblValue );
           }
           case REAL_ARRAY -> {
               final double[] dblArrayValue = ( (WicaChannelValueConnectedRealArray) connectedValue ).getValue( );
               yield WicaChannelValueBuilder.createChannelValueConnectedRealArray( alarmSeverity, alarmStatus, newTimeStamp, dblArrayValue );
           }
           case INTEGER -> {
               final int intValue = ( (WicaChannelValueConnectedInteger) connectedValue ).getValue( );
               yield WicaChannelValueBuilder.createChannelValueConnectedInteger( alarmSeverity, alarmStatus, newTimeStamp, intValue );
           }
           case INTEGER_ARRAY -> {
               final int[] intArrayValue = ( (WicaChannelValueConnectedIntegerArray) connectedValue ).getValue( );
               yield WicaChannelValueBuilder.createChannelValueConnectedIntegerArray( alarmSeverity, alarmStatus, newTimeStamp, intArrayValue );
           }
           case STRING -> {
               final String strValue = ( (WicaChannelValueConnectedString) connectedValue ).getValue( );
               yield WicaChannelValueBuilder.createChannelValueConnectedString( alarmSeverity, alarmStatus, newTimeStamp, strValue );
           }
           case STRING_ARRAY -> {
               final String[] strArrayValue = ( (WicaChannelValueConnectedStringArray) connectedValue ).getValue( );
               yield WicaChannelValueBuilder.createChannelValueConnectedStringArray( alarmSeverity, alarmStatus, newTimeStamp, strArrayValue );
           }
           default -> throw new IllegalArgumentException( "The supplied object was of an unexpected type " );
       };
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
