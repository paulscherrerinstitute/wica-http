/**
 * Provides support for firing notification events on wica-aware elements in the current document.
 * @module
 */
console.debug( "Executing script in event-manager.js module...");

import * as DocumentUtilities from './document-utils.js'


/**
 * Activates this event manager.
 */
export function activate()
{
    try
    {
        fireEvents();
    }
    catch( err )
    {
        logExceptionData( "Programming Error: fireEvents threw an exception: ", err );
    }

    // Allow at least 100ms after each event firing cycle
    setTimeout( activate, 100 );
}


/**
 * Fires wica notification events on all wica-aware elements in the current document. The event which
 * is fired includes full information about the current state of the channel.
 *
 * The following event types are supported:
 * - 'onwica': custom event.
 * - 'onchange': DEPRECATED, provided only for backwards compatibility.
 *
 * The event payload includes the most recently received stream notification information for the wica
 * channel's metadata and wica channel's value.
 *
 * No events will be fired until both the channel's metadata and value have been obtained.
 *
 * In the case of the 'onwica' event the following information is provided in the detail attribute:
 *
 *   - detail.channelName
 *   - detail.streamState
 *   - detail.channelMetadata
 *   - detail.channelValueArray
 *   - detail.channelValueLatest
 *
 * @implNote
 *
 * The current implementation obtains the event payload information by looking at the information in the
 * 'data-wica-channel-value-array' and 'data-wica-channel-metadata' html element attributes.
 */
function fireEvents()
{
    DocumentUtilities.findWicaElements().forEach( (element) => {

        // If we have no information about the channel's current value or the channel's metadata
        // then there is nothing useful that can be done so bail out.
        if ( ( !element.hasAttribute("data-wica-channel-value-array")) || (! element.hasAttribute("data-wica-channel-metadata") ) )
        {
            return;
        }

        // Obtain the channel name object
        const channelName = element.getAttribute( "data-wica-channel-name" );

        // Obtain the channel metadata object
        const channelMetadataObj = JSON.parse( element.getAttribute( "data-wica-channel-metadata" ) );

        // Obtain the object containing the array of recently received channel values.
        const channelValueArrayObj = JSON.parse( element.getAttribute( "data-wica-channel-value-array" ) );

        // Check that the received value object really was an array
        if ( ! Array.isArray( channelValueArrayObj ) ) {
            console.warn( "Stream error: received value object was not an array !" );
            return;
        }

        // If there isn't at least one value present bail out as there is nothing useful to be done
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
            event.channelValueArray = channelValueArrayObj;
            event.channelValueLatest = channelValueArrayObj[ channelValueArrayObj.length - 1 ];
            element.dispatchEvent(event);
        }

        // If an wica event handler IS defined then delegate the handling
        // of the event (typically performing some calculation or rendering
        // a plot) to the defined method.
        if ( element.onwica !== null )
        {
            const customEvent = new CustomEvent( 'wica', {
                detail: {
                    "channelName"        : channelName,
                    "channelMetadata"    : channelMetadataObj,
                    "channelValueArray"  : channelValueArrayObj,
                    "channelValueLatest" : channelValueArrayObj[ channelValueArrayObj.length - 1 ]
                }
            } );
            element.dispatchEvent( customEvent );
        }
    });

    function logExceptionData( msg, err )
    {
        let vDebug = "";
        for ( const prop of err )
        {
            vDebug += "property: "+ prop + " value: ["+ err[prop]+ "]\n";
        }
        vDebug += "toString(): " + " value: [" + err.toString() + "]";
        console.warn( msg + vDebug );
    }

}
