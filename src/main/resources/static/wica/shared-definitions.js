/**
 * Provides definitions that are used throughout the application.
 * @module
 */

/**
 * Object provides general purpose information about a channel.
 *
 * The properties that are available depends on the underlying data source (eg whether the channel data source
 * is an EPICS channel). The type property is always present.
 *
 * @typedef module:shared-definitions.WicaChannelMetadata
 * @property type {string} - One of: "REAL", "INTEGER", "STRING", "REAL_ARRAY", "INTEGER_ARRAY", "STRING_ARRAY".
 * @property egu {string} -  Engineering Units in which the channel's value will be expressed.
 * @property prec {number} - The precision in which the channel's value will be expressed. Applies only to numeric types.
 * @property hopr {number} - High Operating Range. EPICS channels only.
 * @property lopr {number} - Low Operating Range. EPICS channels only.
 * @property drvh {number} - Drive High Control Limit. EPICS channels only.
 * @property drvl {number} - Drive Low Control Limit. EPICS channels only.
 * @property hihi {number} - Upper Alarm Limit. EPICS channels only.
 * @property lolo {number} - Lower Alarm Limit. EPICS channels only.
 * @property high {number} - Upper Warning Limit. EPICS channels only.
 * @property low {number} - Lower Warning Limit. EPICS channels only.
 */

/**
 * Object providing the current value of the channel together with, optionally, the timestamp at which
 * the value snapshot was obtained and the alarm status.
 *
 * @typedef module:shared-definitions.WicaChannelValue
 * @property val {string|null} - JSON String representation of the current value. Set to NULL if the channel's
 *     data source is offline, or otherwise unavailable.
 * @property sevr {number} - [Alarm Severity] -  Present if the WicaStreamProperty 'includeAlarmState' is true. The
 *    following values are defined (0 = No Alarm; 1 = Minor Alarm, 2 = Major Alarm)
 * @property ts {string} - [Timestamp] - present if the WicaStreamProperty 'includeTimeStamp' is true.
 */

/**
 * JS Object that defines the attributes of a wica-aware HTML element that are used by the
 * {@link module:document-stream-connector.DocumentStreamConnector DocumentStreamConnector} when communicating
 * with the Wica backend server. Format: JS string.
 *
 * @property {string} channelName="data-wica-channel-name" - The name of the element attribute which specifies
 *     the wica channel name. This is the minimum information that must be present for an element to be
 *     considered "wica-aware".
 *
 * @property {string} channelProperties="data-wica-channel-properties" - The name of the element attribute which
 *     specifies the wica channel properties. Format: JSON String, representing JS
 *     {@link module:shared-definitions.WicaChannelProperties WicaChannelProperties} object.
 *
 * @property {string} channelStreamState="data-wica-channel-stream-state" - The name of the element attribute
 *     which is set to reflect the state of the connection to the wica server's data stream. Format: JS string
 *     with possible values: [ "disconnected", "connected" ].
 *
 * @property {string} channelConnectionState="data-wica-channel-connection-state" - The name of the element
 *     attribute which is set to reflect the state of the connection between the wica server and the wica
 *     channel's data source. Format: JS string with possible values: ["connecting-N", "opened-X",
 *     "closed-X"], where N represents the incrementing count of connection attempts and X represents the
 *     stream ID assigned by the server.
 *
 * @property {string} channelMetadata="data-wica-channel-metadata" - The name of the element attribute which is
 *     set to reflect the metadata obtained most recently from the wica channel. Format: JSON String, representing
 *     JS {@link module:shared-definitions.WicaChannelMetadata WicaChannelMetadata} object.
 *
 * @property {string} channelValueArray="data-wica-channel-value-array" - The name of the attribute which
 *     is set to reflect the most recently obtained values from the wica channel. Format: JSON String,
 *     representing JS Array of {@link module:shared-definitions.WicaChannelValue WicaChannelValue} objects.
 *
 * @property {string} channelValueLatest="data-wica-channel-value-latest" - The name of the attribute which is
 *     set to reflect the last value obtained from the channel. Format: JSON String, representing JS
 *     {@link module:shared-definitions.WicaChannelValue WicaChannelValue} object.
 *
 * @property {string} channelAlarmState="data-wica-channel-alarm-state" - The attribute which is set to reflect
 *     the alarm status most recently obtained from the channel. Format: JS string with possible values:
 *     ["NO_ALARM", "MINOR_ALARM", "MAJOR_ALARM", "INVALID_ALARM" ].
 */
export const WicaElementConnectionAttributes = Object.freeze ({
    channelName:            "data-wica-channel-name",
    channelProperties:      "data-wica-channel-props",
    streamState:            "data-wica-channel-stream-state",
    channelConnectionState: "data-wica-channel-connection-state",
    channelMetadata:        "data-wica-channel-metadata",
    channelValueArray:      "data-wica-channel-value-array",
    channelValueLatest:     "data-wica-channel-value-latest",
    channelAlarmState:      "data-wica-channel-alarm-state"
} );

/**
 * Object defining the attributes of a wica-aware HTML element that are used to render its visual state. As used,
 * for example, by the {@link module:document-text-renderer.DocumentTextRenderer DocumentTextRenderer}.
 *
 * @property {string} [rendererTooltips="data-wica-renderer-tooltips"] - The attribute which defines the tooltip
 *     that will be displayed when the browser's cursor hovers over the element. When not defined the channel
 *     name will be used.
 * @property {string} [rendererProperties="data-wica-renderer-props"] - The attribute which defines other general
 *     purpose properties which will affect the way the element is rendered.
 *     See {@link module:shared-definitions.WicaElementRenderingProperties WicaElementRenderingProperties}.
 */
export const WicaElementRenderingAttributes = Object.freeze ({
    rendererTooltips:   "data-wica-renderer-tooltips",
    rendererProperties: "data-wica-renderer-props"
} );

/**
 * Object defining properties needed for rendering the visual state of a wica-aware element, together with
 * their default values.
 *
 * @property {boolean} [disable=false] - Disables the rendering for this channel.
 * @property {number} [prec=8] - The precision to be used when rendering numeric information in fixed decimal
 *     point format.
 * @property {boolean} [exp=false] - Whether numeric information should be rendered in exponential format (when
 *     set TRUE) or in fixed decimal point format (when set FALSE).
 */
export const WicaElementRenderingProperties = Object.freeze ({
    disable: false,
    exp: false,
    prec: 8
} );

/**
 * Object defining the properties supported by a WicaStream, together with their default values.
 *
 * @property {number} [heartbeatInterval=15000] - The interval in milliseconds between heartbeat messages.
 * @property {number} [channelValueUpdateInterval=100] The interval in milliseconds between channel value update messages.
 * @property {boolean} [includeAlarmState=true] - Whether alarm information should be included in channel
 *     value updates. Needed if the visual state of the element should change when in the alarm state.
 * @property {boolean} [includeTimeStamp=false] - Whether timestamp information should be included in channel
 *     value updates. Needed for time plots.
 */
export const WicaStreamProperties = Object.freeze ({
    heartBeatInterval: 15000,
    channelValueUpdateInterval: 100,
    includeAlarmState: true,
    includeTimeStamp: false
} );

/**
 * Object defining the properties supported by a WicaChannel, together with their default values.
 *
 * @property {number} [prec=8] - The precision to be used when sending numeric information.
 */
export const WicaChannelProperties = Object.freeze ({
    disable: false,
    exp: false,
    prec: 8
} );
