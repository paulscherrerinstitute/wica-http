/**
 * Provides definitions that are shared throughout the application.
 * @module
 */

/**
 * JS Object that provides a map of channels and their associated metadata.
 *
 * @typedef module:shared-definitions.WicaChannelMetadataMap
 * @property {Object} - The map.
 * @property {Object.key[]} - The channel names.
 * @property {Object.value[]} - The channel metadata.
 *     See {@link module:shared-definitions.WicaChannelMetadata WicaChannelMetadata}
 */

/**
 * JS Object that provides channel metadata information for systems with diverse types of data channel.
 *
 * @typedef module:shared-definitions.WicaChannelMetadata
 * @property {WicaChannelMetadataOther|WicaChannelMetadataEpics} - One or more metadata properties
 *     whose details depend on the data source.
 *     See {@link module:shared-definitions.WicaChannelMetadataOther WicaChannelMetadataOther},
 *     and {@link module:shared-definitions.WicaChannelMetadataEpics WicaChannelMetadataEpics}.
 */

/**
 * JS Object that provides channel metadata information for a data source with minimal additional information.
 *
 * @typedef module:shared-definitions.WicaChannelMetadataOther
 * @property type {string} - One of: "REAL", "INTEGER", "STRING", "REAL_ARRAY", "INTEGER_ARRAY", "STRING_ARRAY".
 *     This property is always present.
 */

/**
 * JS Object that provides channel metadata for an EPICS IOC data source.
 *
 * The available properties may vary according to the EPICS record that provides the EPICS channel.
 *
 * @typedef module:shared-definitions.WicaChannelMetadataEpics
 * @property type {string} - One of: "REAL", "INTEGER", "STRING", "REAL_ARRAY", "INTEGER_ARRAY", "STRING_ARRAY".
 * @property egu {string} -  Engineering Units in which the channel's value will be expressed.
 * @property prec {number} - The precision in which the channel's value will be expressed. Applies only to numeric types.
 * @property hopr {number} - High Operating Range.
 * @property lopr {number} - Low Operating Range.
 * @property drvh {number} - Drive High Control Limit.
 * @property drvl {number} - Drive Low Control Limit.
 * @property hihi {number} - Upper Alarm Limit.
 * @property lolo {number} - Lower Alarm Limit.
 * @property high {number} - Upper Warning Limit.
 * @property low {number} - Lower Warning Limit.
 */

/**
 * JS Object that provides channel value information.
 *
 * The value information includes the raw channel value, the timestamp at which the value was obtained, and the
 * channel alarm status.
 *
 * @typedef module:shared-definitions.WicaChannelValue
 *
 * @property val {string|null} - JSON String representation of the current value. Set to NULL if the channel's
 *     data source is offline, or otherwise unavailable.
 *
 * @property sevr {number} - [Alarm Severity] -  Present if the WicaStreamProperty 'includeAlarmInfo' is true. The
 *    following values are defined (0 = No Alarm; 1 = Minor Alarm, 2 = Major Alarm)
 *
 * @property ts {string} - [Timestamp] - present if the WicaStreamProperty 'includeTimeStamp' is true.
 */

