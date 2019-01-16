export {
    wicaStreamCreate
}

import {WicaStreamManager} from './stream-manager.js'


/**
 * @global
 * @typedef StreamProperties
 * @desc An object describing the properties supported by a WicaStream.
 * @property {number} heartbeatInterval - The interval in milliseconds to be used between successive
 *     heartbeat messages.
 * @property {number} channelValueUpdateInterval - The interval in milliseconds to be used between
 *     successive channel value updates.
 * @property {boolean} plotStream - Whether the stream is to be used for plotting purposes (in which
 *     case timestamp information will be provided for every channel value update).
 */

/**
 * @global
 * @typedef ChannelProperties
 * @desc An object describing the properties supported by a WicaChannel.
 * @property {string} prec - The precision to be used when sending numeric information.
 */


/**
 * Returns an object that can be used to initiate a new Wica Server Sent Event (SSE) data stream from
 * a Wica REST Server and to subsequently handle the received events.
 *
 * Note: The returned object will remain in an inactive state until it is activated by a call to
 *       its activate method.
 *
 * @param {string} serverUrl - the url of the server hosting the Wica Stream.
 *
 * @param {Object} streamConfiguration - the configuration of the stream which should include the
 *        stream properties (if any), the name of the channels, and associated channel properties.
 *
 * @param {Object} streamConfiguration.props - the properties of the stream.
 * @param {Array} streamConfiguration.channels - the configuration of each channel in the stream.
 * @param {string} streamConfiguration.channels[x].name - the name of the channel.
 * @param {Object} streamConfiguration.channels[x].props - the properties of the channel.
 *
 * @param {Object} connectionHandlers - callbacks for handling connection state changes.
 * @param {callback} connectionHandlers.streamOpened - called when the stream is opened (= not yet connected).
 * @param {callback} connectionHandlers.streamConnect - called when the stream successfully connects.
 * @param {callback} connectionHandlers.streamClosed - called when the stream disconnects.
 *
 * @param {Object} messageHandlers - callbacks for handling data received from the SSE stream.
 * @param {callback} messageHandlers.channelMetadataUpdated -  called when stream metadata information is received.
 * @param {callback} connectionHandlers.channelValuesUpdated - called when stream value information is received.
 *
 * @param {Object} options - an object providing other miscellaneous configuration options.
 * @param {number} options.streamReconnectIntervalInSeconds - how often the manager should attempt to reconnect
 *                 with the server if there is a communication outage.
 *
 * @param {number} options.streamTimeoutIntervalInSeconds - how often the stream's heartbeat signal need's
 *                 to be received before the channel connection will be deemed to have failed.
 *
 * @param {boolean} options.crossOriginCheckEnabled - whether this manager should perform a CORS check.
 */
function wicaStreamCreate( serverUrl, streamConfiguration, connectionHandlers, messageHandlers, options )
{
    return new WicaStreamManager( serverUrl, streamConfiguration, connectionHandlers, messageHandlers, options )
}

