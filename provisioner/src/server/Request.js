"use strict";

const inspect = require('util').inspect;
const fs = require('fs');
const OpCode = require('./OpCode');
const Packet = require('./Packet');
const ErrorCode = require('./ErrorCode');
const ProvisionerTemplate = require('./ProvisionerTemplate');
const SipPhone = require('./SipPhone');
const u = require('underscore');
const c = require('./config');
const log = require('winston');
const Promise = require('bluebird'); // jshint ignore: line
const path = require('path');
const CustomError = require('./CustomError');

class Request
{
    /**
     * Constructor for Request Class. Takes dgram rinfo object
     * @param rinfo
     */
    constructor(rinfo)
    {
        this.address = null;
        this.port = null;
        this.metadata = {};

        if (rinfo)
        {
            this.address = rinfo.address;
            this.port = rinfo.port;
            this.metadata = {port: rinfo.port, address: rinfo.address};
        }

        this.block = 0;
        this.buffer = null;
        this.filename = null;
        this.packetToSend = null;
        this.end = false;

        this.sendTimeout = null;
        this.retransmits = 0;

        // Configurable options based on RRQ/WRQ
        this.options = [];
        this.blksize = null;
        this.tsize = null;
        this.timeout = null;
    }

    /**
     * Identifies type of packet and processes it.
     * @param packet {Packet} Packet
     */
    processPacket(packet)
    {
        return new Promise((resolve) =>
        {
            log.debug(`Received\t[${packet.constructor.name}]\tsize [${packet.msg.length}]\tblock [${packet.block || " "}]\t`, this.metadata);

            // Clear the timeout. This assumes all goes well (we'll see...)
            this.clearSendTimeout();

            switch (packet.opcode)
            {
                case OpCode.RRQ:
                    this.processRRQ(packet, resolve);
                    break;
                case OpCode.WRQ:
                    this.processWRQ(packet, resolve);
                    break;
                case OpCode.ACK:
                    this.processAck(packet, resolve);
                    break;
                case OpCode.DATA:
                    this.processData(packet, resolve);
                    break;
                case OpCode.ERROR:
                    this.processError(packet, resolve);
                    break;
                case OpCode.OACK:
                    this.processOAck(packet, resolve);
                    break;
            }

        });
    }

    /**
     * Processes recieved RRQ packet.
     * @param packet
     * @param resolve
     */
    processRRQ(packet, resolve)
    {
        this.filename = packet.filename;
        this.metadata.filename = packet.filename;

        log.debug(`Processing RRQ [${inspect(packet)}]`, this.metadata);

        if (packet.options.length > 0)
        {
            this.options = packet.options;
            for (var i = 0; i < packet.options.length; i++)
            {
                this[packet.options[i].toLowerCase()] = packet[packet.options[i].toLowerCase()];
            }
        }

        this.getFile(this.filename)
            .then((data) =>
            {
                try {
                    this.buffer = data;
                    this.tsize = this.buffer.length; // Set tsize as length of file.
                    this.packetToSend = this.options.length > 0 ? this.createOAckPacket() : this.createDataPacket();
                    // Reset block to 0
                    this.block = this.options.length > 0 ? 0 : 1;
                    resolve();
                } catch (err) {
                    log.error("Caught getFile error: "+inspect(err));
                    throw err;
                }
            })
            .catch((err) =>
            {
                log.error(`Could not retrieve file [${packet.filename}]`, this.metadata);
                this.packetToSend = typeof(err.errorCode) !== "undefined" ? this.createErrorPacket(err.errorCode, err.errorMessage) : this.createErrorPacket(ErrorCode.FILE_NOTFOUND, err.errorMessage);
                resolve();
            });
    }

