/**
 * @module
 * @desc Provides support for rendering the text content of wica-aware elements in the current document.
 */
import * as DocumentUtilities from './document-utils.js'

/**
 * The default precision to be used when rendering a channel with a numeric value.
 * @type {number}
 */
const MAX_PRECISION = 8;

/**
 * Render all wica-aware html elements in the current document.
 */
export function renderWicaElements()
{
    DocumentUtilities.findWicaElements().forEach( (element) => {
        // If we have no information about the channel's current value or the channel's metadata
        // then there is nothing useful that can be done so bail out.
        if ( ( !element.hasAttribute("data-wica-channel-value-array")) || (! element.hasAttribute("data-wica-channel-metadata") ) )
        {
            return;
        }

        // Obtain the object containing the array of recently received channel values.
        const channelValueArrayObj = JSON.parse( element.getAttribute( "data-wica-channel-value-array" ) );

        // Check that the received object was an array
        if ( ! Array.isArray( channelValueArrayObj ) ) {
            console.warn( "Stream error: received value object was not an array !" );
            return;
        }

        // If there isn't at least one value present bail out as there is nothing to be done.
        if ( channelValueArrayObj.length === 0 ) {
            return;
        }

        // For widget rendering purposes we always use only the most recent value,
        // discarding the rest.
        const channelValueObj = channelValueArrayObj.pop();

        // If the current value is not available because the channel is off line then bail out
        if ( channelValueObj.val === null ) {
            return;
        }

        // Obtain the channel metadata object
        const channelMetadataObj = JSON.parse( element.getAttribute( "data-wica-channel-metadata" ) );

        // Now render the widget
        renderWidget_( element, channelValueObj, channelMetadataObj );
    });
}

/**
 * Renders the specified wica-aware html element using information from the the
 * underlying channel.
 *
 * The term 'render' here means manipulating the element in the DOM to enable
 * the Wica CSS styling rules to achieve the desired effect.
 *
 * @private
 * @param element the html element to render.
 * @param channelValueObj the value object associated with the element's underlying wica channel.
 * @param channelMetadataObj the metadata object associated with the element's underlying wica channel.
 */
function renderWidget_( element, channelValueObj, channelMetadataObj ) {
    let channelName = element.getAttribute( "data-wica-channel-name" );
    let renderingHintsString = element.hasAttribute("data-wica-rendering-hints" ) ? element.getAttribute("data-wica-rendering-hints") : "{}";
    let renderingHintsObj;
    try {
        renderingHintsObj = JSON.parse( renderingHintsString );
    }
    catch (err) {
        logExceptionData( channelName + ": Illegal JSON format in data-wica-rendering-hints attribute.\nDetails were as follows:\n", err );
        renderingHintsObj = {};
    }
    let formattedValueText = buildFormattedValueText_( channelValueObj, channelMetadataObj, renderingHintsObj);

    // Suppress manipulation of element text content if overridden by the rendering hint
    let disableRendering = renderingHintsObj.hasOwnProperty( "disable" ) ? renderingHintsObj.disable : false;

    if ( ! disableRendering ) {
        element.textContent = formattedValueText;
    }

    // If a tooltip is explictly defined on the element then use it otherwise add it
    // using the rules defined in the function
    if ( ! element.hasAttribute(  "data-wica-tooltip" ) ) {
        let tooltipText = buildFormattedTooltipText_( element, formattedValueText );
        element.setAttribute( "data-wica-tooltip", tooltipText );
    }
}

/**
 * Returns a String representing the current value using information extracted from the wica-channel.
 *
 * @private
 * @param channelValueObj the value object associated with the element's underlying wica channel.
 * @param channelMetadataObj the metadata object associated with the element's underlying wica channel.
 * @param renderingHintsObj an object containg various styling hints.
 *
 * @returns {*} the formatted string.
 *
 */
