/**
 * Provides wica logging support.
 *
 * @module
 */
console.debug( "Executing script in picolog-wrapper.js module...");


export { load, trace, debug, log, info, warn, error };

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
        script.id = 'wica-picolog-wrapper-id';
        script.src = "/wica/picolog.min.js";
        script.onload = function()
        {
            setLibraryLoaded_();
            console.log( "Picolog wrapper: initialised ok !");
            callback();
        };
        document.head.appendChild( script );
    }
    else
    {
        if ( isLibraryLoaded_() ) {
            console.log("Picolog wrapper library is already loaded.");
            callback();
        }
        else {
            console.log("Picolog wrapper library is loading...");
            awaitLibraryLoad_( callback );
            console.log("Picolog wrapper library is now loaded.");
        }
    }
}

function getAndSetLibraryLoadStarted_()
{
    const result = ( typeof window.picologLibLoadStarted !== "undefined" );
    window.picologLibLoadStarted = true;
    return result;
}

function isLibraryLoaded_()
{
    return typeof window.picologLibLoaded !== "undefined";
}

function setLibraryLoaded_()
{
    window.picologLibLoaded = true;
    log.level = log.DEBUG;
}

function awaitLibraryLoad_( callback )
{
    setTimeout( () => {
        if( isLibraryLoaded_() ) {
            callback();
        }
        else {
            awaitLibraryLoad_( callback );
        } }, 100
    );
}
