function setParamsForCallEndCaller(startTime, endTime, callType, ANI, DNIS, organization) {
    var duration = endTime - startTime;
    return "{\"date\": \""+iiDateToISO(startTime)+"\", \"service\": \""+callType+"\", \"duration\":"+duration+", \"ani\":\""+ANI+"\", \"dnis\":\""+DNIS+"\", \"organization\": {\"href\":\""+organization+"\"}}";
}

function findMeConfigCount(findMeConfigObj, index) {
    var configCount = 0;
    var result = eval("("+findMeConfigObj+")");
    var num = /^\d+$/;
    if (typeof(result.results.length) != 'undefined') {
        configCount = result.results.length;
    }
    if ((num.test(index)) && (index < configCount)) {
        configCount = result.results[index].length;
    }
    if (configCount == 1) {
        if (index == 'all') {
            index = 0;
        }
        var hrefID = getNextElement('L',result.results[index][0].href,'/');
        if (hrefID == 'null') {
            configCount = 0;
        }
    }
    return configCount;
}

function getFindMeConfiguartion(findMeConfigObj, configIndex, groupIndex, flag) {
    var tmpObj = eval("("+findMeConfigObj+")");
    var num = /^\d+$/;
    var result = '';
    var configCount = 0;
    if (typeof(tmpObj.results.length) != 'undefined') {
        configCount = tmpObj.results.length;
    }

    if ((num.test(configIndex)) && (num.test(groupIndex))) {
        if ((configIndex < tmpObj.results.length) && (groupIndex < tmpObj.results[configIndex].length)) {
            var tmpVal = tmpObj.results[configIndex][groupIndex][flag];
            if ((typeof(tmpVal) != 'undefined') && (tmpVal != 'null'))
                result = tmpVal;
        }
    }
    return result;
}

function setStatus(keyPress) {
    var numberStatus = false;
    if (keyPress == 1)
        numberStatus = true;
    return "{\"isEnabled\":" + numberStatus +"}"
}

function removeNumber(findMeConfigObj, configIndex, groupIndex) {
    var result = eval("("+findMeConfigObj+")");
    result.results[configIndex].splice(groupIndex,1);
    return iiStringify(result);
}

function resetConfig(isActivating) {
    return "{\"isActivating\":" + isActivating + "}";
}
