export {
    wicaStreamCreate
}

import {WicaStreamManager} from './stream-manager.js'

/**
 * Creates an object for handling the Server-Sent-Event (SSE) data stream with the Wica backend server.
 *
 * Note: The returned manager will remain in an inactive state until it is activated by a call to
 *       its activate method.
 *
 * @param {string} serverUrl - the url of the server hosting the Wica Stream.
 *
 * @param {Object} streamConfiguration - The configuration of the stream which should include the
 *        stream properties (if any), the name of the channels, and associated channel properties.
 *
 * @param {Object} connectionHandlers - Callbacks for handling connection state changes.
 * @param {callback} connectionHandlers.streamOpened - Called when the stream is opened (= not yet connected)
 * @param {callback} connectionHandlers.streamConnect - Called when the stream sucessfully connects.
 * @param {callback} connectionHandlers.streamClosed - Called when the stream disconnects.
 *
 * @param {Object} messageHandlers - Callbacks for handling data received from the SSE stream.
 * @param {callback} messageHandlers.channelMetadataUpdated -  Called when stream metadata information is received.
 * @param {callback} connectionHandlers.channelValuesUpdated - Called when stream value information is received.
 *
 * @param {Object} options - Object providing miscellaneous configuration options.
 * @param {number} options.streamReconnectIntervalInSeconds - how often the manager should attempt to reconnect
 *                                                            with the server if there is a communication outage.
 *
 * @param {number} options.streamTimeoutIntervalInSeconds - how often the stream's heartbeat signal need's
 *                                                         to be received before the channel connection will
 *                                                         be deemed to have failed.
 *
 * @param {boolean} options.crossOriginCheckEnabled - whether this manager should perform a CORS check.
 */
function wicaStreamCreate( serverUrl, streamConfiguration, connectionHandlers, messageHandlers, options )
{
    return new WicaStreamManager( serverUrl, streamConfiguration, connectionHandlers, messageHandlers, options )
}

