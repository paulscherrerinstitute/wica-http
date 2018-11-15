// to use this:
// A) In HTML file: <script type="module" src=".../wica/wica.js"></script>
// B) In ES6 module file: import 'wica/wica.js'
console.debug( "Executing script in wica.js module...");

import {WicaStreamManager} from './stream-manager.js'
import * as WicaRenderingManager from './rendering-manager.js'
import * as DocumentUtilities from './document-utils.js'

let lastOpenedStreamId = 0;

let connectionHandlers = {};

connectionHandlers.streamConnect = () => DocumentUtilities.findWicaElements().forEach( element => element.setAttribute( "data-wica-stream-state", "connecting" ) );
connectionHandlers.streamOpened = (id) => {
    console.log( "Event stream opened: " + id );
    console.log( "Setting wica stream state on all html elements to: 'opened'" );
    DocumentUtilities.findWicaElements().forEach( element => element.setAttribute( "data-wica-stream-state", "opened-" + id ) );
    lastOpenedStreamId = id;

}
connectionHandlers.streamClosed = (id) => {
    console.log("Event stream closed: " + id );
    if ( id === lastOpenedStreamId ) {
        console.log( "Setting wica stream state on all html elements to: 'closed'" );
        DocumentUtilities.findWicaElements().forEach(element => element.setAttribute("data-wica-stream-state", "closed-" + id));
    }
    else {
        console.log( "Wica stream state on all html elements will be left unchanged as a newer event source is already open !" );
    }
}

let messageHandlers = {};
messageHandlers.channelMetadataUpdated = metadataObject =>
{
    console.log("Event stream received new channel metadata map.");

    // Go through all the elements in the update object and assign
    // each element's metadata to the element's "data-wica-channel-metadata"
    // attribute.
    Object.keys( metadataObject ).forEach( ( key ) =>
    {
        let channelName = key;
        let channelMetadata =  metadataObject[ key ];
        let elements = DocumentUtilities.findWicaElementsWithChannelName( channelName );
        let metadataAsString = JSON.stringify( channelMetadata );
        elements.forEach( ele => {
            ele.setAttribute( "data-wica-channel-metadata", metadataAsString );
            console.log( "Metadata updated: " + metadataAsString );
        } );
    } );
};

messageHandlers.channelValuesUpdated = valueObject =>
{
    console.log( "WicaStream received new channel value map.");

    // Go through all the elements in the update object and assign
    // each element's value to the element's "data-wica-channel-value"
    // attribute. Update the  "data-wica-channel-state" attribute to
    // reflect the channel's underlying connection state.
    Object.keys( valueObject ).forEach( ( key ) =>
    {
        let channelName = key;
        let channelValue = valueObject[ key ];
        let elements = DocumentUtilities.findWicaElementsWithChannelName( channelName );
        let valueAsString = JSON.stringify( channelValue );
        elements.forEach( ele => {
            ele.setAttribute( "data-wica-channel-value", valueAsString );
            let channelConnectionState = ( channelValue.val === null ) ? "disconnected" : "connected";
            ele.setAttribute( "data-wica-channel-connection-state", channelConnectionState );
            ele.setAttribute( "data-wica-channel-alarm-state", channelValue.sevr );
            console.log("Value updated: " + valueAsString);
        } );
    } );
};

function activateStream()
{
    // Look for all wica-aware elements in the current page
    let wicaElements = DocumentUtilities.findWicaElements();
    console.log("Number of Wica elements found: ", wicaElements.length);

    // Create an array of the associated channel names
    let channels = [];
    wicaElements.forEach(function (widget) {
        let channelName = widget.getAttribute("data-wica-channel-name");
        channels.push(channelName);
    });

    let wicaStreamManager = new WicaStreamManager("https://gfa-wica-dev.psi.ch", channels,
        connectionHandlers, messageHandlers,
        {
            streamReconnectIntervalInSeconds: 15,
            streamTimeoutIntervalInSeconds: 20,
            crossOriginCheckEnabled: false
        });

    // Activate manager
    wicaStreamManager.activate();
}

function refreshWicaPage()
{
    try
    {
        WicaRenderingManager.renderWicaElements();
    }
    catch( err )
    {
        logExceptionData( "Programming Error: renderWicaElements threw an exception: ", err );
    }

    // Allow at least 100ms after each rendering cycle
    setTimeout( refreshWicaPage, 100 );
}

function logExceptionData( msg, err )
{
    var vDebug = "";
    for (var prop in err)
    {
        vDebug += "property: "+ prop+ " value: ["+ err[prop]+ "]\n";
    }
    vDebug += "toString(): " + " value: [" + err.toString() + "]";
    console.warn( msg + vDebug );
}

function loadWicaCSS()
{
    if ( ! document.getElementById( 'wica-css-id') )
    {
        let head  = document.getElementsByTagName('head')[0];
        let link  = document.createElement('link');
        link.id   = 'wica-css-id';
        link.rel  = 'stylesheet';
        link.type = 'text/css';
        link.href = 'https://gfa-wica-dev.psi.ch/wica/wica.css';
        link.media = 'all';
        head.appendChild(link);
    }
}

loadWicaCSS();
activateStream();
setTimeout( refreshWicaPage, 100 );


