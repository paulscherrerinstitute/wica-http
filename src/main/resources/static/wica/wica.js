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
log.setLevel( log.logLevels.WARN );

log.info( "Wica is loading support for the current document... ");

// Define the server this application is intended to target.

const WICA_DEV_HOST  = "https://gfa-wica-dev.psi.ch";
const WICA_PROD_HOST = "https://gfa-wica.psi.ch";
const WICA_DMZ_HOST = "https://wica.psi.ch";

// Create and activate a document support loader for the document
// which loads this library.
const documentSupportLoader = new ClientAPI.DocumentSupportLoader( WICA_PROD_HOST );
documentSupportLoader.activate( 200, 200 );

// Attach a handler to shut things down when the browser navigates away.
window.onbeforeunload = () => {
    log.info( "Wica is shutting down support for the current document..." );
    documentSupportLoader.shutdown();
    log.info( "Wica unloaded OK." );
};

// Provide a hook for restarting wica support of the current document
function restartDocumentSupportLoader() {
    log.info( "Wica is restarting support for the current document..." );
    documentSupportLoader.shutdown();
    log.info( "Wica document support loader was shutdown OK." );
    documentSupportLoader.activate( 200, 200 );
    log.info( "Wica document support loader was activated OK." );
}
document.wicaRestartDocumentSupportLoader = restartDocumentSupportLoader;

log.info( "Wica support loaded OK. ");

