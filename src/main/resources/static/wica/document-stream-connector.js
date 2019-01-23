/**
 * Provides support for updating the current document with live information from the data sources on the backend.
 * @module
 */
console.debug( "Executing script in document-stream-connector.js module...");

import {StreamManager} from './stream-manager.js'
import * as DocumentUtilities from './document-utils.js'

/**
 * Provides real-time updates to wica-aware elements in the current document based on information streamed
 * from the Wica server on the backend.
 */
export class DocumentStreamConnector
{
    /**
     * Constructs a new instance to work with the specified backend server.
     *
     * The returned object will remain in a dormant state until triggered by a call to the
     *     {@link module:document-stream-connector.DocumentStreamConnector#activate activate} method.
     *
     * @param {!string} streamServerUrl - The URL of the backend server from whom information is to be obtained.
     *
     * @param {!WicaStreamProperties} streamProperties - The properties of the stream that will be created to
     *     obtain the required information from the data sources.
     *     See {@link module:shared-definitions.WicaStreamProperties WicaStreamProperties}.
     *
     * @param {!WicaElementConnectionAttributes} wicaElementConnectionAttributes - The names of the wica-aware
     *     element attributes that are to be used in the communication process.
     *     See {@link module:shared-definitions.WicaElementConnectionAttributes WicaElementConnectionAttributes}.
     */
    constructor( streamServerUrl, streamProperties, wicaElementConnectionAttributes )
    {
        this.streamServerUrl = streamServerUrl;
        this.streamProperties = streamProperties;
        this.wicaElementConnectionAttributes = wicaElementConnectionAttributes;
        this.lastOpenedStreamId = 0;
        this.streamConnectionHandlers = {};
        this.streamMessageHandlers = {};
    }

    /**
     * Scans the current document for wica-aware elements, creates a stream on the Wica backend server to obtain
     * information for each element's data source, sets up handlers to update each element's attributes on
     * the basis of the received information.
     *
     * See also: {@link module:document-stream-connector.DocumentStreamConnector#shutdown shutdown}.
     */
    activate()
    {
        this.configureStreamConnectionHandlers_( this.wicaElementConnectionAttributes.channelConnectionState );

        this.configureStreamMessageHandlers_( this.wicaElementConnectionAttributes.channelMetadata,
                                              this.wicaElementConnectionAttributes.channelValueArray,
                                              this.wicaElementConnectionAttributes.channelValueLatest,
                                              this.wicaElementConnectionAttributes.channelConnectionState,
                                              this.wicaElementConnectionAttributes.channelAlarmState );

        this.createStream_( this.wicaElementConnectionAttributes.channelName, this.wicaElementConnectionAttributes.channelProperties );
        this.streamManager.activate();
    }

    /**
     * Shuts down the service offered by this class.
     *
     * See also: {@link module:document-stream-connector.DocumentStreamConnector#shutdown activate}.
     */
    shutdown()
    {
        this.streamManager.shutdown();
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
        this.streamConnectionHandlers.streamConnect = ( attempt ) => {
            console.log("Event stream connection attempt: " + attempt );
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
        console.log( "Number of Wica elements found: ", wicaElements.length );

        // Create an array of the associated channel names
        const channels = [];
        wicaElements.forEach(function (widget)
        {
            const channelName = widget.getAttribute( channelNameAttribute );
            if ( widget.hasAttribute( channelPropertiesAttribute ) )
            {
                const channelProps = widget.getAttribute( channelPropertiesAttribute );
                channels.push({"name": channelName, "props": JSON.parse( channelProps ) });
            }
            else
            {
                channels.push( {"name": channelName} );
            }
        });

        const streamOptions = {
            streamReconnectIntervalInSeconds: 15,
            streamTimeoutIntervalInSeconds: 20,
            crossOriginCheckEnabled: false,
        };

        const streamConfiguration = { "channels": channels, "props": this.streamProperties };
        this.streamManager = new StreamManager( this.streamServerUrl, streamConfiguration, this.streamConnectionHandlers, this.streamMessageHandlers, streamOptions );

    }

}
