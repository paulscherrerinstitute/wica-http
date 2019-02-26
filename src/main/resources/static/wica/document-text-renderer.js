/**
 * Provides support for rendering the textual content of wica-aware elements in the current document.
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import * as DocumentUtilities from './document-utils.js'
import * as JsonUtilities from './json5-wrapper.js'

export { DocumentTextRenderer}


/*- Script Execution Starts Here ---------------------------------------------*/

log.log( "Executing script in document-text-renderer.js module...");

/**
 * The default precision to be used when rendering a wica-aware widget's text content with a numeric value.
 */
const DEFAULT_PRECISION = 8;

/**
 * Renders the visual state of wica-aware elements in the current document based on attribute information
 * obtained from the Wica server on the backend.
 */
class DocumentTextRenderer
{
    /**
     * Constructs a new instance.
     *
     * @param {!WicaElementConnectionAttributes} wicaElementConnectionAttributes - The names of the wica-aware
     *     element attributes that are to be used in the communication process.
     *     See {@link module:shared-definitions.WicaElementConnectionAttributes WicaElementConnectionAttributes}.
     *
     * @param {!WicaElementRenderingAttributes} wicaElementRenderingAttributes - The names of the wica-aware
     *     element attributes that are to be used in the rendering process.
     *     See {@link module:shared-definitions.WicaElementRenderingAttributes WicaElementRenderingAttributes}.
     */
    constructor( wicaElementConnectionAttributes, wicaElementRenderingAttributes,  )
    {
        this.wicaElementConnectionAttributes= wicaElementConnectionAttributes;
        this.wicaElementRenderingAttributes = wicaElementRenderingAttributes;
    }

    /**
     * Starts periodically scanning the current document and updating the text content of all wica-aware
     * elements to match the information obtained from the wica server.
     *
     * @param {number} [refreshRateInMilliseconds=100] - The period to wait after each update scan before
     *     starting the next one.
     *
     * See also: {@link module:document-text-renderer.DocumentTextRenderer#shutdown shutdown}.
     */
    activate( refreshRateInMilliseconds = 100 )
    {
        // Start update process if not already active. Otherwise do nothing.
        if ( this.intervalTimer === undefined )
        {
            JsonUtilities.load( () => this.doScan_( refreshRateInMilliseconds ) );
        }
    }

    /**
     * Shuts down the service offered by this class.
     *
     * See also: {@link module:document-text-renderer.DocumentTextRenderer#activate activate}.
     */
    shutdown()
    {
        // Stop update process if already active. otherwise do nothing.
        if ( this.intervalTimer !== undefined )
        {
            clearInterval( this.intervalTimer );
            this.intervalTimer = undefined;
        }
    }


    /**
     * Performs a single update cycle, then schedules the next one.
     *
     * @private
     * @param {number} refreshRateInMilliseconds - The period to wait after every update scan before starting
     *     the next one.
     *

     */
    doScan_( refreshRateInMilliseconds )
    {
        try
        {
            this.renderWicaElements_( this.wicaElementConnectionAttributes.channelName,
                                      this.wicaElementConnectionAttributes.channelMetadata,
                                      this.wicaElementConnectionAttributes.channelValueArray,
                                      this.wicaElementRenderingAttributes.tooltip,
                                      this.wicaElementRenderingAttributes.renderingProperties );
        }
        catch( err )
        {
            DocumentTextRenderer.logExceptionData_("Programming Error: renderWicaElements_ threw an exception: ", err );
        }

        // Reschedule next update
        this.intervalTimer = setTimeout(() => this.doScan_( refreshRateInMilliseconds ), refreshRateInMilliseconds );
    }


