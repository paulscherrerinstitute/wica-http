console.debug( "Executing script in json5-wrapper.js module...");

/**
 * This module causes the browser to load the JSON5 library (thereby,
 * as a side-effect, making it available in the Global memory space).
 *
 * Critical functions of the library are wrapped and exported for use
 * elsewhere inside the application, thus making it independent of the
 * specific implementation.
 *
 * The current implementation is from JSON5.org. See: https://json5.org/
 */

export { load, info };

/**
 * Wrapper for the picolog log.info message.
 *
 * @return {string} the message to log
 */
function info( message )
{
    if ( isLibraryLoaded_() )
    {
        return log.info( message );
    }
    else
    {
        const msg = "Programming Error: call to log.info() before library initialised.";
        console.warn( msg );
        throw Error( msg );
    }
}

/**
 * Loads the library. Invokes the callback handler when complete.
 *
 * @param callback the handler to callback when the library is loaded.
 */
function load( callback )
{
    if ( ! getAndSetLibraryLoadStarted_() )
    {
        const script = document.createElement('script');
        script.id = 'wica-piolog-wrapper-id';
        script.src = "/wica/picolog.min.js";
        script.onload = function()
        {
            setLibraryLoaded_();
            console.log( "picolog wrapper: initialised ok !");
            callback();
        };
        document.head.appendChild( script );
    }
    else
    {
        if ( isLibraryLoaded_() ) {
            console.log("picolog wrapper library is already loaded.");
            callback();
        }
        else {
            console.log("picolog wrapper library is loading...");
            awaitLibraryLoad( callback );
            console.log("picolog wrapper library is now loaded.");
        }
    }
}

function getAndSetLibraryLoadStarted_()
{
    const result = ( typeof window.json5LibLoadStarted !== "undefined" );
    window.json5LibLoadStarted = true;
    return result;
}

function isLibraryLoaded_()
{
    return typeof window.json5LibLoaded !== "undefined";
}

function setLibraryLoaded_()
{
    window.json5LibLoaded = true;
}

function awaitLibraryLoad( callback )
{
    setTimeout( () => {
        if( isLibraryLoaded_() ) {
            callback();
        }
        else {
            awaitLibraryLoad( callback );
        } }, 100
    );
}
