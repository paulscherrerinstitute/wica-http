/**
 * Provides definitions that are used throughout the application.
 * @module
 */

/**
 * Object defining the attributes of a wica-aware HTML element that are used by the
 * {@link module:document-stream-connector.DocumentStreamConnector DocumentStreamConnector} when communicating
 * with the Wica backend server.
 *
 * @property {string} channelName="data-wica-channel-name" - The attribute which defines the channel name.
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
export const WicaElementConnectionAttributes = {
    channelName:            "data-wica-channel-name",
    channelProperties:      "data-wica-channel-props",
    streamState:            "data-wica-channel-stream-state",
    channelConnectionState: "data-wica-channel-connection-state",
    channelMetadata:        "data-wica-channel-metadata",
    channelValueArray:      "data-wica-channel-value-array",
    channelValueLatest:     "data-wica-channel-value-latest",
    channelAlarmState:      "data-wica-channel-alarm-state"
};

/**
 * Object defining the attributes of a wica-aware HTML element that are used by the
 * {@link module:document-renderer.DocumentRenderer DocumentRenderer} when rendering its visual state.
 *
 * @property {string} [rendererTooltips="data-wica-renderer-tooltips"] - The attribute which defines the element's
 *     tooltips. When not defined the channel name will be used.
 * @property {string} [rendererProperties="data-wica-renderer-props"] - The attribute which defines other properties
 *     which will affect the way the element is rendered.
 */
export const WicaElementRenderingAttributes = {
    rendererTooltips:   "data-wica-renderer-tooltips",
    rendererProperties: "data-wica-renderer-props"
};

/**
 * Object defining the properties supported by the wica document renderer and their default values.
 *
 * @property {boolean} [disable=false] - Disables the default rendering for this channel.
 * @property {boolean} [exp=false] - Whether numeric information should be rendered in exponential format.
 * @property {number} [prec=8] - The precision to be used when rendering numeric information in fixed decimal point format.
 */
export const WicaElementChannelRenderingProperties = {
    disable: false,
    exp: false,
    prec: 8
};

/**
 * Object defining the properties supported by a WicaStream and their default values.
 *
 * @property {number} [heartbeatInterval=15000] - The interval in milliseconds between heartbeat messages.
 * @property {number} [channelValueUpdateInterval=100] The interval in milliseconds between channel value update messages.
 * @property {boolean} [includeAlarmState=true] - Whether alarm information should be included in channel
 *     value updates. Needed if the visual state of the element should change when in the alarm state.
 * @property {boolean} [includeTimeStamp=false] - Whether timestamp information should be included in channel
 *     value updates. Needed for time plots.
 */
export const WicaStreamProperties = {
    heartBeatInterval: 15000,
    channelValueUpdateInterval: 100,
    includeAlarmState: true,
    includeTimeStamp: false
};

/**
 * Object defining the properties supported by a WicaChannel and their default values.
 *
 * @property {number} [prec=8] - The precision to be used when sending numeric information.
 */
export const WicaChannelProperties = {
    disable: false,
    exp: false,
    prec: 8
};

/**
 * Object defining the properties supported by a WicaStream and their default values.
 *
 * @property heartbeatInterval {number} - The interval in milliseconds between heartbeat messages.
 * @property channelValueUpdateInterval {number} - The interval in milliseconds between channel value update messages.
 * @property includeAlarmState {boolean} -  Whether alarm information should be included in channel
 *     value updates. Needed if the visual state of the element should change when in the alarm state.
 * @property includeTimeStamp {boolean} - Whether timestamp information should be included in channel
 *     value updates. Needed for time plots.
 */
export class WicaStreamProperties2 {

    /**
     * Constructs a new instance, optionally overriding the one or more default property values.
     * @param {number} heartbeatInterval - override value for property A.
     * @param {number} channelValueUpdateInterval - override value for property B.
     * @param {boolean} includeAlarmState - override value for property B.
     * @param {boolean} includeTimeStamp - override value for property B.
     */
    constructor( heartbeatInterval = 15000, channelValueUpdateInterval = 100,includeAlarmState=true, includeTimeStamp=false )
    {
        this.properties = { heartbeatInterval, channelValueUpdateInterval, includeAlarmState, includeTimeStamp  }
    }
    get heartbeatInterval() { return this.properties.heartbeatInterval; }
    get channelValueUpdateInterval() { return this.properties.channelValueUpdateInterval; }
    get includeAlarmState() { return this.properties.includeAlarmState; }
    get includeTimeStamp() { return this.properties.includeTimeStamp; }

}