function getSubscriber(returnVal) {
    var result = eval("("+returnVal+")");
    result = result.subscriber.href;
    return getNextElement('L',result,'/');
}

function setAudioURL(argList,audioURL) {
    var result = eval("("+argList+")");
    return audioURL + getNextElement(0,result.sessionId,'.') + ".wav";
}

function getDestination(argList, index) {
    var result = '';
    var tmpVal = eval("("+argList+")");
    var phoneNumber = getNextElement(index,tmpVal.destination,',');
    if(phoneNumber != '-1') {
        var sipURL = tmpVal.sipURL;
        var str = new String(phoneNumber);
        var num = /^\d+$/;
        if ((num.test(str)) && (sipURL != 'null'))
            result = phoneNumber + "@" + sipURL;
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
    if ((num.test(str)) && (sipURL != 'null'))
        result = getnum(tmpVal.ani) + "@" + sipURL;
    else
        result = getnum(tmpVal.ani) + "@" + tmpVal.hostName;
    return result;
}

function extendAppObject(argList, key, value) {
    var result = eval("("+argList+")");
    result[key] = value;
    return iiStringify(result);
}

function createDialObj(ConfName, SessionID) {
    return "{\"ConfName\":\"" + ConfName + "\",\"SessionID\":\"" + SessionID + "\"}";
}