/**
 * My sepcial constant
 * @constant
 * @default
 * @type {string}
 */
export const MY_SPECIAL_CONSTANT = "abcdef";


/**
 * GHIJK
 * @type {string}
 */

export class WicaElementAttributes
{
    constructor( STREAM_STATE= MY_SPECIAL_CONSTANT )
    {
        this.STREAM_STATE             = "data-wica-stream-state";
        this.CHANNEL_NAME             = "data-wica-channel-name";
        this.CHANNEL_PROPERTIES       = "data-wica-channel-props";
        this.CHANNEL_METADATA         = "data-wica-channel-metadata";
        this.CHANNEL_VALUE_ARRAY      = "data-wica-channel-value-array";
        this.CHANNEL_VALUE_LATEST     = "data-wica-channel-value-array";
        this.CHANNEL_CONNECTION_STATE = "data-wica-channel-connection-state";
        this.CHANNEL_ALARM_STATE      = "data-wica-channel-alarm-state";
    }

    get STREAM_STATE() { return this.STREAM_STATE; }
    get CHANNEL_NAME() { return this.CHANNEL_NAME; }
}

