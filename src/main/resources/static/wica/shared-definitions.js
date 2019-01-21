/**
 * Provides definitions that are used throughout the application.
 * @module
 */

/**
 * Object defining the attributes of a wica-aware HTML element that are used by the
 * {@link module:document-stream-connector.DocumentStreamConnector DocumentStreamConnector} when communicating
 * with the Wica backend server.
 *
 * @property {string} channelName="data-wica-channel-name" - The attribute which defines the channel name. This
 *     is the minimum information that must be present for the element to be considered "wica-aware'.
 * @property {string} [channelProperties="data-wica-channel-properties"] - The attribute which defines the channel
 *     properties. See {@link module:shared-definitions.WicaChannelProperties WicaChannelProperties}.
 * @property {string} [channelStreamState="data-wica-channel-stream-state"] - The attribute which reflects the state
 *     of the connection to the backend server's data stream.
 * @property {string} [channelConnectionState="data-wica-channel-connection-state"] - The attribute which reflects
 *     the state of the connection between the backend server and the channel's data source.
 * @property {string} [channelMetadata="data-wica-channel-metadata"] - The attribute which reflects the metadata
 *     obtained from the channel.
 * @property {string} [channelValueArray="data-wica-channel-value-array"] - The attribute which reflects the values
 *     most recently obtained from the channel.
 * @property {string} [channelValueLatest="data-wica-channel-value-latest"] - The attribute which reflects the last
 *     value obtained from the channel.
 * @property {string} [channelAlarmState="data-wica-channel-alarm-state"] - The attribute which reflects the alarm
 *     status obtained from the channel.
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
 *     See @link module:shared-definitions.WicaElementRenderingProperties WicaElementRenderingProperties}.
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
