/**
* Provides support for buffering the information received from the document's wica event stream.
*
* @module
*/
console.debug( "Executing script in plot-buffer.js module...");

/**
 * Provides a facility to buffer the received information for one or more wica-aware elements,
 * subsequently making it available to third-parties who may wish to poll for it.
 */
export class PlotBuffer
{

    /**
     * Constructs a new instance based on the specified DOM elements and buffer size.
     *
     * @param {Object} htmlElementIds the elements to listen to.
     *
     * @param maximumBufferSize the number of entries that will be buffered. Beyond this limit the oldest values
     *     will be silently thrown away.
     */
    constructor( htmlElementIds, maximumBufferSize = 32 )
    {
        this.htmlElementIds = htmlElementIds;
        this.maximumBufferSize = maximumBufferSize;
        this.streamValuesBuffer = {};
        this.streamMetadata = {};
    }

    /**
     * Activate the plot buffer to receive information for the elements specified in the constructor.
     */
    activate()
    {
        // Create a mutation observer instance
        this.observer = new MutationObserver( this.mutationHandler_ );

        // TODO: Check here that element exists and that is is data-aware
        for ( const htmlElementId of this.htmlElementIds )
        {
            this.register_( htmlElementId, this.mutationHandler_ );
        }
    }

    /**
     * Deactivates the plot buffer. No further information will be added but existing
     * data will be preserved.
     */
    shutdown()
    {
        this.observer.disconnect();
    }

    /**
     * Returns an indication of whether the connection to the server has been established.
     *
     * @returns {boolean}
     */
    isConnectedToServer()
    {
        // TODO
        return true;
        //return this.streamOpened;
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
        if ( Object.values( this.streamMetadata ).length === 0 )
        {
            return false;
        }

        if ( Object.values( this.streamValuesBuffer ).length === 0 )
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
     * Returns the most recently received Metadata for every channel in the buffer.
     */
    getChannelMetadata()
    {
        return this.streamMetadata;
    }

    /**
     * Returns the most recently received values for every channel in the buffer.
     */
    getChannelValues()
    {
        return this.streamValuesBuffer;
    }


    /**
     * register_ for events
     *
     * @private
     */
    register_( htmlElementId )
    {
        const targetNode = document.getElementById( htmlElementId );

        // Options for the observer (which mutations to observe)
        const config = { attributes: true,
            attributeFilter: [ "data-wica-channel-metadata", "data-wica-channel-value-array" ],
            childList: false,
            subtree: false };

        // Start monitoring the element withg the specified id.
        this.observer.observe( targetNode, config );
    }

    mutationHandler_( mutationList )
    {
        mutationList.forEach( mutation =>
        {
            if ( mutation.type === "attributes" )
            {
                const element = mutation.target;
                const channelName = element.getAttribute( "data-wica-channel-name" );

                if ( mutation.attributeName === "data-wica-channel-metadata" )
                {
                    const metadataAsJsonString = element.getAttribute( "data-wica-channel-metadata" );
                    const metadata = JSON.parse( metadataAsJsonString );
                    this.streamMetadata[ channelName ] = metadata;
                }

                if ( mutation.attributeName === "data-wica-channel-value-array" )
                {
                    const valueArrayAsJsonString = element.getAttribute( "data-wica-channel-value-array" );
                    const valueArray = JSON.parse( valueArrayAsJsonString );
                    this.updateBufferedChannelValues_( channelName, valueArray );
                }

                console.log(  "Mutation on attribute: '" + mutation.attributeName + "' of wica element: '" + channelName + "'" );
            }
        } );

    }


    /**
     * Captures the most recent value information for a channel, where
     * necessary discarding the oldest data (when the buffer size
     * limit has been reached).
     *
     * @private
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

}
