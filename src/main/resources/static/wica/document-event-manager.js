/**
 * Provides support for firing custom notification events on wica-aware elements in the current document to
 * update interested third-parties on the latest status received from the wica event stream.
 *
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import * as DocumentUtilities from './document-utils.js'
import * as JsonUtilities from './json5-wrapper.js'

export { DocumentEventManager }


/*- Script Execution Starts Here ---------------------------------------------*/

log.log( "Executing script in document-event-manager.js module...");

/**
 * Provides a type definition for a JS CustomEvent object that is fired to inform observers of the
 * latest metadata and value information received on a wica-aware element.
 *
 * @typedef module:document-event-manager.OnWicaEvent
 *
 * @property {Object} target - A reference to the target element on which the event was dispatched.
 *
 * @property {Object} detail - An object providing the customised data payload for the event.
 *
 * @property {string} detail.channelName - The name of the channel associated with the element on which the
 *     event was fired. See {@link module:shared-definitions.WicaChannelName WicaChannelName}.
 *
 * @property {WicaChannelMetadata} detail.channelMetadata - The most recent channel metadata.
 *     See {@link module:shared-definitions.WicaChannelMetadata WicaChannelMetadata}.
 *
 * @property {WicaChannelValue[]} detail.channelValueArray - The latest channel values.
 *     See {@link module:shared-definitions.WicaChannelValue WicaChannelValue}.
 *
 * @property {WicaChannelValue} detail.channelValueLatest - The most recent channel value.
 *     See {@link module:shared-definitions.WicaChannelValue WicaChannelValue}.
 */

/**
 * Provides support for periodically scanning the current document for wica-aware elements with attached
 * event handlers or event listeners. Fires a custom {@link module:document-event-manager.OnWicaEvent
 * OnWicaEvent} to inform the attached observers of the latest status received from the wica event
 * stream.
 */
class DocumentEventManager
{
    /**
     * Constructs a new instance.
     *
     * @param {!WicaElementConnectionAttributes} wicaElementConnectionAttributes - The names of the wica-aware
     *     element attributes that can be examined to determine the name of the channel and its current status.
     *     See {@link module:shared-definitions.WicaElementConnectionAttributes WicaElementConnectionAttributes}.
     *
     * @param {!WicaElementEventAttributes} wicaElementEventAttributes - The names of the wica-aware
     *     element attributes that can be examined to determine whether a wica-aware element has any attached handlers.
     *     See {@link module:shared-definitions.WicaElementEventAttributes WicaElementEventAttributes}.
     *
     * @implNote
     *
     * It is currently (2019-01-29) impossible to optimise the firing of events to trigger them only on elements
     * with attached event listeners. This is because it is impossible to detect programmatically the presence
     * of attached event listeners.
     */
    constructor( wicaElementConnectionAttributes, wicaElementEventAttributes )
    {
        this.wicaElementConnectionAttributes = wicaElementConnectionAttributes;
        this.wicaElementEventAttributes = wicaElementEventAttributes;
    }

    /**
     * Starts periodically scanning the current document and firing events on all wica-aware elements
     * to publish their current state.
     *
     * The event that will be published is a 'onwica'
     *
     * @param {number} [refreshRateInMilliseconds=100] - The period to wait after each document scan before
     *     starting the next one.
     *
     * @param {boolean} [supportEventListeners=false] - Determines whether events are fired ONLY on elements which
     *     have defined event handlers or whether they are fired unconditionally on all elements (as is required to
     *     support any attached event listeners).
     *
     * See also: {@link module:document-event-manager.DocumentEventManager#shutdown shutdown}.
     */
    activate( refreshRateInMilliseconds = 100, supportEventListeners = false )
    {
        // Start update process if not already active. Otherwise do nothing.
        if ( this.intervalTimer === undefined )
        {
            try
            {
                this.supportEventListeners = supportEventListeners;
                this.doScan_( refreshRateInMilliseconds );
            }
            catch (err)
            {
                DocumentEventManager.logExceptionData_("Programming Error: fireEvents threw an exception: ", err);
            }
       }
    }

    /**
     * Shuts down the service offered by this class.
     *
     * See also: {@link module:document-event-manager.DocumentEventManager#activate activate}.
     */
    shutdown()
    {
        // Stop update process if already active. otherwise do nothing.
        if (this.intervalTimer !== undefined)
        {
            clearInterval(this.intervalTimer);
            this.intervalTimer = undefined;
        }
    }

