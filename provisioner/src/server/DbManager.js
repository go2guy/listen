"use strict";

const mysql = require('mysql');
const inspect = require('util').inspect;
const c = require('./config');
const log = require('winston');
const Promise = require('bluebird'); // jshint ignore:line

var pool = mysql.createPool({
    acquireTimeout: 10000,
    connectionLimit: 10,
    waitForConnections: true, // Queues the connection request. False = immediately calls back with err
    queueLimit: 0, // No Limit
    host: c.MYSQL_HOST,
    user: c.MYSQL_USER,
    password: c.MYSQL_PASS,
    database: c.MYSQL_DB,
    port: c.MYSQL_PORT,
    insecureAuth: true
});

/**
 * Gets connection from the database connection pool.
 * @return {Promise}
 */
var getConnection = function()
{
    return new Promise((resolve, reject) =>
    {
        pool.getConnection((err, conn) =>
        {
            if (err)
            {
                return reject(err);
            }

            try
            {
                conn.ping((err) =>
                {
                    if (err)
                    {
                        conn.connect((err) =>
                        {
                            if (err)
                            {
                                return reject(err);
                            }
                            // We have a connection
                            resolve(conn);
                        });
                        return;
                    }
                    resolve(conn);
                });
            }
            catch (err)
            {
                reject(err);
            }
        });
    });
};

/**
 * Releases given connection back to the database connection pool.
 * @param connection
 */
var releaseConnection = function(connection)
{
    try
    {
        connection.release();
    }
    catch (err)
    {
        log.error(`Query - failed with error [${err}] releasing connection thread [${connection.threadId}] back to pool`);
    }
};

/**
 * Takes SQL and values, runs the query and releases the connection back to the database connection pool.
 * @param sql
 * @param a
 * @return {Promise}
 */
var query = function(sql, a)
{
    var values = a || [];

    return new Promise((resolve, reject) =>
    {
        getConnection()
            .then((conn) =>
            {
                conn.query(sql, values, (err, results) =>
                {
                    if (err)
                    {
                        return conn.rollback(() =>
                        {
                            releaseConnection(conn);
                            reject(err);
                        });
                    }
                    releaseConnection(conn);
                    resolve(results);
                });
            })
            .catch((err) =>
            {
                reject(err);
            });
    });
};

var queryTransaction = function(sql, values)
{
    return new Promise((resolve, reject) =>
    {
        getConnection()
            .then((conn) =>
                {
                    conn.beginTransaction((err) =>
                    {
                        if (err)
                        {
                            log.error(`Failed to begin transaction: ${inspect(err)}`);
                            return reject(err);
                        }

                        conn.query(sql, values, (err, result) =>
                        {
                            if (err)
                            {
                                log.error(`Failed to execute query: ${inspect(err)}`);
                                return reject(err);
                            }

                            conn.commit((err) =>
                            {
                                if (err)
                                {
                                    log.error(`Failed to commit: ${inspect(err)}`);
                                    return conn.rollback(() =>
                                    {
                                        reject(err);
                                    });
                                }

                                resolve(result);
                            });

                            releaseConnection(conn);
                        });
                    });
                })
            .catch((err) =>
                {
                    reject(err);
                });
    });
};

exports.getConnection = getConnection;
exports.query = query;
exports.queryTransaction = queryTransaction;
