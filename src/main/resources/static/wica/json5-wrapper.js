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

export { parse, stringify }

/**
 * Wrapper for the JSON5 "flexible" parser that can handle real numbers sent as NaNs, Infinity etc.
 *
 * The following text was taken from the JSON5.org site. See: https://json5.org.
 *
 * Parses a JSON5 string, constructing the JavaScript value or object described by the string.
 * An optional reviver function can be provided to perform a transformation on the resulting
 * object before it is returned.
 *
 * @param {String} text - The string to parse as JSON5.
 *
 * @param {Object} [reviver] - If a function, this prescribes how the value originally produced
 *     by parsing is transformed, before being returned.
 *
 * @return {*} the object corresponding to the given JSON5 text.
 */
function parse( text, reviver )
{
    if ( ! isInitialised() )
    {
        waitForLoad_().then(() => { return JSON5.parse( value, reviver ); } );
    }
    return JSON5.parse( value, reviver );
}

/**
 * Wrapper for the JSON5 alternative stringifier.
 *
 * The following text was taken from the JSON5.org site. See: https://json5.org.
 *
 * Converts a JavaScript value to a JSON5 string, optionally replacing values if a replacer
 * function is specified, or optionally including only the specified properties if a replacer
 * array is specified.
 *
 * @param {string} value - The value to convert to a JSON5 string.
 *
 * @param {function} [replacer] - A function that alters the behavior of the stringification process, or
 *     an array of String and Number objects that serve as a whitelist for selecting/filtering
 *     the properties of the value object to be included in the JSON5 string. If this value
 *     is null or not provided, all properties of the object are included in the resulting
 *     JSON5 string.
 *
 * @param {string|number} [space] - A String or Number object that's used to insert white space into
 *     the output JSON5 string for readability purposes. If this is a Number, it indicates the
 *     number of space characters to use as white space; this number is capped at 10 (if it is
 *     greater, the value is just 10). Values less than 1 indicate that no space should be used.
 *     If this is a String, the string (or the first 10 characters of the string, if it's longer
 *     than that) is used as white space. If this parameter is not provided (or is null), no
 *     white space is used. If white space is used, trailing commas will be used in objects and
 *     arrays.
 *
 * @return {string} A JSON5 string representing the value.
 */
function stringify( value, replacer, space )
{
    if ( ! isInitialised() )
    {
        waitForLoad_().then(() => {
            return JSON5.stringify( value, replacer, space );
        } );
    }

    return JSON5.stringify( value, replacer, space );
}


function waitForLoad_( )
{
    return new Promise(resolve  => load_( resolve ) );
}


/**
 * Loads the library. Invokes the callback handler when complete.
 *
 * @param callback the handler to callback when the library is loaded.
 */
function load_( callback )
{
    if ( ! isInitialised() )
    {
        const script = document.createElement('script');
        script.id = 'wica-json5-wrapper-id';
        script.src = "/extlibs/json5-latest.min.js";
        script.onload = function()
        {
            console.log( "JSON5 wrapper: initialised ok !");
            callback();
        };
        document.head.appendChild( script );
    }
}

function isInitialised()
{
    return ( document.getElementById('wica-json5-wrapper-id' ) !== null);
}