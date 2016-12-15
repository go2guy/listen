"use strict";

/**
 * TFTP Protocol Error Codes
 * @type {{NOT_DEFINED: number, FILE_NOTFOUND: number, ACCESS_VIOLATION: number, DISK_FULL: number, ILLEGAL_OP: number, UNKNOWN_XFER_ID: number, FILE_EXISTS: number, NO_USER: number, BAD_OPTION_NEG: number}}
 */
module.exports =
{
    NOT_DEFINED: 0,
    FILE_NOTFOUND: 1,
    ACCESS_VIOLATION: 2,
    DISK_FULL: 3,
    ILLEGAL_OP: 4,
    UNKNOWN_XFER_ID: 5,
    FILE_EXISTS: 6,
    NO_USER: 7,
    BAD_OPTION_NEG: 8
};