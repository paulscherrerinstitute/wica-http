/**
 * Provides the main entry point for supporting a Wica-aware document.
 *
 * @module
 */
console.debug( "Executing script in wica.js module...");

import * as log from "./picolog-wrapper.js"
import * as ClientAPI from "./client-api.js"

const WICA_HOST="https://gfa-wica-dev.psi.ch";

var documentSupportLoader;

log.load(() => {

    // The logging library needs to load before everything else
    // Once it has loaded we can pull in the other modules
    log.info( "Picolog library loaded ok !!" );

    // Create and activate a document support loader to server this document
    documentSupportLoader = new ClientAPI.DocumentSupportLoader( WICA_HOST );
    documentSupportLoader.activate( 200, 200 );
} );


// Attach a handler to shut things down when the browser navigates away
window.onbeforeunload = () => {
    log.info( "Shutting down wica document support..." );
    documentSupportLoader.shutdown();
    log.info( "Done." );
};
