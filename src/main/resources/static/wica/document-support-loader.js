/**
 * Loads the services that are required to provide Wica support for the current HTML document.
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import { WicaElementConnectionAttributes, WicaElementEventAttributes,
         WicaElementRenderingAttributes, WicaStreamProperties} from './shared-definitions.js';

import {DocumentStreamConnector} from "./document-stream-connector.js";
import {DocumentTextRenderer} from "./document-text-renderer.js";
import {DocumentEventManager} from "./document-event-manager.js";
import * as JsonUtilities from "./json5-wrapper.js";

export { DocumentSupportLoader }

/*- Script Execution Starts Here ---------------------------------------------*/

log.debug( "Executing script in document-support-loader.js module...");

/**
 * Provides the functionality necessary to support a wica-aware html page.
 */
class DocumentSupportLoader
{
    /**
     * Constructs a new instance to work with the specified Wica Server.
     *
     * @param {!string} streamServerUrl - The URL of the Wica Server with whom
     *    this instance should communicate.
     */
    constructor( streamServerUrl )
    {
        this.streamServerUrl = streamServerUrl;
        this.documentStreamConnector = new DocumentStreamConnector( streamServerUrl, WicaStreamProperties, WicaElementConnectionAttributes );
        this.documentTextRenderer = new DocumentTextRenderer( WicaElementConnectionAttributes, WicaElementRenderingAttributes );
        this.documentEventManager = new DocumentEventManager( WicaElementConnectionAttributes, WicaElementEventAttributes );
    }

    /**
     * Activates support for the current document.
     *
     * @param {number} [textRendererRefreshRate=100] - The rate at which the document's text renderer should run to update the
     *     visual state of the document's wica-aware elements.
     *
     * @param {number} [eventManagerRefreshRate=100] - The rate at which the document's event manager should run to fire
     *    notification events on the state of the document's wica-aware elements.
     */
    activate( textRendererRefreshRate = 100, eventManagerRefreshRate = 100 )
    {
        this.loadWicaCSS_();

        JsonUtilities.load(() => {
            this.documentStreamConnector.activate();
            this.documentTextRenderer.activate( textRendererRefreshRate );
            this.documentEventManager.activate( eventManagerRefreshRate );
        });
    }

    /**
     * Shuts down support for the current document.
     */
    shutdown()
    {
        this.documentStreamConnector.shutdown();
        this.documentTextRenderer.shutdown();
        this.documentEventManager.shutdown();
    }

    /**
     * Loads the CSS that is used to render the visual state of wica-aware elements
     * using information in the element's attributes.
     * @private
     */
    loadWicaCSS_()
    {
        if ( !document.getElementById('wica-css-id') )
        {
            const head = document.getElementsByTagName('head')[0];
            const link = document.createElement('link');
            link.id = 'wica-css-id';
            link.rel = 'stylesheet';
            link.type = 'text/css';
            link.href = this.streamServerUrl + '/wica/wica.css';
            link.media = 'all';
            head.appendChild(link);
        }
    }

}
