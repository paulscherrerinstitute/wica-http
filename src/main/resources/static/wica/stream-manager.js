/**
 * Provides support for creating and using Wica streams.
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import * as JsonUtilities from './json5-wrapper.js'

export {StreamManager}

/*- Script Execution Starts Here ---------------------------------------------*/

log.log( "Executing script in stream-manager.js module...");

/**
 * Callback invoked when the stream connect sequence begins.
 *
 * @callback module:stream-manager.StreamConnectCallback
 * @property {number} count - The connection request counter. The counter, set to 1 initially, increments
 *     after every stream connection attempt. This information is useful mainly for debug purposes (for
 *     example for outputting a message to the console).
 */

/**
 * Callback invoked when the stream is opened (that's to say when the connection with the server
 * has been successfully established).
 *
 * @callback module:stream-manager.StreamOpenedCallback
 * @property {string} id - The ID of the stream that was opened. This information is useful mainly
 *    for debug purposes (for example for outputting a message to the console).
 */

/**
 * Callback invoked when the stream is closed (that's to say when the connection with the server
 * has been shut down).
 *
 * @callback module:stream-manager.StreamClosedCallback
 * @property {string} id - The ID of the stream that was closed. This information is useful mainly
 *    for debug purposes (for example for outputting a message to the console).
 */

/**
 * @callback module:stream-manager.ChannelMetadataUpdatedCallback
 * @property {Object.<WicaChannelName, WicaChannelMetadata>} metadataMap - Map of channel names and their
 *     associated metadata. See {@link module:shared-definitions.WicaChannelName WicaChannelName} and
 *     {@link module:shared-definitions.WicaChannelMetadata WicaChannelMetadata}.
 */

/**
 * @callback module:stream-manager.ChannelValuesUpdatedCallback
 * @property {Object.<WicaChannelName,WicaChannelValue[]>} valueMap - Map of channel names and array of
 *     associated values that have been received for the channel in chronological order.
 *     See {@link module:shared-definitions.WicaChannelName WicaChannelName} and
 *     {@link module:shared-definitions.WicaChannelValue WicaChannelValue}.
 */

/**
 * Provides support for creating a new WicaStream on the Wica server, for subscribing to it and for
 * publishing the received information.
 */
class StreamManager
{
    /**
     * Constructs a new instance.
     *
     * The returned object will remain in a dormant state until triggered by a call to the activate method.
     *
     * @param {string} serverUrl - The URL of the server to contact to request the creation of the new stream.
     *
     * @param {Object} streamConfiguration - The stream specification to be sent to the server. This includes
     *     the configuration of each of the stream's channels, together with, optionally, the stream properties
     *     object.
     *
     * @param {Object<WicaChannelName, WicaChannelProperties>} streamConfiguration.channels - The configuration
     *     of each stream channel. See {@link module:shared-definitions.WicaChannelName WicaChannelName} and
     *     {@link module:shared-definitions.WicaChannelProperties WicaChannelProperties}.
     *
     * @param {WicaStreamProperties} [streamConfiguration.props] - The stream properties object.
     *     See {@link module:shared-definitions.WicaStreamProperties WicaStreamProperties}.
     *
     * @param {Object} connectionHandlers - Callbacks for handling connection state changes.
     * @param {StreamConnectCallback} connectionHandlers.streamConnect - Called when the stream manager begins
     *     a new connect sequence. This occurs after the stream manager activate method has been invoked, or
     *     if the stream manager doesn't see a stream heartbeat message within the expected time interval. See
     *     {@link module:stream-manager.StreamConnectCallback StreamConnectCallback}.
     * @param {StreamOpenedCallback} connectionHandlers.streamOpened - Called when the stream is opened. See
     *     {@link module:stream-manager.StreamOpenedCallback StreamOpenedCallback}.
     * @param {StreamClosedCallback} connectionHandlers.streamClosed - Called when the stream is opened. See
     *     {@link module:stream-manager.StreamClosedCallback StreamClosedCallback}.
     *
     * @param {Object} messageHandlers - Callbacks for handling data received from the SSE stream.
     * @param {ChannelMetadataUpdatedCallback} messageHandlers.channelMetadataUpdated - Called when channel
     *     metadata information is received. See
     *     {@link module:stream-manager.ChannelMetadataUpdatedCallback ChannelMetadataUpdatedCallback}.
     * @param {ChannelValuesUpdatedCallback} messageHandlers.channelValuesUpdated - Called when channel
     *     value information is received. See
     *     {@link module:stream-manager.ChannelValuesUpdatedCallback ChannelValuesUpdatedCallback}.
     *
     * @param {Object} options - Provides additional client-side configuration options.
     * @param {number} [options.streamTimeoutIntervalInSeconds] - Periodicity with which the stream's heartbeat
     *     message needs to be received before the manager will conclude that a communication outage has occurred.
     * @param {number} [options.streamReconnectIntervalInSeconds] - Period between successive reconnection
     *     attempts following a communication outage.
     * @param {boolean} [options.crossOriginCheckEnabled] - Whether this manager should perform a CORS check
     *     to verify that the origin of the event stream is the same as the origin from which this manager
     *     was loaded.
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
        this.connectionRequestCounter = 1;
        this.activeStreamId = undefined;
    }

    /**
     * Activates this stream manager instance.
     *
     * More specifically this sets up a state machine to create and manage an active event stream
     * and for calling other handlers as required to track the evolving connection state and received
     * data.
     *
     * See also: {@link module:stream-manager.StreamManager#shutdown shutdown}.
     *
     * @implNote
     * The current implementation expects to receive a periodic "heartbeat" message to confirm
     * that the connection to the data server is ok. If the message is not received within the
     * allowed time window then the existing stream will be closed and a new stream will be
     * negotiated with the server.
     */
    activate()
    {
        // Activation has to wait until we received the callback that the
        // JSON5 library is loaded.
        JsonUtilities.load( () => this.startStreamMonitoring_() )
    }

