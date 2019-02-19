/**
 * Provides the main entry point for supporting a Wica-aware document.
 *
 * @module
 */
console.debug( "Executing script in wica.js module...");

import {DocumentSupportLoader} from './client-api.js'

const WICA_HOST="https://gfa-wica-dev.psi.ch";

// Create and activate a document support loader to server this document
const documentSupportLoader = new DocumentSupportLoader( WICA_HOST );
documentSupportLoader.activate( 200, 200 );

// Attach a handler to shut things down when the browser navigates away
window.onbeforeunload = () => loader.shutdown();
