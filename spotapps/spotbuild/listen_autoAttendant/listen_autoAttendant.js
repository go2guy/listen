function setControllerURL(appObj) {
    var tmpVal = eval("("+appObj+")");
    return changeSuffix(tmpVal.cntrlURL,'/','meta');
}

function getMenuHotKeys(menuObj) {
    var result = '';
    var tmpVal = getJsonVal(menuObj, 'args');
    var wildCard = '?';
    for (var i=0; i<tmpVal.keyPresses.length; i++) {
        if (tmpVal.keyPresses[i].length == 1) {
            if (tmpVal.keyPresses[i] == wildCard)
                result = result + '1,2,3,4,5,6,7,8,9,0,#,*,';
            else
                result = result + tmpVal.keyPresses[i] + ',';
        }
    }
    return result;
}

function getDtmfLength(menuObj) {
    var max = 1;
    var index = 0;
    var result = getJsonVal(menuObj, 'args');
    var list = result.keyPresses;
    while (index < list.length) {
        if (list[index].length > max)
            max = list[index].length;
        index++;
    }
    return max;
}

function setOutBoundID (phoneNumber, passValues, ANI) {
    var str = new String(phoneNumber);
    var num = /^\d+$/;
    var tmpSipURL = getJsonVal(passValues, 'sipURL');
    var tmpPstn = getJsonVal(passValues, 'pstnLength');
    if ((num.test(str)) && (phoneNumber.length >= tmpPstn) && (tmpSipURL.length > 0))
        return ANI + "@" + tmpSipURL;
    else
        return ANI + getJsonVal(passValues, 'EXT_SUFFIX');
}

function setDestValue (phoneNumber, passValues, EXT_SUFFIX) {
    var str = new String(phoneNumber);
    var num = /^\d+$/;
    if (num.test(str)) {
        var tmpSipURL = getJsonVal(passValues, 'sipURL');
        var tmpPstn = getJsonVal(passValues, 'pstnLength');
        var tmpExtLength = getJsonVal(passValues, 'EXT_LENGTH');
        if ((phoneNumber.length >= tmpPstn) && (tmpSipURL.length > 0))
            return phoneNumber + "@" + tmpSipURL;
        else if (phoneNumber.length < tmpExtLength)
            if (EXT_SUFFIX == '')
                return getJsonVal(passValues, 'EXT_PREFIX') + phoneNumber + getJsonVal(passValues, 'EXT_SUFFIX');
            else
                return getJsonVal(passValues, 'EXT_PREFIX') + phoneNumber + '@' + EXT_SUFFIX;
        else
            return phoneNumber;
    }
    else
        return phoneNumber;
}

function setParamsForCallEnd(passValues, ANI, callResult, organization) {
    var duration = getJsonVal(passValues, 'callTime');
    if(duration == '')
    {
    duration = 0;
    }
    var callStartTime = getJsonVal(passValues, 'callStartTime');
    if(callStartTime == '')
    {
        callStartTime = (new Date().getTime());
    }
    return "{\"date\": \""+iiDateToISO(callStartTime)+"\", \"service\": \""+getJsonVal(passValues, 'callType')+"\", \"duration\":"+duration+", \"ani\":\""+ANI+"\", \"dnis\":\""+getJsonVal(passValues, 'destination')+"\", \"result\":\""+callResult+"\", \"organization\": {\"href\":\""+organization+"\"}}";
}

function setVoicemailObj(passValues) {
    var ext;
    var transExt = getnum(getJsonVal(passValues, 'transExt'));
    if (transExt != '') {
        ext = transExt;
    }
    else {    
        ext = getnum(getJsonVal(passValues, 'destination'));
    }

    return extendJsonObject(passValues,'toExt',ext);
}