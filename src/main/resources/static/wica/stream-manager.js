/**
 * @module stream-manager
 * @desc Provides support for creating and using Wica streams.
 */

/**
 * Provides support for creating a new WicaStream on the Wica backend server, for thereafter subscribing
 * to it and for publishing locally the received information.
 */
export class WicaStreamManager
{
    /**
     * Constructs a new instance.
     *
     * The returned object will remain in a dormant state until triggered by a call to the activate
     * method.
     *
     * @param {string} serverUrl - The url of the server to contact to request the creation of the new stream.
     *
     * @param {Object} streamConfiguration - The stream specification to be sent to the server. This includes
     *     the configuration of each of the stream's channels, together with, optionally, the stream properties
     *     object.
     *
     * @param {StreamProperties} [streamConfiguration.props] - The stream properties object.
     * @param {Object[]} streamConfiguration.channels - The configuration of each stream channel.
     * @param {string} streamConfiguration.channels[].name - The name of the channel.
     * @param {ChannelProperties} [streamConfiguration.channels[].props] - The channel properties object.
     *
     * @param {Object} connectionHandlers - Callbacks for handling connection state changes.
     * @param {callback} connectionHandlers.streamOpened - Called when the stream is opened (= not yet connected).
     * @param {callback} connectionHandlers.streamConnect - Called when the stream successfully connects.
     * @param {callback} connectionHandlers.streamClosed - Called when the stream disconnects.
     *
     * @param {Object} messageHandlers - Callbacks for handling data received from the SSE stream.
     * @param {callback} messageHandlers.channelMetadataUpdated -  Called when stream metadata information is received.
     * @param {callback} messageHandlers.channelValuesUpdated - Called when stream value information is received.
     *
     * @param {Object} options - Provides additional client-side configuration options.
     * @param {number} [options.streamReconnectIntervalInSeconds] - How often the manager should attempt to reconnect
     *     with the server if there is a communication outage.
     * @param {number} [options.streamTimeoutIntervalInSeconds] - Periodicity with which the stream's heartbeat signal
     *     needs to be received before the manager will conclude that a communication outage has occurred.
     * @param {boolean} [options.crossOriginCheckEnabled] - whether this manager should perform a CORS check.
     */
    constructor( serverUrl, streamConfiguration, connectionHandlers, messageHandlers, options )
    {
        this.serverUrl = serverUrl;
        this.streamConfiguration = streamConfiguration;
        this.streamOpened = connectionHandlers.streamOpened;
        this.streamConnect = connectionHandlers.streamConnect;
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
                this.createStream_();
                console.warn("Event source: 'stream' - OK: create event stream task started");
                this.countdownInSeconds = this.streamReconnectIntervalInSeconds;
            }
            this.countdownInSeconds--;
        }, ONE_SECOND_IN_TIMER_UNITS );
    }

    /**
     * Sends a POST request to the Wica Server to create a new stream. Adds a handler to
     * subscribe to the stream once it has been created.
     *
     * @private
     */
    createStream_()
    {
        // Inform listeners that a stream connection attempt is in progress
        this.streamConnect();

        // Create a request object which will be used to ask the server to create the new stream.
        const xhttp = new XMLHttpRequest();

        // Add a handler which will print an error message if the stream couldn't be created.
        xhttp.onerror = () => {
            console.warn( "XHTTP error when sending request to create event source" );
        };

        // Add a handler which will subscribe to the stream once it has been created.
        xhttp.onreadystatechange = () => {
            if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200) {
                const streamId = xhttp.responseText;
                console.warn( "Stream created, returned id is: ", streamId );
                const subscribeUrl = this.serverUrl + "/ca/streams/" + streamId;
                this.subscribeStream_( subscribeUrl );
            }
            if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status !== 200) {
                console.warn( "Error when sending create stream request." );
            }
        };

        // Now send off the request
        const createUrl = this.serverUrl + "/ca/streams";
        xhttp.withCredentials = true;
        xhttp.open("POST", createUrl, true);
        xhttp.setRequestHeader("Content-Type", "application/json");
        const jsonStreamConfiguration = JSON.stringify( this.streamConfiguration );
        xhttp.send( jsonStreamConfiguration );
    }

    /**
     * Sends a GET request to the Wica Server to subscribe to the specified streams.
     * Adds handlers to deal with the various events and/or messages which may be
     * associated with the stream.
     *
     * @private
     * @param subscribeUrl the stream subscription URL.
     */
    subscribeStream_( subscribeUrl )
    {
        const eventSource = new EventSource( subscribeUrl, { withCredentials: true } );

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
                const metadataArrayObject = JSON.parse( ev.data );
                this.channelMetadataUpdated( metadataArrayObject );
            }

        }, false);

        eventSource.addEventListener( 'ev-wica-channel-value', ev => {
            if ( this.crossOriginCheckOk( ev ) ) {
                const valueArrayObject = JSON.parse( ev.data );
                this.channelValuesUpdated( valueArrayObject );
            }
        }, false);

        eventSource.addEventListener( 'open', ev => {
            if ( this.crossOriginCheckOk( ev ) ) {
                const id = WicaStreamManager.extractEventSourceStreamIdFromUrl_( ev.target.url );
                this.streamOpened( id );
                console.warn("Event source: 'stream' - open event on stream with id: " + id );
            }
        }, false);

        eventSource.addEventListener( 'error', ev => {
            if ( this.crossOriginCheckOk_( ev ) ) {
                const id = WicaStreamManager.extractEventSourceStreamIdFromUrl_( ev.target.url );
                console.warn("Event source: 'stream'  - error event on stream with id: " + id );
                ev.target.close();  // close the event source that triggered this message
                this.streamClosed( id );
            }

        }, false);
    }

    /**
     * Performs a CORS check to verify the origin of the supplied event
     * is the same as the origin of the page that is currently loaded.
     *
     * @private
     * @param event the event to check
     * @returns boolean result, true when the check is ok.
     */
    crossOriginCheckOk_( event )
    {
        if ( ! this.crossOriginCheckEnabled ) {
            return true;
        }

        const expectedOrigin = location.origin;
        if ( event.origin === expectedOrigin ) {
            return true;
        }
        else {
            console.warn( "Event source: 'stream' unexpected event origin." );
            return false;
        }
    }

    /**
     * Extracts the last part of the url which is expected to contain the stream id.
     *
     * @private
     * @param url
     * @returns {string}
     */
    static extractEventSourceStreamIdFromUrl_( url )
    {
        return url.substr( url.lastIndexOf( "/" ) + 1 );
    }
}