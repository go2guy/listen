"use strict";

class TFTPError extends Error {}
class FileNotFoundError extends Error{}
class IllegalOpError extends Error{}
class AccessViolationError extends Error{}

module.exports.TFTPError = TFTPError;
module.exports.FileNotFoundError = FileNotFoundError;
module.exports.IllegalOpError = IllegalOpError;