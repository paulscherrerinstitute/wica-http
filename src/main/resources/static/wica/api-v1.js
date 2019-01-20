/**
 * @module
 * @desc APT
 */
export {
    wicaStreamCreate
}

import {WicaStreamManager} from './stream-manager.js'


/**
 * Returns an object that can be used to initiate a new Wica Server Sent Event (SSE) data stream from
 * a Wica REST Server and to subsequently handle the received events.
 *
 * The returned object will remain in a dormant state until triggered by a call to the activate method.
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

export function wicaStreamCreate( serverUrl, streamConfiguration, connectionHandlers, messageHandlers, options )
{
    return new WicaStreamManager( serverUrl, streamConfiguration, connectionHandlers, messageHandlers, options )
}

/**
 * Returns an object which can be used for XXX...
 *
 * @param serverUrl
 * @returns {DocumentStreamManager}
 */
export function wicaDocumentManagerCreate( serverUrl )
{
    return new DocumentStreamManager( serverUrl );
}
