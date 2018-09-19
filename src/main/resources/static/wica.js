'use strict';

/**
 * Finds all HTML elements in the current document which are wica aware.
 *
 * @returns {NodeListOf<Element>}
 */
function find_wica_elements()
{
    return document.querySelectorAll('[data-wica-channel-name]');
}

/**
 *
 * @param target
 * @returns {NodeListOf<Element>}
 */
function find_wica_elements_with_channel_name( target )
{
    let selector = "*[data-wica-channel-name = \'" + target + "\']";
    return document.querySelectorAll( selector );
}


/**
 * Sends a request to the Wica Server to create a new event stream to
 * monitor the channels associated with each wica_aware element.
 *
 * @param ev_heartbeat_callback
 * @param ev_metadata_callback
 * @param ev_value_callback
 */
function create_event_stream( ev_heartbeat_callback, ev_metadata_callback, ev_value_callback ) {

    let wica_widgets = find_wica_elements();
    console.log( "Number of Wica elements found: ", wica_widgets.length );

    let channels = [];
    wica_widgets.forEach( function( widget )
    {
        let channelName = widget.getAttribute( "data-wica-channel-name");
        channels.push( channelName );
    } );

    //var SUBSCRIBE_CHANNEL_URL= "http://gfa-autodeploy.psi.ch:8080/ca/streams/";
    //var SUBSCRIBE_CHANNEL_URL= "http://localhost:8080/ca/streams/";
    const SUBSCRIBE_CHANNEL_URL= "https://gfa-wica.psi.ch/ca/streams/";
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function ()
    {
        if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200)
        {
            let id = xhttp.responseText;
            console.log( "Stream created, returned id is: ", id );
            subscribe_event_stream( id, ev_heartbeat_callback, ev_metadata_callback, ev_value_callback );
        }
    };
    let jsonChannelString = JSON.stringify( channels );
    xhttp.onerror = function() { console.log( "XHTTP error when subscribing to channel" ); };
    xhttp.withCredentials = true;
    xhttp.open( "POST", SUBSCRIBE_CHANNEL_URL, true);
    xhttp.setRequestHeader( "Content-Type", "application/json" );
    xhttp.send( jsonChannelString );
}

/**
 * Sends a request to the Wica Server to subscribe to the specified stream Id
 *
 * @param streamId
 * @param ev_heartbeat_callback
 * @param ev_metadata_callback
 * @param ev_value_callback
 */
function subscribe_event_stream( streamId, ev_heartbeat_callback, ev_metadata_callback, ev_value_callback )
{
    //var EVENT_STREAM_URL_BASE= "http://gfa-autodeploy.psi.ch:8080/ca/streams/";
    //var EVENT_STREAM_URL_BASE= "http://localhost:8080/ca/streams/";
    const EVENT_STREAM_URL_BASE = "https://gfa-wica.psi.ch/ca/streams/";
    let eventStreamUrl = EVENT_STREAM_URL_BASE + streamId;

    let eventSource = new EventSource( eventStreamUrl, { withCredentials: true } );
    eventSource.addEventListener( 'ev-wica-channel-value', function( ev )
    {
        // The code block below can be used to perform a security check
        // but for the moment at PSI it is disabled as the page may be located
        // on a different server from wica.
        // var expectedOrigin = location.origin;
        // if ( e.origin !== expectedOrigin )
        // {
        //     console.warn( "Event source: 'stream' unexpected event origin." );
        //     return false;
        // }
        let valueArrayObject = JSON.parse( ev.data );
        ev_value_callback( valueArrayObject );

    }, false );

    eventSource.addEventListener( 'ev-wica-server-heartbeat', function()
    {
        ev_heartbeat_callback();

    }, false );

    eventSource.addEventListener( 'ev-wica-channel-metadata', function( ev )
    {
        let metadataArrayObject = JSON.parse( ev.data );
        ev_metadata_callback( metadataArrayObject );

    }, false );

    eventSource.addEventListener( 'open', function()
    {
        console.log( "Event source: 'stream' - open event." );
        handle_wica_server_event_stream_established();

    }, false );

    eventSource.addEventListener( 'error', function()
    {
        console.log( "Event source: 'stream'  - error event. Closing event source." );
        eventSource.close();
        handle_wica_server_event_stream_terminated();

    }, false );
}

/**
 * Sets up a plan for managing the event stream, calling other handlera
 * as required.
 */
