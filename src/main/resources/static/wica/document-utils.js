/**
 * Provides helper functions for wica-aware html pages.
 * @module
 */

export {
    findWicaElements,
    findWicaElementsWithChannelName
}

/**
 * Finds all wica-aware HTML elements in the current document.
 *
 * @returns {NodeListOf<Element>} the result list.
 */
function findWicaElements() {
    const elements = document.querySelectorAll('[data-wica-channel-name]');
    return elements;
}

/**
 * Finds all wica-aware HTML elements in the current document with the specified wica channel name.
 *
 * @param target the channel name to search for.
 * @returns {NodeListOf<Element>}  the result list.
 */
function findWicaElementsWithChannelName( target ) {
    let selector = "*[data-wica-channel-name = \'" + target + "\']";
    return document.querySelectorAll(selector);
}
