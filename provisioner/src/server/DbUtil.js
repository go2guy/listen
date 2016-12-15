"use strict";

var convertJSDateToMysqlDateTime = function(d)
{
    var year = d.getFullYear();
    var month = d.getMonth() < 10 ? `0${d.getMonth()}` : d.getMonth();
    var day = d.getDate() < 10 ? `0${d.getDate()}` : d.getDate();
    var hour = d.getHours() < 10 ? `0${d.getHours()}` : d.getHours();
    var minute = d.getMinutes() < 10 ? `0${d.getMinutes()}` : d.getMinutes();
    var second = d.getSeconds() < 10 ? `0${d.getSeconds()}` : d.getSeconds();
    return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
};

exports.convertJSDateToMysqlDateTime = convertJSDateToMysqlDateTime;