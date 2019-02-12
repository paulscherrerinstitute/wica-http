console.debug( "Executing script in json5-wrapper.js module...");

// This module causes the browser to load the JSON5 library (thereby,
// as a side-effect, making it available in the Global memory space).

// Critical functions of the library are exported for use elsewhere
// inside the application, thus making the application independent
// of the underlying library implementation.

export { parse, stringify }

let initialised = false;

function parse( u, D )
{
    if ( initialised ) {
        return JSON5.parse( u, D );
    }
    else {
        console.warn( "JSON5 wrapper: call to parse() before initialise." );
    }
}

function stringify( u, D, e )
{
    if ( initialised ) {
        return JSON5.stringify( u, D, e );
    }
    else {
        console.warn( "JSON5 wrapper: call to stringify() before initialise." );
    }
}

function initialise() {
   console.log( "JSON5 wrapper: initialised ok !");
   initialised = true;
}

const script = document.createElement('script');

script.onload = function()
{
    initialise();
};

// Assumption: JSON5 library is colocated on the same server as this wrapper module
script.src = "/extlibs/json5-latest.min.js";

document.head.appendChild( script );

