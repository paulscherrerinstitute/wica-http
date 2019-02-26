/**
 * Provides wica logging support.
 *
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

export { logLevels, setLevel, trace, debug, log, info, warn, error };


/*- Script Execution Starts Here ---------------------------------------------*/

console.debug( "Executing script in logger.js module...");

function trace( msg ) { logger.trace( msg ) }
function debug( msg ) { logger.debug( msg ) }
function   log( msg ) {   logger.log( msg ) }
function  info( msg ) {  logger.info( msg ) }
function  warn( msg ) {  logger.warn( msg ) }
function error( msg ) { logger.error( msg ) }

const nop = function() {};

const logger = {
    "error" : nop,
    "warn"  : nop,
    "info"  : nop,
    "log"   : nop,
    "debug" : nop,
    "trace" : nop,
};

const logLevels = {
    "NONE":  0,
    "ERROR": 1,
    "WARN":  2,
    "INFO":  3,
    "LOG":   4,
    "DEBUG": 5,
    "TRACE": 6

};

const consoleLogMap = {
    "error" : { "level": logLevels[ "ERROR" ], "func": console.error },
    "warn"  : { "level": logLevels[ "WARN"  ], "func": console.warn  },
    "info"  : { "level": logLevels[ "INFO"  ], "func": console.info  },
    "log"   : { "level": logLevels[ "LOG"   ], "func": console.log   },
    "debug" : { "level": logLevels[ "DEBUG" ], "func": console.debug },
    "trace" : { "level": logLevels[ "TRACE" ], "func": console.trace },
};

/**
 * Sets the logging level. Zero means log nothing.
 *
 * @param {number} level - The logging level.
 */
function setLevel( level )
{
     Object.keys( consoleLogMap ).forEach(( key ) => {
        logger[ key ] = level >= consoleLogMap[ key ].level ? consoleLogMap[ key ].func : nop;
    } );
}

const DEFAULT_LOG_LEVEL = logLevels.INFO;
setLevel( DEFAULT_LOG_LEVEL );
