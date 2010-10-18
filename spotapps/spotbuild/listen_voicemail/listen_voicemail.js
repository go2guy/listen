function getResultsKeyValue(jsonObj, index, key) {
    var result;
    var tmpVal = eval("("+jsonObj+")");
    result = tmpVal.results[index][key];
    if (result == null)
        result = "";
    return result;
}

function getAccessNumSubID(jsonObj, status) {
    var result = "NOT_FOUND";
    var tmpVal = eval("("+jsonObj+")");
    if ((status == 'Success') && (tmpVal.count== 1))
        result = getNextElement('L',tmpVal.results[0].subscriber.href,'/');
    return result;
}

function setCallerName (jsonObj, status, ANI) {
    var result = getnum(ANI);
    if (status == 'Success') {
        var tmpVal = getJsonVal(jsonObj, 'realName');
        if (tmpVal.length > 0)
            result = tmpVal;
    }
    return result;
}

function createTranscriptionObj(returnVal, fileLocation, passValues) {
    var number = eval("("+returnVal+")");
    var result = eval("("+passValues+")");
    result.fileLocation = fileLocation;
    result.fileNumber = number.id;
    return iiStringify(result);
}

function getTimeDifference(startTime, endTime) {
    var num = /^\d+$/;
    if ((num.test(startTime)) && (num.test(endTime)))
        return Math.round((endTime - startTime)/1000);
    else
        return 1;
}
