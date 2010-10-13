function setControllerURL(appObj) {
    var tmpVal = eval("("+appObj+")");
    var result = tmpVal.cntrlURL.split('/api');
    return result[0] + "/meta";
}

function getTimeDifference(startTime, endTime) {
    var num = /^\d+$/;
    if ((num.test(startTime)) && (num.test(endTime)))
        return Math.round((endTime - startTime)/1000);
    else
        return 1;
}
