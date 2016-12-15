"use strict";

const inspect = require('util').inspect;
const log = require('winston');
const logUtil = require('./server/logUtil');
const c = require('./server/config');

/**
 * DB Setup (automagic within module)
 */
require('./server/DbManager');

/**
 * Logging Setup
 */
var logOptions =
{
    timestamp: logUtil.logTimestampFormatter,
    json: false,
    level: c.LOG_LEVEL,
    colorize: false,
    handleExceptions: true,
    exitOnError: false
};

log.remove(log.transports.Console);
log.add(log.transports.Console, logOptions);
log.setLevels(log.config.syslog.levels);

logUtil.createLogger({transport: logUtil.transports.Console});

/**
 * TFTP Server Setup
 */
var TFTP = require('./server/TFTP').TFTP;
log.debug(`Starting the server...`);

var server = new TFTP();
log.debug(`New TFTP Server Initiated...`);
server.createServer();

server.on('error', (err) =>
{
    log.error(`Error identified on TFTP Server: ${inspect(err)}`);
});

server.on('uncaughtException', (err) =>
{
    log.debug(`Uncaught TFTP Server Exception: ${err}`);
    server.shutdown();
});

server.on('close', () =>
{
    log.debug(`Server shutdown completed.`);
});