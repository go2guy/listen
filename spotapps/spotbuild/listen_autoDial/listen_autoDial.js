function setAudioURL(argList,audioURL) {
    var result = eval("("+argList+")");
    return audioURL + getNextElement(0,result.sessionId,'.') + ".wav";
}

function getNextDestination(argList, index) {
    var result = '';
    var tmpVal = eval("("+argList+")");
    var phoneNumber = getNextElement(index,tmpVal.destination,',');
    if(phoneNumber != '-1') {
        var sipURL = tmpVal.sipURL;
        var str = new String(phoneNumber);
        var num = /^\d+$/;
        if (num.test(str)) {
            if ((phoneNumber.length >= tmpVal.pstnLength) && (sipURL.length > 0))
                result = phoneNumber + "@" + sipURL;
            else if (phoneNumber.length < tmpVal.EXT_LENGTH)
                result = tmpVal.EXT_PREFIX + phoneNumber + tmpVal.EXT_SUFFIX;
            else
                result = phoneNumber;
        }
        else
            result = phoneNumber;
    }
    else
        result = "DONE";
    return result;
}

function setCallingID(phoneNumber, argList) {
    var result = '';
    var tmpVal = eval("("+argList+")");
    var sipURL = tmpVal.sipURL;
    var str = new String(phoneNumber);
    var num = /^\d+$/;
    if ((num.test(str)) && (phoneNumber >= tmpVal.pstnLength) && (sipURL.length > 0))
        result = getnum(tmpVal.ani) + "@" + sipURL;
    else
        result = getnum(tmpVal.ani) + "@" + tmpVal.hostName;
    return result;
}
