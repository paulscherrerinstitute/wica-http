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
                    console.warn("Event source: 'stream' - OK: create event stream task started");
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

            // Create a request object which will be used to ask the server to create the new stream.
            let xhttp = new XMLHttpRequest();

            // Add a handler which will print an error message if the stream couldn't be created.
            xhttp.onerror = () => {
                console.warn( "XHTTP error when sending request to create event source" );
            };

            // Add a handler which will subscribe to the stream once it has been created.
            xhttp.onreadystatechange = () => {
                if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200) {
                    let streamId = xhttp.responseText;
                    console.warn( "Stream created, returned id is: ", streamId );
                    let subscribeUrl = this.serverUrl + "/ca/streams/" + streamId;
                    this.subscribeStream( subscribeUrl );
                }
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

            // The heartbeat message is for internal use of this stream handler.
            // If a heartbeat isn't received periodically then the connection
            // will be deemed to have failed, triggering a new stream creation
            // and subscription cycle.
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
                    console.warn("Event source: 'stream' - open event.");
                }
            }, false);

            eventSource.addEventListener( 'error', ev => {
                if ( this.crossOriginCheckOk( ev ) ) {
                    console.warn("Event source: 'stream'  - error event. Closing event source.");
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

    class WicaRenderingManager
    {

        /**
         * Render all wica elements on the page.
         */
        static renderWicaElements()
        {
            DocumentUtilities.findWicaElements().forEach( (element) => {
                // If we have no information about the channel's current value or the channel's metadata
                // then there is nothing useful that can be done so bail out.
                if ( ( !element.hasAttribute("data-wica-channel-value")) || (! element.hasAttribute("data-wica-channel-metadata") ) )
                {
                    return;
                }

                // Obtain the channel value object
                let channelValueObj = JSON.parse( element.getAttribute( "data-wica-channel-value" ) );

                // Obtain the channel metadata object
                let channelMetadataObj = JSON.parse( element.getAttribute( "data-wica-channel-metadata" ) );

                // If an onchange event handler is defined then delegate the handling
                // of the event (typically rendering) to the defined method.
                if ( element.onchange !== null ) {
                    let event = new Event('change');
                    event.channelValue = channelValueObj;
                    event.channelMetadata = channelMetadataObj;
                    element.dispatchEvent(event);
                    return;
                }

                // If an onchange handler is NOT defined then render the widget according
                // to the default local rules.
                WicaRenderingManager.renderWidget( element, channelValueObj, channelMetadataObj );
            });
        }

        static renderWidget( element, channelValueObj, channelMetadataObj ) {
            let channelName = element.getAttribute( "data-wica-channel-name" );
            let renderingHintsString = element.hasAttribute("data-wica-rendering-hints" ) ? element.getAttribute("data-wica-rendering-hints") : "{}";
            let renderingHintsObj;
            try {
                renderingHintsObj = JSON.parse( renderingHintsString );
            }
            catch (err) {
                logExceptionData( channelName + ": Illegal JSON format in data-wica-rendering-hints attribute.\nDetails were as follows:\n", err );
                renderingHintsObj = {};
            }
            let formattedValueText = WicaRenderingManager.buildFormattedValueText( channelValueObj, channelMetadataObj, renderingHintsObj);
            let tooltipText = WicaRenderingManager.buildFormattedTooltipText( element, formattedValueText );

            element.textContent = formattedValueText;
            element.title = tooltipText;
        }

        static buildFormattedValueText( channelValueObj, channelMetadataObj, renderingHintsObj )
        {
            if ( channelMetadataObj.type === "REAL")
            {
                let number = channelValueObj.val;
                let exponential = renderingHintsObj.hasOwnProperty( "exp" ) ? renderingHintsObj.exp : null;
                let precision = renderingHintsObj.hasOwnProperty( "prec" ) ? renderingHintsObj.prec: channelMetadataObj.prec;
                let units = renderingHintsObj.hasOwnProperty( "units" ) ? renderingHintsObj.units: channelMetadataObj.egu;

                if ( exponential === null ) {
                    return number.toFixed( precision ) + " " + units;
                }
                else {
                    return number.toExponential( exponential ) + " " + units;
                }
            }
            else if (channelMetadataObj.type === "INTEGER")
            {
                let number = channelValueObj.val;
                let units = renderingHintsObj.hasOwnProperty( "units" ) ? renderingHintsObj.units: channelMetadataObj.egu;
                return number + " " + units;
            }
            else {
                return channelValueObj.val;
            }
        }
        static buildFormattedTooltipText( element, formattedValueText )
        {
            let channelName = element.getAttribute( "data-wica-channel-name" );
            let streamConnectState = element.getAttribute("data-wica-stream-state");
            let streamConnected = streamConnectState === "opened";

            if ( !streamConnected ) {
                return "Channel Name: " + channelName + "\n" +
                       "Stream Connect State: " + streamConnectState;
            }

            let channelConnectState = element.getAttribute("data-wica-channel-connection-state");
            let channelConnected = channelConnectState === "connected";

            if ( !channelConnected ) {
                return "Channel Name: " + channelName + "\n" +
                       "Stream Connect State: " + streamConnectState + "\n" +
                       "Channel Connect State: " + channelConnectState;
            }
            let alarmState = element.getAttribute("data-wica-channel-alarm-state");

            return "Channel Name: " + channelName + "'\n" +
                   "Stream Connect State: '" + streamConnectState + "'\n" +
                   "Channel Connect State: '" + channelConnectState + "'\n" +
                   "Channel Alarm State: '" + alarmState + "'\n" +
                   "Channel Value Text: '" + formattedValueText + "'";
        }
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

    function loadWicaStylesheet()
    {
        if ( ! document.getElementById( 'wica-css-id') )
        {
            let head  = document.getElementsByTagName('head')[0];
            let link  = document.createElement('link');
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