'use strict';

function find_epics_aware_elements() {
    return document.querySelectorAll('[data-epics-channel]');
}

function create_event_stream( ev_callback ) {

    let epics_widgets = find_epics_aware_elements();
    console.log( "Epics widgets found: !", epics_widgets.length );

    let channels = [];
    epics_widgets.forEach( function( widget ) {
        let channelName = widget.getAttribute( "data-epics-channel");
        channels.push( channelName );
    } );

    //var SUBSCRIBE_CHANNEL_URL= "http://gfa-autodeploy.psi.ch:8080/ca/streams/";
    //var SUBSCRIBE_CHANNEL_URL= "http://localhost:8080/ca/streams/";
    let SUBSCRIBE_CHANNEL_URL= "https://gfa-autodeploy.psi.ch:8443/ca/streams/";
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200) {
            let id = xhttp.responseText;
            console.log( "Stream created, returned id is: ", id );
            connect_event_stream( id, ev_callback );
        }
    };
    let jsonChannelString = JSON.stringify( channels );
    xhttp.onerror = function() { console.log( "XHTTP error when subscribing to channel" ); };
    xhttp.withCredentials = true;
    xhttp.open( "POST", SUBSCRIBE_CHANNEL_URL, true);
    xhttp.setRequestHeader( "Content-Type", "application/json" );
    xhttp.send( jsonChannelString );
}

function connect_event_stream( id, ev_callback )
{
    //var EVENT_STREAM_URL_BASE= "http://gfa-autodeploy.psi.ch:8080/ca/streams/";
    //var EVENT_STREAM_URL_BASE= "http://localhost:8080/ca/streams/";
    let EVENT_STREAM_URL_BASE= "https://gfa-autodeploy.psi.ch:8443/ca/streams/";
    let eventStreamUrl = EVENT_STREAM_URL_BASE + id;

    let eventSource = new EventSource( eventStreamUrl, { withCredentials: true } );
    eventSource.addEventListener( 'message', function( e ) {

        // The code block below can be used to perform a security check
        // but for the moment at PSI it is disabled as the page may be located
        // on a different server from wica.
        // var expectedOrigin = location.origin;
        // if ( e.origin !== expectedOrigin )
        // {
        //     console.warn( "Event source: 'stream' unexpected event origin." );
        //     return false;
        // }
        let eventData = JSON.parse( e.data );
        ev_callback( eventData );

    }, false);

    eventSource.addEventListener( 'open', function() {
        console.log( "Event source: 'stream' - open event." );
        handle_wica_server_event_stream_established();
    }, false );

    eventSource.addEventListener( 'error', function() {
        console.log( "Event source: 'stream'  - error event. Closing event source." );
        eventSource.close();
        handle_wica_server_event_stream_terminated();
    }, false );
}

function manage_event_stream() {

    let ONE_SECOND_IN_TIMER_UNITS = 1000;
    let STREAM_TIMEOUT_INTERVAL =   20;
    let STREAM_RECONNECT_INTERVAL = 15;

    let countdownInSeconds = 0;
    setInterval( function () {
        if (countdownInSeconds === 0) {
            console.warn("Event source 'stream': creating new..." );
            handle_wica_server_event_stream_timeout();
            create_event_stream( gotActivity );
            console.log("Event source: 'stream' - OK: create event stream task started" );
            countdownInSeconds = STREAM_RECONNECT_INTERVAL;
        }
        countdownInSeconds--;
    }, ONE_SECOND_IN_TIMER_UNITS );

    function gotActivity( eventData ) {
        //console.log( "Event source: 'stream' has activity" );
        countdownInSeconds = STREAM_TIMEOUT_INTERVAL;
        process_event_stream_update( eventData );
    }
}

function process_event_stream_update( eventData )
{
    find_epics_aware_elements().forEach( function( element ) {
        let channelName = element.getAttribute( "data-epics-channel");
        let channelValue= eventData[ channelName ];

        // The WICA server sends a null event on the CA stream if the
        // underlying channel disconnects. Update the html element to indicate this.
        if ( channelValue === null ) {
            element_set_connection_failed( element, "EpicsChannelDisconnected" );
            element.setAttribute( "data-epics-channel-disconnected", true );
            return;
        }

        // If the channel now has a value but was previously disconnected
        // then update the html element to show that the channel is now ok.
        if ( element.hasAttribute( "data-epics-channel-disconnected" ) ) {
            element_set_connection_established( element );
            element.removeAttribute( "data-epics-channel-disconnected" )
        }

        // If an onchange event handler is defined then delegate the handling
        // of the event (typically rendering) to the defined method.
        if ( element.onchange !== null) {
            let event = new Event( 'change' );
            event.channelValue= channelValue;
            element.dispatchEvent( event );
        }
        // If an onchange event handler is NOT defined then simply update
        // the textContent field with the latest value.
        else {
            element.textContent = channelValue;
        }

    } );
}

function handle_wica_server_event_stream_established() {
    find_epics_aware_elements().forEach( element => element_set_connection_established( element ) );
}

function handle_wica_server_event_stream_terminated() {
    find_epics_aware_elements().forEach( element => element_set_connection_failed( element, "WicaServerTerminatedEventStream") );
}

function handle_wica_server_event_stream_timeout() {
    find_epics_aware_elements().forEach( element => element_set_connection_failed( element, "WicaServerConnectionAttemptFailed" ) );
}

function element_set_connection_established( element )
{
    // Restore the previously saved background and foreground colors
    if ( element.hasAttribute( "data-epics-saved-background-color" ) ) {
         element.style.backgroundColor = element.getAttribute( "data-epics-saved-background-color" );
         element.removeAttribute("data-epics-saved-background-color" );
    }

    if ( element.hasAttribute( "data-epics-saved-color" ) ) {
         element.style.color = element.getAttribute( "data-epics-saved-color" );
         element.removeAttribute("data-epics-saved-color" );
    }

    // Set the tooltip to show the name of the channel
    element.title=element.getAttribute( "data-epics-channel" );
}

function element_set_connection_failed( element, failureReason )
{
    // The first time there is a failure save the existing state of foreground and background colors
    if ( ! element.hasAttribute( "data-epics-saved-background-color" ) ) {
        element.setAttribute("data-epics-saved-background-color", element.style.backgroundColor );
    }

    if ( ! element.hasAttribute( "data-epics-saved-color" ) ) {
        element.setAttribute( "data-epics-saved-color", element.style.color );
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
    element.title= element.getAttribute( "data-epics-channel") + " - " + failureMessage;
}

manage_event_stream();