function manage_event_stream()
{
    const ONE_SECOND_IN_TIMER_UNITS = 1000;
    const STREAM_TIMEOUT_INTERVAL_IN_SECONDS =   20;
    const STREAM_RECONNECT_INTERVAL_IN_SECONDS = 15;

    let countdownInSeconds = 0;
    setInterval( function ()
    {
        if (countdownInSeconds === 0) {
            console.warn( "Event source 'stream': creating new..." );
            handle_wica_server_event_stream_timeout();
            create_event_stream( gotHeartbeat, gotNewMetadataMap, gotNewValueMap );
            console.log( "Event source: 'stream' - OK: create event stream task started" );
            countdownInSeconds = STREAM_RECONNECT_INTERVAL_IN_SECONDS;
        }
        countdownInSeconds--;
    }, ONE_SECOND_IN_TIMER_UNITS );

    function gotHeartbeat() {
        console.log( "Event source: received heartbeat." );
        countdownInSeconds = STREAM_TIMEOUT_INTERVAL_IN_SECONDS;
    }

    function gotNewMetadataMap( metadataObject ) {
        console.log( "Event source: received new channel metadata." );
        process_event_stream_metadata_update( metadataObject );
    }

    function gotNewValueMap( valueObject ) {
        console.log( "Event source: received new channel values." );
        process_event_stream_value_update( valueObject );
    }
}

/**
 * Processes the arrival of a new channel values from the event stream.
 *
 * @param valueObject
 */
function process_event_stream_value_update( valueObject )
{
    // Go through all the elements in the update object and assign
    // each element's value to the element's "data-wica-channel-value"
    // attribute.
    Object.keys( valueObject ).forEach( ( key ) =>
    {
        let channelName = key;
        let channelValue = valueObject[ key ];
        let elements = find_wica_elements_with_channel_name( channelName );
        let valueAsString = JSON.stringify(channelValue);
        elements.forEach( ele => {
            ele.setAttribute("data-wica-channel-value", valueAsString);
            console.log("Value updated: " + valueAsString);
        } );
    } );
}

/**
 * Processes the arrival of a new channel metadata from the event stream.
 *
 * @param metadataObject
 */
function process_event_stream_metadata_update( metadataObject )
{
    // Go through all the elements in the update object and assign
    // each element's metadata to the element's "data-wica-channel-metadata"
    // attribute.
    Object.keys( metadataObject ).forEach( ( key ) =>
    {
        let channelName = key;
        let channelMetadata =  metadataObject[ key ];
        let elements = find_wica_elements_with_channel_name( channelName );
        let metadataAsString = JSON.stringify( channelMetadata );
        elements.forEach( ele => {
            ele.setAttribute( "data-wica-channel-metadata", metadataAsString );
            console.log( "Metadata updated: " + metadataAsString );
        } );
    } );
}

/**
 *
 */
function render_wica_elements()
{
    find_wica_elements().forEach( (element) =>
    {
        if ( ( ! element.hasAttribute( "data-wica-channel-value" ) ) || ( ! element.hasAttribute( "data-wica-channel-metadata" ) ) )
        {
            return;
        }

        let channelValueObj = JSON.parse( element.getAttribute( "data-wica-channel-value" ) );
        let channelMetadataObj = JSON.parse( element.getAttribute( "data-wica-channel-metadata" ) );

        let connectionFailed = ( ( channelValueObj.val === null ) &&
                                 ( ! element.hasAttribute( "data-wica-channel-disconnected" ) ) );

        let connectionFixed = ( ( channelValueObj.val !== null ) &&
                                ( element.hasAttribute( "data-wica-channel-disconnected" ) ) );

        if ( connectionFailed )
        {
            element_set_connection_failed( element, "EpicsChannelDisconnected" );
            element.setAttribute( "data-wica-channel-disconnected", true );
        }

        if ( connectionFixed )
        {
            element_set_connection_established( element );
            element.removeAttribute( "data-wica-channel-disconnected" )
        }

        render_element_alarm_state( element, channelValueObj );

        // If an onchange event handler is defined then delegate the handling
        // of the event (typically rendering) to the defined method.
        if ( element.onchange !== null) {
            let event = new Event( 'change' );
            event.channelValue = channelValueObj;
            event.channelMetadata = channelMetadataObj;
            element.dispatchEvent( event );
        }
        else
        {
            if ( channelMetadataObj.type === "REAL" )
            {
                let number = channelValueObj.val;
                let precision = channelMetadataObj.prec;
                let units = channelMetadataObj.egu;
                element.textContent = number.toFixed( precision ) + " " + units;
            }
            else
            {
                element.textContent = channelValueObj.val;
            }
        }
    } );
}