    /**
     * Renders all wica-aware html elements in the current document.
     *
     * @private
     * @param {string} channelNameAttribute - The name of the attribute which holds the channel name.
     * @param {string} channelMetadataAttribute - The name of the attribute which holds the channel metadata.
     * @param {string} channelValueArrayAttribute - The name of the attribute which holds channel value array.
     * @param {string} tooltipAttribute - The name of the attribute which holds the tooltip.
     * @param {string} renderingPropertiesAttribute - The name of the attribute which holds the properties
     *     needed for rendering.
     */
    renderWicaElements_( channelNameAttribute, channelMetadataAttribute, channelValueArrayAttribute, tooltipAttribute, renderingPropertiesAttribute )
    {
        DocumentUtilities.findWicaElements().forEach((element) =>
        {
            // Always ensure the element's tooltips are available for rendering.
            DocumentTextRenderer.configureWicaElementToolTip_( element, tooltipAttribute, channelNameAttribute );

            // Get the element's rendering properties object if available
            // Note: since this attribute is configured by the user as a JSON string it's important
            // to validate the data and to output some diagnostic message if there is a problem.
            const renderingProperties = DocumentTextRenderer.getRenderingProperties( element, renderingPropertiesAttribute );

            // Bail out if rendering is disabled for this widget
            const disableRendering = renderingProperties.hasOwnProperty("disable") ? renderingProperties.disable : false;
            if ( disableRendering )
            {
                return;
            }

            // Bail out if the channel's metadata and current value are not both available
            if ( ( ! element.hasAttribute( channelMetadataAttribute ) ) || ( ! element.hasAttribute( channelValueArrayAttribute ) ) )
            {
                return;
            }

            // Get the channel value object
            const channelValueArray = JsonUtilities.parse( element.getAttribute( channelValueArrayAttribute ) );

            // Bail out if the value obtained from the stream was not an array
            if ( ! Array.isArray( channelValueArray ) )
            {
                log.warn("Stream error: received value object that was not an array !");
                return;
            }

            // Bail out if there isn't at least one value present.
            if ( channelValueArray.length === 0 )
            {
                return;
            }

            // Bail out if the latest value indicates that the channel is offline.
            const channelValueLatest = channelValueArray.pop();
            if ( channelValueLatest.val === null )
            {
                return;
            }

            // Get the channel metadata object
            const channelMetadata = JsonUtilities.parse( element.getAttribute( channelMetadataAttribute ) );

            // Now render the widget's text content
            DocumentTextRenderer.renderWicaElementTextContent_( element, channelMetadata, channelValueLatest, renderingProperties );
        });
    }

    /**
     * Renders the element's textual content.
     *
     * @private
     * @param {Element} element - The element.
     * @param {WicaChannelMetadata} channelMetadata - the channel's metadata.
     * @param {WicaChannelValue} channelValueLatest - the channel's latest value.
     * @param {WicaRenderingProperties} renderingProperties - the channel's rendering properties.
     */
    static renderWicaElementTextContent_( element, channelMetadata, channelValueLatest, renderingProperties )
    {
        const rawValue = channelValueLatest.val;

        // The renderer assigns units either from either the rendering properties "units" field if
        // available or from the metadata "egu" field if available. Otherwise it assigns blank.
        const units = renderingProperties.hasOwnProperty("units") ? renderingProperties.units :
                      channelMetadata.hasOwnProperty( "egu") ? channelMetadata.egu : "";

        switch ( channelMetadata.type )
        {
            case "REAL_ARRAY":
            case "INTEGER_ARRAY":
            case "STRING_ARRAY":
                element.textContent = JsonUtilities.stringify( rawValue );
                break;

            case "REAL":
                const useExponentialFormat = renderingProperties.hasOwnProperty("exp" ) ? renderingProperties.exp : false;
                const precision = Math.min( renderingProperties.hasOwnProperty("prec") ? renderingProperties.prec : channelMetadata.prec, DEFAULT_PRECISION );
                // TODO: Look at improved deserialisation of NaN's, Infinity etc
                // TODO: The backend serialiser has been changed (2019-02-02) to the more rigorous implementation of
                // TODO: sending Nan and Infinity as numbers not strings. Need to check whether the implementation
                // TODO: here still works.
                if ( (rawValue === "Infinity") || (rawValue === "NaN"))
                {
                    // This was required in earlier versions of the backend server where Infinity
                    // and Nan was sent as a JSON string. Since 2019-02-02 should no longer be required.
                    logger.warn( "Programming error: unexpected JSON String format for numeric value of NaN or Infinity." );
                    element.textContent = rawValue;
                }
                else if ( useExponentialFormat )
                {
                    element.textContent =  rawValue.toExponential( useExponentialFormat ) + " " + units;
                }
                else
                {
                    element.textContent =  rawValue.toFixed( precision ) + " " + units;
                }
                break;

            case "INTEGER":
                // TODO: Look at improved deserialisation of NaN's, Infinity etc
                // TODO: The backend serialiser has been changed (2019-02-02) to the more rigorous implementation of
                // TODO: sending Nan and Infinity as numbers not strings. Need to check whether the implementation
                // TODO: here still works.
                if ( rawValue === "Infinity" )
                {
                    // This was required in earlier versions of the backend server where Infinity
                    // and Nan was sent as a JSON string. Since 2019-02-02 should no longer be required.
                    logger.warn( "Programming error: unexpected JSON String format for numeric value of NaN or Infinity." );
                    element.textContent = rawValue;
                }
                else
                {
                    element.textContent = rawValue + " " + units;
                }
                break;

            case "STRING":
                element.textContent = rawValue;
                break;

            default:
                element.textContent = rawValue;
                break;
        }

    }

