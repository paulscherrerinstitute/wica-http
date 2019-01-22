/**
 * Loads the services that are required to provide Wica support for the current HTML document.
 * @module
 */
console.debug( "Executing script in document-service-support-loader.js module...");

import {WicaElementConnectionAttributes, WicaElementRenderingAttributes, WicaStreamProperties} from "./shared-definitions";
import {DocumentStreamConnector} from "./document-stream-connector";
import {DocumentTextRenderer} from "./document-text-renderer";


export function load()
{
    loadWicaCSS();

    const documentStreamConnector = new DocumentStreamConnector( WICA_HOST, WicaStreamProperties, WicaElementConnectionAttributes );
    documentStreamConnector.activate();

    const documentTextRenderer = new DocumentTextRenderer( WicaElementConnectionAttributes, WicaElementRenderingAttributes );
    documentTextRenderer.activate();
}

function loadWicaCSS()
{
    if ( !document.getElementById('wica-css-id' ) )
    {
        const head = document.getElementsByTagName('head')[0];
        const link = document.createElement('link');
        link.id = 'wica-css-id';
        link.rel = 'stylesheet';
        link.type = 'text/css';
        link.href = WICA_HOST + '/wica/wica.css';
        link.media = 'all';
        head.appendChild(link);
    }
}
