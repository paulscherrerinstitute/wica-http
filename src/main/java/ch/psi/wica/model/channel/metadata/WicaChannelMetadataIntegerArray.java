/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.metadata;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import org.apache.commons.lang3.Validate;

/**
 * Represents the metadata for a channel whose type is INTEGER_ARRAY.
 */
public class WicaChannelMetadataIntegerArray extends WicaChannelMetadata
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

    private final String units;
    private final int upperDisplay;
    private final int lowerDisplay;
    private final int upperControl;
    private final int lowerControl;
    private final int upperAlarm;
    private final int lowerAlarm;
    private final int upperWarning;
    private final int lowerWarning;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

    /**
     * Constructs a new instance of the metadata for a channel whose type is INTEGER.
     *
     * @param units the units associated with the channel.
     * @param upperDisplay the upper display limit of the channel.
     * @param lowerDisplay the lower display limit of the channel.
     * @param upperControl the upper control limit of the channel.
     * @param lowerControl the lower control limit of the channel.
     * @param upperAlarm the upper alarm limit of the channel.
     * @param lowerAlarm the lower alarm limit of the channel.
     * @param upperWarning the upper warning limit of the channel.
     * @param lowerWarning the lower warning limit of the channel.
     */
    public WicaChannelMetadataIntegerArray( String units,
                                            int upperDisplay, int lowerDisplay,
                                            int upperControl, int lowerControl,
                                            int upperAlarm, int lowerAlarm,
                                            int upperWarning, int lowerWarning )
    {
        super( WicaChannelType.INTEGER_ARRAY );
        this.units = Validate.notNull( units, "The 'units' argument was null." );
        this.upperDisplay = upperDisplay;
        this.lowerDisplay = lowerDisplay;
        this.upperControl = upperControl;
        this.lowerControl = lowerControl;
        this.upperAlarm = upperAlarm;
        this.lowerAlarm = lowerAlarm;
        this.upperWarning = upperWarning;
        this.lowerWarning = lowerWarning;
    }


/*- Public methods -----------------------------------------------------------*/

    /**
     * Returns the Engineering Units.
     *
     * @return the value.
     */
    public String getUnits()
    {
        return units;
    }

    /**
     * Returns the High Operating Range.
     *
     * @return the value.
     */
    public int getUpperDisplay()
    {
        return upperDisplay;
    }

    /**
     * Returns the Low Operating Range.
     *
     * @return the value.
     */
    public int getLowerDisplay()
    {
        return lowerDisplay;
    }

    /**
     * Returns the Drive High Control Limit.
     *
     * @return the value.
     */
    public int getUpperControl()
    {
        return upperControl;
    }

    /**
     * Returns the Drive Low Control Limit.
     *
     * @return the value.
     */
    public int getLowerControl()
    {
        return lowerControl;
    }

    /**
     * Returns the Upper Alarm limit.
     *
     * @return the value.
     */
    public int getUpperAlarm()
    {
        return upperAlarm;
    }

    /**
     * Returns the Lower Alarm Limit.
     *
     * @return the value.
     */
    public int getLowerAlarm()
    {
        return lowerAlarm;
    }

    /**
     * Returns the Upper Warning Limit.
     *
     * @return the value.
     */
    public int getUpperWarning()
    {
        return upperWarning;
    }

    /**
     * Returns the Lower Warning Limit.
     *
     * @return the value.
     */
    public int getLowerWarning()
    {
        return lowerWarning;
    }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
