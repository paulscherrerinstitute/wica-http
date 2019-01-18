
// to use this:
// A) In HTML file: <script type="module" src=".../wica/wica.js"></script>
// B) In ES6 module file: import 'wica/wica.js'
console.debug( "Executing script in wica.js module...");

import {WicaDocumentManager} from './document-manager.js'
import * as WicaEventManager from './event-manager.js'
import * as WicaRenderingManager from './rendering-manager.js'

//const WICA_HOST = "https://gfa-wica.psi.ch";
const WICA_HOST = "https://gfa-wica-dev.psi.ch";

function fireWicaEvents()
{
    try
    {
        WicaEventManager.fireEvents();
    }
    catch( err )
    {
        logExceptionData( "Programming Error: fireEvents threw an exception: ", err );
    }

    // Allow at least 100ms after each event firing cycle
    setTimeout( fireWicaEvents, 100 );
}


function refreshWicaPage()
{
    try
    {
        WicaRenderingManager.renderWicaElements();
    }
    catch( err )
    {
        logExceptionData( "Programming Error: fireEvents threw an exception: ", err );
    }

    // Allow at least 100ms after each rendering cycle
    setTimeout( refreshWicaPage, 100 );
}

function logExceptionData( msg, err )
{
    let vDebug = "";
    for ( const prop of err )
    {
        vDebug += "property: "+ prop + " value: ["+ err[prop]+ "]\n";
    }
    vDebug += "toString(): " + " value: [" + err.toString() + "]";
    console.warn( msg + vDebug );
}

function loadWicaCSS()
{
    if ( ! document.getElementById( 'wica-css-id') )
    {
        const head  = document.getElementsByTagName('head')[0];
        const link  = document.createElement('link');
        link.id   = 'wica-css-id';
        link.rel  = 'stylesheet';
        link.type = 'text/css';
        link.href = WICA_HOST + '/wica/wica.css';
        link.media = 'all';
        head.appendChild(link);
    }
}

const wicaDocumentManager = new WicaDocumentManager();
wicaDocumentManager.startAttributeUpdater();

loadWicaCSS();

setTimeout( fireWicaEvents, 100 );
setTimeout( refreshWicaPage, 100 );


