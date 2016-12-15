"use strict";

const dgram = require('dgram');
const EventEmitter = require('events');
const inspect = require('util').inspect;
const Packet = require('./Packet');
const Request = require('./Request').Request;
const c = require('./config');
const CustomError = require('./CustomError');
const log = require('winston');
const ErrorCode = require('./ErrorCode');
const fs = require('fs');

class TFTP extends EventEmitter
{
    constructor(address, port)
    {
        super();
        this.address = address || '127.0.0.1';
        this.port = port || 69;

        this.socket = null;
        this.requests = new Array(65535);

        for (var i = 0; i < 65535; i++)
        {
            this.requests[i] = new Map();
        }

        this.blacklist = null;
        this.whitelist = null;
    }

    /**
     * Returns a regular expression of ip's from the list, or null if none in the list
     * @param list
     * @return {RegExp}
     */
    parseIPList(list)
    {
        list = list || [];
        var rlist = [];
        for (var i = 0; i < list.length; i++)
        {
            var parts = list[i].split(".");
            var re = "";

            while (parts.length < 4)
            {
                parts[parts.length] = null;
            }

            for (var j = 0; j < 4; j++)
            {
                re += (parts[j] || "\\d+") + (j !== (parts.length-1) ? '\\.' : '');
            }

            log.info(`Adding regex ${re} to list`);
            rlist.push(re);
        }

        return rlist.length > 0 ? new RegExp(rlist.join("|"), "i") : null;
    }

    /**
     * Creates the Server and binds the port to the socket.
     * @return {*}
     */
    createServer()
    {
        if (this.socket !== null)
        {
            return;
        }

        // Setup blacklist
        log.info(`Parsing IP Blacklist: ${c.IP_BLACKLIST}`);
        this.blacklist = this.parseIPList(c.IP_BLACKLIST);
        log.info(`Parsing IP Whitelist: ${c.IP_WHITELIST}`);
        this.whitelist = this.parseIPList(c.IP_WHITELIST);

        // Connect and set up the event handling
        this.socket = dgram.createSocket('udp4');

        this.socket.on('listening', () =>
        {
            var address = this.socket.address();
            log.debug(`listening on ${address.address}:${address.port}`);
            this.emit('listening', this.socket);
        });

        this.socket.on('error', (error) =>
        {
            log.error(`Error on socket:\n ${inspect(error)}`);
            this.socket.close();
            this.emit('error', new CustomError.TFTPError(error));
        });

        this.socket.on('message', (msg, rinfo) =>
        {
            // Check blacklist if enabled
            if(this.blacklist && this.blacklist.test(rinfo.address))
            {
                log.error(`IP matches a blacklist entry.`, {port: rinfo.port, address: rinfo.address});
                return;
            }

            // Check whitelist if enabled
            if (this.whitelist && !this.whitelist.test(rinfo.address))
            {
                log.error(`IP not in whitelist.`, {port: rinfo.port, address: rinfo.address});
                return;
            }

            // Sets up a new request if one is not already present
            var request = null;
            if (this.requests[rinfo.port].has(rinfo.address))
            {
                request = this.requests[rinfo.port].get(rinfo.address);
            }
            else
            {
                request = new Request(rinfo, this);
                log.info(`New request generated`, request.metadata);
                this.requests[rinfo.port].set(rinfo.address, request);
            }

            // Creates a packet that request must handle
            var packet = Packet.Packet.fromMsg(msg);

            request.processPacket(packet)
                .then(() =>
                {
                    // Clear any timeouts that were originally on this request.
                    if (!request.packetToSend && request.end)
                    {
                        this.closeRequest(request);
                    }

                    if (request.packetToSend)
                    {
                        this.send(request);
                    }
                })
                .catch((err) =>
                {
                    log.error(`Error parsing packet`, request.metadata);
                    log.error(inspect(err));
                    if (!(request.packetToSend instanceof Packet.ErrorPacket))
                    {
                        request.packetToSend = request.createErrorPacket(ErrorCode.NOT_DEFINED, "Internal Server Error");
                        this.send(request);
                    }
                });
            return;
        });

        this.socket.bind(this.port);

        return this;
    }

    /**
     * Closes the given request and removes it from memory
     * @param request
     */
    closeRequest(request)
    {
        // This deletes this request for memory cleanup
        log.info(`Closing Request`, request.metadata);
        request.clearSendTimeout();
        this.requests[request.port].delete(request.address);
    }

    /**
     * Sends a packet on the request port and address, based on request.packetToSend
     * @TODO: Maybe this could be designed a bit better so it doesn't have to rely on request? This feels dirty...
     * @param request
     */
    send(request)
    {
        if (!request.packetToSend)
        {
            return;
        }

        log.debug(`Sending\t[${request.packetToSend.constructor.name}]\tsize [${request.packetToSend.msg.length}]\tblock [${request.packetToSend.block || " "}]\t`, request.metadata);

        var timeoutCallback = () =>
        {
            if (c.MAX_RETRANSMITS > ++request.retransmits)
            {
                log.debug(`Sending packet ${request.packetToSend.id} timed out. Attempt [${request.retransmits}] Retransmitting...`, request.metadata);
                this.send(request);
                return;
            }

            log.error(`Sending packet ${request.packetToSend.id} timed out after 3 retries. Closing request`, request.metadata);
            this.closeRequest(request);
        };

        // Send the packet
        this.socket.send(
            request.packetToSend.msg,
            0,
            request.packetToSend.msg.length,
            request.port,
            request.address,
            (err) =>
            {
                if (err)
                {
                    // Attempt a resend after timeout. If a new packet comes in, this timeout will be cleared anyways
                    log.error(`Error sending packet:\n ${request.packetToSend.id}\n ${inspect(err)}`);
                    request.setSendTimeout(timeoutCallback);
                    return;
                }

                request.setSendTimeout(timeoutCallback);

                if (request.packetToSend instanceof Packet.ErrorPacket || request.end)
                {
                    this.closeRequest(request);
                }
            }
        );
    }

    /**
     * Shuts down the socket
     */
    shutdown()
    {
        this.socket.close(() =>
        {
            this.emit('close', this);
        });
    }
}

module.exports.TFTP = TFTP;