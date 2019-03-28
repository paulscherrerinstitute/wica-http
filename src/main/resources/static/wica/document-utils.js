/**
 * Provides helper functions for wica-aware html pages.
 * @module
 */

/*- Import/Export Declarations -----------------------------------------------*/

import * as log from "./logger.js"
import {WicaElementConnectionAttributes} from './shared-definitions.js';

export { findWicaElements,
         findWicaElementsWithAttributeName,
         findWicaElementsWithChannelName,
         findWicaElementsWithAttributeValue }

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
    return findWicaElementsWithAttributeNameAlsoInShadowDom( document, WicaElementConnectionAttributes.channelName );
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
* Finds all wica-aware HTML elements in the current document whose attribute name
* matches the specified value.
*
* @param {ParentNode} parentNode - the node at which to start searching.
* @param {!string} attributeName - The attribute name to target.
* @returns {NodeListOf<Element>} - The result list.
*/
function findWicaElementsWithAttributeNameAlsoInShadowDom( parentNode, attributeName )
{
    const selector = "[" + attributeName + "]";
    const nodesInParent = parentNode.querySelectorAll( selector );
    let nodesInChildren = [];
    Array.from( parentNode.querySelectorAll('*') )
        .filter( element => element.shadowRoot )
        .forEach( element => {
            const nodesInChild = findWicaElementsWithAttributeNameAlsoInShadowDom( element.shadowRoot, attributeName );
            const nodesInChildAsArray = Array.from( nodesInChild );
            nodesInChildren = nodesInChildren.concat( nodesInChildAsArray );
        });

    return [ ...nodesInParent, ...nodesInChildren ];
}
/**
 * Finds all wica-aware HTML elements in the current document with the specified wica channel name.
 *
 * @param {!string} channelName - The channel name to search for.
 * @returns {NodeListOf<Element>} - The result list.
 */
function findWicaElementsWithChannelName( channelName )
{
    return findWicaElementsWithAttributeValueAlsoInShadowDom( document, WicaElementConnectionAttributes.channelName, channelName );
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

/**
 * Finds all wica-aware HTML elements in the current document whose attribute name
 * matches the specified value.
 *
 * @param {ParentNode} parentNode - the node at which to start searching.
 * @param {!string} attributeName - The attribute name to target.
 * @param {!string} attributeValue - The attribute value to target.
 * @returns {Array<Element>} - The result list.
 */
function findWicaElementsWithAttributeValueAlsoInShadowDom( parentNode, attributeName, attributeValue )
{
    const selector = "*[" + attributeName + " = \'" + attributeValue + "\']";
    const nodesInParent = parentNode.querySelectorAll( selector );

    let nodesInChildren = [];
    Array.from( parentNode.querySelectorAll('*') )
        .filter( element => element.shadowRoot)
        .forEach( element => {
            const nodesInChild = findWicaElementsWithAttributeValueAlsoInShadowDom( element.shadowRoot, attributeName, attributeValue );
            const nodesInChildAsArray = Array.from( nodesInChild );
            nodesInChildren = nodesInChildren.concat( nodesInChildAsArray );
        });

    return  [ ...nodesInParent, ...nodesInChildren ];
}