    /**
     * Configure the element's tooltip attribute.
     *
     * @implNote
     *
     * The wica CSS rules ensure that when the browser's cursor hovers over the element of interest a
     * a window will be automatically popped up to display the contents of the string specified by the
     * element's tooltip attribute.
     *
     * The implementation here does nothing if the tooltip attribute has already been set explicitly in
     * the HTML document. If the attribute has not been set then the first time this method is invoked
     * it will set the attribute to the name of the channel.
     *
     * @param {Element} element - The element.
     * @param {string} tooltipAttribute - The name of the attribute which contains the tooltip.
     * @param {string} channelNameAttribute - The name of the attribute which contains the channel name.
     * @private
     */
    static configureWicaElementToolTip_( element, tooltipAttribute, channelNameAttribute )
    {
        if ( ! element.hasAttribute( tooltipAttribute ) )
        {
            const channelName = element.getAttribute( channelNameAttribute );
            element.setAttribute( tooltipAttribute, channelName );
        }
    }

    /**
     * Attempts to return a JS WicaRenderingProperties object using the JSON string that may optionally
     * be present in the element's rendering properties attribute.
     *
     * @private
     * @param {Element} element - The element.
     * @param {string} renderingPropertiesAttribute - The name of the element's HTML attribute which
     *      contains the rendering properties.
     * @return {WicaRenderingProperties} - the object, or {} if for any reason it cannot be obtained
     *     from the element's HTML attribute.
     */
    static getRenderingProperties( element, renderingPropertiesAttribute )
    {
        const renderingPropertiesString = element.hasAttribute( renderingPropertiesAttribute ) ? element.getAttribute( renderingPropertiesAttribute ) : "{}";
        try
        {
            return JsonUtilities.parse( renderingPropertiesString );
        }
        catch( err )
        {
            DocumentTextRenderer.logExceptionData_( channelName + ": Illegal JSON format in '" + renderingPropertiesAttribute + "' attribute.\nDetails were as follows:\n", err);
            return {};
        }
    }

    /**
     * Log any error data generated in this class.
     *
     * @private
     * @param {string} msg - custom error message.
     * @param {Error} err - the Error object
     */
    static logExceptionData_( msg, err )
    {
        let vDebug = "";
        for ( const prop in err )
        {
            if ( err.hasOwnProperty( prop ) )
            {
                vDebug += "property: " + prop + " value: [" + err[ prop ] + "]\n";
            }
        }
        vDebug += "Details: [" + err.toString() + "]";
        log.warn( msg + vDebug );
    }

}