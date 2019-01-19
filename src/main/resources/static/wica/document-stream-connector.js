/**
 * @module
 * @desc Provides support for updating the current document with live information from the data sources on the backend.
 */
console.debug( "Executing script in document-stream-connector.js module...");

import {WicaStreamManager} from './stream-manager.js'
import * as DocumentUtilities from './document-utils.js'


//const WICA_HOST = "https://gfa-wica.psi.ch";
const WICA_HOST = "https://gfa-wica-dev.psi.ch";


/**
 * Attributes of a wica-aware HTML element that are used when communicating with the Wica backend server.
 *
 * @typedef WicaChannelConnectionAttributes
 * @property {string} CHANNEL_NAME - "data-wica-channel-name" - The attribute which defines the channel name.
 * @property {string} CHANNEL_PROPERTIES - "data-wica-channel-properties" - The attribute which defines the channel
 *     properties.
 * @property {string} CHANNEL_STREAM_STATE - "data-wica-stream-state" - The attribute which reflects the state of the
 *     connection to the backend server data stream.
 * @property {string} CHANNEL_CONNECTION_STATE - "data-wica-channel-connection-state" - The attribute which reflects
 *     the state of connection between the backend server and the channel's data source.
 * @property {string} CHANNEL_METADATA - "data-wica-channel-metadata" - The attribute which reflects the metadata
 *     obtained from the channel.
 * @property {string} CHANNEL_VALUE_ARRAY - "data-wica-channel-value-array" - The attribute which reflects the
 *     values most recently obtained from the channel.
 * @property {string} CHANNEL_VALUE_LATEST - "data-wica-channel-value-latest" - The attribute which reflects the last
 *     value obtained from the channel.
 * @property {string} CHANNEL_ALARM_STATE - "data-wica-channel-alarm-state" - The attribute which reflects whether
 *     the latest alarm status obtained from the channel.
 */
export const wicaChannelConnectionAttributes = { CHANNEL_NAME:             "data-wica-channel-name",
                                                 CHANNEL_PROPERTIES:       "data-wica-channel-properties",
                                                 CHANNEL_STREAM_STATE:     "data-wica-channel-stream-state",
                                                 CHANNEL_CONNECTION_STATE: "data-wica-channel-connection-state",
                                                 CHANNEL_METADATA:         "data-wica-channel-metadata",
                                                 CHANNEL_VALUE_ARRAY:      "data-wica-channel-value-array",
                                                 CHANNEL_VALUE_LATEST:     "data-wica-channel-value-latest",
                                                 CHANNEL_ALARM_STATE:      "data-wica-channel-alarm-state" };
/**
 * Provides real-time updates on the attributes of one or more of the current document's
 * wica-aware elements based on information streamed from a Wica backend data server.
 */
export class DocumentStreamConnector
{
    /**
     * Constructs a new instance.
     *
     * The returned object will remain in a dormant state until triggered by a call to the activate method.

     * @param {string} streamServerUrl - the URL of the backend server from whom information is to be obtained.
     * @param {StreamProperties} streamProperties - The properties of the stream that will be created to obtain the
     *     required information from the data sources.
     * @param {WicaChannelConnectionAttributes} wicaChannelConnectionAttributes - The names of the wica-aware
     *     element attributes that are used in the communication process.
     */
    constructor( streamServerUrl, streamProperties, wicaChannelConnectionAttributes )
    {
        this.streamServerUrl = streamServerUrl;
        this.streamProperties = streamProperties;
        this.wicaChannelConnectionAttributes = wicaChannelConnectionAttributes;
        this.lastOpenedStreamId = 0;
        this.streamConnectionHandlers = {};
        this.streamMessageHandlers = {};
    }

    /**
     * Scans the current document for wica-aware elements, creates a stream on the Wica backend server to obtain
     * information for each element's data source, sets up handlers to update each element's attributes on
     * the basis of the received information.
     */
    activate()
    {
        this.configureStreamConnectionHandlers_( this.wicaChannelConnectionAttributes.CHANNEL_STREAM_STATE );

        this.configureStreamMessageHandlers_( this.wicaChannelConnectionAttributes.CHANNEL_METADATA,
                                              this.wicaChannelConnectionAttributes.CHANNEL_VALUE_ARRAY,
                                              this.wicaChannelConnectionAttributes.CHANNEL_VALUE_LATEST,
                                              this.wicaChannelConnectionAttributes.CHANNEL_CONNECTION_STATE,
                                              this.wicaChannelConnectionAttributes.CHANNEL_ALARM_STATE );

        this.createStream_( this.wicaChannelConnectionAttributes.CHANNEL_NAME, this.wicaChannelConnectionAttributes.CHANNEL_PROPERTIES );
        this.activateStream_();
    }

    /**
     * Shuts down the service offered by this class.
     */
    shutdown()
    {
        this.wicaStreamManager.shutdown();
    }

