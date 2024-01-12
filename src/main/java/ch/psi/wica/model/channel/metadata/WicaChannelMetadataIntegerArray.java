/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the metadata for a channel whose type is INTEGER_ARRAY.
 */
public class WicaChannelMetadataIntegerArray extends WicaChannelMetadataInteger
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaChannelMetadataIntegerArray( String units,
                                    int upperDisplay, int lowerDisplay,
                                    int upperControl, int lowerControl,
                                    int upperAlarm, int lowerAlarm,
                                    int upperWarning, int lowerWarning )
   {
       super( WicaChannelType.INTEGER_ARRAY, units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
