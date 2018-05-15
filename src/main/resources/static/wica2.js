var linkStatusWidget = document.getElementById('server-link-status-widget');

function set_server_link_status_indicator( ok ) {
    if ( ok ) {
        linkStatusWidget.style.backgroundColor = "limegreen";
    }
    else {
        linkStatusWidget.style.backgroundColor = "red";
    }
}

function connect_event_stream( url, ev_callback )
{
    var eventSource = new EventSource(url);
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

    return true;
}

function manage_event_stream( url ) {

    var ONE_SECOND_IN_TIMER_UNITS = 1000;
    var STREAM_TIMEOUT_INTERVAL =   10;
    var STREAM_RECONNECT_INTERVAL = 10;

    var countdownInSeconds = 0;
    setInterval( function () {
        if (countdownInSeconds === 0) {
            console.warn("Event source: 'stream' trying to connect...");
            if( connect_event_stream( url, gotActivity ) ) {
                console.log("Event source: 'stream' - OK: connection task started" );
            }
            else {
                console.warn("Event source: 'stream' - FAILED: connection task startup failed" );
            }
            countdownInSeconds = STREAM_RECONNECT_INTERVAL;
        }
        countdownInSeconds--;
    }, ONE_SECOND_IN_TIMER_UNITS );

    function gotActivity( eventData ) {
        console.log( "Event source: 'stream' has activity" );
        countdownInSeconds = STREAM_TIMEOUT_INTERVAL;
    }
}

function find_epics_aware_widgets() {
    return document.querySelectorAll('[data-epics-channel]');
}

function register_listener( widget ) {
    console.log( "Epics wiget found !")
    var channel = widget.getAttribute( "data-epics-channel");
    console.log( "Channel is: ", channel );
    create_channel_monitor( widget, channel );
}

function create_channel_monitor( widget, channel ) {

    var url ="../stream/" + channel;
    if( connect_event_stream( url, gotActivity ) ) {
        console.log("Event source: 'stream' - OK: connection task started" );
    }

    function gotActivity( eventData ) {
        widget.textContent = eventData;
    }
}

var epics_widgets = find_epics_aware_widgets();

epics_widgets.forEach( function(w) { register_listener( w ) } );

//manage_event_stream( "../stream" );
