var linkStatusWidget = document.getElementById('server-link-status-widget');

function set_server_link_status_indicator( ok ) {
    if ( ok ) {
        linkStatusWidget.style.backgroundColor = "limegreen";
    }
    else {
        linkStatusWidget.style.backgroundColor = "red";
    }
}


function find_epics_aware_widgets() {
    return document.querySelectorAll('[data-epics-channel]');
}

function create_event_stream( ev_callback) {

    var epics_widgets = find_epics_aware_widgets();
    console.log( "Epics widgets found: !", epics_widgets.length );

    var channels = new Array();
    epics_widgets.forEach( function( widget ) {
        var channelName = widget.getAttribute( "data-epics-channel");
        channels.push( channelName );
    } );

    var SUBSCRIBE_CHANNEL_URL= "../ca/streams";
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200) {
            var id = xhttp.responseText;
            console.log( "Stream created, returned id is: ", id );
            connect_event_stream( id, ev_callback );
        }
    };
    var jsonChannelString = JSON.stringify( channels );
    xhttp.open( "POST", SUBSCRIBE_CHANNEL_URL, true);
    xhttp.setRequestHeader( "Content-Type", "application/json" );
    xhttp.send( jsonChannelString );
}

function connect_event_stream( id, ev_callback )
{
    var EVENT_STREAM_URL_BASE= "ca/streams/";
    var eventStreamUrl = EVENT_STREAM_URL_BASE + id;

    var eventSource = new EventSource( eventStreamUrl );
    eventSource.addEventListener('message', function(e) {

        // Perform recommended security check
        var expectedOrigin = location.origin;
        if ( e.origin !== expectedOrigin )
        {
            console.warn( "Event source: 'stream' unexpected event origin." );
            return false;
        }

        var eventData = JSON.parse( e.data );

        ev_callback( eventData );

    }, false);

    eventSource.addEventListener( 'open', function() {
        console.log( "Event source: 'stream' - open event." );
        set_server_link_status_indicator( true );
    }, false );

    eventSource.addEventListener( 'error', function() {
        console.log( "Event source: 'stream'  - error event. Closing event source." );
        eventSource.close();
        set_server_link_status_indicator( false );
    }, false );
}

function manage_event_stream() {

    var ONE_SECOND_IN_TIMER_UNITS = 1000;
    var STREAM_TIMEOUT_INTERVAL =   70;
    var STREAM_RECONNECT_INTERVAL = 15;

    var countdownInSeconds = 0;
    setInterval( function () {
        if (countdownInSeconds === 0) {
            console.warn("Event source 'stream': creating new..." );
            create_event_stream( gotActivity );
            console.log("Event source: 'stream' - OK: create even stream task started" );
            countdownInSeconds = STREAM_RECONNECT_INTERVAL;
        }
        countdownInSeconds--;
    }, ONE_SECOND_IN_TIMER_UNITS );

    function gotActivity( eventData ) {
        console.log( "Event source: 'stream' has activity" );
        countdownInSeconds = STREAM_TIMEOUT_INTERVAL;
        process_event_stream_update( eventData );
    }
}

function process_event_stream_update( eventData )
{
    find_epics_aware_widgets().forEach( function(widget) {

        var channelName = widget.getAttribute( "data-epics-channel");
        var channelValue= eventData[ channelName ];
        if (channelValue != null ) {
            widget.textContent= channelValue;
        }
    });
}

manage_event_stream();