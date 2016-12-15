"use strict";

const OpCode = require('./OpCode');
const crypto = require('crypto');
const ErrorCode = require('./ErrorCode');
const log = require('winston');
const inspect = require('util').inspect;

const supportedOptions =
    [
        "blksize",
        "tsize",
        "timeout"
    ];

/**
 * Generic Packet.
 */
class Packet
{
    constructor(msg)
    {
        this.id = crypto.randomBytes(12).toString('hex'); // Lets give it an id so we can keep track of retries
        this.opcode = null;
        this.msg = null;
        if (msg)
        {
            this.msg = msg;
        }
    }

    static fromMsg(msg)
    {
        var code = msg.readUInt16BE(0);
        var packet = null;

        switch (code)
        {
            case OpCode.RRQ:
                packet = new RRQPacket(msg);
                break;
            case OpCode.WRQ:
                packet = new WRQPacket(msg);
                break;
            case OpCode.ACK:
                packet = new AckPacket(msg);
                break;
            case OpCode.DATA:
                packet = new DataPacket(msg);
                break;
            case OpCode.ERROR:
                packet = new ErrorPacket(msg);
                break;
            case OpCode.OACK:
                packet = new OAckPacket(msg);
                break;
            default:
                packet = new ErrorPacket();
                packet.serialize(ErrorCode.ILLEGAL_OP, "Unknown Operation");
        }

        return packet;
    }

    // Implement this
    serialize() {}

    // Implement this
    deserialize() {}
}

/**
 * Read Request Packet
 2 bytes     string    1 byte     string   1 byte
 ------------------------------------------------
 | Opcode |  Filename  |   0  |    Mode    |   0  |
 ------------------------------------------------
 */
class RRQPacket extends Packet
{
    constructor(msg)
    {
        super(msg);
        this.filename = null;
        this.mode = null;
        this.blksize = 512; // default block size
        this.opcode = OpCode.RRQ;
        this.options = [];
        if (this.msg)
        {
            this.deserialize();
        }
    }

    deserialize()
    {
        var data = this.msg.toString('ascii', 2, this.msg.length-1).split('\u0000');
        this.filename = data[0];
        this.mode = data[1];
        if (data.length > 2 && data.length % 2 === 0)
        {
            this.parseOptions(data.slice(2, data.length));
        }

        return this;
    }

    /**
     * Not implemented/tested due to us not being a client
     * @param filename
     * @param mode
     * @return {RRQPacket}
     */
    serialize(filename, mode)
    {
        if (filename)
        {
            this.filename = filename;
        }

        if (mode)
        {
            this.mode = mode;
        }

        var bytes = 0;
        var buffer = new Buffer();
        this.msg = buffer;

        return this;
    }

    parseOptions(data)
    {
        for (var i = 0; i < data.length; i += 2)
        {
            if (supportedOptions.indexOf(data[i].toLowerCase()) > -1)
            {
                this[data[i].toLowerCase()] = data[i+1];
                this.options.push(data[i]);
                continue;
            }

            log.error(`Option [${data[i]}] [${data[i+1]}] is not supported`);
        }
    }
}

/**
 * WRQ Packet
 2 bytes     string    1 byte     string   1 byte
 ------------------------------------------------
 | Opcode |  Filename  |   0  |    Mode    |   0  |
 ------------------------------------------------
 */
class WRQPacket extends Packet
{
    constructor(msg)
    {
        super(msg);
        this.filename = null;
        this.options = [];
        this.mode = null;
        this.opcode = OpCode.WRQ;
        if (this.msg)
        {
            this.deserialize();
        }
    }

    deserialize()
    {
        var data = this.msg.toString('ascii', 2, this.msg.length-1).split('\u0000');
        this.filename = data[0];
        this.mode = data[1];
        if (data.length > 2 && data.length % 2 === 0)
        {
            this.parseOptions(data.slice(2, data.length));
        }
    }

    /**
     * Not implemented/tested due to us not being a client
     * @param filename
     * @param mode
     * @return {WRQPacket}
     */
    serialize(filename, mode)
    {
        if (filename)
        {
            this.filename = filename;
        }

        if (mode)
        {
            this.mode = mode;
        }

        var bytes = 0;
        var buffer = new Buffer();
        this.msg = buffer;

        return this;
    }

    parseOptions(data)
    {
        for (var i = 0; i < data.length; i += 2)
        {
            if (supportedOptions.indexOf(data[i].toLowerCase()) > -1)
            {
                this[data[i].toLowerCase()] = data[i+1];
                this.options.push(data[i]);
                continue;
            }

            log.error(`Option [${data[i]}] [${data[i+1]}] is not supported`);
        }
    }
}

