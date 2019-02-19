/**
* Provides support for buffering the information received from the document's wica event stream.
*
* @module
*/
console.debug( "Executing script in plot-buffer.js module...");

import * as JsonUtilities from './json5-wrapper.js'
import {WicaElementConnectionAttributes} from './shared-definitions.js';

/**
 * Provides a facility to buffer the received information for one or more wica-aware elements,
 * subsequently making it available to third-parties who may wish to poll for it.
 */
export class PlotBuffer
{

    /**
     * Constructs a new instance based on the specified DOM elements and buffer size.
     *
     * @param {string[]} htmlElementIds the names of the elements to listen to.
     *
     * @param maximumBufferSize the number of entries that will be buffered. Beyond this limit the oldest values
     *     will be silently thrown away.
     */
    constructor(  htmlElementIds, maximumBufferSize = 32 )
    {
        this.htmlElementIds = htmlElementIds;
        this.maximumBufferSize = maximumBufferSize;
        this.htmlElements = [];
        this.metadataMap = {};
        this.valueMap = {};

        // TODO: Check here that element exists and that is is data-aware
        for ( const htmlElementId of this.htmlElementIds )
        {
            const ele = document.getElementById( htmlElementId );
            if ( ele !== null )
            {
                if ( ele.hasAttribute( WicaElementConnectionAttributes.channelName ) )
                {
                    const channelName = ele.getAttribute( WicaElementConnectionAttributes.channelName );
                    this.valueMap[ channelName ]= [];
                    this.htmlElements.push( ele );
                }
                else
                {
                    console.warn( "One or more element ID's did not correspond to a wica-aware element" );
                }
            }
            else
            {
                console.warn( "One or more element ID's were not found " );
            }
        }

        this.observer = new MutationObserver(( mutationList ) => this.mutationHandler_( mutationList ) );
    }

    /**
     * Activate the plot buffer to receive information for the elements specified in the constructor.
     */
    activate()
    {
        const mutationObserverOptions = { subtree: false,
                                          childList: false,
                                          attributes: true,
                                          attributeFilter: [ WicaElementConnectionAttributes.channelMetadata,
                                                             WicaElementConnectionAttributes.channelValueArray ] };
        for ( const htmlElement of this.htmlElements )
        {
            this.observer.observe( htmlElement, mutationObserverOptions );
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
        // Scan through all elements and check that the stream state is shown as opened
        for ( const ele of this.htmlElements )
        {
            if ( ele.hasAttribute( WicaElementConnectionAttributes.streamState ) )
            {
                const streamState = ele.getAttribute( WicaElementConnectionAttributes.streamState );
                if ( ! streamState.includes( "opened-") )
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }

        return true;
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
        if ( Object.values( this.metadataMap ).length === 0 )
        {
            return false;
        }

        if ( Object.values( this.valueMap ).length === 0 )
        {
            return false;
        }

        for ( const channelValueArray of Object.values( this.valueMap ) )
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
     * @param mutationList
     * @private
     */
    mutationHandler_( mutationList )
    {
        mutationList.forEach( mutation =>
        {
            if ( mutation.type === "attributes" )
            {
                const element = mutation.target;
                const channelName = element.getAttribute( WicaElementConnectionAttributes.channelName );

                if ( mutation.attributeName === WicaElementConnectionAttributes.channelMetadata )
                {
                    const metadataAsJsonString = element.getAttribute( WicaElementConnectionAttributes.channelMetadata );
                    const metadata = JsonUtilities.parse( metadataAsJsonString );
                    this.metadataMap[ channelName ] = metadata;
                }

                if ( mutation.attributeName === WicaElementConnectionAttributes.channelValueArray )
                {
                    const valueArrayAsJsonString = element.getAttribute( WicaElementConnectionAttributes.channelValueArray );
                    const valueArray = JsonUtilities.parse( valueArrayAsJsonString );
                    this.updateBufferedChannelValues_( channelName, valueArray );
                }

                // console.log( "Mutation on attribute: '" + mutation.attributeName + "' of wica element: '" + channelName + "'" );
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
    updateBufferedChannelValues_( channelName, channelValueArray )
    {
        // Now add the most recently received channel values
        for ( const channelValue of channelValueArray )
        {
            this.valueMap[ channelName ].push( channelValue );
        }

        // If the previous notification buffer is full throw away the oldest values until
        // it is the right size again.
        while ( this.valueMap[ channelName ].length > this.maximumBufferSize )
        {
            this.valueMap[ channelName ].shift();
        }
    }
}
