/**
 * Provides the entrypoint API for leveraging all functionality associated with Wica.
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import {DocumentSupportLoader} from "./document-support-loader.js";
import {StreamManager} from "./stream-manager.js";
import {PlotBuffer} from "./plot-buffer.js";

export {

    /**
     * Provides a facility to buffer the received information for one or more wica-aware elements,
     * subsequently making it available to third-parties who may wish to poll for it.
     */
    PlotBuffer,

    /**
     * Provides support for creating a new WicaStream on the Wica server, for subscribing to it and for
     * publishing the received information.
     */
    StreamManager,

    /**
     * Provides the functionality necessary to support a wica-aware html page.
     */
    DocumentSupportLoader
}


/*- Script Execution Starts Here ---------------------------------------------*/

log.debug( "Executing script in client-api.js module...");
