/**
 * Provides support for firing notification events on wica-aware elements in the current document that have defined
 * handlers.
 * @module
 */
console.debug( "Executing script in document-event-manager.js module...");

import * as DocumentUtilities from './document-utils.js'

/**
 * Provides a type definition for a JS CustomEvent object that is fired when a wica-aware element is updated
 * with new information from the wica stream.
 *
 * @typedef module:document-event-manager.OnWicaEvent
 *
 * @property {Object} target - A reference to the target element on which the event was dispatched.
 * @property {Object} detail - An object providing the customised data payload for the event.
 * @property {string} detail.channelName - the name of the channel.
 * @property {WicaChannelMetadata} detail.channelMetadata - the most recent channel metadata.
 * @property {WicaChannelValue[]} detail.channelValueArray - the latest channel values.
 * @property {WicaChannelValue} detail.channelValueLatest - the most recent channel value.
 */

/**
 * Renders the visual state of wica-aware elements in the current document based on attribute information
 * obtained from the Wica server on the backend.
 */
export class DocumentEventManager
{
    /**
     * Constructs a new instance.
     *
     * @param {!WicaElementConnectionAttributes} wicaElementConnectionAttributes - The names of the wica-aware
     *     element attributes that are to be used in the communication process.
     *     See {@link module:shared-definitions.WicaElementConnectionAttributes WicaElementConnectionAttributes}.
     */
    constructor( wicaElementConnectionAttributes )
    {
        this.wicaElementConnectionAttributes = wicaElementConnectionAttributes;
    }

    /**
     * Starts periodically scanning the current document and firing events on all wica-aware elements
     * to publish their current state.
     *
     * @param {number} [refreshRateInMilliseconds=100] - The period to wait after each document scan before
     *     starting the next one.
     *
     * See also: {@link module:document-event-manager.DocumentEventManager#shutdown shutdown}.
     */
    activate( refreshRateInMilliseconds = 100 )
    {
        // Start update process if not already active. Otherwise do nothing.
        if ( this.intervalTimer === undefined )
        {
            try
            {
                this.doFireEvents_();
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
     * Fires wica notification events on all wica-aware elements in the current document. The event which
     * is fired includes full information about the current state of the channel.
     *
     * The following event types are supported:
     * - 'onwica': custom event.
     * - 'onchange': DEPRECATED, provided only for backwards compatibility.
     *
     * The event payload includes the most recently received stream notification information for the wica
     * channel's metadata and wica channel's value.
     *
     * No events will be fired until both the channel's metadata and value have been obtained.
     *
     * In the case of the 'onwica' event the following information is provided in the detail attribute:
     *
     *   - detail.channelName
     *   - detail.streamState
     *   - detail.channelMetadata
     *   - detail.channelValueArray
     *   - detail.channelValueLatest
     *
     * @private
     * @implNote
     *
     * The current implementation obtains the event payload information by looking at the information in the
     * 'data-wica-channel-value-array' and 'data-wica-channel-metadata' html element attributes.
     */
    doFireEvents_() {
        DocumentUtilities.findWicaElements().forEach((element) => {

            // If we have no information about the channel's current value or the channel's metadata
            // then there is nothing useful that can be done so bail out.
            if ((!element.hasAttribute(this.wicaElementConnectionAttributes.channelValueArray)) ||
                (!element.hasAttribute(this.wicaElementConnectionAttributes.channelMetadata))) {
                return;
            }

            // Obtain the channel name object
            const channelName = element.getAttribute(this.wicaElementConnectionAttributes.channelName);

            // Obtain the channel metadata object
            const channelMetadataObj = JSON.parse(element.getAttribute(this.wicaElementConnectionAttributes.channelMetadata));

            // Obtain the object containing the array of recently received channel values.
            const channelValueArrayObj = JSON.parse(element.getAttribute(this.wicaElementConnectionAttributes.channelValueArray));

            // Check that the received value object really was an array
            if (!Array.isArray(channelValueArrayObj)) {
                console.warn("Stream error: received value object was not an array !");
                return;
            }

            // If there isn't at least one value present bail out as there is nothing useful to be done
            if (channelValueArrayObj.length === 0) {
                return;
            }

            // If an onchange event handler IS defined then delegate the handling
            // of the event (typically performing some calculation or rendering a plot) to
            // the defined method.
            if (element.onchange !== null) {
                let event = new Event('change');
                event.channelName = channelName;
                event.channelMetadata = channelMetadataObj;
                event.channelValueArray = channelValueArrayObj;
                event.channelValueLatest = channelValueArrayObj[channelValueArrayObj.length - 1];
                element.dispatchEvent(event);
            }

            // If an wica event handler IS defined then delegate the handling
            // of the event (typically performing some calculation or rendering
            // a plot) to the defined method.
            if (element.onwica !== null) {
                const customEvent = new CustomEvent('wica', {
                    detail: {
                        "channelName": channelName,
                        "channelMetadata": channelMetadataObj,
                        "channelValueArray": channelValueArrayObj,
                        "channelValueLatest": channelValueArrayObj[channelValueArrayObj.length - 1]
                    }
                });
                element.dispatchEvent(customEvent);
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
        console.warn( msg + vDebug );
    }

}