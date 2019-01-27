/**
 * Provides definitions that are shared throughout the application.
 * @module
 */

/**
 * Provides a type definition for a JS string which defines the name of a channel.
 *
 * @typedef module:shared-definitions.WicaChannelName
 */

/**
 * Provides a type definition for a JS CustomEvent that is fired when a wica-aware element is updated with new
 * information from the wica stream.
 *
 * @typedef module:shared-definitions.OnWica
 *
 * @property {}
 * @property {detail.streamState} -
 * @property {detail.channelName} -
 * @property {detail.channelProperties} -
 * @property {detail.channelConnectionState} -
 * @property {detail.channelValueArray} -
 * @property {detail.channelValueLatest} -
 * @property {detail.channelAlarmState} -
 *
 *
 *     whose details depend on the data source.
 *     See {@link module:shared-definitions.WicaChannelMetadataOther WicaChannelMetadataOther},
 *     and {@link module:shared-definitions.WicaChannelMetadataEpics WicaChannelMetadataEpics}.
*/

/**
 * Provides a union type definition for the filtering possibilities that may be configured on a wica channel.
 *
 * See {@link module:shared-definitions.WicaFilterTypeAllValue WicaFilterTypeAllValue},
 *     {@link module:shared-definitions.WicaFilterTypeLatestValue WicaFilterTypeLatestValue},
 *     {@link module:shared-definitions.WicaFilterTypeFixedSampler WicaFilterTypeFixedSampler},
 *     {@link module:shared-definitions.WicaFilterTypeRateLimiter WicaFilterTypeRateLimiter},
 *     and {@link module:shared-definitions.WicaFilterTypeChangeFilterer WicaFilterTypeChangeFilterer}.
 *
 * @typedef module:shared-definitions.WicaFilterType
 */

/**
 * Provides a type definition for a filter that "does nothing", passing through all values obtained from the
 * channel's data source.
 *
 * @typedef module:shared-definitions.WicaFilterTypeAllValue
 * @property {string} filterType - "none" - the string literal that configures this type of filter.
 */

/**
 * Provides a type definition for a filter that passes through only the latest values received from the
 * channel during the wica server's previous value update sampling time window.
 *
 * @typedef module:shared-definitions.WicaFilterTypeLatestValue
 * @property {string} filterType - "latest" - the string literal that configures this type of filter.
 * @property {number} numSamples - The maximum number of values to pass through the filter on each update cycle.
 */

/**
 * Provides a type definition for a filter that passes through values obtained from the channel's data source
 * on a fixed one-in-N sampling basis.
 *
 * @typedef module:shared-definitions.WicaFilterTypeFixedSampler
 * @property {string} filterType - "fixed" - the string literal that configures this type of filter.
 * @property {number} cycleLength - The sampling cycle length.
 */

/**
 * Provides a type definition for a filter that passes through values obtained from the channel's data source based
 * on a minimum time interval between successive samples.
 *
 * @typedef module:shared-definitions.WicaFilterTypeRateLimiter
 * @property {string} filterType - "rate" - the string literal that configures this type of filter.
 * @property {number} ms - The minimum time duration between samples in milliseconds.
 */

/**
 *  Provides a type definition for a filter that that passes through values every time the input signal makes a
 *  transition whose absolute value exceeds the configured deadband. The filter operates only on channels whose
 *  underlying type is numeric; the information for all other channel types passes through unchanged.
 *
 * @typedef module:shared-definitions.WicaFilterTypeChangeFilterer
 * @property {string} filterType - "changes" - the string literal that configures this type of filter.
 * @property {number} deadband - Defines the absolute change which must occur in the input value in order for
 *     the new value to be passed through the filter.
 */

/**
 * Provides a type definition for a union type which describes the metadata information associated with a wica channel.
 *
 * See {@link module:shared-definitions.WicaChannelMetadataOther WicaChannelMetadataOther},
 * and {@link module:shared-definitions.WicaChannelMetadataEpics WicaChannelMetadataEpics}.
 *
 * @typedef module:shared-definitions.WicaChannelMetadata
 *
 */

/**
 * Provides a type definition to describe the metadata associated with a channel based on a data source with
 * minimal additional information.
 *
 * @typedef module:shared-definitions.WicaChannelMetadataOther
 * @property type {string} - One of: "REAL", "INTEGER", "STRING", "REAL_ARRAY", "INTEGER_ARRAY", "STRING_ARRAY".
 *     This property is always present.
 */

