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
            this.renderWicaElements_();
        }
        catch( err )
        {
            logExceptionData( "Programming Error: renderWicaElements_ threw an exception: ", err );
        }

        // Allow at least 100ms after each rendering cycle
        setTimeout( this.activate, 100 );
    }

    /**
     * Renders all wica-aware html elements in the current document.
     *
     * @private
     * @param {string} channelMetadataAttribute - The attribute which holds channel metadata information.
     * @param {string} channelValueArrayAttribute - The attribute which holds channel value information.
     */
    renderWicaElements_( channelMetadataAttribute, channelValueArrayAttribute )
    {
        DocumentUtilities.findWicaElements().forEach((element) => {
            // If we have no information about the channel's current value or the channel's metadata
            // then there is nothing useful that can be done so bail out.
            if ( ( !element.hasAttribute( channelMetadataAttribute ) ) || ( !element.hasAttribute(channelValueArrayAttribute ) ) ) {
                return;
            }

            // Obtain the object containing the array of recently received channel values.
            const channelValueArrayObj = JSON.parse( element.getAttribute( channelValueArrayAttribute ) );

            // Check that the received object was an array
            if ( !Array.isArray(channelValueArrayObj) ) {
                console.warn("Stream error: received value object was not an array !");
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
            if ( channelValueObj.val === null )
            {
                return;
            }

            // Obtain the channel metadata object
            const channelMetadataObj = JSON.parse(element.getAttribute( channelMetadataAttribute ) );

            // Now render the widget
            this.renderWidget_(element, channelValueObj, channelMetadataObj);
        });
    }

    /**
     * Renders the specified wica-aware html element using information from the the
     * underlying channel.
     *
     * The term 'render' here means manipulating the element in the DOM to enable
     * the browser engine to achieve the desired effect.
     *
     * @private
     * @param element the html element to render.
     * @param channelValueObj the value object associated with the element's underlying wica channel.
     * @param channelMetadataObj the metadata object associated with the element's underlying wica channel.
     * @param {string} channelNameAttribute - The attribute which holds channel name information.
     * @param {string} rendererPropertiesAttribute - The attribute which holds channel value information.

     */
    renderTextContent_( element, channelValueObj, channelMetadataObj, channelNameAttribute, rendererPropertiesAttribute )
    {
        const channelName = element.getAttribute( channelNameAttribute );
        const rendererPropertiesString = element.hasAttribute( rendererPropertiesAttribute ) ? element.getAttribute( rendererPropertiesAttribute ) : "{}";
        let rendererPropertiesObj;
        try
        {
            rendererPropertiesObj = JSON.parse( rendererPropertiesString );
        }
        catch (err)
        {
            logExceptionData( channelName + ": Illegal JSON format in '" + rendererPropertiesAttribute + "' attribute.\nDetails were as follows:\n", err);
            rendererPropertiesObj = {};
        }
        let formattedValueText = this.buildFormattedValueText_(channelValueObj, channelMetadataObj, rendererPropertiesObj);

        // Suppress manipulation of element text content if overridden by the rendering hint
        let disableRendering = rendererPropertiesObj.hasOwnProperty("disable") ? rendererPropertiesObj.disable : false;

        if ( !disableRendering )
        {
            element.textContent = formattedValueText;
        }

    }

    /**
     * Renders the element's textual content.
     *
     * @param {Element} element - The element.
     * @param {WicaChannelMetadata} channelMetadata - the channel's metadata.
     * @param {WicaChannelValue} channelValue - the channel's latest value.
     * @param {WicaRendererProperties} rendererProperties -
     */
    static renderTextContent( element, channelMetadata, channelValue, rendererProperties )
    {

    }


    /**
     * Renders the element's tooltip.
     *
     * @implNote
     *
     * The wica CSS rules ensure that when the browser's cursor hovers over the element of interest a
     * a window will be automatically popped up to display the contents of the string specified by the
     * element's tooltip attribute.
     *
     * The implementation here does nothing if the tooltip attribute has already been set explicitly in
     * the HTML document. If the attribute has not been set then the first time this method is invoked
     * then it will set the attribute to the name of the channel.
     *
     * @param {Element} element - The element.
     * @param {string} tooltipAttribute - The name of the attribute which contains the tooltip.
     * @param {string} channelName - The channel name.
     * @private
     */
    static renderToolTips_( element, tooltipAttribute, channelName )
    {
        if ( ! element.hasAttribute( tooltipAttribute ) )
        {
            element.setAttribute( channelName );
        }
    }


    /**
     * Returns a string representation of the current value using information extracted from the wica-channel.
     *
     * @private
     * @param channelValueObj the value object associated with the element's underlying wica channel.
     * @param channelMetadataObj the metadata object associated with the element's underlying wica channel.
     * @param rendererPropertiesObj an object containg various styling hints.
     *
     * @returns {string} the formatted string.
     */
    static buildFormattedValueText_( channelValueObj, channelMetadataObj, rendererPropertiesObj )
    {
        // If the supplied value is non-scalar just return the string representation.
        if ( ( channelMetadataObj.type === "INTEGER_ARRAY") || (channelMetadataObj.type === "REAL_ARRAY") || (channelMetadataObj.type === "STRING_ARRAY") )
        {
            return JSON.stringify( channelValueObj.val );
        }
        else
        {
            return DocumentTextRenderer.formatScalarValue_( channelValueObj, channelMetadataObj, rendererPropertiesObj )
        }
    }

    /**
     * Returns a formatted value
     *
     * @private
     * @param channelValueObj
     * @param channelMetadataObj
     * @param rendererPropertiesObj
     * @returns {string}
     */
    static formatScalarValue_( channelValueObj, channelMetadataObj, rendererPropertiesObj)
    {
        let rawValue = channelValueObj.val;

        if ( channelMetadataObj.type === "REAL" )
        {
            let exponential = rendererPropertiesObj.hasOwnProperty("exp") ? rendererPropertiesObj.exp : null;
            let precision = rendererPropertiesObj.hasOwnProperty("prec") ? rendererPropertiesObj.prec : channelMetadataObj.prec;
            let units = rendererPropertiesObj.hasOwnProperty("units") ? rendererPropertiesObj.units : channelMetadataObj.egu;

            // TODO: look at more rigorous deserialisation of NaN's, Infinity etc
            if ((rawValue === "Infinity") || (rawValue === "NaN"))
            {
                return rawValue;
            }
            else if (exponential === null)
            {
                if (precision > MAX_PRECISION)
                {
                    console.warn("Channel precision is out-of-range. Precision will be truncated to " + MAX_PRECISION);
                    precision = MAX_PRECISION;
                }
                return rawValue.toFixed(precision) + " " + units;
            }
            else
            {
                return rawValue.toExponential(exponential) + " " + units;
            }
        }
        else if ( channelMetadataObj.type === "INTEGER" )
        {
            // TODO: look at more rigorous deserialisation of NaN's, Infinity etc
            if (rawValue === "Infinity")
            {
                return rawValue;
            }
            else
            {
                let units = rendererPropertiesObj.hasOwnProperty("units") ? rendererPropertiesObj.units : channelMetadataObj.egu;
                return rawValue + " " + units;
            }
        }
        else
        {
            return rawValue;
        }
    }

}