    /**
     * Processes received WRQ packet.
     * @param packet
     * @param resolve
     */
    processWRQ(packet, resolve)
    {
        this.filename = packet.filename;
        this.metadata.filename = packet.filename;

        log.debug(`Processing WRQ [${inspect(packet)}]`, this.metadata);

        if (packet.options.length > 0)
        {
            this.options = packet.options;
            for (var i = 0; i < packet.options.length; i++)
            {
                this[packet.options[i].toLowerCase()] = packet[packet.options[i].toLowerCase()];
            }
        }

        var fullpath = path.normalize(path.join(c.DIR, this.filename));
        var parsedpath = path.parse(fullpath);

        if (parsedpath.dir.indexOf(c.DIR) === -1)
        {
            log.error(`The file [${fullpath}] is not allowed to be accessed on this system.`);
            this.packetToSend = this.createErrorPacket(ErrorCode.ACCESS_VIOLATION, "Attempt to write file outside of root directory.");
            resolve();
            return;
        }

        if (this.tsize && this.tsize > c.MAX_WRITE_FILESIZE)
        {
            this.packetToSend = this.createErrorPacket(ErrorCode.DISK_FULL, "Max allocation of "+c.MAX_WRITE_FILESIZE+" exceeded");
            resolve();
            return;
        }

        this.packetToSend = this.options.length > 0 ? this.createOAckPacket() : this.createAckPacket();
        resolve();
    }

    /**
     * Processes received Ack packet.
     * @param packet
     * @param resolve
     */
    processAck(packet, resolve)
    {
        // If blocks are the same, that means it is NOT a duplicate
        if (!this.filename || !this.buffer)
        {
            log.error(`No file data associated with request`, this.metadata);
            this.packetToSend = this.createErrorPacket(ErrorCode.UNKNOWN_XFER_ID, "Unknown Transfer ID");
            resolve();
            return;
        }

        if (packet.block === this.block)
        {
            // Check if this is the last ack packet
            if (Math.min((this.block)*(this.blksize || 512), this.buffer.length) === this.buffer.length)
            {
                // This is the last data request we should be getting...
                this.end = true;
                this.packetToSend = null;
                resolve();
            }

            this.packetToSend = this.createDataPacket();
            resolve();
            return;
        }
    }

    /**
     * Processes received error packet
     * @param packet
     * @param resolve
     */
    processData(packet, resolve)
    {
        // Add to the buffer
        if (!this.filename)
        {
            log.error(`No filename associated with request`, this.metadata);
            // Error out here
            this.packetToSend = this.createErrorPacket(ErrorCode.UNKNOWN_XFER_ID, "Unknown Transfer ID");
            resolve();
            return;
        }

        if (!this.buffer)
        {
            this.buffer = new Buffer(0);
            this.blksize = this.blksize || 512;
        }

        this.packetToSend = this.createAckPacket();

        var len = this.buffer.length + packet.data.length;
        this.buffer = Buffer.concat([this.buffer, packet.data], len);

        if (this.buffer.length > c.MAX_WRITE_FILESIZE)
        {
            this.packetToSend = this.createErrorPacket(ErrorCode.DISK_FULL, "Max File Size Exceeded");
            log.error(`Max File Size of [${c.MAX_WRITE_FILESIZE}] bytes exceeded, buffer size [${this.buffer.length}] bytes`);
            resolve();
            return;
        }

        if (packet.data.length < this.blksize)
        {
            var fullpath = path.normalize(path.join(c.DIR, this.filename));

            // Save to file
            fs.open(fullpath, 'w', (err, fd) =>
            {
                if (err)
                {
                    this.packetToSend = this.createErrorPacket(ErrorCode.NOT_DEFINED, "Error Creating/Opening File");
                    resolve();
                    return;
                }

                fs.write(fd, this.buffer, 0, this.buffer.length, null, (err) =>
                {
                    if (err)
                    {
                        this.packetToSend = this.createErrorPacket(ErrorCode.NOT_DEFINED, "Error Writing File");
                        resolve();
                        return;
                    }

                    fs.close(fd, (err) =>
                    {
                        if (err)
                        {
                            this.packetToSend = this.createErrorPacket(ErrorCode.NOT_DEFINED, "Error Writing File");
                            resolve();
                            return;
                        }
                        log.debug(`File [${this.filename}] successfully written and closed`, this.metadata);
                        resolve();
                        this.end = true;
                    });
                });
            });

            return;
        }

        resolve();
    }