    /**
     * Configures the document stream connection handling object to deal with the connection-related events generated
     * by the document's stream manager.
     *
     * @private
     * @param {string} streamConnectionStateAttribute - The attribute whose value is to be updated when the stream
     *     manager connects / is opened / is closed.
     */
    configureStreamConnectionHandlers_( streamConnectionStateAttribute )
    {
        this.streamConnectionHandlers.streamConnect = () => {
            console.log("Event stream connect" );
            DocumentUtilities.findWicaElements().forEach(element => element.setAttribute( streamConnectionStateAttribute, "connecting"));
        };

        this.streamConnectionHandlers.streamOpened = (id) => {
            console.log("Event stream opened: " + id);
            console.log("Setting wica stream state on all html elements to: 'opened'");
            DocumentUtilities.findWicaElements().forEach(element => element.setAttribute( streamConnectionStateAttribute, "opened-" + id));
            this.lastOpenedStreamId = id;
        };

        this.streamConnectionHandlers.streamClosed = (id) => {
            console.log("Event stream closed: " + id);
            if (id === this.lastOpenedStreamId) {
                console.log("Setting wica stream state on all html elements to: 'closed'");
                DocumentUtilities.findWicaElements().forEach(element => element.setAttribute( streamConnectionStateAttribute, "closed-" + id));
            } else {
                console.log("Wica stream state on all html elements will be left unchanged as a newer event source is already open !");
            }
        };
    }

    /**
     * Configures the document stream connection handling object to deal with the message-related events generated
     * by the document's stream manager.

     * @param {string} channelMetadataAttribute
     * @param {string} channelValueArrayAttribute
     * @param {string} channelValueLatestAttribute
     * @param {string} channelConnectionStateAttribute
     * @param {string} channelAlarmStateAttribute
     * @private
     */
    configureStreamMessageHandlers_( channelMetadataAttribute, channelValueArrayAttribute,
                                     channelValueLatestAttribute, channelConnectionStateAttribute,
                                     channelAlarmStateAttribute )
    {
        this.streamMessageHandlers.channelMetadataUpdated = metadataObject => {
            console.log("Event stream received new channel metadata map.");

            // Go through all the elements in the update object and assign
            // each element's metadata to the element's "data-wica-channel-metadata"
            // attribute.
            Object.keys(metadataObject).forEach((key) => {
                const channelName = key;
                const channelMetadata = metadataObject[key];
                const elements = DocumentUtilities.findWicaElementsWithChannelName(channelName);
                const metadataAsString = JSON.stringify(channelMetadata);
                elements.forEach(ele => {
                    ele.setAttribute( channelMetadataAttribute, metadataAsString);
                    console.log("Metadata updated: " + metadataAsString);
                });
            });
        };

        this.streamMessageHandlers.channelValuesUpdated = valueObject => {
            //console.log( "WicaStream received new channel value map.");

            // Go through all the elements in the update object and assign
            // each element's value to the element's "data-wica-channel-value-latest"
            // and "data-wica-channel-value-array" attributes. Update the
            // "data-wica-channel-connection-state" attribute to reflect the
            // channel's underlying connection state.
            Object.keys(valueObject).forEach((key) => {
                const channelName = key;
                const channelValueArray = valueObject[key];
                const elements = DocumentUtilities.findWicaElementsWithChannelName(channelName);
                const channelValueArrayAsString = JSON.stringify(channelValueArray);

                if (!Array.isArray(channelValueArray)) {
                    console.warn("Stream Error: not an array !");
                    return;
                }
                const channelValueLatest = channelValueArray.pop();
                const channelValueLatestAsString = JSON.stringify(channelValueLatest);
                const channelConnectionState = (channelValueLatest.val === null) ? "disconnected" : "connected";
                elements.forEach(ele => {
                    ele.setAttribute( channelValueArrayAttribute, channelValueArrayAsString);
                    ele.setAttribute( channelValueLatestAttribute, channelValueLatestAsString);
                    ele.setAttribute( channelConnectionStateAttribute, channelConnectionState);
                    ele.setAttribute( channelAlarmStateAttribute, channelValueLatest.sevr);
                    //console.log("Value updated: " + channelValueLatest);
                });
            });
        };
    }

    /**
     * Creates the stream based on the wica-aware elements in the current document.
     *
     * @private
     * @param channelNameAttribute
     * @param channelPropertiesAttribute
     * @private
     */
    createStream_( channelNameAttribute, channelPropertiesAttribute )
    {
        // Look for all wica-aware elements in the current page
        const wicaElements = DocumentUtilities.findWicaElements();
        console.log("Number of Wica elements found: ", wicaElements.length);

        // Create an array of the associated channel names
        const channels = [];
        wicaElements.forEach(function (widget) {
            const channelName = widget.getAttribute( channelNameAttribute );
            if (widget.hasAttribute( channelPropertiesAttribute )) {
                const channelProps = widget.getAttribute( channelPropertiesAttribute );
                channels.push({"name": channelName, "props": JSON.parse( channelProps ) });
            } else {
                channels.push({"name": channelName});
            }
        });

        const streamOptions = {
            streamReconnectIntervalInSeconds: 15,
            streamTimeoutIntervalInSeconds: 20,
            crossOriginCheckEnabled: false,
        };

        const streamConfiguration = { "channels": channels, "props": this.streamProperties };
        this.wicaStreamManager = new WicaStreamManager( this.streamServerUrl, streamConfiguration, this.connectionHandlers, this.messageHandlers, streamOptions );

    }

    activateStream_()
    {
        this.wicaStreamManager.activate();
    }

}
