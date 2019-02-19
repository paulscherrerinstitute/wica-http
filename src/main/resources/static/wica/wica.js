/**
 * Provides the main entry point for supporting a Wica-aware document.
 *
 * @module
 */
console.debug( "Executing script in wica.js module...");

import DocumentServiceSupportLoader from './client-api.js'

DocumentServiceSupportLoader.load( 200, 200 );

// import * as PlotBuffer from './client-api.js'
// const buffer = new PlotBuffer( ["abc", "def" ], 222 );
// buffer.activate();