    /**
     * Processes received error packet.
     * @param packet
     * @param resolve
     */
    processError(packet, resolve)
    {
        log.error(`Error: Code ${packet.errorCode}, Message: ${packet.errorMessage}`, this.metadata);
        this.packetToSend = null;
        this.end = true;
        resolve();
    }

    /**
     * Processes OAck packet. Not currently implemented as we are not a client.
     * @param packet
     * @param resolve
     */
    processOAck(packet, resolve)
    {
        log.error(`processOAck is not currently implemented`, this.metadata);
        this.packetToSend = this.createErrorPacket(ErrorCode.ILLEGAL_OP, "OAck not implemented");
        this.end = true;
        resolve();
    }

    /**
     * Gets the file from the configured directory and filename given, or the database if file does not exist.
     * @param filename
     * @return {Promise}
     */
    getFile(filename)
    {
        return new Promise((resolve, reject) =>
        {
            var template = null;
            var templateId = null;
            var values = {};

            var fullpath = path.normalize(path.join(c.DIR, filename));
            var parsedpath = path.parse(fullpath);

            log.debug(`Looking up file [${fullpath}]`, this.metadata);

            if (parsedpath.dir.indexOf(c.DIR) === -1)
            {
                log.error(`The file [${fullpath}] is not allowed to be accessed on this system.`);
                reject({errorCode: ErrorCode.ACCESS_VIOLATION, errorMessage: "Attempt to access file outside of root directory."});
                return;
            }

            fs.exists(fullpath, (exists) =>
            {
                if (!exists)
                {
                    if (!c.MYSQL_ENABLED) {
                        reject({errorCode: ErrorCode.FILE_NOTFOUND, errorMessage: "File Not Found"});
                        return;
                    }

                    // Check db now
                    log.debug(`[${fullpath}] does not exist on filesystem`, this.metadata);

                    // Check if Mitel, because mitel has to be a special little freaking snowflake...
                    if (parsedpath.name.indexOf("MN_") !== -1 && parsedpath.name.length === 15)
                    {
                        parsedpath.name = parsedpath.name.slice(-12).toLowerCase();
                    }

                    if (parsedpath.ext === ".cfg" && parsedpath.name.length === 12)
                    {
                        log.debug(`Looking for mac address ${parsedpath.name} in database`, this.metadata);
                        SipPhone.getSipPhoneDataByMac(parsedpath.name)
                            .then((row) =>
                            {
                                // Have id, get template
                                templateId = row.provisioner_template_id;

                                // Automatically included values
                                values.number = row.number;
                                values.sipPhoneId = row.id;
                                values.organizationId = row.organization_id;
                                values.username = row.username;
                                values.password = row.password;
                                values.userId = row.phone_user_id;
                                values.realName = row.real_name;
                                values.serveraddress = "";

                                return ProvisionerTemplate.getProvisionerTemplate(templateId);
                            })
                            .then((row) =>
                            {
                                // Have template, get rows
                                template = row.template;
                                var compiledTemplate = u.template(template);
                                var result = new Buffer(compiledTemplate(values));
                                resolve(result);

                                SipPhone.updateSipPhoneProvisionerLastUpdated(values.sipPhoneId, new Date());

                                // @TODO: Implement Custom Fields
//                            return ProvisionerTemplate.getProvisionerTemplateFields(templateId);
                            })
//                        .then((rows) => {
//                            for (var i = 0; i < rows.length; i++) {
//                                values[rows[i].name] = rows[i].field_value || "";
//                            }
//
//                            var compiledTemplate = u.template(template);
//                            var result = new Buffer(compiledTemplate(values));
//                            log.debug(result.toString())
//                            resolve(result);
//                        })
                            .catch((err) =>
                            {
                                log.error(`Mac [${parsedpath.name}] not found.`, this.metadata);
                                reject({errorCode: ErrorCode.FILE_NOTFOUND, errorMessage: "File Not Found"});
                            });
                        return;
                    }

                    reject({errorCode: ErrorCode.FILE_NOTFOUND, errorMessage: "File Not Found"});
                    return;
                }

                fs.readFile(fullpath, (err, data) =>
                {
                    if (err)
                    {
                        log.debug(`Error reading file [${fullpath}]:\n ${err}`);
                        reject({errorCode: ErrorCode.NOT_DEFINED, errorMessage: "Internal Server Error"});
                        return;
                    }
                    log.debug(`[${fullpath}] exists. Reading into memory.`, this.metadata);
                    resolve(data);
                });
            });
        });
    }

