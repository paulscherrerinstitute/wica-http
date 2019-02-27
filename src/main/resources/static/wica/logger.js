/**
 * Provides wica logging support.
 *
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

export { logLevels, setLevel, trace, debug, log, info, warn, error };


/*- Script Execution Starts Here ---------------------------------------------*/

/**
 * JS Object that defines the available log levels including the default that
 * will be used in the absence of an explict call to the setLevel function.
 *
 * @property {number} [NONE = 0]
 * @property {number} [ERROR = 1]
 * @property {number} [WARN = 2]
 * @property {number} [INFO = 3]
 * @property {number} [LOG = 4]
 * @property {number} [DEBUG = 5]
 * @property {number} [TRACE = 6]
 * @property {number} [DEFAULT = 2]
 */
const logLevels = {
    "NONE":    0,
    "ERROR":   1,
    "WARN":    2,
    "INFO":    3,
    "LOG":     4,
    "DEBUG":   5,
    "TRACE":   6,
    "DEFAULT": 2
};

/**
 * Outputs a log message at TRACE level.
 *
 * @param {string} msg - A message string containing zero or more substitution strings.
 * @param {Object} msgOptions - Zero or more objects with which to replace substitution
 *     strings within msg.
 */
function trace( msg, ...msgOptions  ) { logger_.trace( msg, ...msgOptions ) }

/**
 * Outputs a log message at DEBUG level.
 *
 * @param {string} msg - A message string containing zero or more substitution strings.
 * @param {Object} msgOptions - Zero or more objects with which to replace substitution
 *     strings within msg.
 */
function debug(msg, ...msgOptions  ) { logger_.debug( msg, ...msgOptions ) }

/**
 * Outputs a log message at LOG level.
 *
 * @param {string} msg - A message string containing zero or more substitution strings.
 * @param {Object} msgOptions - Zero or more objects with which to replace substitution
 *     strings within msg.
 */
function log( msg, ...msgOptions  ) { logger_.log( msg, ...msgOptions ) }

/**
 * Outputs a log message at INFO level.
 *
 * @param {string} msg - A message string containing zero or more substitution strings.
 * @param {Object} msgOptions - Zero or more objects with which to replace substitution
 *     strings within msg.
 */
function info( msg, ...msgOptions  ) { logger_.info( msg, ...msgOptions ) }

/**
 * Outputs a log message at WARN level.
 *
 * @param {string} msg - A message string containing zero or more substitution strings.
 * @param {Object} msgOptions - Zero or more objects with which to replace substitution
 *     strings within msg.
 */
function warn( msg, ...msgOptions  ) { logger_.warn( msg, ...msgOptions ) }

/**
 * Outputs a log message at TRACE level.
 *
 * @param {string} msg - A message string containing zero or more substitution strings.
 * @param {Object} msgOptions - Zero or more objects with which to replace substitution
 *     strings within msg.
 */
function error(msg, ...msgOptions  ) { logger_.error( msg, ...msgOptions ) }

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

setLevel( logLevels.DEFAULT );
