
/**
 * Object defining the attributes of a wica-aware HTML element that are used when communicating with the Wica backend server.
 *
 * @typedef WicaElementConnectionAttributes
 * @property {string} CHANNEL_NAME - "data-wica-channel-name" - The attribute which defines the channel name.
 * @property {string} CHANNEL_PROPERTIES - "data-wica-channel-properties" - The attribute which defines the channel
 *     properties.
 * @property {string} CHANNEL_STREAM_STATE - "data-wica-stream-state" - The attribute which reflects the state of the
 *     connection to the backend server's data stream.
 * @property {string} CHANNEL_CONNECTION_STATE - "data-wica-channel-connection-state" - The attribute which reflects
 *     the state of the connection between the backend server and the channel's data source.
 * @property {string} CHANNEL_METADATA - "data-wica-channel-metadata" - The attribute which reflects the metadata
 *     obtained from the channel.
 * @property {string} CHANNEL_VALUE_ARRAY - "data-wica-channel-value-array" - The attribute which reflects the values
 *     most recently obtained from the channel.
 * @property {string} CHANNEL_VALUE_LATEST - "data-wica-channel-value-latest" - The attribute which reflects the last
 *     value obtained from the channel.
 * @property {string} CHANNEL_ALARM_STATE - "data-wica-channel-alarm-state" - The attribute which reflects the alarm
 *     status obtained from the channel.
 */
const WicaElementConnectionAttributes = {
    CHANNEL_NAME:             "data-wica-channel-name",
    CHANNEL_PROPERTIES:       "data-wica-channel-properties",
    CHANNEL_STREAM_STATE:     "data-wica-channel-stream-state",
    CHANNEL_CONNECTION_STATE: "data-wica-channel-connection-state",
    CHANNEL_METADATA:         "data-wica-channel-metadata",
    CHANNEL_VALUE_ARRAY:      "data-wica-channel-value-array",
    CHANNEL_VALUE_LATEST:     "data-wica-channel-value-latest",
    CHANNEL_ALARM_STATE:      "data-wica-channel-alarm-state"
};

/**
 * Object defining the attributes of a wica-aware HTML element that are used when rendering its visual state.
 *
 * @typedef WicaElementRenderingAttributes
 * @property {string} CHANNEL_TOOLTIPS - "data-wica-channel-tooltips" - The attribute which defines the channel's
 *     tooltips.
 * @property {string} CHANNEL_RENDERING_PROPERTIES - "data-wica-channel-rendering-props" - The attribute which
 *     defines properties which will affect the way it is rendered.
 */
const WicaElementRenderingAttributes = {
    CHANNEL_TOOLTIPS:             "data-wica-channel-tooltips",
    CHANNEL_RENDERING_PROPERTIES: "data-wica-channel-rendering-props",
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