    /**
     * Creates a RRQ packet. Not currently implemented as we are not a client
     * @return {exports.RRQPacket}
     */
    createRRQPacket()
    {
        var packet = new Packet.RRQPacket();
        return packet;
    }

    /**
     * Creates WRQ packet. Not currently implemented as we are not a client
     * @return {exports.WRQPacket}
     */
    createWRQPacket()
    {
        var packet = new Packet.WRQPacket();
        return packet;
    }

    /**
     * Creates Ack packet and increments block
     * @return {exports.AckPacket}
     */
    createAckPacket()
    {
        var packet = new Packet.AckPacket();
        packet.serialize(this.block); // block starts at 0 for Ack

        this.block += 1; // Increment block
        return packet;
    }

    /**
     * Creates Data packet and increments block. Buffer must contain the data at this point.
     * @return {exports.DataPacket}
     */
    createDataPacket()
    {
        // Check if there is data
        if (!this.buffer)
        {
            return this.createErrorPacket(ErrorCode.ILLEGAL_OP, "No Data for Data Packet");
        }

        this.block += 1; // Increment block here. Starts at 1 for sendData
        var start = (this.block - 1)*(this.blksize || 512);
        var end = Math.min((this.block)*(this.blksize || 512), this.buffer.length);
        if (start >= end)
        {
            // No packet to send
            this.end = true;
            return null;
        }
        var packet = new Packet.DataPacket();
        var data = this.buffer.slice(start, end);
        packet.serialize(this.block, data);

        return packet;
    }

    /**
     * Creates Error packet
     * @param errCode
     * @param errMessage
     * @return {exports.ErrorPacket}
     */
    createErrorPacket(errCode, errMessage)
    {
        var packet = new Packet.ErrorPacket();
        this.error =
        {
            errorCode: errCode,
            errorMessage: errMessage
        };
        packet.serialize(errCode, errMessage);
        return packet;
    }

    /**
     * Creates OAck packet and increments block. Used to respond to RRQ and WRQ that contain options
     * @return {exports.OAckPacket}
     */
    createOAckPacket()
    {
        var packet = new Packet.OAckPacket();
        // Check if blksize is greater than MTU
        if (this.blksize && parseInt(this.blksize, 10) > c.MAX_BLOCK_SIZE)
        {
            this.blksize = c.MAX_BLOCK_SIZE;
        }

        packet.serialize(this.options, {
            blksize: this.blksize ? this.blksize.toString() : null,
            tsize: this.tsize ? this.tsize.toString() : null,
            timeout: this.timeout ? this.timeout.toString() : null
        });
        this.block += 1; // Increment block
        return packet;
    }

    clearSendTimeout()
    {
        if (this.sendTimeout)
        {
            clearTimeout(this.sendTimeout);
            this.retransmits = 0;
        }
    }

    setSendTimeout(callback)
    {
        var timeout = this.timeout;
        if (this.timeout && this.timeout.length < 3)
        {
            timeout = parseInt(this.timeout, 10)*1000;
        }

        timeout = timeout || c.TRANSMIT_TIMEOUT;

        this.sendTimeout = setTimeout(callback, timeout);
    }
}

module.exports.Request = Request;