/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the metadata for a channel whose type is REAL_ARRAY.
 */
public class WicaChannelMetadataRealArray extends WicaChannelMetadataReal
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

    /**
     * Constructs a new instance of the metadata for a channel whose type is REAL_ARRAY.
     *
     * @param units
     * @param precision
     * @param upperDisplay
     * @param lowerDisplay
     * @param upperControl
     * @param lowerControl
     * @param upperAlarm
     * @param lowerAlarm
     * @param upperWarning
     * @param lowerWarning
     */
    WicaChannelMetadataRealArray( String units,
                                  int precision,
                                  double upperDisplay, double lowerDisplay,
                                  double upperControl, double lowerControl,
                                  double upperAlarm, double lowerAlarm,
                                  double upperWarning, double lowerWarning )
   {
       super( WicaChannelType.REAL_ARRAY, units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
