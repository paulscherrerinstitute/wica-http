/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelAlarmSeverity;
import ch.psi.wica.model.channel.WicaChannelAlarmStatus;
import ch.psi.wica.model.channel.WicaChannelValue;
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
      final WicaChannelValue.WicaChannelValueConnected connectedValue;
      if ( valueObj.isConnected() )
      {
         connectedValue = ((WicaChannelValue.WicaChannelValueConnected) valueObj);
      }
      else
      {
         return WicaChannelValue.createChannelValueDisconnected();
      }

      final WicaChannelAlarmStatus alarmStatus = connectedValue.getWicaChannelAlarmStatus();
      final WicaChannelAlarmSeverity alarmSeverity = connectedValue.getWicaAlarmSeverity();

       return switch ( connectedValue.getWicaChannelType( ) )
       {
           case REAL -> {
               final double dblValue = ( (WicaChannelValue.WicaChannelValueConnectedReal) connectedValue ).getValue( );
               yield WicaChannelValue.createChannelValueConnectedReal( alarmSeverity, alarmStatus, newTimeStamp, dblValue );
           }
           case REAL_ARRAY -> {
               final double[] dblArrayValue = ( (WicaChannelValue.WicaChannelValueConnectedRealArray) connectedValue ).getValue( );
               yield WicaChannelValue.createChannelValueConnectedRealArray( alarmSeverity, alarmStatus, newTimeStamp, dblArrayValue );
           }
           case INTEGER -> {
               final int intValue = ( (WicaChannelValue.WicaChannelValueConnectedInteger) connectedValue ).getValue( );
               yield WicaChannelValue.createChannelValueConnectedInteger( alarmSeverity, alarmStatus, newTimeStamp, intValue );
           }
           case INTEGER_ARRAY -> {
               final int[] intArrayValue = ( (WicaChannelValue.WicaChannelValueConnectedIntegerArray) connectedValue ).getValue( );
               yield WicaChannelValue.createChannelValueConnectedIntegerArray( alarmSeverity, alarmStatus, newTimeStamp, intArrayValue );
           }
           case STRING -> {
               final String strValue = ( (WicaChannelValue.WicaChannelValueConnectedString) connectedValue ).getValue( );
               yield WicaChannelValue.createChannelValueConnectedString( alarmSeverity, alarmStatus, newTimeStamp, strValue );
           }
           case STRING_ARRAY -> {
               final String[] strArrayValue = ( (WicaChannelValue.WicaChannelValueConnectedStringArray) connectedValue ).getValue( );
               yield WicaChannelValue.createChannelValueConnectedStringArray( alarmSeverity, alarmStatus, newTimeStamp, strArrayValue );
           }
           default -> throw new IllegalArgumentException( "The supplied object was of an unexpected type " );
       };
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