function buildFormattedValueText_( channelValueObj, channelMetadataObj, renderingHintsObj )
{
    // If the supplied value is non-scalar just return the string representation.
    if ( ( channelMetadataObj.type === "INTEGER_ARRAY") || ( channelMetadataObj.type === "REAL_ARRAY") || ( channelMetadataObj.type === "STRING_ARRAY") ) {
        return JSON.stringify( channelValueObj.val );
    }
    else {
        return formatScalarValue_( channelValueObj, channelMetadataObj, renderingHintsObj )
    }
}

/**
 *
 * @private
 * @param channelValueObj
 * @param channelMetadataObj
 * @param renderingHintsObj
 * @returns {*}
 * @private
 */
function formatScalarValue_( channelValueObj, channelMetadataObj, renderingHintsObj )
{
    let rawValue = channelValueObj.val;

    if ( channelMetadataObj.type === "REAL")
    {
        let exponential = renderingHintsObj.hasOwnProperty( "exp" ) ? renderingHintsObj.exp : null;
        let precision = renderingHintsObj.hasOwnProperty( "prec" ) ? renderingHintsObj.prec : channelMetadataObj.prec;
        let units = renderingHintsObj.hasOwnProperty( "units" ) ? renderingHintsObj.units: channelMetadataObj.egu;

        // TODO: look at more rigorous deserialisation of NaN's, Infinity etc
        if ( ( rawValue === "Infinity" ) || ( rawValue === "NaN") )  {
            return rawValue;
        }
        else if ( exponential === null ) {

            if ( precision > MAX_PRECISION )
            {
                console.warn( "Channel precision is out-of-range. Precision will be truncated to " + MAX_PRECISION );
                precision = MAX_PRECISION;
            }
            return rawValue.toFixed( precision ) + " " + units;
        }
        else {
            return rawValue.toExponential( exponential ) + " " + units;
        }
    }
    else if ( channelMetadataObj.type === "INTEGER")
    {
        // TODO: look at more rigorous deserialisation of NaN's, Infinity etc
        if ( rawValue === "Infinity" )  {
            return rawValue;
        }
        else {
            let units = renderingHintsObj.hasOwnProperty("units") ? renderingHintsObj.units : channelMetadataObj.egu;
            return rawValue + " " + units;
        }
    }
    else {
        return rawValue;
    }
}

/**
 *
 * @private
 * @param element
 * @param formattedValueText
 * @returns {string}
 * @private
 */

function buildFormattedTooltipText_( element, formattedValueText )
{
    const SUPPORT_OPTION_TOOLTIPS_SHOW_CHANNEL_NAME_ONLY = true;
    const MAX_TOOLTIP_VALUE_STRING_LENGTH = 64;

    let channelName = element.getAttribute( "data-wica-channel-name" );

    if ( SUPPORT_OPTION_TOOLTIPS_SHOW_CHANNEL_NAME_ONLY ) {
        return channelName;
    }

    let streamConnectState = element.getAttribute("data-wica-stream-state");
    let streamConnected = streamConnectState.startsWith( "opened" );

    if ( !streamConnected ) {
        return "Channel Name: " + channelName + "\n" +
            "Stream Connect State: " + streamConnectState;
    }

    let channelConnectState = element.getAttribute("data-wica-channel-connection-state");
    let channelConnected = channelConnectState.startsWith( "connected" );

    if ( !channelConnected ) {
        return "Channel Name: " + channelName + "\n" +
            "Stream Connect State: " + streamConnectState + "\n" +
            "Channel Connect State: " + channelConnectState;
    }
    let alarmState = element.getAttribute("data-wica-channel-alarm-state");

    let truncatedValueString = truncateValueString_( formattedValueText, MAX_TOOLTIP_VALUE_STRING_LENGTH );

    return "Channel Name: " + channelName + "'\n" +
        "Stream Connect State: '" + streamConnectState + "'\n" +
        "Channel Connect State: '" + channelConnectState + "'\n" +
        "Channel Alarm State: '" + alarmState + "'\n" +
        "Channel Value Text: '" + truncatedValueString + "'";
}

function truncateValueString_( inputString, maxLength )
{
    return ( inputString.length <= maxLength ) ? inputString : inputString.substring( 0, maxLength - 1) + "...";
}