"use strict";

const express = require('express');
const bodyParser = require('body-parser');
const C = require('./config').config;
const inspect = require('util').inspect;
const log = require('winston');
const logUtil = require('./logUtil');
const os = require('os');
const cluster = require('cluster');

/**
 * Logging Setup
 */
var logOptions =
{
    timestamp: logUtil.logTimestampFormatter,
    json: false,
    level: C.LOG_LEVEL,
    colorize: false,
    handleExceptions: true,
    exitOnError: false
};

log.remove(log.transports.Console);
log.add(log.transports.Console, logOptions);
log.setLevels(log.config.syslog.levels);

logUtil.createLogger({transport: logUtil.transports.Console});

const returnValues = new Array(100);

/**
 * Set up percentages
 */
log.debug("Setting up request percentages");
var totalPercentage = 0;
var low = 0;
var high = 0;
for (var i = 0; i < C.REQUESTS.length; i++)
{
    var data = C.REQUESTS[i];
    totalPercentage += data.percent;

    if (totalPercentage > 100)
    {
        log.error(`${totalPercentage}% is greater than 100%. Shutting down.`);
        process.exit(1);
        return;
    }

    // Otherwise, lets
    low = high + 1;
    high = totalPercentage;
    for (var j = low; j <= high; j++)
    {
        returnValues[j] = data.code;
    }
}

const getStatusCode = function()
{
    return returnValues[Math.floor(Math.random() * (100))];
};

/**
 * Set up server application
 */

if (cluster.isMaster)
{
    for (var i = 0; i < os.cpus().length; i++)
    {
        cluster.fork();
    }
}
else
{
    log.debug(`Starting worker [${cluster.worker.id}]`);
    const app = express();

    app.use(bodyParser.urlencoded({
        extended: true
    }));

    app.use(bodyParser.json());

    app.post('/post', function (req, res) {
        res.status(getStatusCode());
        log.debug(`Got a /post request on worker [${cluster.worker.id}] with data [${JSON.stringify(req.body)}]. Returning status code [${res.statusCode}]`);
        res.send();
    });

    app.listen(C.HTTP_PORT, function() {
        log.debug(`CDR Post Simulator running on port ${C.HTTP_PORT}`);
    });
}
