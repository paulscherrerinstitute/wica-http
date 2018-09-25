(function () {
    'use strict';

    class DocumentUtilities
    {
        /**
         * Finds all HTML elements in the current document which are wica aware.
         *
         * @returns {NodeListOf<Element>} the result list.
         */
        static findWicaElements() {
            return document.querySelectorAll('[data-wica-channel-name]');
        }

        /**
         * Finds all HTML elements in the current document with the specified wica channel name.
         *
         * @param target thechannel name to search for.
         * @returns {NodeListOf<Element>}  the result list.
         */
        static findWicaElementsWithChannelName(target) {
            let selector = "*[data-wica-channel-name = \'" + target + "\']";
            return document.querySelectorAll(selector);
        }
    }

    class WicaStreamManager
    {

        /**
         * @param serverUrl the url of the server hosting the Wica Stream.
         *
         * @param channels array of channel names to monitor.
         *
         * @param connectionHandlers object containing references to optional
         *        connection callback handlers. The following handlers are
         *        supported: streamConnect(), streamOpened(), streamClosed().
         *
         * @param messageHandlers object containing references to optional
         *        message callback handlers. The following handlers are
         *        supported: channelMetadataUpdated(), channelValuesUpdated().
         *
         * @param options object containing definitions which further control
         *        the behaviour of this class. The following options are supported:
         *        streamReconnectIntervalInSeconds, streamReconnectIntervalInSeconds,
         *        crossOriginCheckEnabled.
         */
        constructor( serverUrl, channels, connectionHandlers, messageHandlers, options )
        {
            this.serverUrl = serverUrl;
            this.channels = channels;

            this.streamConnect = connectionHandlers.streamConnect;
            this.streamOpened = connectionHandlers.streamOpened;
            this.streamClosed = connectionHandlers.streamClosed;

            this.channelMetadataUpdated = messageHandlers.channelMetadataUpdated;
            this.channelValuesUpdated = messageHandlers.channelValuesUpdated;

            this.streamReconnectIntervalInSeconds = options.streamReconnectIntervalInSeconds;
            this.streamTimeoutIntervalInSeconds = options.streamTimeoutIntervalInSeconds;
            this.crossOriginCheckEnabled = options.crossOriginCheckEnabled;

            this.countdownInSeconds = 0;
        }

        /**
         * Sets up a plan for managing the event stream, calling other handlers as required.
         *
         * If the countdownTimer reaches zero the stream will be reccreated.
         */
        activate()
        {
            const ONE_SECOND_IN_TIMER_UNITS = 1000;
            setInterval( () => {
                if ( this.countdownInSeconds === 0 ) {
                    console.warn("Event source 'stream': creating new...");
                    this.createStream();
                    console.log("Event source: 'stream' - OK: create event stream task started");
                    this.countdownInSeconds = this.streamReconnectIntervalInSeconds;
                }
                this.countdownInSeconds--;
            }, ONE_SECOND_IN_TIMER_UNITS );
        }

        /**
         * Sends a POST request to the Wica Server to create a new stream. Adds a handler to
         * subscribe to the stream once it has been created.
         */
        createStream()
        {
            // Inform listeners that a stream connection attempt is in progress
            this.streamConnect();

            // Create a request object which will ask the server to create the new stream.
            // Add a handler which will subscribe to the stream once it has been created.
            let xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = () => {
                if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200) {
                    let streamId = xhttp.responseText;
                    console.log("Stream created, returned id is: ", streamId);
                    let subscribeUrl = this.serverUrl + "/ca/streams/" + streamId;
                    this.subscribeStream( subscribeUrl );
                }
            };

            // Add a handler which will print an error message if the stream couldn't be created.
            xhttp.onerror = () => {
                console.log("XHTTP error when subscribing to channel");
            };

            // Now send off the request
            xhttp.withCredentials = true;
            let createUrl = this.serverUrl + "/ca/streams";
            xhttp.open("POST", createUrl, true);
            xhttp.setRequestHeader("Content-Type", "application/json");
            let jsonChannelString = JSON.stringify( channels );
            xhttp.send( jsonChannelString );
        }

        /**
         * Sends a GET request to the Wica Server to subscribe to the specified streams.
         * Adds handlers to deal with the various events and/or messages which may be
         * associated with the stream.
         *
         * @param subscribeUrl the stream subscription URL.
         */
        subscribeStream( subscribeUrl )
        {
            let eventSource = new EventSource( subscribeUrl, { withCredentials: true } );

            // The heartbeat message is for internal use of the stream handler.
            // If a heartbeat isn't received periodically then the stream will
            // be automatically recreated.
            eventSource.addEventListener( 'ev-wica-server-heartbeat', ev => {
                if ( this.crossOriginCheckOk( ev ) ) {
                    this.countdownInSeconds = this.streamTimeoutIntervalInSeconds;
                }
            }, false) ;

            eventSource.addEventListener( 'ev-wica-channel-metadata',ev => {
                if ( this.crossOriginCheckOk( ev ) ) {
                    let metadataArrayObject = JSON.parse( ev.data );
                    this.channelMetadataUpdated( metadataArrayObject );
                }

            }, false);

            eventSource.addEventListener( 'ev-wica-channel-value', ev => {
                if ( this.crossOriginCheckOk( ev ) ) {
                    let valueArrayObject = JSON.parse( ev.data );
                    this.channelValuesUpdated( valueArrayObject );
                }
            }, false);

            eventSource.addEventListener( 'open', ev => {
                if ( this.crossOriginCheckOk( ev ) ) {
                    this.streamOpened();
                    console.log("Event source: 'stream' - open event.");
                }
            }, false);

            eventSource.addEventListener( 'error', ev => {
                if ( this.crossOriginCheckOk( ev ) ) {
                    console.log("Event source: 'stream'  - error event. Closing event source.");
                    eventSource.close();
                    this.streamClosed();
                }

            }, false);
        }

        /**
         * Performs a CORS check to verify the origin of the supplied event
         * is the same as the origin of the page that is currently loaded.
         *
         * @param event the event to check
         * @returns boolean result, true when the check is ok.
         */
        crossOriginCheckOk( event )
        {
            if ( ! this.crossOriginCheckEnabled ) {
                return true;
            }

            let expectedOrigin = location.origin;
            if ( event.origin === expectedOrigin ) {
                return true;
            }
            else {
                console.warn( "Event source: 'stream' unexpected event origin." );
                return false;
            }
        }
    }


    let connectionHandlers = {};
    connectionHandlers.streamConnect = () => DocumentUtilities.findWicaElements().forEach( element => element.setAttribute( "data-wica-stream-state", "connecting" ) );
    connectionHandlers.streamOpened = () => DocumentUtilities.findWicaElements().forEach( element => element.setAttribute( "data-wica-stream-state", "opened" ) );
    connectionHandlers.streamClosed = () => DocumentUtilities.findWicaElements().forEach( element => element.setAttribute( "data-wica-stream-state", "closed" ) );

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
        console.log("Event stream received new channel value map.");

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


    // Look for all wica-aware elements in the current page
    let wicaElements = DocumentUtilities.findWicaElements();
    console.log("Number of Wica elements found: ", wicaElements.length );

    // Create an array of the associated channel names
    let channels = [];
    wicaElements.forEach( function( widget ) {
        let channelName = widget.getAttribute( "data-wica-channel-name" );
        channels.push( channelName );
    } );

    let wicaStreamManager = new WicaStreamManager( "https://gfa-wica.psi.ch", channels,
                                                    connectionHandlers, messageHandlers,
                                                    { streamReconnectIntervalInSeconds: 15,
                                                      streamTimeoutIntervalInSeconds: 20,
                                                      crossOriginCheckEnabled: false } );

    // Activate channel
    wicaStreamManager.activate();

    /**
     * Render all wica elements on the page.
     */
    function renderWicaElements()
    {
        DocumentUtilities.findWicaElements().forEach( (element) =>
        {
            // If we have no information about the channel's current value or the channel's metadata
            // then there is nothing useful that can be done so bail out.
            if ( ( ! element.hasAttribute( "data-wica-channel-value" ) ) || ( ! element.hasAttribute( "data-wica-channel-metadata" ) ) )
            {
                return;
            }

            // Obtain the channel value object
            let channelValueObj = JSON.parse( element.getAttribute( "data-wica-channel-value" ) );

            // Obtain the channel metadata object
            let channelMetadataObj = JSON.parse( element.getAttribute( "data-wica-channel-metadata" ) );

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
                // If the value object indicates that the channel is NOT connected then render the element's
                // title attribute (= "tooltip") using information extracted the element's connection attributes
                // only.
                if ( channelValueObj.val === null )
                {
                    // Set the tooltip to show the name of the channel and the disconnection reason.
                    element.title= "Channel Name: " + element.getAttribute( "data-wica-channel-name") +  "\n"  +
                                   "Stream Connect State: " + element.getAttribute( "data-wica-stream-state" ) + "\n" +
                                   "Channel Connect State: " + element.getAttribute( "data-wica-channel-connection-state" );
                }
                else
                {
                    let formattedValueText;
                    if ( channelMetadataObj.type === "REAL" ) {
                        let number = channelValueObj.val;
                        let precision = channelMetadataObj.prec;
                        let units = channelMetadataObj.egu;
                        formattedValueText = number.toFixed(precision) + " " + units;
                    }
                    else if ( channelMetadataObj.type === "INTEGER" ) {
                        let number = channelValueObj.val;
                        let units = channelMetadataObj.egu;
                        formattedValueText= number + " " + units;
                    }
                    else {
                        formattedValueText = channelValueObj.val;
                    }

                    // Render the element's title attribute (= "tooltip") using the information extracted from
                    // the element's connection attributes and the channel metadata and value information.
                    element.title= "Channel Name: '" + element.getAttribute( "data-wica-channel-name") +  "'\n"  +
                                   "Stream Connect State: '" + element.getAttribute( "data-wica-stream-state" ) + "'\n" +
                                   "Channel Connect State: ' " + element.getAttribute( "data-wica-channel-connection-state" ) + "'\n" +
                                   "Channel Alarm State: '" + element.getAttribute( "data-wica-channel-alarm-state" ) + "'\n" +
                                   "Channel Value Text: '" + formattedValueText;

                    // Render the element's textContent attribute with the formatted value
                    element.textContent = formattedValueText;
                }
            }

        } );
    }





    function refreshWicaPage()
    {
        try
        {
            renderWicaElements();
        }
        catch( err )
        {
            console.warn( "Programming Error: renderWicaElements threw an exception: " + err.message );
        }

        // Allow at least 100ms after each rendering cycle
        setTimeout( refreshWicaPage, 100 );
    }

    function loadWicaStylesheet()
    {
        if ( ! document.getElementById( 'wica-css-id') )
        {
            var head  = document.getElementsByTagName('head')[0];
            var link  = document.createElement('link');
            link.id   = 'wica-css-id';
            link.rel  = 'stylesheet';
            link.type = 'text/css';
            link.href = 'https://gfa-wica.psi.ch/wica.css';
            link.media = 'all';
            head.appendChild(link);
        }
    }

    loadWicaStylesheet();
    setTimeout( refreshWicaPage, 100 );

}());