/**
 * Provides a type definition to describe the metadata associated with a channel based on an EPICS data source.
 *
 * The published properties may vary according to the EPICS record that publishes the EPICS channel. A combination
 * of any or all of the following properties is possible.
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
 * Provides a type definition for a JS Object that provides channel value information.
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
 * @property {string} streamState="data-wica-stream-state" - The name of the element attribute which reflects
 *     the state of the connection to the wica server's data stream. Format: JS string literal with possible
 *     values: [ "connect-CCC", "opened-XXX", "closed-XXX" ], where CCC represents the incrementing connection
 *     request counter and XXX the id of the last stream that was opened.
 *
 * @property {string} channelName="data-wica-channel-name" - The name of the element attribute which specifies
 *     the wica channel name. This is the minimum information that must be present for an element to be
 *     considered "wica-aware". Format: JS string literal.
 *
 * @property {string} channelProperties="data-wica-channel-properties" - The name of the element attribute which
 *     specifies the wica channel properties. Format: JSON string literal, representing JS
 *     {@link module:shared-definitions.WicaChannelProperties WicaChannelProperties} object.
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
 *     the alarm status most recently obtained from the channel. Format: JS number literal with possible values:
 *     [ 0 (= "NO_ALARM"), 1 (= "MINOR_ALARM"), 2 (= "MAJOR_ALARM"), 3 (= "INVALID_ALARM") ].
 */
export const WicaElementConnectionAttributes = Object.freeze ({
    streamState:            "data-wica-stream-state",
    channelName:            "data-wica-channel-name",
    channelProperties:      "data-wica-channel-props",
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
 * @property {string} tooltip="data-wica-tooltip" - The name of the attribute which specifies the tooltip to
 *     be displayed when the browser's cursor hovers over the element. When not explicitly set by the developer
 *     the wica channel name will be assigned to this attribute instead. Format: JS string literal.
 *
 * @property {string} renderingProperties="data-wica-rendering-props" - The name of the attribute which provides
 *     other miscellaneous properties which affect the way the element is rendered. Format: JSON string literal
 *     representing JS {@link module:shared-definitions.WicaRenderingProperties WicaRenderingProperties}
 *     object.
 */
export const WicaElementRenderingAttributes = Object.freeze ({
    tooltip:             "data-wica-tooltip",
    renderingProperties: "data-wica-rendering-props"
} );

/**
 * JS Object that defines the properties and default values used by the
 * {@link module:document-text-renderer.DocumentTextRenderer DocumentTextRenderer} when rendering the element's
 * visual state.
 *
 * @property {boolean} [disable=false] - Disables rendering for this channel.
 * @property {string} [units=""] - The units to be displayed when rendering numeric information. When this
 *     property is specified it will be used. When not specified an attempt will be made to obtain the units
 *     from the metadata.
 * @property {boolean} [exp=false] - Sets the rendering format for channels which return numeric data. Possible
 *     values: [true (use exponential format, eg 1.27E-1), false (use fixed decimal point format, eg 0.127)].
 * @property {number} [prec=8] - The precision (= number of digits after the decimal point) to be used for
 *     channels which return numeric data.
 */
export const WicaRenderingProperties = Object.freeze ({
    disable: false,
    exp: false,
    prec: 8,
    units: ""
} );

/**
 * JS Object that defines the properties and default values supported by a WicaStream.
 *
 * @property {number} [heartbeatInterval=15000] - The interval in milliseconds between heartbeat messages.
 * @property {number} [channelValueUpdateInterval=100] The interval in milliseconds between channel value
 *     update messages.
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
 * JS Object that defines the properties and supported by a WicaChannel and the default values
 *
 * @property {number} [prec=8] - The precision (= number of digits after the decimal point) to be used when
 *     sending numeric information.
 * @property {WicaChannelFilterTypes} [filterType=WicaFilterTypeAllValue] - The type of filtering to be used on the
 *     channel. See {@link module:shared-definitions.WicaChannelFilterType WicaChannelFilterType}.
 */
export const WicaChannelProperties = Object.freeze ({
    prec: 8,
    filterType: "latest",
    numSamples: 1
} );
