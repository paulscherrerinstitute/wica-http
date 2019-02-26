/**
 * Provides wica logging support.
 *
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

export { logLevels, setLevel, trace, debug, log, info, warn, error };


/*- Script Execution Starts Here ---------------------------------------------*/

console.log( "Executing script in logger.js module...");

/**
 * Defines the integer values associated with the available logging levels.
 * @type {{TRACE: number,
 *         LOG: number,
 *         ERROR: number,
 *         NONE: number,
 *         INFO: number,
 *         DEBUG: number,
 *         WARN: number}}
 */
const logLevels = {
    "NONE":  0,
    "ERROR": 1,
    "WARN":  2,
    "INFO":  3,
    "LOG":   4,
    "DEBUG": 5,
    "TRACE": 6
};

/**
 * Outputs a log message at TRACE level.
 *
 * @param {string} msg the message.
 */
function trace( msg ) { logger_.trace( msg ) }

/**
 * Outputs a log message at DEBUG level.
 *
 * @param {string} msg the message.
 */
function debug( msg ) { logger_.debug( msg ) }

/**
 * Outputs a log message at LOG level.
 *
 * @param {string} msg the message.
 */
function log( msg ) {   logger_.log( msg ) }

/**
 * Outputs a log message at INFO level.
 *
 * @param {string} msg the message.
 */
function info( msg ) {  logger_.info( msg ) }

/**
 * Outputs a log message at WARN level.
 *
 * @param {string} msg the message.
 */
function warn( msg ) {  logger_.warn( msg ) }

/**
 * Outputs a log message at TRACE level.
 *
 * @param {string} msg the message.
 */
function error( msg ) { logger_.error( msg ) }

/**
 * Sets the logging level. Zero means log nothing.
 *
 * @param {number} level - The logging level.
 */
function setLevel( level )
{
    Object.keys( consoleLogMap_ ).forEach(( key ) => {
        logger_[ key ] = level >= consoleLogMap_[ key ].level ? consoleLogMap_[ key ].func : nop_;
    } );
}

const nop_ = function() {};

const logger_ = {
    "error" : nop_,
    "warn"  : nop_,
    "info"  : nop_,
    "log"   : nop_,
    "debug" : nop_,
    "trace" : nop_,
};

const consoleLogMap_ = {
    "error" : { "level": logLevels[ "ERROR" ], "func": console.error },
    "warn"  : { "level": logLevels[ "WARN"  ], "func": console.warn  },
    "info"  : { "level": logLevels[ "INFO"  ], "func": console.info  },
    "log"   : { "level": logLevels[ "LOG"   ], "func": console.log   },
    "debug" : { "level": logLevels[ "DEBUG" ], "func": console.debug },
    "trace" : { "level": logLevels[ "TRACE" ], "func": console.trace },
};

// Set the default log level that will be used in tha absence
// of an explicit  call to the setLevel function.
const DEFAULT_LOG_LEVEL = logLevels.INFO;
setLevel( DEFAULT_LOG_LEVEL );