    /**
     * Shuts down this stream manager instance.
     *
     * See also: {@link module:stream-manager.StreamManager#activate activate}.
     */
    shutdown()
    {
        // If the stream manager is activated cancel the interval timer.
        if( this.intervalTimer !== undefined )
        {
            clearInterval( this.intervalTimer );
        }

        // Cancel the most recently established stream (if one has been established).
        if ( this.activeStreamId !== undefined )
        {
            this.deleteStream_( this.activeStreamId );
        }
    }

    /**
     * Sends a DELETE request to the Wica Server to delete an existing stream.
     *
     * @private
     * @param {string} streamId the ID of the stream to be deleted.
     */
    deleteStream_( streamId )
    {
        // Create a request object which will be used to ask the server to create the new stream.
        const xhttp = new XMLHttpRequest();

        // Add a handler which will print an error message if the stream couldn't be deleted.
        xhttp.onerror = () => {
            log.warn( "XHTTP error when sending request to delete event source" );
        };

        // Add a handler which will subscribe to the stream once it has been created.
        xhttp.onreadystatechange = () => {
            if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200) {
                const deletedId = xhttp.responseText;
                log.info( "Stream deleted, deleted id was: ", deletedId );
            }
            if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status !== 200) {
                log.warn( "Error when sending delete stream request." );
            }
        };

        // Now send off the request. Note async is FALSE so that the page does not
        // get a chance to unload before the request has been sent.
        const deleteUrl = this.serverUrl + "/ca/streams/" + streamId;
        xhttp.withCredentials = true;
        xhttp.open("DELETE", deleteUrl, false );
        xhttp.setRequestHeader("Content-Type", "application/json");
        xhttp.send();
    }

    /**
     * Starts the stream monitoring process.
     *
     * @private
     */
    startStreamMonitoring_()
    {
        const ONE_SECOND_IN_TIMER_UNITS = 1000;
        this.intervalTimer = setInterval( () => {
            if ( this.countdownInSeconds === 0 ) {
                log.info( "Event source 'stream': creating new...");
                // Set up an asynchronous chain of events that will create a stream
                // then subscribe to it, then start monitoring it. If the heartbeat
                // signal is not seen the process will repeat itself after the
                // heartbeat interval timeout.
                this.createStream_();
                log.info( "Event source: 'stream' - OK: create event stream task started");
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
        this.streamConnect( this.connectionRequestCounter++ );

        // Create a request object which will be used to ask the server to create the new stream.
        const xhttp = new XMLHttpRequest();

        // Add a handler which will print an error message if the stream couldn't be created.
        xhttp.onerror = () => {
            log.warn( "XHTTP error when sending request to create event source" );
        };

        // Add a handler which will subscribe to the stream once it has been created.
        xhttp.onreadystatechange = () => {
            if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status === 200) {
                const streamId = xhttp.responseText;
                log.info( "Stream created, returned id is: ", streamId );
                const subscribeUrl = this.serverUrl + "/ca/streams/" + streamId;
                this.subscribeStream_( subscribeUrl );
            }
            if (xhttp.readyState === XMLHttpRequest.DONE && xhttp.status !== 200) {
                log.warn( "Error when sending create stream request." );
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
            if ( this.crossOriginCheckOk_( ev ) ) {
                const id = StreamManager.extractEventSourceStreamIdFromUrl_( ev.target.url );
                log.info( "Event source: 'wica stream' - heartbeat event on stream with id: " + id );
                this.countdownInSeconds = this.streamTimeoutIntervalInSeconds;
            }
        }, false) ;

        eventSource.addEventListener( 'ev-wica-channel-metadata',ev => {
            if ( this.crossOriginCheckOk_( ev ) ) {
                const metadataArrayObject = JsonUtilities.parse( ev.data );
                this.channelMetadataUpdated( metadataArrayObject );
            }

        }, false);

        eventSource.addEventListener( 'ev-wica-channel-value', ev => {
            if ( this.crossOriginCheckOk_( ev ) ) {
                const valueArrayObject = JsonUtilities.parse( ev.data );
                this.channelValuesUpdated( valueArrayObject );
            }
        }, false);

        eventSource.addEventListener( 'open', ev => {
            if ( this.crossOriginCheckOk_( ev ) ) {
                const id = StreamManager.extractEventSourceStreamIdFromUrl_( ev.target.url );
                this.streamOpened( id );
                log.info( "Event source: 'wica stream' - open event on stream with id: " + id );
                this.activeStreamId = id;
            }
        }, false);

        eventSource.addEventListener( 'error', ev => {
            if ( this.crossOriginCheckOk_( ev ) ) {
                const id = StreamManager.extractEventSourceStreamIdFromUrl_( ev.target.url );
                log.warn("Event source: 'wica stream'  - error event on stream with id: " + id );
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
            log.warn( "Event source: 'stream' unexpected event origin." );
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