
/**
 * Object defining the attributes of a wica-aware HTML element that are used when communicating with the Wica backend server.
 *
 * @typedef WicaElementConnectionAttributes
 * @property {string} channelName - "data-wica-channel-name" - The attribute which defines the channel name.
 * @property {string} channelProperties - "data-wica-channel-properties" - The attribute which defines the channel
 *     properties.
 * @property {string} channelStreamState - "data-wica-stream-state" - The attribute which reflects the state of the
 *     connection to the backend server's data stream.
 * @property {string} channelConnectionState - "data-wica-channel-connection-state" - The attribute which reflects
 *     the state of the connection between the backend server and the channel's data source.
 * @property {string} channelMetadata - "data-wica-channel-metadata" - The attribute which reflects the metadata
 *     obtained from the channel.
 * @property {string} channelValueArray - "data-wica-channel-value-array" - The attribute which reflects the values
 *     most recently obtained from the channel.
 * @property {string} channelValueLatest - "data-wica-channel-value-latest" - The attribute which reflects the last
 *     value obtained from the channel.
 * @property {string} channelAlarmState - "data-wica-channel-alarm-state" - The attribute which reflects the alarm
 *     status obtained from the channel.
 */
const WicaElementConnectionAttributes = {
    channelName:            "data-wica-channel-name",
    channelProperties:      "data-wica-channel-properties",
    channelStreamState:     "data-wica-channel-stream-state",
    channelConnectionState: "data-wica-channel-connection-state",
    channelMetadata:        "data-wica-channel-metadata",
    channelValueArray:      "data-wica-channel-value-array",
    channelValueLatest:     "data-wica-channel-value-latest",
    channelAlarmState:      "data-wica-channel-alarm-state"
};

/**
 * Object defining the attributes of a wica-aware HTML element that are used when rendering its visual state.
 *
 * @typedef WicaElementRenderingAttributes
 * @property {string} channelTooltips - "data-wica-channel-tooltips" - The attribute which defines the channel's
 *     tooltips.
 * @property {string} channelRenderingProperties - "data-wica-channel-rendering-props" - The attribute which
 *     defines properties which will affect the way it is rendered.
 */
const WicaElementRenderingAttributes = {
    channelTooltips:            "data-wica-channel-tooltips",
    channelRenderingProperties: "data-wica-channel-rendering-props",
};

/**
 * Object defining the properties supported by the default wica element renderer.
 *
 * @typedef WicaElementChannelRenderingProperties
 * @property {boolean} disable - false - Disables the default rendering for this channel.
 * @property {boolean} exp - false - Whether numeric information should be rendered in exponential format.
 * @property {string} prec - 8 - The precision to be used when rendering numeric information.
 */
const WicaElementChannelRenderingProperties = {
    disable: false,
    exp: false,
    prec: 8
}

/**
 * Object defining the properties supported by a WicaStream.
 *
 * @typedef WicaStreamProperties
 * @property {number} heartbeatInterval - 15000 - The interval in milliseconds to be used between the
 *     sending of successive heartbeat messages.
 * @property {number} channelValueUpdateInterval 100 - The interval in milliseconds to be used between
 *     the sending of successive channel value updates.
 * @property {boolean} includeAlarmState - true - Whether alarm information should be included in channel
 *     value updates. Needed if the visual state of the element should change when in the alarm state.
 * @property {boolean} includeTimeStamp - false - Whether timestamp information should be included in channel
 *     value updates. Needed for time plots.
 */
const WicaStreamProperties = {
    heartBeatInterval: 15000,
    channelValueUpdateInterval: 100,
    includeAlarmState: true,
    includeTimeStamp: false
};

/**
 * Object defining the properties supported by a WicaChannel.
 *
 * @typedef WicaChannelProperties
 * @property {number} prec - 8 - The precision to be used when sending numeric information.
 */
const WicaChannelProperties = {
    disable: false,
    exp: false,
    prec: 8
};