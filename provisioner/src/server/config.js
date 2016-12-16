"use strict";

module.exports =
{
    DIR: "/interact/provisioner/root", // Root directory of TFTP server
    MAX_RETRANSMITS: 4, // Xmitted 4 times before failure
    TRANSMIT_TIMEOUT: 3000, // In Milliseconds
    MAX_WRITE_FILESIZE: 100000, // In bytes, default set at 100k bytes. Any SIP file should not be greater than that.
    MAX_BLOCK_SIZE: 1468, // Should be set as the smallest MTU value on the system - 32 bytes, Ex: 1500 - 32 = 1468
    MYSQL_HOST: "localhost",
    MYSQL_USER: "root",
    MYSQL_PASS: "",
    MYSQL_DB: "listen2",
    MYSQL_PORT: 3306,
    MYSQL_ENABLED: true,
    LOG_LEVEL: "debug", // winston syslog levels {"debug":0,"info":1,"notice":2,"warning":3,"error":4,"crit":5,"alert":6,"emerg":7}
    IP_BLACKLIST: [],
    IP_WHITELIST: []
};