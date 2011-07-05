function setAudioURL(argList,audioURL) {
    var result = eval("("+argList+")");
    return audioURL + getNextElement(0,result.sessionId,'.') + ".wav";
}

function getNextDestination(argList, index) {
    var result = '';
    var tmpVal = eval("("+argList+")");
    var phoneNumber = getNextElement(index,tmpVal.destination,',');
    if(phoneNumber != '-1') {
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
    var str = getNextElement(0,phoneNumber,'@');
    var num = /^\d+$/;
    if ((num.test(str)) && (phoneNumber >= tmpVal.pstnLength) && (sipURL.length > 0))
        result = getnum(tmpVal.ani) + "@" + sipURL;
    else
        result = getnum(tmpVal.ani) + "@" + tmpVal.hostName;
    return result;
}


function saveSipDirect(argList, index)
{
    var result = '';
    var tmpVal = eval("("+argList+")");
    var phoneNumber = getNextElement(index,tmpVal.destination,',');
    if(phoneNumber != '-1') {
        var sipURL = tmpVal.sipURL;
        var sipDirect = tmpVal.sipDirect;
        var str = new String(phoneNumber);
        var num = /^\d+$/;
        if (num.test(str)) {
            if ((phoneNumber.length >= tmpVal.pstnLength) && (sipURL.length > 0)) {
            }
            else if (phoneNumber.length < tmpVal.EXT_LENGTH)
                return extendJsonObject(argList,'sipDirect','y');
            else
                return argList;
        }
    }
    return argList;
}

function saveNumber(argList, phoneNumber) {
    var number = getNextElement(0,phoneNumber,'@');
    if (number[0] == 'F')
        number = getNextElement(1,number,'F');
    return extendJsonObject(argList,'phoneNumber',number);
}

function setNextDestination(argList, index, phoneNumber, EXT_SUFFIX) {
    var result = '';
    var tmpVal = eval("("+argList+")");
    var sipURL = tmpVal.sipURL;
    var sipDirect = tmpVal.sipDirect;
    var str = new String(phoneNumber);
    var num = /^\d+$/;
    if (num.test(str)) {
        if ((phoneNumber.length >= tmpVal.pstnLength) && (sipURL.length > 0)) {
            if (sipDirect == 'n')
                result = 'F' + phoneNumber + "@" + sipURL;
            else
                result = phoneNumber + "@" + sipURL;
        }
        else if (phoneNumber.length < tmpVal.EXT_LENGTH)
            if (EXT_SUFFIX == '')
                result = tmpVal.EXT_PREFIX + phoneNumber + tmpVal.EXT_SUFFIX;
            else
                result = tmpVal.EXT_PREFIX + phoneNumber + '@' + EXT_SUFFIX;

        else
            result = phoneNumber;
    }
    else
        result = phoneNumber;
    return result;
}