/**
 *
 */
function handle_wica_server_event_stream_established()
{
    find_wica_elements().forEach( element => render_element_channel_connected_state( element ) );
}

/**
 *
 */
function handle_wica_server_event_stream_terminated()
{
    find_wica_elements().forEach( element => render_element_channel_disconnected_state( element, "WicaServerTerminatedEventStream") );
}

/**
 *
 */
function handle_wica_server_event_stream_timeout()
{
    find_wica_elements().forEach( element => render_element_channel_disconnected_state( element, "WicaServerConnectionAttemptFailed" ) );
}

/**
 * Renders the element to reflect the alarm severity of the underlying channel.
 * @param element
 */
function render_element_alarm_state(element, channelValueObj )
{
    const NO_ALARM = 0;
    const MINOR_ALARM = 1;
    const MAJOR_ALARM = 2;

    switch ( channelValueObj.sevr )
    {
        case MAJOR_ALARM:
            element.setAttribute( "data-wica-saved-color", element.style.color );
            element.style.color = "red";
            break;

        case MINOR_ALARM:
            element.setAttribute( "data-wica-saved-color", element.style.color );
            element.style.color = "orange";
            break;

        case NO_ALARM:
            if ( element.hasAttribute( "data-wica-saved-color" ) ) {
                element.style.color = element.getAttribute( "data-wica-saved-color" );
                element.removeAttribute( "data-epics-saved-color" );
            }
            break;
    }
}

/**
 * Renders the element to show that the underlying channel has been reconnected.
 *
 * @param element
 */
function render_element_channel_connected_state( element )
{
    // Restore the previously saved background and foreground colors
    if ( element.hasAttribute( "data-wica-saved-background-color" ) ) {
         element.style.backgroundColor = element.getAttribute( "data-wica-saved-background-color" );
         element.removeAttribute("data-wica-saved-background-color" );
    }

    if ( element.hasAttribute( "data-wica-saved-color" ) ) {
         element.style.color = element.getAttribute( "data-wica-saved-color" );
         element.removeAttribute("data-wica-saved-color" );
    }

    // Set the tooltip to show the name of the channel
    element.title=element.getAttribute( "data-wica-channel-name" );
}

/**
 * Renders the element to show that the underlying data connection has failed.
 *
 * @param element
 * @param failureReason
 */
function render_element_channel_disconnected_state(element, failureReason )
{
    // The first time there is a failure save the existing state of foreground and background colors
    if ( ! element.hasAttribute( "data-wica-saved-background-color" ) ) {
        element.setAttribute("data-wica-saved-background-color", element.style.backgroundColor );
    }

    if ( ! element.hasAttribute( "data-wica-saved-color" ) ) {
        element.setAttribute( "data-wica-saved-color", element.style.color );
    }

    let failureBackgroundColor="white";
    let failureForegroundColor="darkgrey";
    let failureMessage="";

    switch( failureReason )
    {
        case "WicaServerTerminatedEventStream":
            failureBackgroundColor= 'rgb( 255, 200, 200 )';  // light pink
            failureMessage="Wica Server Event Stream Terminated.";
            break;

        case "WicaServerConnectionAttemptFailed":
            failureBackgroundColor= 'rgb( 255, 224, 0 )';    // light orange
            failureMessage="Wica Server Event Stream Connect Timeout. Will try to reconnect...";
            break;

        case "EpicsChannelDisconnected":
            failureBackgroundColor="white";
            failureMessage="Epics Channel Disconnected.";
            break;
    }

    // Set the element so that it is rendered white
    element.style.backgroundColor = failureBackgroundColor;
    element.style.color = failureForegroundColor;

    // Set the tooltip to show the name of the channel and the disconnection reason.
    element.title= element.getAttribute( "data-wica-channel-name") + " - " + failureMessage;
}

function refresh_wica_page()
{
    render_wica_elements();

    // Allow at least 100ms after each rendering cycle
    setTimeout( refresh_wica_page, 100 );
}

manage_event_stream();
setTimeout( refresh_wica_page, 100 );
