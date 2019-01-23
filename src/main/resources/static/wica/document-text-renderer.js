/**
 * Provides support for rendering the textual content of wica-aware elements in the current document.
 * @module
 */
import * as DocumentUtilities from './document-utils.js'


/**
 * The default precision to be used when rendering a channel with a numeric value.
 * @type {number}
 */
const MAX_PRECISION = 8;

/**
 * Renders the visual state of wica-aware elements in the current document based on attribute information
 * obtained from the Wica server on the backend.
 */
export class DocumentTextRenderer
{
    /**
     * Constructs a new instance.
     *
     * @param {!WicaElementConnectionAttributes} wicaElementConnectionAttributes - The names of the wica-aware
     *     element attributes that are to be used in the communication process.
     *     See {@link module:shared-definitions.WicaElementConnectionAttributes WicaElementConnectionAttributes}.
     *
     * @param {!WicaElementRenderingAttributes} wicaElementRenderingAttributes - The names of the wica-aware
     *     element attributes that are to be used in the communication process.
     *     See {@link module:shared-definitions.WicaElementConnectionAttributes WicaElementConnectionAttributes}.
     *
     */
    constructor( wicaElementConnectionAttributes, wicaElementRenderingAttributes,  )
    {
        this.wicaElementConnectionAttributes= wicaElementConnectionAttributes;
        this.wicaElementRenderingAttributes = wicaElementRenderingAttributes;

    }

    /**
     *
     */
    activate()
    {
        try
        {
            this.renderWicaElements_( this.wicaElementConnectionAttributes.channelName,
                                      this.wicaElementConnectionAttributes.channelMetadata,
                                      this.wicaElementConnectionAttributes.channelValueArray,
                                      this.wicaElementRenderingAttributes.rendererTooltips,
                                      this.wicaElementRenderingAttributes.rendererProperties );
        }
        catch( err )
        {
            DocumentTextRenderer.logExceptionData_( "Programming Error: renderWicaElements_ threw an exception: ", err );
        }

        // Allow at least 100ms after each rendering cycle
        setTimeout( this.activate, 100 );
    }

    /**
     * Renders all wica-aware html elements in the current document.
     *
     * @private
     * @param {string} channelNameAttribute - The name of the attribute which holds the channel name.
     * @param {string} channelMetadataAttribute - The name of the attribute which holds the channel metadata.
     * @param {string} channelValueArrayAttribute - The name of the attribute which holds channel value array
     * @param {string} rendererTooltipsAttribute - The name of the attribute which holds the renderer's tooltips
     * @param {string} rendererPropertiesAttribute - The name of the attribute which holds the renderer's properties.
     */
    renderWicaElements_( channelNameAttribute, channelMetadataAttribute, channelValueArrayAttribute, rendererTooltipsAttribute, rendererPropertiesAttribute )
    {
        DocumentUtilities.findWicaElements().forEach((element) =>
        {
            // Always ensure the element's tooltips are available for rendering.
            DocumentTextRenderer.configureWicaElementToolTips_( element, rendererTooltipsAttribute, channelNameAttribute );

            // Get the element's renderer properties object if available
            // Note: since this attribute is configured by the user as a JSON string it's important
            // to validate the data and to output some diagnostic message if there is a problem.
            const rendererProperties = DocumentTextRenderer.getRendererProperties( element, rendererPropertiesAttribute );

            // Bail out if rendering is disabled for this widget
            const disableRendering = rendererProperties.hasOwnProperty("disable") ? rendererProperties.disable : false;
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
            const channelValueArray = JSON.parse( element.getAttribute( channelValueArrayAttribute ) );

            // Bail out if the value obtained from the stream was not an array
            if ( ! Array.isArray( channelValueArray ) )
            {
                console.warn("Stream error: received value object that was not an array !");
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
            const channelMetadata = JSON.parse( element.getAttribute( channelMetadataAttribute ) );

            // Now render the widget's text content
            DocumentTextRenderer.renderWicaElementTextContent_( element, channelMetadata, channelValueLatest, rendererProperties );
        });
    }

    /**
     * Renders the element's textual content.
     *
     * @param {Element} element - The element.
     * @param {WicaChannelMetadata} channelMetadata - the channel's metadata.
     * @param {WicaChannelValue} channelValueLatest - the channel's latest value.
     * @param {WicaRendererProperties} rendererProperties - the channel's rendering properties.
     */
    static renderWicaElementTextContent_( element, channelMetadata, channelValueLatest, rendererProperties )
    {
        const rawValue = channelValueLatest.val;
        const units = rendererProperties.hasOwnProperty("units") ? rendererProperties.units :
                      channelMetadata.hasOwnProperty( "egu") ? channelMetadata.egu : "";

        switch ( channelMetadata.type )
        {
            case "REAL_ARRAY":
            case "INTEGER_ARRAY":
            case "STRING_ARRAY":
                element.textContent = JSON.stringify( rawValue );
                break;

            case "REAL":
                const useExponentialFormat = rendererProperties.hasOwnProperty("exp" ) ? rendererProperties.exp : false;
                const precision = Math.max( rendererProperties.hasOwnProperty("prec") ? rendererProperties.prec : channelMetadata.prec, MAX_PRECISION );

                // TODO: look at more rigorous deserialisation of NaN's, Infinity etc
                if ( (rawValue === "Infinity") || (rawValue === "NaN"))
                {
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
                // TODO: look at more rigorous deserialisation of NaN's, Infinity etc
                if ( rawValue === "Infinity" )
                {
                    element.textContent = rawValue;
                }
                else
                {
                    const units = rendererProperties.hasOwnProperty("units" ) ? rendererProperties.units : channelMetadata.egu;
                    element.textContent =  rawValue + " " + units;
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
     * Configure the element's tooltips attribute.
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
     * @param {string} rendererTooltipAttribute - The name of the attribute which contains the tooltip.
     * @param {string} channelNameAttribute - The name of the attribute which contains the channel name.
     * @private
     */
    static configureWicaElementToolTips_( element, rendererTooltipAttribute, channelNameAttribute )
    {
        if ( ! element.hasAttribute( rendererTooltipAttribute ) )
        {
            const channelName = element.getAttribute( channelNameAttribute );
            element.setAttribute( rendererTooltipAttribute, channelName );
        }
    }

    /**
     * Attempts to return a JS WicaRendererProperties object using the JSON string that may optionally
     * be present in the element's renderer properties attribute.
     *
     * @private
     * @param {Element} element - The element.
     * @param {string} rendererPropertiesAttribute - The name of the element's HTML attribute which
     *      contains the renderer properties.
     * @return {WicaRendererProperties} - the object, or {} if for any reason it cannot be obtained
     *     from the element's HTML attribute.
     */
    static getRendererProperties( element, rendererPropertiesAttribute )
    {
        const rendererPropertiesString = element.hasAttribute( rendererPropertiesAttribute ) ? element.getAttribute( rendererPropertiesAttribute ) : "{}";
        try
        {
            return JSON.parse( rendererPropertiesString );
        }
        catch( err )
        {
            DocumentTextRenderer.logExceptionData_( channelName + ": Illegal JSON format in '" + rendererPropertiesAttribute + "' attribute.\nDetails were as follows:\n", err);
            return {};
        }
    }


    /**
     *
     * @private
     *
     * @param msg
     * @param err
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
        vDebug += "toString(): " + " value: [" + err.toString() + "]";
        console.warn( msg + vDebug );
    }

}