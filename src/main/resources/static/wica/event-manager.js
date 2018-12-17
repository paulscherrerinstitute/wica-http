import * as DocumentUtilities from './document-utils.js'

/**
 * Fire events on all wica-aware html elements in the current document.
 *
 * @implNote
 *
 * The current implementation obtains its information about the state
 * of each wica-aware element by looking at the information in the
 * 'data-wica-channel-value' and 'data-wica-channel-metadata' html
 * element attributes.
 *
 * No events will be fired until the channel is connected to its
 * underlying datasource.
 *
 * The following events are supported:
 *   - onchange: backwards compatibility open
 *   - CustomEvent "wica"
 *
 * The following information is provided in the CustomEvent detail
 * attribute:
 *
 *   detail.channelName
 *   detail.channelMetadata
 *   detail.channelValues
 *
 */
export function fireEvents()
{
    DocumentUtilities.findWicaElements().forEach( (element) => {

        // If we have no information about the channel's current value or the channel's metadata
        // then there is nothing useful that can be done so bail out.
        if ( ( !element.hasAttribute("data-wica-channel-value")) || (! element.hasAttribute("data-wica-channel-metadata") ) )
        {
            return;
        }

        // Obtain the channel name object
        const channelName = element.getAttribute( "data-wica-channel-name" );

        // Obtain the channel metadata object
        const channelMetadataObj = JSON.parse( element.getAttribute( "data-wica-channel-metadata" ) );

        // Obtain the object containing the array of recently received channel values.
        const channelValueArrayObj = JSON.parse( element.getAttribute( "data-wica-channel-value" ) );

        // Check that the received value object really was an array
        if ( ! Array.isArray( channelValueArrayObj ) ) {
            console.warn( "Stream error: received value object was not an array !" )
            return;
        }

        // If there isn't at least one value present bail out as there is nothing useful
        // that can be done so bail out.
        if ( channelValueArrayObj.length === 0 ) {
            return;
        }

        // If an onchange event handler IS defined then delegate the handling
        // of the event (typically performing some calculation or rendering a plot) to
        // the defined method.
        if ( element.onchange !== null ) {
            let event = new Event('change');
            event.channelName = channelName;
            event.channelMetadata = channelMetadataObj;
            event.channelValues = channelValueArrayObj;
            element.dispatchEvent(event);
        }

        // If an wica event handler IS defined then delegate the handling
        // of the event (typically performing some calculation or rendering
        // a plot) to the defined method.
        if ( element.onwica !== null )
        {
            const customEvent = new CustomEvent( 'wica', {
                detail: {
                    "channelName"     : channelName,
                    "channelMetadata" : channelMetadataObj,
                    "channelValues"   : channelValueArrayObj
                }
            } );
            element.dispatchEvent( customEvent );
        }
    });
}
