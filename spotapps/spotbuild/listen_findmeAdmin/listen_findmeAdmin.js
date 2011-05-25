function findMeConfigCount(findMeConfigObj, index) {
    var result = eval("("+findMeConfigObj+")");
    var num = /^\d+$/;
    var configCount = result.length;
    if ((num.test(index)) && (index < configCount)) {
        configCount = result[index].length;
    }
    if (configCount == 1) {
        if (index == 'all') {
            index = 0;
        }
        var hrefID = getNextElement('L',result[index][0].href,'/');
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
    var configCount = tmpObj.length;
    if ((num.test(configIndex)) && (num.test(groupIndex))) {
        if ((configIndex < tmpObj.length) && (groupIndex < tmpObj[configIndex].length)) {
            var tmpVal = tmpObj[configIndex][groupIndex][flag];
            if ((typeof(tmpVal) != 'undefined') && (tmpVal != 'null'))
                result = tmpVal;
        }
    }
    return result;
}

function checkNumberStatus(status, keyPress) {
    var result = 'false';
    if (status == keyPress)
        result = 'Play Status';
    else {
        if (keyPress == 1)
            result = 'true';
    }
    return result;
}

function removeNumber(findMeConfigObj, configIndex, groupIndex) {
    var result = eval("("+findMeConfigObj+")");
    result[configIndex].splice(groupIndex,1);
    return iiStringify(result);
}

function isFindMeExpired(findMeObj) {
    var isExpired = 'true';
    var tmpVal = getResultsKeyValue(findMeObj, 0, 'findMeExpiration');
    if (tmpVal.length == 0)
        isExpired = 'false';
    else if (typeof(tmpVal != 'undefined') && (tmpVal.length > 0)) {
        var date = tmpVal.split('T')[0];
        var time = tmpVal.split('T')[1].split('.')[0];
        var year = date.split('-')[0];
        var month = date.split('-')[1];
        month = parseInt(month,10) - 1;
        var day = date.split('-')[2];
        var hrs = time.split(':')[0];
        var mins = time.split(':')[1];
        var secs = time.split(':')[2];
        var expireTime = new Date(year, month, day, hrs, mins, secs);
        var currTime = new Date();
        if (expireTime > currTime)
            isExpired = 'false';
    }
    return isExpired;
}

function resetConfig(isActivating) {
    var addDay = 0;
    var currTime = new Date().getTime();
    if (isActivating == 'true')
        addDay = 86400000;
    currTime = currTime + addDay;
    return "{\"findMeExpiration\":\"" + iiDateToISO(currTime)+"\"}"
}
