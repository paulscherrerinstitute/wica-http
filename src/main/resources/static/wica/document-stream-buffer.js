/**
 * Provides support for buffering the information received from the document's wica event stream.
 *
 * @module
 */
console.debug( "Executing script in document-stream-buffer.js module...");

import * as DocumentUtilities from './document-utils.js'

/**
 * Buffers metadata and value updates for channels of interest within the current document and
 * provides subsequent access to the received values.
 *
 * @implNote
 *
 * The current implementation works by observing DOM mutation events on channels of interest.
 */
export class DocumentStreamBuffer
{
    /**
     * Constructs a new instance.
     *
     * @param {!WicaElementConnectionAttributes} wicaElementConnectionAttributes - The names of the wica-aware
     *     element attributes whose mutations will be observed to obtain the required information.
     *     See {@link module:shared-definitions.WicaElementConnectionAttributes WicaElementConnectionAttributes}.
     *
     * @param {!WicaElementBufferingAttributes} wicaElementBufferingAttributes - The names of the wica-aware
     *     element attributes that control the buffering behaviour.
     *     See {@link module:shared-definitions.WicaElementBufferingAttributes WicaElementBufferingAttributes}.
     */
    constructor( wicaElementConnectionAttributes, wicaElementBufferingAttributes )
    {
        this.wicaElementConnectionAttributes = wicaElementConnectionAttributes;
        this.wicaElementBufferingAttributes= wicaElementBufferingAttributes;

        this.streamMetadata = {};
        this.streamValuesBuffer = {};
    }

    /**
     * Creates and subscribes to a new data stream based on the properties supplied in the constructor.
     */
    activate()
    {
        const bufferingElements = DocumentUtilities.findWicaElementsWithAttributeName( this.wicaElementBufferingAttributes.bufferingProperties );

        for ( const element of bufferingElements )
        {
            const bufferSize = element.getAttribute( this.wicaElementBufferingAttributes.bufferingProperties );

            if ( ( bufferSize > 0 ) && element.hasAttribute( this.wicaElementConnectionAttributes.channelName ) )
            {
                this.registerMutationObserver_( element, this.mutationHandler_ );
            }
        }
    }

    /**
     * Returns an indication of whether the connection to the server has been established.
     *
     * @returns {boolean}
     */
    isConnectedToServer()
    {
        return this.streamOpened;
    }

    /**
     * Returns an indication of whether data has been received from ALL the data
     * sources in the stream.
     *
     * - at least one stream metadata object has been received.
     * - at least one stream value object has been received.
     * - at least one value has been received for every channel in the stream.
     *
     * @returns {boolean}
     */
    isDataAvailable()
    {
        if ( this.streamMetadata == null )
        {
            return false;
        }

        if ( this.streamValuesBuffer == null )
        {
            return false;
        }

        for ( const channelValueArray of Object.values( this.streamValuesBuffer ) )
        {
            if ( channelValueArray[ channelValueArray.length - 1 ].val === null  )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a map containing the most recently received channel metadata.
     *
     * @return metadataMap - Map of channel names and their associated metadata. See
     *     {@link module:shared-definitions.WicaChannelName WicaChannelName} and
     *     {@link module:shared-definitions.WicaChannelMetadata WicaChannelMetadata}.
     *
     */
    getMetadataMap()
    {
        return this.metadataMap;
    }

    /**
     * Returns a map containing the most recently received channel values.
     *
     * @return valueMap - Map of channel names and array of values that have been received for the channel in
     *     chronological order. See {@link module:shared-definitions.WicaChannelName WicaChannelName} and
     *     {@link module:shared-definitions.WicaChannelValue WicaChannelValue}.
     */
    getValueMap()
    {
        return this.valueMap;
    }


    /**
     *
     * @private
     * @param mutationList
     * @private
     */
    mutationHandler_( mutationList )
    {
        mutationList.forEach( mutation =>  {

            if ( mutation.type === "attributes" ) {
                const element = mutation.target;
                const wicaChannelName = element.getAttribute( "data-wica-channel-name" )
                console.log(  "Mutation on attribute: '" + mutation.attributeName + "' of wica element: '" + wicaChannelName + "'" );
            }

            console.log (mutation.type );
        });
    }


    /**
     * Internal handler used for capturing stream value updates.
     */
    handleStreamValuesUpdated( vObj )
    {
        //console.log( "Datasource received new channel values map.");

        // For each channel update the array of stored channel values
        // with any new information provided in the notification object.
        // Where necessary throw away the oldest data to make space.
        for ( const channelName of Object.keys( vObj ) )
        {
           this.updateChannelValues( channelName, vObj[ channelName ] );
        }
    }

    /**
     * Captures the most recent value information for a channel, where
     * necessary discarding the oldest data (when the buffer size
     * limit has been reached).
     */
    updateBufferedChannelValues_( channelName, channelValues )
    {
        // Now add the most recently received channel values
        for ( const channelValue of channelValues )
        {
            this.streamValuesBuffer[ channelName ].push( channelValue );
        }

        // If the previous notification buffer is full throw away the oldest values until
        // it is the right size again.
        while ( this.streamValuesBuffer[ channelName ].length > this.maximumBufferSize )
        {
            this.streamValuesBuffer[ channelName ].shift();
        }
    }

    /**
     *
     * @private
     * @param htmlElementId
     * @param handler
     * @private
     */
    static registerMutationObserver_( htmlElementId, handler )
    {
        const targetNode = document.getElementById( htmlElementId );

        // Define options for the observer (which mutations to observe)
        const config = { attributes: true, childList: false, subtree: false };

        // Create a mutation observer with the defined options that will invoke the specified handler
        const observer = new MutationObserver( handler );
        observer.observe( targetNode, config );
    }

}
