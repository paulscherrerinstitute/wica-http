/**
 * Provides the API for leveraging
 *
 * @module
 */

console.debug( "Executing script in client-api.js module...");

import {load,unload} from "./document-support-loader";
import {StreamManager} from "./stream-manager.js";
import {PlotBuffer} from "./plot-buffer.js";

export {
    PlotBuffer,
    StreamManager,
    load,
    unload
}