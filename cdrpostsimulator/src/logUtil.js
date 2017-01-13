"use strict";

var C = require('./config').config;
var log = require('winston');
var loggingLevels = ["debug", "info", "notice", "warning", "error", "crit", "alert", "emerg"];

/**
 * Type of transport we want to modify
 * @type {{Console: string, File: string}}
 */
var transports = {
    Console: "Console",
    File: "File"
};

/**
 * Formats the timestamp for all winston logging.
 * @returns {string}
 */
var logTimestampFormatter = function logTimestampFormatter()
{
    var addZero = function (x, n) {
        while (x.toString().length < n)
        {
            x = '0' + x;
        }
        return x;
    };

    var date = new Date(Date.now());

    return date.getFullYear() + '/' + addZero(date.getMonth() + 1, 2) + '/' + date.getDate() + ' ' +
        addZero(date.getHours(), 2) + ":" + addZero(date.getMinutes(), 2) + ":" + addZero(date.getSeconds(), 2) + ":" + addZero(date.getMilliseconds(), 3);
};

// Base Log Options for COnsole & File
var logOptions =
{
    Console:
    {
        timestamp: logTimestampFormatter,
        json: false,
        level: C.LOG_LEVEL,
        colorize: false,
        handleExceptions: true,
        exitOnError: false
    },
    File:
    {
        timestamp: logTimestampFormatter,
        json: false,
        level: C.LOG_LEVEL,
        colorize: false,
        handleExceptions: true,
        exitOnError: false,
        filename: '/interact/logs/vex_default.log',
        options:
        {
            flags: 'a',
            highWaterMark: 24
        }
    }
};

/**
 * Creates the logger
 * @param params - an object with transport, optional workerThread
 */
var createLogger = function createLogger(params)
{
    log.remove(log.transports.Console);
    log.add(log.transports[params.transport], logOptions[params.transport]);
    log.setLevels(log.config.syslog.levels);
};

/**
 * Changes the winston log level to the given level & for the given transport
 * @param level
 * @param transport
 * @returns {boolean}
 */
var changeLogLevel = function changeLogLevel(params)
{
    params.level = params.level.toLowerCase();
    if (loggingLevels.indexOf(params.level) > -1)
    {
        logOptions[params.transport].level = params.level;
        log.remove(log.transports[params.transport]);
        log.add(log.transports[params.transport], logOptions[params.transport]);
        log.setLevels(log.config.syslog.levels);
        return true;
    }

    return false;
};

exports.changeLogLevel = changeLogLevel;
exports.createLogger = createLogger;
exports.transports = transports;
exports.logTimestampFormatter = logTimestampFormatter;