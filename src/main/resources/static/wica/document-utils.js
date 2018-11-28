export {
    findWicaElements,
    findWicaElementsWithChannelName
}

/**
 * Finds all HTML elements in the current document which are wica aware.
 *
 * @returns {NodeListOf<Element>} the result list.
 */
function findWicaElements() {
    const elements =  document.querySelectorAll('[data-wica-channel-name]');
    return elements;
}

/**
 * Finds all HTML elements in the current document with the specified wica channel name.
 *
 * @param target the channel name to search for.
 * @returns {NodeListOf<Element>}  the result list.
 */
function findWicaElementsWithChannelName( target ) {
    let selector = "*[data-wica-channel-name = \'" + target + "\']";
    return document.querySelectorAll(selector);
}
