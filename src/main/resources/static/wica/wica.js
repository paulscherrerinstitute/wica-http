/**
 * Provides the main entry point for supporting a Wica-aware document.
 *
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import * as ClientAPI from "./client-api.js"


/*- Script Execution Starts Here ---------------------------------------------*/

// Configure the logging level required for this application.
log.setLevel( LOGGING_LEVEL, logLevels.LOG );

log.debug( "Executing script in wica.js module...");

// Define the server this application is intended to target.
const WICA_HOST="https://gfa-wica-dev.psi.ch";

// Create and activate a document support loader for the document
// which loads this library.
const documentSupportLoader = new ClientAPI.DocumentSupportLoader( WICA_HOST );
documentSupportLoader.activate( 200, 200 );

// Attach a handler to shut things down when the browser navigates away.
window.onbeforeunload = () => {
    log.info( "Shutting down wica document support..." );
    documentSupportLoader.shutdown();
    log.info( "Done." );
};



