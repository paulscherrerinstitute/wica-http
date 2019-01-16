
// to use this:
// A) In HTML file: <script type="module" src=".../wica/wica.js"></script>
// B) In ES6 module file: import 'wica/wica.js'
console.debug( "Executing script in wica.js module...");

import {WicaStreamManager} from './stream-manager.js'
import * as DocumentUtilities from './document-utils.js'
import * as WicaEventManager from './event-manager.js'
import * as WicaRenderingManager from './rendering-manager.js'

//const WICA_HOST = "https://gfa-wica.psi.ch";
const WICA_HOST = "https://gfa-wica-dev.psi.ch";

let lastOpenedStreamId = 0;

const connectionHandlers = {};
connectionHandlers.streamConnect = () => DocumentUtilities.findWicaElements().forEach( element => element.setAttribute( "data-wica-stream-state", "connecting" ) );
connectionHandlers.streamOpened = (id) => {
    console.log( "Event stream opened: " + id );
    console.log( "Setting wica stream state on all html elements to: 'opened'" );
    DocumentUtilities.findWicaElements().forEach( element => element.setAttribute( "data-wica-stream-state", "opened-" + id ) );
    lastOpenedStreamId = id;

};

connectionHandlers.streamClosed = (id) => {
    console.log("Event stream closed: " + id );
    if ( id === lastOpenedStreamId ) {
        console.log( "Setting wica stream state on all html elements to: 'closed'" );
        DocumentUtilities.findWicaElements().forEach(element => element.setAttribute("data-wica-stream-state", "closed-" + id));
    }
    else {
        console.log( "Wica stream state on all html elements will be left unchanged as a newer event source is already open !" );
    }
};

const messageHandlers = {};
messageHandlers.channelMetadataUpdated = metadataObject =>
{
    console.log("Event stream received new channel metadata map.");

    // Go through all the elements in the update object and assign
    // each element's metadata to the element's "data-wica-channel-metadata"
    // attribute.
    Object.keys( metadataObject ).forEach( ( key ) =>
    {
        const channelName = key;
        const channelMetadata =  metadataObject[ key ];
        const elements = DocumentUtilities.findWicaElementsWithChannelName( channelName );
        const metadataAsString = JSON.stringify( channelMetadata );
        elements.forEach( ele => {
            ele.setAttribute( "data-wica-channel-metadata", metadataAsString );
            console.log( "Metadata updated: " + metadataAsString );
        } );
    } );
};

messageHandlers.channelValuesUpdated = valueObject =>
{
    //console.log( "WicaStream received new channel value map.");

    // Go through all the elements in the update object and assign
    // each element's value to the element's "data-wica-channel-value-latest"
    // and "data-wica-channel-value-array" attributes. Update the
    // "data-wica-channel-connection-state" attribute to reflect the
    // channel's underlying connection state.
    Object.keys( valueObject ).forEach( ( key ) =>
    {
        const channelName = key;
        const channelValueArray = valueObject[ key ];
        const elements = DocumentUtilities.findWicaElementsWithChannelName( channelName );
        const channelValueArrayAsString = JSON.stringify( channelValueArray );

        if ( ! Array.isArray( channelValueArray ) )
        {
            console.warn( "Stream Error: not an array !" );
            return;
        }
        const channelValueLatest = channelValueArray.pop();
        const channelValueLatestAsString = JSON.stringify( channelValueLatest );
        const channelConnectionState = ( channelValueLatest.val === null ) ? "disconnected" : "connected";
        elements.forEach( ele => {
            ele.setAttribute( "data-wica-channel-value-latest", channelValueLatestAsString );
            ele.setAttribute( "data-wica-channel-value-array", channelValueArrayAsString );
            ele.setAttribute( "data-wica-channel-connection-state", channelConnectionState );
            ele.setAttribute( "data-wica-channel-alarm-state", channelValueLatest.sevr );
            //console.log("Value updated: " + channelValueLatest);
        } );
    } );
};


function activateStream()
{
    // Look for all wica-aware elements in the current page
    const wicaElements = DocumentUtilities.findWicaElements();
    console.log("Number of Wica elements found: ", wicaElements.length);

    // Create an array of the associated channel names
    const channels = [];
    wicaElements.forEach(function (widget) {
        const channelName = widget.getAttribute("data-wica-channel-name");
        if ( widget.hasAttribute( "data-wica-channel-props" ) )
        {
            const channelProps = widget.getAttribute("data-wica-channel-props");
            channels.push( { "name" : channelName, "props" : JSON.parse( channelProps ) } );
        }
        else
        {
            channels.push( { "name" : channelName } );
        }
    } );

    const streamOptions = {
        streamReconnectIntervalInSeconds: 15,
        streamTimeoutIntervalInSeconds: 20,
        crossOriginCheckEnabled: false,
    };

    const streamConfiguration = { "channels": channels };
    const wicaStreamManager = new WicaStreamManager( WICA_HOST, streamConfiguration, connectionHandlers, messageHandlers, streamOptions );

    // Activate manager
    wicaStreamManager.activate();
}


function fireWicaEvents()
{
    try
    {
        WicaEventManager.fireEvents();
    }
    catch( err )
    {
        logExceptionData( "Programming Error: fireEvents threw an exception: ", err );
    }

    // Allow at least 100ms after each event firing cycle
    setTimeout( fireWicaEvents, 100 );
}


function refreshWicaPage()
{
    try
    {
        WicaRenderingManager.renderWicaElements();
    }
    catch( err )
    {
        logExceptionData( "Programming Error: fireEvents threw an exception: ", err );
    }

    // Allow at least 100ms after each rendering cycle
    setTimeout( refreshWicaPage, 100 );
}

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

function loadWicaCSS()
{
    if ( ! document.getElementById( 'wica-css-id') )
    {
        const head  = document.getElementsByTagName('head')[0];
        const link  = document.createElement('link');
        link.id   = 'wica-css-id';
        link.rel  = 'stylesheet';
        link.type = 'text/css';
        link.href = WICA_HOST + '/wica/wica.css';
        link.media = 'all';
        head.appendChild(link);
    }
}

loadWicaCSS();

activateStream();

setTimeout( fireWicaEvents, 100 );
setTimeout( refreshWicaPage, 100 );


