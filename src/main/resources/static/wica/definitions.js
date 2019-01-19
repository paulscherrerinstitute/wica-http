
/**
 * Attributes of a wica-aware HTML element that are used when communicating with the Wica backend server.
 *
 * @typedef WicaElementConnectionAttributes
 * @property {string} CHANNEL_NAME - "data-wica-channel-name" - Defines the channel name.
 * @property {string} CHANNEL_PROPERTIES - "data-wica-channel-properties" - Defines the channel properties.
 * @property {string} CHANNEL_STREAM_STATE - "data-wica-stream-state" - Reflects the state of the connection to the
 *     backend server data stream.
 * @property {string} CHANNEL_CONNECTION_STATE - "data-wica-channel-connection-state" - Reflects the state of
 *     connection between the backend server and the channel's data source.
 * @property {string} CHANNEL_METADATA - "data-wica-channel-metadata" - Reflects the metadata obtained from the channel.
 * @property {string} CHANNEL_VALUE_ARRAY - "data-wica-channel-value-array" - Reflects the values most recently
 *     obtained from the channel.
 * @property {string} CHANNEL_VALUE_LATEST - "data-wica-channel-value-latest" - Reflects the last value obtained from
 *     the channel.
 * @property {string} CHANNEL_ALARM_STATE - "data-wica-channel-alarm-state" - Reflects whether the latest alarm
 * status obtained from the channel.
 */
export const WicaElementConnectionAttributes = {
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
 * Attributes of a wica-aware HTML element that are used when rendering its visual state.
 *
 * @typedef WicaElementRenderingAttributes
 * @property {string} CHANNEL_TOOLTIPS - "data-wica-channel-tooltips" - The attribute which defines the channel's
 *     tooltips.
 * @property {string} CHANNEL_RENDERING_HINTS - "data-wica-channel-rendering-hints" - The attribute which defines
 * properties which will affect the way it is rendered.
 */
export const WicaElementRenderingAttributes = {
    CHANNEL_TOOLTIPS:         "data-wica-channel-tooltips",
    CHANNEL_RENDERING_HINTS:  "data-wica-channel-rendering-hints",
};
