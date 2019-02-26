/**
 * Provides helper functions for wica-aware html pages.
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import {WicaElementConnectionAttributes} from './shared-definitions.js';

export { findWicaElements, findWicaElementsWithAttributeName,
         findWicaElementsWithChannelName, findWicaElementsWithAttributeValue }


/*- Script Execution Starts Here ---------------------------------------------*/

log.log( "Executing script in document-utils.js module...");

/**
 * Finds all "wica-aware" HTML elements in the current document. That's to say, all elements
 * which include an attribute which defines the name of the wica channel.
 *
 * @returns {NodeListOf<Element>} - The result list.
 */
function findWicaElements()
{
    return findWicaElementsWithAttributeName( WicaElementConnectionAttributes.channelName );
}

/**
 * Finds all wica-aware HTML elements in the current document with the given attribute name.
 *
 * @param {!string} attributeName - The attribute name to search for.
 * @returns {NodeListOf<Element>} - The result list.
 */
function findWicaElementsWithAttributeName( attributeName )
{
    const selector = "[" + attributeName + "]";
    return document.querySelectorAll( selector );
}

/**
 * Finds all wica-aware HTML elements in the current document with the specified wica channel name.
 *
 * @param {!string} channelName - The channel name to search for.
 * @returns {NodeListOf<Element>} - The result list.
 */
function findWicaElementsWithChannelName( channelName )
{
    return findWicaElementsWithAttributeValue( WicaElementConnectionAttributes.channelName, channelName );
}

/**
 * Finds all wica-aware HTML elements in the current document whose attribute name matches the specified value.
 *
 * @param {!string} attributeName - The attribute name to target.
 * @param {!string} attributeValue - The attribute value to target.
 * @returns {NodeListOf<Element>} - The result list.
 */
function findWicaElementsWithAttributeValue( attributeName, attributeValue )
{
    const selector = "*[" + attributeName + " = \'" + attributeValue + "\']";
    return document.querySelectorAll( selector );
}