/**
 * Data Packet
 2 bytes     2 bytes      n bytes
 ----------------------------------
 | Opcode |   Block #  |   Data     |
 ----------------------------------
 */
class DataPacket extends Packet
{
    constructor(msg)
    {
        super(msg);
        this.block = null;
        this.data = null;
        this.opcode = OpCode.DATA;

        if (this.msg)
        {
            this.deserialize();
        }
    }

    deserialize()
    {
        this.block = this.msg.readUInt16BE(2);
        this.data = this.msg.slice(4);
    }

    serialize(block, data)
    {
        if (block)
        {
            this.block = block;
        }

        if (data)
        {
            this.data = data;
        }

        var buffer = new Buffer(4 + (this.data.length || 0));
        buffer.writeUInt16BE(OpCode.DATA, 0);
        buffer.writeUInt16BE(this.block, 2);
        if (this.data.length)
        {
            this.data.copy(buffer, 4);
        }
        this.msg = buffer;
        return this;
    }
}

/**
 * Acknowledge Packet
 2 bytes     2 bytes
 ---------------------
 | Opcode |   Block #  |
 ---------------------
 */
class AckPacket extends Packet
{
    constructor(msg)
    {
        super(msg);
        this.block = null;
        this.opcode = OpCode.ACK;
        if (this.msg)
        {
            this.deserialize();
        }
    }

    deserialize()
    {
        this.block = this.msg.readUInt16BE(2);
    }

    serialize(block)
    {
        if (block)
        {
            this.block = block;
        }

        var buffer = new Buffer(4);
        buffer.writeUInt16BE(OpCode.ACK, 0);
        buffer.writeUInt16BE(this.block, 2);
        this.msg = buffer;
        return this;
    }
}

/**
 * Options Acknowledge Packet
 +-------+---~~---+---+---~~---+---+---~~---+---+---~~---+---+
 |  opc  |  opt1  | 0 | value1 | 0 |  optN  | 0 | valueN | 0 |
 +-------+---~~---+---+---~~---+---+---~~---+---+---~~---+---+
 */
class OAckPacket extends Packet
{
    constructor(msg)
    {
        super(msg);
        this.options = {};
        this.optionObjects = [];
        this.opcode = OpCode.OACK;
        if (this.msg)
        {
            this.deserialize();
        }
    }

    deserialize()
    {
        var data = this.msg.toString('ascii', 2, this.msg.length-1).split('\u0000');
        var options = data.slice(-2);

        return this;
    }

    serialize(options, values)
    {
        var buffer = new Buffer(2);
        buffer.writeUInt16BE(OpCode.OACK, 0);

        for (var i=0; i<options.length; i++)
        {
            var newBuffer = new Buffer(values[options[i].toLowerCase()].length + options[i].length + 2);
            newBuffer.write(options[i], 0, 'ascii');
            newBuffer[options[i].length] = 0;
            newBuffer.write(values[options[i].toLowerCase()].toString(), options[i].length+1);
            newBuffer[newBuffer.length - 1] = 0;
            buffer = Buffer.concat([buffer, newBuffer], (buffer.length + newBuffer.length));
        }

        this.msg = buffer;

        return this;
    }
}

/**
 * Error Packet
 2 bytes     2 bytes      string    1 byte
 -----------------------------------------
 | Opcode |  ErrorCode |   ErrMsg   |   0  |
 -----------------------------------------
 */
class ErrorPacket extends Packet
{
    constructor(msg)
    {
        super(msg);
        this.errorCode = null;
        this.errorMessage = null;
        this.opcode = OpCode.ERROR;
        if (this.msg)
        {
            this.deserialize();
        }
    }

    deserialize()
    {
        this.errorCode = this.msg.readUInt16BE(2);
        this.errorMessage = this.msg.toString('ascii', 4, this.msg.length-1).split('\u0000')[0];
    }

    serialize(errCode, errMsg)
    {
        if (errCode)
        {
            this.errorCode = errCode;
        }

        if (errMsg)
        {
            this.errorMessage = errMsg;
        }

        var buffer = new Buffer((this.errorMessage ? this.errorMessage.length : 0) + 5);
        buffer.writeUInt16BE(OpCode.ERROR, 0);
        buffer.writeUInt16BE(this.errorCode, 2);
        buffer.write(this.errorMessage, 4, 'ascii');
        buffer[buffer.length - 1] = 0;

        this.msg = buffer;

        return this;
    }
}

module.exports.Packet = Packet;
module.exports.RRQPacket = RRQPacket;
module.exports.WRQPacket = WRQPacket;
module.exports.AckPacket = AckPacket;
module.exports.DataPacket = DataPacket;
module.exports.OAckPacket = OAckPacket;
module.exports.ErrorPacket = ErrorPacket;