/**
 * JS Object that defines the HTML element attributes used by the
 * {@link module:document-stream-connector.DocumentStreamConnector DocumentStreamConnector} when communicating
 * with the Wica server.
 *
 * @property {string} channelName="data-wica-channel-name" - The name of the element attribute which specifies
 *     the wica channel name. This is the minimum information that must be present for an element to be
 *     considered "wica-aware". Format: JS string literal.
 *
 * @property {string} channelProperties="data-wica-channel-properties" - The name of the element attribute which
 *     specifies the wica channel properties. Format: JSON string literal, representing JS
 *     {@link module:shared-definitions.WicaChannelProperties WicaChannelProperties} object.
 *
 * @property {string} channelStreamState="data-wica-channel-stream-state" - The name of the element attribute
 *     which reflects the state of the connection to the wica server's data stream. Format: JS string
 *     literal with possible values: [ "disconnected", "connected" ].
 *
 * @property {string} channelConnectionState="data-wica-channel-connection-state" - The name of the element
 *     attribute which reflects the state of the connection between the wica server and the wica
 *     channel's data source. Format: JS string literal with possible values: ["connecting-N", "opened-X",
 *     "closed-X"], where N represents the incrementing count of connection attempts and X represents the
 *     stream ID assigned by the server.
 *
 * @property {string} channelMetadata="data-wica-channel-metadata" - The name of the element attribute which
 *     reflects the metadata obtained most recently from the wica channel. Format: JSON string literal,
 *     representing JS {@link module:shared-definitions.WicaChannelMetadata WicaChannelMetadata} object.
 *
 * @property {string} channelValueArray="data-wica-channel-value-array" - The name of the attribute which
 *     reflects the most recently obtained values from the wica channel. Format: JSON string literal,
 *     representing JS Array of {@link module:shared-definitions.WicaChannelValue WicaChannelValue} objects.
 *
 * @property {string} channelValueLatest="data-wica-channel-value-latest" - The name of the attribute which is
 *     set to reflect the last value obtained from the channel. Format: JSON string literal, representing JS
 *     {@link module:shared-definitions.WicaChannelValue WicaChannelValue} object.
 *
 * @property {string} channelAlarmState="data-wica-channel-alarm-state" - The name of the attribute which reflects
 *     the alarm status most recently obtained from the channel. Format: JS string literal with possible values:
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
 * JS Object that defines the HTML element attributes used by the
 * {@link module:document-text-renderer.DocumentTextRenderer DocumentTextRenderer} when rendering the element's
 * visual state.
 *
 * @property {string} rendererTooltips="data-wica-renderer-tooltips" - The name of the attribute which
 *     specifies the tooltip to be displayed when the browser's cursor hovers over the element.
 *     When not explicitly set by the developer the wica channel name will be assigned to this
 *     attribute instead. Format: JS string literal.
 *
 * @property {string} rendererProperties="data-wica-renderer-props" - The name of the attribute which provides
 *     other miscellaneous properties which affect the way the element is rendered. Format: JSON string literal
 *     representing JS {@link module:shared-definitions.WicaRendererProperties WicaRendererProperties}
 *     object.
 */
export const WicaElementRenderingAttributes = Object.freeze ({
    rendererTooltips:   "data-wica-renderer-tooltips",
    rendererProperties: "data-wica-renderer-props"
} );

/**
 * JS Object that defines the properties and default values used by the
 * {@link module:document-text-renderer.DocumentTextRenderer DocumentTextRenderer} when rendering the element's
 * visual state.
 *
 * @property {boolean} [disable=false] - Disables the rendering for this channel.
 * @property {number} [prec=8] - The precision to be used when rendering numeric information in fixed decimal
 *     point format.
 * @property {boolean} [exp=false] - Whether numeric information should be rendered in exponential format (when
 *     set TRUE) or in fixed decimal point format (when set FALSE).
 */
export const WicaRendererProperties = Object.freeze ({
    disable: false,
    exp: false,
    prec: 8
} );

/**
 * JS Object that defines the properties and default values supported by a WicaStream.
 *
 * @property {number} [heartbeatInterval=15000] - The interval in milliseconds between heartbeat messages.
 * @property {number} [channelValueUpdateInterval=100] The interval in milliseconds between channel value update messages.
 * @property {boolean} [includeAlarmInfo=true] - Whether alarm information should be included in channel
 *     value updates. Needed if the visual state of the element should change when in the alarm state.
 * @property {boolean} [includeTimeStamp=false] - Whether timestamp information should be included in channel
 *     value updates. Needed for time plots.
 */
export const WicaStreamProperties = Object.freeze ({
    heartBeatInterval: 15000,
    channelValueUpdateInterval: 100,
    includeAlarmInfo: true,
    includeTimeStamp: false
} );

/**
 * JS Object that defines the properties and default values supported by a WicaChannel.
 *
 * @property {number} [prec=8] - The precision to be used when sending numeric information.
 */
export const WicaChannelProperties = Object.freeze ({
    disable: false,
    exp: false,
    prec: 8
} );