    /**
     * Performs a single update cycle, then schedules the next one.
     *
     * @private
     * @param {number} refreshRateInMilliseconds - The period to wait after every update scan before starting the next one.
     */
    doScan_( refreshRateInMilliseconds )
    {
        try
        {
            this.fireEvents_( this.wicaElementConnectionAttributes.channelName,
                              this.wicaElementConnectionAttributes.channelMetadata,
                              this.wicaElementConnectionAttributes.channelValueArray,
                              this.wicaElementEventAttributes.eventHandler,
                              this.supportEventListeners );
        }
        catch( err )
        {
            DocumentEventManager.logExceptionData_( "Programming Error: fireEvents_ threw an exception: ", err );
        }

        // Reschedule next update
        this.intervalTimer = setTimeout(() => this.doScan_( refreshRateInMilliseconds ), refreshRateInMilliseconds );
    }

    /**
     * Fires a custom {@link module:document-event-manager.OnWicaEvent OnWicaEvent} on all wica-aware elements in the
     * current document to inform any attached event handlers or event listeners of the latest state.
     *
     * The event payload includes the most recently received stream notification information for the wica channel's
     * metadata and wica channel's value.
     *
     * No events will be fired until both the channel's metadata and value have been obtained.
     *
     * @private
     *
     * @param {string} channelNameAttribute - The name of the attribute which holds the channel name.
     * @param {string} channelMetadataAttribute - The name of the attribute which holds the channel metadata.
     * @param {string} channelValueArrayAttribute - The name of the attribute which holds the channel value array.
     * @param {string} eventHandlerAttribute - The name of the attribute which determines whether an element has
     *    a defined event handler.
     * @param {boolean} supportEventListeners - Whether events are to be fired unconditionally to support event
     *     listeners or in a more optimised way which supports event handlers only.
     */
    fireEvents_( channelNameAttribute, channelMetadataAttribute, channelValueArrayAttribute,
                 eventHandlerAttribute, supportEventListeners )
    {
        DocumentUtilities.findWicaElements().forEach((element) => {

            // If we have no information about the channel's current value or the channel's metadata
            // then there is nothing useful that can be done so bail out.
            if ( (!element.hasAttribute( channelValueArrayAttribute )) || ( !element.hasAttribute( channelMetadataAttribute )))
            {
                return;
            }

            // Obtain the channel name object
            const channelName = element.getAttribute( channelNameAttribute );

            // Obtain the channel metadata object
            const channelMetadata = JsonUtilities.parse( element.getAttribute(channelMetadataAttribute ));

            // Obtain the object containing the array of recently received channel values.
            const channelValueArray = JsonUtilities.parse( element.getAttribute( channelValueArrayAttribute ));

            // Check that the received value object really was an array
            if (!Array.isArray( channelValueArray )) {
                log.warn("Stream error: received value object was not an array !");
                return;
            }

            // If there isn't at least one value present bail out as there is nothing useful to be done
            if ( channelValueArray.length === 0 ) {
                return;
            }

            // If an onchange event handler IS defined then dispatch an onchange event to trigger
            // the handler.
            if ( typeof element[ eventHandlerAttribute ] == "function" ) {
                const event = new Event('change');
                event.channelName = channelName;
                event.channelMetadata = channelMetadata;
                event.channelValueArray = channelValueArray;
                event.channelValueLatest = channelValueArray[channelValueArray.length - 1];
                element.dispatchEvent( event );
            }

            // Events are fired unconditionally if event listener support is required.
            if ( supportEventListeners ) {
                const customEvent = new CustomEvent('wica', {
                    detail: {
                        "channelName": channelName,
                        "channelMetadata": channelMetadata,
                        "channelValueArray": channelValueArray,
                        "channelValueLatest": channelValueArray[channelValueArray.length - 1]
                    }
                });
                element.dispatchEvent( customEvent );
            }
        });
    }

    /**
     * Log any error data generated in this class.
     *
     * @private
     * @param {string} msg - custom error message.
     * @param {Error} err - the Error object
     */
    static logExceptionData_( msg, err )
    {
        let vDebug = "";
        for ( const prop in err )
        {
            if ( err.hasOwnProperty( prop ) )
            {
                vDebug += "property: " + prop + " value: [" + err[ prop ] + "]\n";
            }
        }
        vDebug += "Details: [" + err.toString() + "]";
        log.warn( msg + vDebug );
    }
}